package com.uno_restart.types.game;

import com.uno_restart.types.player.PlayerInfo;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

@Data
public class GamePlayerInfo {
    @NotNull
    private PlayerInfo who;
    @NotNull
    private Integer restCardCount;
    @NotNull
    private LinkedList<GameCard> cards;
}
