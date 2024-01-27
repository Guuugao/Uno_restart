package com.uno_restart.dataFetcher;

import cn.dev33.satoken.stp.StpUtil;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.DgsSubscription;
import com.uno_restart.event.GameStartEvent;
import com.uno_restart.exception.RoomNotExistsException;
import com.uno_restart.exception.playerNotLoginException;
import com.uno_restart.service.GameService;
import com.uno_restart.service.PlayerService;
import com.uno_restart.service.RoomService;
import com.uno_restart.types.game.GameSettings;
import com.uno_restart.types.player.PlayerInfo;
import com.uno_restart.types.room.RoomFeedback;
import com.uno_restart.types.room.RoomInfo;
import com.uno_restart.types.room.RoomPlayerState;
import graphql.relay.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO 修改成员变量可能需要线程安全
// TODO 查询房间内玩家信息可以用sub datafetcher实现
// TODO 若房间正在游玩, 则不接受部分请求
@Slf4j
@DgsComponent
public class RoomDataFetcher {
    @Autowired
    private PlayerService playerService;
    @Autowired
    private RoomService roomService;
    @Autowired
    private GameService gameService;
    @Autowired
    private Base64.Encoder encoder;
    @Autowired
    private Base64.Decoder decoder;
    @Autowired
    private ConfigurableApplicationContext context;

    private static final int minPlayerCnt = 2;
    private static final int maxPlayerCnt = 10;
    private static final int minRoomNameLen = 2;
    private static final int maxRoomNameLen = 8;

    @DgsQuery
    public RoomInfo queryRoomByID(String roomID) {
        StpUtil.checkLogin();
        return roomService.getRoom(roomID);
    }

    @DgsQuery
    public Connection<RoomInfo> queryRoomByName(String roomName, Integer first, String after) {
        StpUtil.checkLogin();
        String cursor = (after == null) ? null : new String(decoder.decode(after));

        Stream<RoomInfo> roomInfoStream = roomService.getPublicRooms().values()
                .stream()
                .filter(room -> room.getRoomName().matches(roomName + ".*"));
        if (after != null && after.isEmpty()) {
            roomInfoStream = roomInfoStream.filter(room -> room.getRoomName().compareTo(cursor) >= 0);
        }
        List<RoomInfo> rooms = new ArrayList<>(
                roomInfoStream
                        .limit(first + 2)
                        .toList());

        boolean hasPreviousPage = false;
        boolean hasNextPage = false;
        if (!rooms.isEmpty()) { // 去除首尾多余节点, 还有数据
            // 若存在前驱页, 则第一条数据的playerName与解码后的after相等
            // 此时设置hasPreviousPage = true, 同时删除该前驱节点
            String previousNodeCursor = encoder.encodeToString(
                    rooms.get(0).getRoomName().getBytes(StandardCharsets.UTF_8));
            if (previousNodeCursor.equals(after)) {
                hasPreviousPage = true;
                rooms.remove(0);
            }
            // 若数据条数大于给定大小(first), 代表包含多余节点, 即存在后驱页
            hasNextPage = rooms.size() > first;
        }

        List<Edge<RoomInfo>> edges =
                rooms.stream()
                        .limit(first)
                        .map(roomInfo -> new DefaultEdge<>(roomInfo,
                                new DefaultConnectionCursor(
                                        encoder.encodeToString(
                                                roomInfo.getRoomName().getBytes(
                                                        StandardCharsets.UTF_8)))))
                        .collect(Collectors.toList());

        // 没有数据, 则游标为空
        ConnectionCursor startCursor = !edges.isEmpty() ? edges.get(0).getCursor() : new DefaultConnectionCursor("null");
        ConnectionCursor endCursor = !edges.isEmpty() ? edges.get(edges.size() - 1).getCursor() : new DefaultConnectionCursor("null");

        PageInfo pageInfo = new DefaultPageInfo(
                startCursor,
                endCursor,
                hasPreviousPage,
                hasNextPage
        );

        return new DefaultConnection<>(edges, pageInfo);
    }

