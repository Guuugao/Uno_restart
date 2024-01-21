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
public class GameService {
    // 玩家id及其及加入的房间
    // key: playerName value: roomID
    private HashMap<String, String> playerRooms;
    // 公开房间
    // key: roomID value: roomInfo
    private HashMap<String, GameRoomInfo> publicRooms;
    // 私有房间
    // key: roomID value: roomInfo
    private HashMap<String, GameRoomInfo> privateRooms;
    // 所有房间
    // key: roomID value: roomInfo
    private HashMap<String, GameRoomInfo> rooms;

    public boolean canCreate(String playerName) {
        return playerRooms.containsKey(playerName);
    }

    public GameRoomInfo createRoom(String roomName, Boolean isPrivate,
                           Integer maxPlayerCount, String password) {
        GameRoomInfo room = new GameRoomInfo(roomName, isPrivate, maxPlayerCount, password);
        rooms.put(room.getRoomID(), room);

        if (isPrivate) privateRooms.put(room.getRoomID(), room);
        else publicRooms.put(room.getRoomID(), room);

        return room;
    }

    public boolean join(PlayerInfo player, String roomID, String password) {
        // 若房间不存在, 不做处理
        if (!rooms.containsKey(roomID)) return false;
        // 若已加入房间, 则先退出
        quit(player.getPlayerName());
        GameRoomInfo room = rooms.get(roomID);
        boolean join = room.join(player, password);
        if (join) playerRooms.put(player.getPlayerName(), roomID);
        return join;
    }

    public boolean quit(String playerName) {
        // 若玩家未加入房间, 则不处理
        if (!playerRooms.containsKey(playerName)) return false;
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
        if (!playerRooms.containsKey(playerName)) return;
        GameRoomInfo room = rooms.get(roomID);
        room.ready(playerName, isReady);
    }

    public RoomPlayerState getPlayerState(String playerName){
        // 未加入房间, 不做处理
        if (!playerRooms.containsKey(playerName)) return null;
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

    public GameService() {
        playerRooms = new HashMap<>();
        publicRooms = new HashMap<>();
        privateRooms = new HashMap<>();
        rooms = new HashMap<>();
    }
}
