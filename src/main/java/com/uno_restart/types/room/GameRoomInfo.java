package com.uno_restart.types.room;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Data
public class GameRoomInfo {
    @NotNull
    private String roomId;

    @NotNull
    private String roomName;
    @NotNull
    private Boolean isPlaying;
    @NotNull
    private Boolean privateRoom;
    @NotNull
    private Boolean requirePassword;
    @NotNull
    private Integer maxPlayerCount;
    @NotNull
    private Integer currentPlayerCount;
    @NotNull
    private ArrayList<RoomPlayerState> joinedPlayer;
}
