package com.uno_restart.types.game;

import com.uno_restart.types.player.PlayerInfo;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.TreeMap;

@Data
public class GamePlayerInfo {
    @NotNull
    private PlayerInfo who;
    @NotNull
    private Integer restCardCount;
    @NotNull
    private TreeMap<Integer, GameCard> cards;

    public void setRestCardCount(int diff) {
        this.restCardCount += diff;
    }

    public String getPlayerName() {
        return who.getPlayerName();
    }
}
