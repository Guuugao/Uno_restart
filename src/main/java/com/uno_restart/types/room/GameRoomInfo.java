package com.uno_restart.types.room;

import com.uno_restart.util.RoomIDUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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

    public GameRoomInfo(@NotNull String roomName, @NotNull Boolean isPrivate,
                        @NotNull Integer maxPlayerCount, RoomPlayerState state,
                        String password) {
        this.roomID = RoomIDUtil.getNextId();
        this.roomName = roomName;
        this.isPlaying = false;
        this.isPrivate = isPrivate;
        this.maxPlayerCount = maxPlayerCount;
        this.currentPlayerCount = 1;
        this.joinedPlayer = new ArrayList<>(List.of(state));

        if (password == null || password.isEmpty()) {
            this.requirePassword = false;
            this.password = "";
        } else {
            this.requirePassword = true;
            this.password = password;
        }
    }

}
