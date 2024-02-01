package com.uno_restart.types.game;

import com.uno_restart.types.player.PlayerInfo;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.TreeMap;

@Data
public class GamePlayerInfo {
    @NotNull
    private PlayerInfo who;
    private Collection<GameCard> cards; // handCards值的引用, 用于返回给前端
    @NotNull
    private TreeMap<Integer, GameCard> handCards; // 实际记录手牌

    public GamePlayerInfo(@NotNull PlayerInfo who, @NotNull TreeMap<Integer, GameCard> handCards) {
        this.who = who;
        this.handCards = handCards;
        this.cards = handCards.values();
    }

    public String getPlayerName() {
        return who.getPlayerName();
    }
}
