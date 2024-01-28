package com.uno_restart.types.game;

import com.uno_restart.types.player.PlayerInfo;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.TreeMap;

@Data
public class GamePlayerInfo {
    @NotNull
    private PlayerInfo who;
    @NotNull
    private Integer remainingCardCnt;
    @NotNull
    private TreeMap<Integer, GameCard> handCards;

    public void setRemainingCardCnt(int diff) {
        this.remainingCardCnt += diff;
    }

    public String getPlayerName() {
        return who.getPlayerName();
    }
}