    @DgsQuery
    public Connection<RoomInfo> availableRooms(Integer first, String after) {
        StpUtil.checkLogin();

        String cursor = (after == null) ? null : new String(decoder.decode(after));

        Stream<RoomInfo> roomInfoStream = roomService.getPublicRooms().values()
                .stream();
        if (after != null && after.isEmpty()) {
            roomInfoStream = roomInfoStream.filter(room -> room.getRoomName().compareTo(cursor) >= 0);
        }
        List<RoomInfo> rooms = new ArrayList<>(
                roomInfoStream
                        .limit(first + 2)
                        .toList());

        boolean hasPreviousPage = false;
        boolean hasNextPage = false;
        if (!rooms.isEmpty()) { // 去除首尾多余节点, 还有数据
            // 若存在前驱页, 则第一条数据的playerName与解码后的after相等
            // 此时设置hasPreviousPage = true, 同时删除该前驱节点
            String previousNodeCursor = encoder.encodeToString(
                    rooms.get(0).getRoomName().getBytes(StandardCharsets.UTF_8));
            if (previousNodeCursor.equals(after)) {
                hasPreviousPage = true;
                rooms.remove(0);
            }
            // 若数据条数大于给定大小(first), 代表包含多余节点, 即存在后驱页
            hasNextPage = rooms.size() > first;
        }

        List<Edge<RoomInfo>> edges =
                rooms.stream()
                        .limit(first)
                        .map(roomInfo -> new DefaultEdge<>(roomInfo,
                                new DefaultConnectionCursor(
                                        encoder.encodeToString(
                                                roomInfo.getRoomName().getBytes(
                                                        StandardCharsets.UTF_8)))))
                        .collect(Collectors.toList());

        // 没有数据, 则游标为空
        ConnectionCursor startCursor = !edges.isEmpty() ? edges.get(0).getCursor() : new DefaultConnectionCursor("null");
        ConnectionCursor endCursor = !edges.isEmpty() ? edges.get(edges.size() - 1).getCursor() : new DefaultConnectionCursor("null");

        PageInfo pageInfo = new DefaultPageInfo(
                startCursor,
                endCursor,
                hasPreviousPage,
                hasNextPage
        );

        return new DefaultConnection<>(edges, pageInfo);
    }

    @DgsQuery
    public RoomFeedback currentJoinedRoom() {
        RoomFeedback feedback = new RoomFeedback(false, false);

        if (!StpUtil.isLogin()) {
            feedback.setMessage("未能读取到有效 token");
        } else {
            String playerName = StpUtil.getLoginIdAsString();
            RoomInfo room = roomService.whichRoom(playerName);
            if (room != null) {
                feedback.setSuccess(true)
                        .setMessage("玩家当前位于房间 " + room.getRoomName())
                        .setIsInsideRoom(true)
                        .setRoom(room)
                        .setSelf(roomService.getPlayerState(playerName));
            } else {
                feedback.setSuccess(true)
                        .setMessage("玩家 " + playerName + " 未加入任何房间");
            }
        }

        return feedback;
    }

    /* TODO
     * UNO游戏原则上允许2-10人参与。
     * 但为了保证游戏的趣味性，降低运气因素，一般建议3-4人参与最佳。
     * 如果人数超过5个，可以考虑多加1副牌，使游戏进程更顺利
     * */
    @DgsMutation
    public RoomFeedback roomCreate(String roomName, Integer maxPlayerCount, Boolean isPrivate, String password) {
        RoomFeedback feedback = new RoomFeedback(false, false);

        if (!StpUtil.isLogin()) {
            feedback.setMessage("未能读取到有效 token");
            return feedback;
        }

        if (!checkPlayerCount(maxPlayerCount) || !checkRoomName(roomName)) {
            feedback.setMessage("创建房间失败, 请检查房间设置");
        } else {
            String playerName = StpUtil.getLoginIdAsString();
            PlayerInfo player = playerService.getById(playerName);
            RoomInfo room = roomService.createRoom(roomName, isPrivate, maxPlayerCount, password, playerName);
            String roomID = room.getRoomID();

            roomService.join(player, roomID, password);

            feedback.setSuccess(true)
                    .setMessage("创建房间成功")
                    .setIsInsideRoom(true)
                    .setRoom(room)
                    .setSelf(roomService.getPlayerState(playerName));
        }

        return feedback;
    }

