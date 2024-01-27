package com.uno_restart.types.game;

import com.uno_restart.types.enums.EnumUnoCardColor;
import com.uno_restart.types.enums.EnumUnoCardType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Data
@AllArgsConstructor
// TODO 转为记录?
public class GameCard implements Comparable<GameCard> {
    @NotNull
    private final EnumUnoCardType cardType;
    @NotNull
    private final EnumUnoCardColor cardColor;

    private final int cardID;

    public static Map<Integer, GameCard> generateDeck() {
        int cardID = 0;
        Map<Integer, GameCard> deck = new TreeMap<>();
        EnumUnoCardColor[] colors = EnumUnoCardColor.values();
        EnumUnoCardType[] types = EnumUnoCardType.values();

        for (EnumUnoCardType type : types) {
            switch (type) {
                case WILD, ADD4 -> {
                    for (int universalCardCnt = 0; universalCardCnt < 4; ++universalCardCnt) {
                        GameCard universalCard = new GameCard(type, EnumUnoCardColor.BLANK, ++cardID);
                        deck.put(cardID, universalCard);
                    }
                }
                case N0 -> {
                    for (int zeroCnt = 0; zeroCnt < 4; ++zeroCnt) {
                        GameCard zeroCard = new GameCard(type, colors[zeroCnt], ++cardID);
                        deck.put(cardID, zeroCard);
                    }
                }
                default -> {
                    for (int i = 0; i < 4; ++i) {
                        // 每种颜色的其他牌各有两张
                        for (int cardCnt = 0; cardCnt < 2; ++cardCnt) {
                            GameCard normalCard = new GameCard(type, colors[cardCnt], ++cardID);
                            deck.put(cardID, normalCard);
                        }
                    }
                }
            }
        }

        return deck;
    }

    @Override
    public int compareTo(@NotNull GameCard o) {
        // 首先根据卡牌类型排序
        if (this.cardType != o.cardType) return this.cardType.compareTo(o.cardType);
        // 其次根据颜色次序排序
        else if (this.cardColor != o.getCardColor()) return this.cardColor.compareTo(o.cardColor);
        // 最后根据卡牌id排序
        else return this.cardID - o.cardID;
    }
}
