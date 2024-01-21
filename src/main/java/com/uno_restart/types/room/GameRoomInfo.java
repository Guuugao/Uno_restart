package com.uno_restart.types.room;

import com.uno_restart.types.player.PlayerInfo;
import com.uno_restart.util.RoomIDUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class GameRoomInfo {
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
    private Integer currentPlayerCount;
    @NotNull
    private ArrayList<RoomPlayerState> joinedPlayer;

    public void quit(String playerName) {
        --currentPlayerCount;
        joinedPlayer.removeIf(playerState ->
                playerState.getPlayer().getPlayerName().equals(playerName));
    }

    // 成功加入返回房间ID, 失败返回null
    public boolean join(PlayerInfo player, String password) {
        boolean join = true;

        if (this.requirePassword) {
            join = this.password.equals(password);
        }

        if (join) {
            ++currentPlayerCount;
            joinedPlayer.add(new RoomPlayerState(false, player));
        }

        return join;
    }

    public void ready(String playerName, boolean isReady) {
        Optional<RoomPlayerState> first = joinedPlayer.stream()
                .filter(playerState -> playerState.getPlayer().getPlayerName().equals(playerName))
                .findFirst();
        first.ifPresent(roomPlayerState -> roomPlayerState.setIsReady(isReady));
    }

    public GameRoomInfo(@NotNull String roomName, @NotNull Boolean isPrivate,
                        @NotNull Integer maxPlayerCount, String password) {
        this.roomID = RoomIDUtil.getNextId();
        this.roomName = roomName;
        this.isPlaying = false;
        this.isPrivate = isPrivate;
        this.maxPlayerCount = maxPlayerCount;
        this.currentPlayerCount = 0;
        this.joinedPlayer = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            this.requirePassword = false;
        } else {
            this.requirePassword = true;
            this.password = password;
        }
    }
}