    @DgsMutation
    public RoomFeedback roomJoin(String roomID, String password) {
        RoomFeedback feedback = new RoomFeedback(false, false);

        if (!StpUtil.isLogin()) {
            feedback.setMessage("未能读取到有效 token");
        } else {
            PlayerInfo player = playerService.getById(StpUtil.getLoginIdAsString());
            if (roomService.isRoomFull(roomID)) {
                feedback.setMessage("房间已满");
            } else if (roomService.join(player, roomID, password)) {
                feedback.setSuccess(true)
                        .setMessage("加入成功")
                        .setIsInsideRoom(true)
                        .setRoom(roomService.getRoom(roomID))
                        .setSelf(roomService.getPlayerState(player.getPlayerName()));
            } else {
                feedback.setMessage("加入失败, 密码错误");
            }
        }

        return feedback;
    }

    @DgsMutation
    public RoomFeedback roomQuit() {
        RoomFeedback feedback = new RoomFeedback(false, false);

        if (!StpUtil.isLogin()) {
            feedback.setMessage("未能读取到有效 token");
        } else {
            if (roomService.quit(StpUtil.getLoginIdAsString())) {
                feedback.setSuccess(true)
                        .setMessage("退出房间成功");
            } else {
                feedback.setMessage("退出失败, 未加入任何房间");
            }
        }

        return feedback;
    }

    @DgsMutation
    public RoomFeedback roomPlayerReady(String roomID) {
        RoomFeedback feedback = new RoomFeedback(false, false);

        if (!StpUtil.isLogin()) {
            feedback.setMessage("未能读取到有效 token");
        } else {
            String playerName = StpUtil.getLoginIdAsString();
            roomService.ready(roomID, playerName, true);
            RoomInfo room = roomService.getRoom(roomID);
            // 若玩家全部准备并且玩家数量大于最小玩家数量, 则开始
//            if (checkPlayerCount(room.getCurrentPlayerCount()) &&
//                    room.getCurrentPlayerCount() == room.getReadyPlayerCnt()) {
//                context.publishEvent(new GameStartEvent(roomID));
//            }
            // 方便测试
            context.publishEvent(new GameStartEvent(roomID));

            feedback.setSuccess(true)
                    .setMessage("玩家 " + playerName + " 已准备")
                    .setIsInsideRoom(true)
                    .setSelf(roomService.getPlayerState(playerName))
                    .setRoom(room);
        }

        return feedback;
    }

    @DgsMutation
    public RoomFeedback roomPlayerUnready(String roomID) {
        RoomFeedback feedback = new RoomFeedback(false, false);

        if (!StpUtil.isLogin()) {
            feedback.setMessage("未能读取到有效 token");
        } else {
            String playerName = StpUtil.getLoginIdAsString();
            roomService.ready(roomID, playerName, false);

            feedback.setSuccess(true)
                    .setMessage("玩家 " + playerName + " 已取消准备")
                    .setIsInsideRoom(true)
                    .setSelf(roomService.getPlayerState(playerName))
                    .setRoom(roomService.getRoom(roomID));
        }

        return feedback;
    }

    @DgsSubscription
    public Mono<GameSettings> roomWaitStart(String roomID, String token) {
        // 此处检查是否登录的手段都会失败, 因为使用websocket, 无法读取HTTP请求头
        if (!StpUtil.isLogin(StpUtil.getLoginIdByToken(token))) {
            return Mono.error(new playerNotLoginException("未能读取到有效 token"));
        } if (roomService.isRoomNotExists(roomID)) {
            return Mono.error(new RoomNotExistsException("游戏房间不存在"));
        } else {
            return Mono.create(sink -> context.addApplicationListener((ApplicationListener<GameStartEvent>) event -> {
                if (roomID.equals(event.getSource())) { // 忽略其他房间的信号
                    RoomInfo room = roomService.getRoom(event.getSource().toString());
                    LinkedList<PlayerInfo> players = room.getJoinedPlayer().stream()
                            .map(RoomPlayerState::getPlayer)
                            .collect(Collectors.toCollection(LinkedList::new));
                    GameSettings gameSettings = new GameSettings(players, room);
                    room.setIsPlaying(true);
                    sink.success(gameSettings); // 通知客户端游戏开始
                    gameService.gameInit(gameSettings); // 初始化游戏必要信息
                }
            }));

        }
    }

    private boolean checkPlayerCount(Integer playerCount) {
        return playerCount >= minPlayerCnt && playerCount <= maxPlayerCnt;
    }

    private boolean checkRoomName(String roomName) {
        return roomName.length() >= minRoomNameLen && roomName.length() <= maxRoomNameLen;
    }
}