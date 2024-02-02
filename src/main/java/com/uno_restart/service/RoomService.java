package com.uno_restart.service;

import com.uno_restart.event.GameInterruptionEvent;
import com.uno_restart.event.GameStartEvent;
import com.uno_restart.event.RoomCloseEvent;
import com.uno_restart.exception.PlayerAbnormalException;
import com.uno_restart.exception.RoomAbnormalException;
import com.uno_restart.types.game.GamePlayerState;
import com.uno_restart.types.game.GameSettings;
import com.uno_restart.types.player.PlayerInfo;
import com.uno_restart.types.room.RoomInfo;
import com.uno_restart.types.room.RoomPlayerState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Getter
@Component
public class RoomService {
    private static final int minPlayerCnt = 2;
    private static final int maxPlayerCnt = 10;
    private static final int minRoomNameLen = 2;
    private static final int maxRoomNameLen = 8;


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
    @Autowired
    ApplicationEventPublisher eventPublisher;
    @Autowired
    PlayerService playerService;
    @Autowired
    GameService gameService;

    public RoomInfo createRoom(String roomName, Boolean isPrivate,
                               Integer maxPlayerCount, String password, String playerName) {
        // 若已加入房间, 则退出
        quit(playerName);
        RoomInfo room = new RoomInfo(roomName, isPrivate, maxPlayerCount, password);
        rooms.put(room.getRoomID(), room);

        if (isPrivate) privateRooms.put(room.getRoomID(), room);
        else publicRooms.put(room.getRoomID(), room);

        return room;
    }

    public boolean join(PlayerInfo player, String roomID, String password) {
        // 若已加入房间, 则先退出
        quit(player.getPlayerName());
        RoomInfo room = rooms.get(roomID);

        // 若需要密码且密码错误, 则返回
        if (room.getRequirePassword() && !room.getPassword().equals(password)) {
            return false;
        }

        room.addCurPlayerCnt();
        room.getJoinedPlayer().add(new RoomPlayerState(false, player));
        playerRooms.put(player.getPlayerName(), roomID);

        return true;
    }

    public void quit(String playerName) {
        // 若玩家未加入房间, 则不处理
        if (!playerRooms.containsKey(playerName)) return; // 加入房间和创建房间也需要先退出, 所以需要检查一遍是否加入房间

        String roomID = playerRooms.get(playerName);
        if (isRoomPlaying(roomID)) {
            eventPublisher.publishEvent(new GameInterruptionEvent(roomID));
        }

        playerRooms.remove(playerName);
        RoomInfo room = rooms.get(roomID);
        // 将玩家从指定房间踢出
        room.subCurPlayerCnt();
        room.getJoinedPlayer().removeIf(playerState ->
                playerState.getPlayer().getPlayerName().equals(playerName));

        // 若房间没人则关闭房间
        if (room.getCurPlayerCnt() == 0) {
            rooms.remove(roomID);
            publicRooms.remove(roomID);
            publicRooms.remove(roomID);
            eventPublisher.publishEvent(new RoomCloseEvent(roomID));

            log.info("room " + roomID + ": close");
        }
    }

    public void ready(String roomID, String playerName, boolean isReady) {
        RoomInfo room = rooms.get(roomID);
        Optional<RoomPlayerState> first = room.getJoinedPlayer().stream()
                .filter(playerState -> playerState.getPlayer().getPlayerName().equals(playerName))
                .findFirst();
        first.ifPresent(roomPlayerState -> {
            // 若登录状态未改变, 不做修改
            if (roomPlayerState.getIsReady() != isReady) {
                roomPlayerState.setIsReady(isReady);
                // 根据玩家是否准备, 修改已准备玩家数量
                if (isReady) room.addReadyPlayerCnt();
                else room.subReadyPlayerCnt();

                // 若玩家全部准备并且玩家数量大于最小玩家数量, 则开始
                if (isPlayerNumOK(room.getCurPlayerCnt()) && isAllPlayerReady(room)) {
                    LinkedList<PlayerInfo> players = room.getJoinedPlayer().stream()
                            .map(RoomPlayerState::getPlayer)
                            .collect(Collectors.toCollection(LinkedList::new));
                    GameSettings settings = new GameSettings(players, room);
                    room.setIsPlaying(true);
                    eventPublisher.publishEvent(new GameStartEvent(roomID, settings));
                    gameService.createGame(settings); // 创建游戏

                    log.debug("room " + roomID + ": create game");
                }
            }
        });
    }

    public RoomPlayerState getPlayerState(String playerName) {
        return rooms.get(playerRooms.get(playerName)).getJoinedPlayer().stream()
                .filter(playerState -> playerState.getPlayer().getPlayerName().equals(playerName))
                .findFirst()
                .orElse(null);
    }


    public RoomInfo getRoom(String roomID) {
        return rooms.get(roomID);
    }

    public RoomInfo whichRoom(String playerName) {
        return rooms.get(playerRooms.get(playerName));
    }

    // 根据排名更新玩家数据, 参数需要有序
    public void saveRankInfo(String roomID, List<GamePlayerState> rank) {
        Map<String, PlayerInfo> players = rooms.get(roomID).getPlayerInfos();
        players.get(rank.get(0).getPlayerName()).addWinTimes();
        for (int i = 1; i < rank.size(); i++) {
            players.get(rank.get(i).getPlayerName()).addFailTimes();
        }
        playerService.saveBatch(players.values()); // 批量保存数据
    }

    // 检查房间是否存在
    public void checkRoomExists(String roomID) throws RoomAbnormalException {
        if (!rooms.containsKey(roomID))
            throw new RoomAbnormalException("房间不存在");
    }

    // 检查玩家是否加入指定房间
    public void checkPlayerInRoom(String roomID, String playerName) throws PlayerAbnormalException {
        if (playerRooms.containsKey(playerName) && !playerRooms.get(playerName).equals(roomID))
            throw new PlayerAbnormalException("玩家未加入房间 " + roomID);
    }

    // 玩家未加入任何房间的情况下抛出异常
    public void checkPlayerInAnyRoom(String playerName) throws RoomAbnormalException {
        if (!playerRooms.containsKey(playerName))
            throw new RoomAbnormalException("玩家未加入任何房间");
    }

    public void checkRoomFull(String roomID) throws RoomAbnormalException {
        if (rooms.get(roomID).getCurPlayerCnt().equals(rooms.get(roomID).getMaxPlayerCount()))
            throw new RoomAbnormalException("房间已满");
    }

    // 在房间正在进行游戏时抛出异常
    public void checkIsPlaying(String roomID) throws RoomAbnormalException {
        if (isRoomPlaying(roomID))
            throw new RoomAbnormalException("正在游戏中");
    }

    @NotNull
    private Boolean isRoomPlaying(String roomID) {
        return rooms.get(roomID).getIsPlaying();
    }


    public boolean isPlayerNumOK(Integer curPlayerCnt) {
        return curPlayerCnt >= minPlayerCnt && curPlayerCnt <= maxPlayerCnt;
    }

    public boolean isAllPlayerReady(RoomInfo room) {
        return room.getCurPlayerCnt() == room.getReadyPlayerCnt().get();
    }

    public boolean isRoomNameOK(String roomName) {
        return roomName.length() >= minRoomNameLen && roomName.length() <= maxRoomNameLen;
    }

    public RoomService() {
        playerRooms = new HashMap<>();
        publicRooms = new HashMap<>();
        privateRooms = new HashMap<>();
        rooms = new HashMap<>();
    }
}
