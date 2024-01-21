package com.uno_restart.dataFetcher;

import cn.dev33.satoken.stp.StpUtil;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.uno_restart.service.GameService;
import com.uno_restart.service.PlayerService;
import com.uno_restart.types.player.PlayerInfo;
import com.uno_restart.types.room.GameRoomFeedback;
import com.uno_restart.types.room.GameRoomInfo;
import graphql.relay.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO 修改成员变量可能需要线程安全
// TODO 查询房间内玩家信息可以用sub datafetcher实现
@Slf4j
@DgsComponent
public class RoomDataFetcher {
    @Autowired
    PlayerService playerService;
    @Autowired
    GameService gameService;
    @Autowired
    Base64.Encoder encoder;
    @Autowired
    Base64.Decoder decoder;

    @DgsQuery
    public GameRoomInfo queryRoomByID(String roomID) {
        return gameService.getRoom(roomID);
    }

    @DgsQuery
    public Connection<GameRoomInfo> queryRoomByName(String roomName, Integer first, String after) {
        StpUtil.checkLogin();
        String cursor = (after == null) ? null : new String(decoder.decode(after));

        Stream<GameRoomInfo> roomInfoStream = gameService.getPublicRooms().values()
                .stream()
                .filter(room -> room.getRoomName().matches(roomName + ".*"));
        if (after != null && after.isEmpty()) {
            roomInfoStream = roomInfoStream.filter(room -> room.getRoomName().compareTo(cursor) >= 0);
        }
        List<GameRoomInfo> rooms = new ArrayList<>(
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

        List<Edge<GameRoomInfo>> edges =
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
    public Connection<GameRoomInfo> availableRooms(Integer first, String after) {
        StpUtil.checkLogin();

        String cursor = (after == null) ? null : new String(decoder.decode(after));

        Stream<GameRoomInfo> roomInfoStream = gameService.getPublicRooms().values()
                .stream();
        if (after != null && after.isEmpty()) {
            roomInfoStream = roomInfoStream.filter(room -> room.getRoomName().compareTo(cursor) >= 0);
        }
        List<GameRoomInfo> rooms = new ArrayList<>(
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

        List<Edge<GameRoomInfo>> edges =
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
    public GameRoomFeedback currentJoinedRoom() {
        GameRoomFeedback feedback = new GameRoomFeedback(false, false);

        if (!StpUtil.isLogin()) {
            feedback.setMessage("请登录");
        } else {
            String playerName = StpUtil.getLoginIdAsString();
            GameRoomInfo room = gameService.whichRoom(playerName);
            if (room != null) {
                feedback.setSuccess(true)
                        .setMessage("玩家当前位于房间 " + room.getRoomName())
                        .setIsInsideRoom(true)
                        .setRoom(room)
                        .setSelf(gameService.getPlayerState(playerName));
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
    public GameRoomFeedback roomCreate(String roomName, Integer maxPlayerCount, Boolean isPrivate, String password) {
        GameRoomFeedback feedback = new GameRoomFeedback(false, false);

        if (!StpUtil.isLogin()) {
            feedback.setMessage("请登录");
            return feedback;
        }
        String playerName = StpUtil.getLoginIdAsString();

        if (gameService.canCreate(playerName)) {
            feedback.setMessage("创建失败, 同一玩家禁止创建多个房间");
        } else if (!checkMaxPlayerCount(maxPlayerCount) || !checkRoomName(roomName)) {
            feedback.setMessage("创建房间失败, 请检查房间设置");
        } else {
            PlayerInfo player = playerService.getById(playerName);
            GameRoomInfo room = gameService.createRoom(roomName, isPrivate, maxPlayerCount, password);
            String roomID = room.getRoomID();

            gameService.join(player, roomID, password);

            feedback.setSuccess(true)
                    .setMessage("创建房间成功")
                    .setIsInsideRoom(true)
                    .setRoom(room)
                    .setSelf(gameService.getPlayerState(playerName));
        }

        return feedback;
    }

    @DgsMutation
    public GameRoomFeedback roomJoin(String roomId, String password) {
        GameRoomFeedback feedback = new GameRoomFeedback(false, false);

        if (!StpUtil.isLogin()) {
            feedback.setMessage("请登录");
        } else {
            PlayerInfo player = playerService.getById(StpUtil.getLoginIdAsString());
            if (gameService.join(player, roomId, password)) {
                feedback.setSuccess(true)
                        .setMessage("加入成功")
                        .setIsInsideRoom(true)
                        .setRoom(gameService.getRoom(roomId))
                        .setSelf(gameService.getPlayerState(player.getPlayerName()));
            } else {
                feedback.setMessage("加入失败, 密码错误");
            }
        }

        return feedback;
    }

    @DgsMutation
    public GameRoomFeedback roomQuit(){
        GameRoomFeedback feedback = new GameRoomFeedback(false, false);

        if (!StpUtil.isLogin()) {
            feedback.setMessage("请登录");
        } else {
            if (gameService.quit(StpUtil.getLoginIdAsString())) {
                feedback.setSuccess(true)
                        .setMessage("退出房间成功");
            } else {
                feedback.setMessage("退出失败, 未加入任何房间");
            }
        }

        return feedback;
    }

    @DgsMutation
    public GameRoomFeedback roomPlayerReady(String roomID){
        GameRoomFeedback feedback = new GameRoomFeedback(false, false);

        if (!StpUtil.isLogin()) {
            feedback.setMessage("请登录");
        } else {
            String playerName = StpUtil.getLoginIdAsString();
            gameService.ready(roomID, playerName, true);
            feedback.setSuccess(true)
                    .setMessage("玩家 " + playerName + " 已准备")
                    .setIsInsideRoom(true)
                    .setSelf(gameService.getPlayerState(playerName))
                    .setRoom(gameService.getRoom(roomID));
        }

        return feedback;
    }

    @DgsMutation
    public GameRoomFeedback roomPlayerUnready(String roomID){
        GameRoomFeedback feedback = new GameRoomFeedback(false, false);

        if (!StpUtil.isLogin()) {
            feedback.setMessage("请登录");
        } else {
            String playerName = StpUtil.getLoginIdAsString();
            gameService.ready(roomID, playerName, false);
            feedback.setSuccess(true)
                    .setMessage("玩家 " + playerName + " 已取消准备")
                    .setIsInsideRoom(true)
                    .setSelf(gameService.getPlayerState(playerName))
                    .setRoom(gameService.getRoom(roomID));
        }

        return feedback;
    }

    private boolean checkMaxPlayerCount(Integer maxPlayerCount) {
        return maxPlayerCount >= 2 && maxPlayerCount <= 10;
    }

    private boolean checkRoomName(String roomName) {
        return roomName.length() >= 2 && roomName.length() <= 8;
    }
}
