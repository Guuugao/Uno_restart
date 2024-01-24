package com.uno_restart.service;

import com.uno_restart.types.player.PlayerInfo;
import com.uno_restart.types.room.GameRoomInfo;
import com.uno_restart.types.room.RoomPlayerState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Getter
@Component
public class GameRoomService {
    // 玩家id及其及加入的房间
    // key: playerName value: roomID
    private final HashMap<String, String> playerRooms;
    // 公开房间
    // key: roomID value: roomInfo
    private final HashMap<String, GameRoomInfo> publicRooms;
    // 私有房间
    // key: roomID value: roomInfo
    private final HashMap<String, GameRoomInfo> privateRooms;
    // 所有房间
    // key: roomID value: roomInfo
    private final HashMap<String, GameRoomInfo> rooms;

    public boolean isRoomFull(String roomID) {
        GameRoomInfo room = rooms.get(roomID);
        return room.getCurrentPlayerCount().equals(room.getMaxPlayerCount());
    }
    public boolean isRoomNotExists(String roomID) {
        return !rooms.containsKey(roomID);
    }
    private boolean isPlayerNotJoinRoom(String playerName) {
        return !playerRooms.containsKey(playerName);
    }

    public GameRoomInfo createRoom(String roomName, Boolean isPrivate,
                           Integer maxPlayerCount, String password, String playerName) {
        // 若已加入房间, 则退出
        quit(playerName);
        GameRoomInfo room = new GameRoomInfo(roomName, isPrivate, maxPlayerCount, password);
        rooms.put(room.getRoomID(), room);

        if (isPrivate) privateRooms.put(room.getRoomID(), room);
        else publicRooms.put(room.getRoomID(), room);

        return room;
    }

    public boolean join(PlayerInfo player, String roomID, String password) {
        // 若已加入房间, 则先退出
        quit(player.getPlayerName());
        GameRoomInfo room = rooms.get(roomID);
        boolean join = room.join(player, password);
        if (join) playerRooms.put(player.getPlayerName(), roomID);
        return join;
    }

    public boolean quit(String playerName) {
        // 若玩家未加入房间, 则不处理
        if (isPlayerNotJoinRoom(playerName)) return false;
        String roomID = playerRooms.get(playerName);
        playerRooms.remove(playerName);
        GameRoomInfo room = rooms.get(roomID);
        // 将玩家从指定房间踢出
        room.quit(playerName);
        // 若房间没人则关闭房间
        if (room.getCurrentPlayerCount() == 0) {
            rooms.remove(roomID);
            publicRooms.remove(roomID);
            publicRooms.remove(roomID);
        }
        return true;
    }

    public void ready(String roomID, String playerName, boolean isReady){
        // 若玩家未加入房间, 则不处理
        if (isPlayerNotJoinRoom(playerName)) return;
        GameRoomInfo room = rooms.get(roomID);
        room.ready(playerName, isReady);
    }


    public boolean canStart(String roomID) {
        GameRoomInfo room = rooms.get(roomID);
        // TODO 开始游戏需要检查玩家数量是否大于最小玩家数量, 但是方便测试, 先不写
        return room.getCurrentPlayerCount().equals(room.getReadyCnt());
    }

    public RoomPlayerState getPlayerState(String playerName){
        // 未加入房间, 不做处理
        if (isPlayerNotJoinRoom(playerName)) return null;
        GameRoomInfo room = rooms.get(playerRooms.get(playerName));
        Optional<RoomPlayerState> first = room.getJoinedPlayer().stream()
                .filter(playerState -> playerState.getPlayer().getPlayerName().equals(playerName))
                .findFirst();
        return first.orElse(null);
    }

    public GameRoomInfo getRoom(String roomID){
        return rooms.get(roomID);
    }

    public GameRoomInfo whichRoom(String playerName) {
        return rooms.get(playerRooms.get(playerName));
    }

    public GameRoomService() {
        playerRooms = new HashMap<>();
        publicRooms = new HashMap<>();
        privateRooms = new HashMap<>();
        rooms = new HashMap<>();
    }
}
