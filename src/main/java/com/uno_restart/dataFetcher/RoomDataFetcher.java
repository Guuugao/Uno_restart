package com.uno_restart.dataFetcher;

import cn.dev33.satoken.stp.StpUtil;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.DgsSubscription;
import com.uno_restart.event.GameStartEvent;
import com.uno_restart.event.RoomCloseEvent;
import com.uno_restart.exception.PlayerAbnormalException;
import com.uno_restart.exception.RoomAbnormalException;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        StpUtil.checkLogin();

        RoomFeedback feedback = new RoomFeedback(false, false);

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

        return feedback;
    }

    // TODO 如果游玩中玩家创建房间
    @DgsMutation
    public RoomFeedback roomCreate(String roomName, Integer maxPlayerCount, Boolean isPrivate, String password) {
        StpUtil.checkLogin();

        RoomFeedback feedback = new RoomFeedback(false, false);

        if (!roomService.isPlayerCntOK(maxPlayerCount) || !roomService.isRoomNameOK(roomName)) {
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
    public RoomFeedback roomJoin(String roomID, String password)
            throws RoomAbnormalException, PlayerAbnormalException {
        RoomFeedback feedback = new RoomFeedback(false, false);

        StpUtil.checkLogin();
        roomService.checkRoomExists(roomID);
        roomService.checkPlayerInRoom(roomID, StpUtil.getLoginIdAsString());
        roomService.checkRoomFull(roomID);
        roomService.checkIsPlaying(roomID);

        PlayerInfo player = playerService.getById(StpUtil.getLoginIdAsString());
        if (roomService.join(player, roomID, password)) {
            feedback.setSuccess(true)
                    .setMessage("加入成功")
                    .setIsInsideRoom(true)
                    .setRoom(roomService.getRoom(roomID))
                    .setSelf(roomService.getPlayerState(player.getPlayerName()));
        } else {
            feedback.setMessage("加入失败, 密码错误");
        }

        return feedback;
    }

    // TODO 若游玩中退出, 则结束游戏
    @DgsMutation
    public RoomFeedback roomQuit() throws RoomAbnormalException {
        RoomFeedback feedback = new RoomFeedback(false, false);
        StpUtil.checkLogin();
        String playerName = StpUtil.getLoginIdAsString();
        roomService.checkPlayerInAnyRoom(playerName);

        roomService.quit(playerName);
        feedback.setSuccess(true)
                .setMessage("退出房间成功");

        return feedback;
    }

    @DgsMutation
    public RoomFeedback roomPlayerReady(String roomID)
            throws RoomAbnormalException, PlayerAbnormalException {
        RoomFeedback feedback = new RoomFeedback(false, false);

        StpUtil.checkLogin();
        roomService.checkRoomExists(roomID);
        roomService.checkPlayerInRoom(roomID, StpUtil.getLoginIdAsString());
        roomService.checkIsPlaying(roomID);

        String playerName = StpUtil.getLoginIdAsString();
        roomService.ready(roomID, playerName, true);

        feedback.setSuccess(true)
                .setMessage("玩家 " + playerName + " 已准备")
                .setIsInsideRoom(true)
                .setSelf(roomService.getPlayerState(playerName))
                .setRoom(roomService.getRoom(roomID));


        return feedback;
    }

    @DgsMutation
    public RoomFeedback roomPlayerUnready(String roomID)
            throws RoomAbnormalException, PlayerAbnormalException {
        RoomFeedback feedback = new RoomFeedback(false, false);

        StpUtil.checkLogin();
        roomService.checkRoomExists(roomID);
        roomService.checkPlayerInRoom(roomID, StpUtil.getLoginIdAsString());
        roomService.checkIsPlaying(roomID);

        String playerName = StpUtil.getLoginIdAsString();
        roomService.ready(roomID, playerName, false);

        feedback.setSuccess(true)
                .setMessage("玩家 " + playerName + " 已取消准备")
                .setIsInsideRoom(true)
                .setSelf(roomService.getPlayerState(playerName))
                .setRoom(roomService.getRoom(roomID));

        return feedback;
    }

    @DgsSubscription
    public Mono<GameSettings> roomWaitStart(String roomID, String token) {

        Object playerName = StpUtil.getLoginIdByToken(token);
        if (playerName == null) {
            return Mono.error(new PlayerAbnormalException("未能读取到有效 token"));
        }

        // 订阅使用socket, 所以错误不能直接抛出, 需要转换为错误流
        try {
            roomService.checkRoomExists(roomID);
            roomService.checkPlayerInRoom(roomID, playerName.toString());
            roomService.checkIsPlaying(roomID);
        } catch (RoomAbnormalException | PlayerAbnormalException e) {
            return Mono.error(e);
        }

        return Mono.create(sink -> {
            // 监听游戏开始事件
            context.addApplicationListener((ApplicationListener<GameStartEvent>) event -> {
                if (roomID.equals(event.getSource())) { // 忽略其他房间的信号
                    RoomInfo room = roomService.getRoom(event.getSource().toString());
                    LinkedList<PlayerInfo> players = room.getJoinedPlayer().stream()
                            .map(RoomPlayerState::getPlayer)
                            .collect(Collectors.toCollection(LinkedList::new));
                    GameSettings gameSettings = new GameSettings(players, room);
                    room.setIsPlaying(true);
                    sink.success(gameSettings); // 通知客户端游戏开始
                    gameService.gameInit(gameSettings); // 初始化游戏必要信息
                    log.info("game " + event.getSource() + " start");
                }
            });
            // 监听房间关闭事件
            context.addApplicationListener((ApplicationListener<RoomCloseEvent>) event -> {
                if (roomID.equals(event.getSource())) { // 忽略其他房间的信号
                    sink.success(); // 取消流, 若流已经开始, 则需要使用Disposable.dispose()
                    log.info("cancel subscribe of wait room " + event.getSource() + " start");
                }
            });
        });
    }
}
