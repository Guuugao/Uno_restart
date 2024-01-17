package com.uno_restart.types.game;

import com.uno_restart.types.interfaces_enum.EnumGamePlayerStatus;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class GamePlayerState {
    @NotNull
    private String playerID;
    @NotNull
    private EnumGamePlayerStatus status;
}
