package com.uno_restart.service;

import com.uno_restart.exception.PlayerNotInRoomException;
import com.uno_restart.exception.RoomNotExistsException;
import com.uno_restart.types.player.PlayerInfo;
import com.uno_restart.types.room.RoomInfo;
import com.uno_restart.types.room.RoomPlayerState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


// TODO boolean类型的返回值可以改成抛出异常, 简化playerDataFetcher代码
@Slf4j
@Getter
@Component
public class RoomService {
    // 玩家id及其及加入的房间
    // key: playerName value: roomID
    private final Map<String, String> playerRooms;
    // 公开房间
    // key: roomID value: roomInfo
    private final Map<String, RoomInfo> publicRooms;
    // 私有房间
    // key: roomID value: roomInfo
    private final Map<String, RoomInfo> privateRooms;
    // 所有房间
    // key: roomID value: roomInfo
    private final Map<String, RoomInfo> rooms;

    public boolean isRoomFull(String roomID) {
        RoomInfo room = rooms.get(roomID);
        return room.getCurrentPlayerCount().equals(room.getMaxPlayerCount());
    }

    public RoomInfo createRoom(String roomName, Boolean isPrivate,
                               Integer maxPlayerCount, String password, String playerName) {
        // 若已加入房间, 则退出
        quit(playerRooms.get(playerName), playerName);
        RoomInfo room = new RoomInfo(roomName, isPrivate, maxPlayerCount, password);
        rooms.put(room.getRoomID(), room);

        if (isPrivate) privateRooms.put(room.getRoomID(), room);
        else publicRooms.put(room.getRoomID(), room);

        log.info("create room " + room.getRoomID());

        return room;
    }

    public boolean join(PlayerInfo player, String roomID, String password) {
        // 若已加入房间, 则先退出
        quit(roomID, player.getPlayerName());
        RoomInfo room = rooms.get(roomID);

        // 若需要密码且密码错误, 则返回
        if (room.getRequirePassword() && !room.getPassword().equals(password)) {
                return false;
        }

        room.setCurrentPlayerCount(1);
        room.getJoinedPlayer().add(new RoomPlayerState(false, player));
        playerRooms.put(player.getPlayerName(), roomID);

        log.info("player " + player.getPlayerName() + " join room " + roomID);

        return true;
    }

    public boolean quit(String roomID, String playerName) {
        // 若玩家未加入房间, 则不处理
        if (!playerRooms.get(playerName).equals(roomID)) return false;
        playerRooms.remove(playerName);
        RoomInfo room = rooms.get(roomID);
        // 将玩家从指定房间踢出
        room.setCurrentPlayerCount(-1);
        room.getJoinedPlayer().removeIf(playerState ->
                playerState.getPlayer().getPlayerName().equals(playerName));
        // 若房间没人则关闭房间
        if (room.getCurrentPlayerCount() == 0) {
            rooms.remove(roomID);
            publicRooms.remove(roomID);
            publicRooms.remove(roomID);

            log.info("room " + roomID + " is closed");
        }

        log.info("player " + playerName + " quit room " + roomID);

        return true;
    }

    public void ready(String roomID, String playerName, boolean isReady){
        // 若玩家未加入房间, 则不处理
        RoomInfo room = rooms.get(roomID);
        Optional<RoomPlayerState> first = room.getJoinedPlayer().stream()
                .filter(playerState -> playerState.getPlayer().getPlayerName().equals(playerName))
                .findFirst();
        first.ifPresent(roomPlayerState -> {
            roomPlayerState.setIsReady(isReady);
            room.setReadyPlayerCnt(isReady ? 1 : -1); // 根据玩家是否准备, 修改已准备玩家数量
            log.info("player " + playerName + " is ready: " + isReady);
        });
    }

    public RoomPlayerState getPlayerState(String playerName){
        return rooms.get(playerRooms.get(playerName)).getJoinedPlayer().stream()
                .filter(playerState -> playerState.getPlayer().getPlayerName().equals(playerName))
                .findFirst()
                .orElse(null);
    }


    public RoomInfo getRoom(String roomID){
        return rooms.get(roomID);
    }

    public RoomInfo whichRoom(String playerName) {
        return rooms.get(playerRooms.get(playerName));
    }

    // 检查房间是否存在
    public void checkRoomExists(String roomID) throws RoomNotExistsException {
        if (!rooms.containsKey(roomID))
            throw new RoomNotExistsException("房间不存在");
    }

    // 检查玩家是否加入指定房间
    public void checkPlayerInRoom(String roomID, String playerName) throws PlayerNotInRoomException {
        if (!playerRooms.get(playerName).equals(roomID))
            throw new PlayerNotInRoomException("玩家未加入房间 " + roomID);
    }

    public RoomService() {
        playerRooms = new HashMap<>();
        publicRooms = new HashMap<>();
        privateRooms = new HashMap<>();
        rooms = new HashMap<>();
    }
}
