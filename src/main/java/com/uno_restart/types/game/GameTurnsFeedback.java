package com.uno_restart.types.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Data
@AllArgsConstructor
public class GameTurnsFeedback {
    @NotNull
    private EnumGamePlayerStatus yourStatus;
    @NotNull
    private ArrayList<EnumGamePlayerStatus> othersStatus;
    @NotNull
    private ArrayList<GamePlayerInfo> playerGameInfo;
    private GameCard lastCard;
    private String message;
}
