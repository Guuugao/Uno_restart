package com.uno_restart.types.room;

import com.uno_restart.types.player.PlayerInfo;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class RoomPlayerState {
    @NotNull
    private Boolean isReady;
    @NotNull
    private PlayerInfo player;
}
