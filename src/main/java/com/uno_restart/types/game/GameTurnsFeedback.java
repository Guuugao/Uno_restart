package com.uno_restart.types.game;

import com.uno_restart.types.interfaces_enum.EnumGamePlayerStatus;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Data
public class GameTurnsFeedback {
    @NotNull
    private Boolean success;
    private String message;

    @NotNull
    private EnumGamePlayerStatus yourStatus;
    @NotNull
    private ArrayList<EnumGamePlayerStatus> othersStatus;
    @NotNull
    private ArrayList<GamePlayerInfo> playerGameInfo;
    private GameCard lastCard;
}
