package com.uno_restart.types.game;

import com.uno_restart.event.DrawCardEvent;
import com.uno_restart.types.enums.EnumGameDirection;
import com.uno_restart.types.enums.EnumGamePlayerStatus;
import com.uno_restart.types.player.PlayerInfo;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

// 代表一局游戏
@Getter
public class Game {
    // 所有游戏使用同一副牌的引用生成的牌堆
    public static final Map<Integer, GameCard> DECK = GameCard.generateDeck();
    // 用于确定第一轮的庄家
    public static final Random random = new Random();

    public static final int INITIAL_NUMBER_OF_CARDS = 7; // 初始卡牌为7张
    // 游戏出牌方向, 默认顺时针
    private EnumGameDirection gameDirection;
    // 记录当前出牌玩家下标, 配合gameDirection与playerList, 即可推断玩家状态
    private int currentPlayerIndex;

    // 玩家名称链表, 配合currentPlayerIndex以及gameDirection
    private List<GamePlayerState> playerList;

    // 记录玩家手牌
    // key: playerName, value: 手牌
    private Map<String, GamePlayerInfo> handCards;
    // 抽牌堆, 使用list, 可以打乱顺序
    private LinkedList<GameCard> drawPile;
    // 弃牌堆
    private LinkedList<GameCard> discardPile;

    // 抽牌事件, 与roomID绑定且频繁触发, 故创建为变量以复用
    private DrawCardEvent drawCardEvent;

    public Game(GameSettings settings) {
        int playerCnt = settings.getPlayers().size();
        gameDirection = EnumGameDirection.clockwise;
        currentPlayerIndex = random.nextInt(playerCnt);
        drawPile = new LinkedList<>(DECK.values());
        Collections.shuffle(drawPile); // 洗牌
        discardPile = new LinkedList<>();
        handCards = settings.getPlayers()
                .stream()
                .collect(Collectors.toMap(PlayerInfo::getPlayerName,
                        player -> new GamePlayerInfo(player, 0, new TreeMap<>())));
        playerList = settings.getPlayers()
                .stream()
                .map(player -> new GamePlayerState(player.getPlayerName(), EnumGamePlayerStatus.watching))
                .collect(Collectors.toCollection(LinkedList::new));
        drawCardEvent = new DrawCardEvent(settings.getRoomInfo().getRoomID());

        // 确定当前回合与下回合出牌玩家
        playerList.get(currentPlayerIndex).setStatus(EnumGamePlayerStatus.onTurns);
        playerList.get((currentPlayerIndex + 1) % playerList.size()).setStatus(EnumGamePlayerStatus.nextTurns);
    }
}
