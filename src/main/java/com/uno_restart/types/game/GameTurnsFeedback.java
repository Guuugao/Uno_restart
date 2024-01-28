package com.uno_restart.types.game;

import com.uno_restart.types.enums.EnumGamePlayerStatus;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class GameTurnsFeedback {
    @NotNull
    private Boolean success;
    private String message;

    @NotNull
    private EnumGamePlayerStatus yourStatus;
    @NotNull
    private List<GamePlayerState> othersStatus;
    private List<GamePlayerInfo> gamePlayerInfo;
    private GameCard lastCard;
    @NotNull
    private GamePlayerAction playerAction;
}
