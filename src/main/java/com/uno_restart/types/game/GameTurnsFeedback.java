package com.uno_restart.types.game;

import com.uno_restart.types.enums.EnumGamePlayerStatus;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

@Data
@Accessors(chain = true)
public class GameTurnsFeedback {
    @NotNull
    private Boolean success;
    private String message;

    @NotNull
    private EnumGamePlayerStatus yourStatus;
    @NotNull
    private List<GamePlayerState> othersStatus;
    private Collection<GamePlayerInfo> gamePlayerInfo;
    private GameCard lastCard;
    private List<GamePlayerAction> playerActions;
}
