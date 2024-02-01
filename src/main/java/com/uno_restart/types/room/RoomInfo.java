package com.uno_restart.types.room;

import com.uno_restart.types.player.PlayerInfo;
import com.uno_restart.util.RoomIDUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class RoomInfo {
    @NotNull
    private String roomID;

    @NotNull
    private String roomName;
    @NotNull
    private Boolean isPlaying;
    @NotNull
    private Boolean isPrivate;
    @NotNull
    private Boolean requirePassword;
    private String password;
    @NotNull
    private Integer maxPlayerCount;
    @NotNull
    private Integer curPlayerCnt;
    @NotNull
    private List<RoomPlayerState> joinedPlayer;

    // 记录已准备玩家数量
    private AtomicInteger readyPlayerCnt;

    // 当前玩家数量加/减
    public void addCurPlayerCnt() {
        this.curPlayerCnt += 1;
    }

    public void subCurPlayerCnt() {
        this.curPlayerCnt -= 1;
    }

    // 当前已准备玩家数量加/减
    public void addReadyPlayerCnt() {
        this.readyPlayerCnt.incrementAndGet();
    }

    public void subReadyPlayerCnt() {
        this.readyPlayerCnt.incrementAndGet();
    }

    public Map<String, PlayerInfo> getPlayerInfos() {
        return joinedPlayer.stream()
                .map(RoomPlayerState::getPlayer)
                .collect(Collectors.toMap(PlayerInfo::getPlayerName, Function.identity()));
    }

    public RoomInfo(@NotNull String roomName, @NotNull Boolean isPrivate,
                    @NotNull Integer maxPlayerCount, String password) {
        this.roomID = RoomIDUtil.getNextId();
        this.roomName = roomName;
        this.isPlaying = false;
        this.isPrivate = isPrivate;
        this.maxPlayerCount = maxPlayerCount;
        this.curPlayerCnt = 0;
        this.joinedPlayer = new LinkedList<>();

        if (password == null || password.isEmpty()) {
            this.requirePassword = false;
        } else {
            this.requirePassword = true;
            this.password = password;
        }

        this.readyPlayerCnt = new AtomicInteger(0);
    }
}
