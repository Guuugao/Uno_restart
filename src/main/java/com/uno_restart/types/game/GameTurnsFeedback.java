package com.uno_restart.types.game;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@RequiredArgsConstructor
public class GameTurnsFeedback {
    @NotNull
    private Collection<GamePlayerState> playerStatus;
    @NotNull
    private GamePlayerInfo yourGameInfo;
    private GameCard lastCard;
    @NotNull
    private List<GamePlayerAction> playerActions;
}
