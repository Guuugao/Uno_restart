package com.uno_restart.types.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uno_restart.exception.GameAbnormalException;
import com.uno_restart.types.enums.EnumUnoCardColor;
import com.uno_restart.types.enums.EnumUnoCardType;
import org.jetbrains.annotations.NotNull;
import java.util.Map;
import java.util.TreeMap;

public record GameCard(@NotNull EnumUnoCardType cardType, @NotNull EnumUnoCardColor cardColor,
                       Integer cardID) implements Comparable<GameCard> {
    public static ObjectMapper jsonParser = new ObjectMapper();

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
        else if (this.cardColor != o.cardColor()) return this.cardColor.compareTo(o.cardColor);
            // 最后根据卡牌id排序
        else return this.cardID - o.cardID;
    }

    public int getScore() {
        return cardType.getScore();
    }

    // 从GameCardInput类型获取GameCard类型
    // 在卡牌非法时抛出异常
    public static GameCard getCardFromInput(Map<Object, Object> cardInput)
            throws GameAbnormalException {
        // 打出的卡牌图案必须与牌组中对应卡牌一致, 若为万能牌, 则颜色可以不一致
        // 如果为万能牌, 则创建一张新牌, 因为牌色需要改变
        // 如果非万能牌, 则找到全局静态牌组, 根据卡牌id返回引用
        GameCard card = Game.DECK.get((Integer)cardInput.get("cardID"));
        // 使用valueOf转换, 可以顺便检查输入是否合理, 若枚举值不存在则抛出IllegalArgumentException
        boolean isSameType = card.cardType() == EnumUnoCardType.valueOf(cardInput.get("cardType").toString());
        boolean isSameColor = card.cardColor() == EnumUnoCardColor.valueOf(cardInput.get("cardColor").toString());
        boolean isBlank = card.cardColor() == EnumUnoCardColor.BLANK;

        if (isSameType && (isSameColor || isBlank)) {
            return isBlank ? jsonParser.convertValue(cardInput, GameCard.class) : card;
        } else {
            throw new GameAbnormalException("非法卡牌");
        }
    }
}

//@Data
//@AllArgsConstructor
//public class GameCard implements Comparable<GameCard> {
//    @NotNull
//    private final EnumUnoCardType cardType;
//    @NotNull
//    private final EnumUnoCardColor cardColor;
//
//    private final int cardID;
//
//    public static Map<Integer, GameCard> generateDeck() {
//        int cardID = 0;
//        Map<Integer, GameCard> deck = new TreeMap<>();
//        EnumUnoCardColor[] colors = EnumUnoCardColor.values();
//        EnumUnoCardType[] types = EnumUnoCardType.values();
//
//        for (EnumUnoCardType type : types) {
//            switch (type) {
//                case WILD, ADD4 -> {
//                    for (int universalCardCnt = 0; universalCardCnt < 4; ++universalCardCnt) {
//                        GameCard universalCard = new GameCard(type, EnumUnoCardColor.BLANK, ++cardID);
//                        deck.put(cardID, universalCard);
//                    }
//                }
//                case N0 -> {
//                    for (int zeroCnt = 0; zeroCnt < 4; ++zeroCnt) {
//                        GameCard zeroCard = new GameCard(type, colors[zeroCnt], ++cardID);
//                        deck.put(cardID, zeroCard);
//                    }
//                }
//                default -> {
//                    for (int i = 0; i < 4; ++i) {
//                        // 每种颜色的其他牌各有两张
//                        for (int cardCnt = 0; cardCnt < 2; ++cardCnt) {
//                            GameCard normalCard = new GameCard(type, colors[cardCnt], ++cardID);
//                            deck.put(cardID, normalCard);
//                        }
//                    }
//                }
//            }
//        }
//
//        return deck;
//    }
//
//    @Override
//    public int compareTo(@NotNull GameCard o) {
//        // 首先根据卡牌类型排序
//        if (this.cardType != o.cardType) return this.cardType.compareTo(o.cardType);
//            // 其次根据颜色次序排序
//        else if (this.cardColor != o.getCardColor()) return this.cardColor.compareTo(o.cardColor);
//            // 最后根据卡牌id排序
//        else return this.cardID - o.cardID;
//    }
//
//    public int getScore(){
//        return cardType.getScore();
//    }
//}
