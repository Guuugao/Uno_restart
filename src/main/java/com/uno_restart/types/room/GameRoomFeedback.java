package com.uno_restart.types.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public class GameRoomFeedback {
    @NotNull
    private Boolean success;
    private String message;
    @NotNull
    private Boolean isInsideRoom;
    private RoomPlayerState self;
    private GameRoomInfo room;
}
