package com.uno_restart.types.room;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Data
@Accessors(chain = true)
public class GameRoomFeedback {
    @NotNull
    private Boolean success;
    private String message;
    @NotNull
    private Boolean isInsideRoom;
    private RoomPlayerState self;
    private GameRoomInfo room;
}
