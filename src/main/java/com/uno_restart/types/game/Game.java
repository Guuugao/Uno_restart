package com.uno_restart.types.game;

import com.uno_restart.types.enums.EnumGameDirection;
import com.uno_restart.types.enums.EnumGamePlayerStatus;
import com.uno_restart.types.player.PlayerInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

// 代表一局游戏
@Getter
@Setter
public class Game {
    // 所有游戏使用同一副牌的引用生成的牌堆
    public static final Map<Integer, GameCard> DECK = GameCard.generateDeck();
    // 用于确定第一轮的庄家
    public static final Random random = new Random();

    public static final int INITIAL_NUMBER_OF_CARDS = 7; // 初始卡牌为7张

    // 房间ID
    private final String roomID;
    // 游戏出牌方向, 默认顺时针
    private EnumGameDirection gameDirection;
    // 记录当前出牌玩家下标, 配合gameDirection与playerList, 即可推断玩家状态
    private int curPlayerIndex;
    // 玩家名称链表, 配合currentPlayerIndex以及gameDirection
    private List<GamePlayerState> playerList;
    // 记录玩家手牌
    // key: playerName, value: 手牌
    private Map<String, GamePlayerInfo> gamePlayers;
    // 抽牌堆, 使用list, 可以打乱顺序
    private LinkedList<GameCard> drawPile;
    // 弃牌堆
    private LinkedList<GameCard> discardPile;
    // 上一张牌
    private GameCard previousCard;

    // 记录是否所有玩家已订阅'gameWaitNextReaction'
    private CountDownLatch playerConnectLatch;

    public Game(GameSettings settings) {
        int playerCnt = settings.getPlayers().size();
        roomID = settings.getRoomInfo().getRoomID();
        gameDirection = EnumGameDirection.clockwise;
        curPlayerIndex = random.nextInt(playerCnt);
        drawPile = new LinkedList<>(DECK.values());
        Collections.shuffle(drawPile); // 洗牌
        discardPile = new LinkedList<>();
        gamePlayers = settings.getPlayers()
                .stream()
                .collect(Collectors.toMap(PlayerInfo::getPlayerName,
                        player -> new GamePlayerInfo(player, 0, new TreeMap<>())));
        playerList = settings.getPlayers()
                .stream()
                .map(player -> new GamePlayerState(player.getPlayerName(), EnumGamePlayerStatus.watching))
                .collect(Collectors.toCollection(LinkedList::new));

        // 确定当前回合与下回合出牌玩家
        setGamePlayerStateByIndex(curPlayerIndex, EnumGamePlayerStatus.onTurns);
        setGamePlayerStateByIndex(getNextPlayerIndex(), EnumGamePlayerStatus.nextTurns);

        playerConnectLatch = new CountDownLatch(playerCnt);
    }

    // 获取指定玩家游戏信息
    public GamePlayerInfo getGamePlayerInfo(String playerName) {
        return gamePlayers.get(playerName);
    }

    // 获取指定玩家游戏状态
    public GamePlayerState getGamePlayerState(String playerName) {
        return playerList.stream()
                .filter(gamePlayerState -> gamePlayerState.getPlayerName().equals(playerName))
                .findAny().get();
    }

    // 获取当前回合玩家游戏状态
    public GamePlayerState getCurGamePlayerState(){
        return playerList.get(curPlayerIndex);
    }

    // 获取当前玩家游戏信息
    public GamePlayerInfo getCurGamePlayerInfo(){
        return gamePlayers.get(getCurGamePlayerState().getPlayerName());
    }

    // 获取玩家数量
    public int getPlayerCnt(){
        return playerList.size();
    }

    // 根据下标修改玩家状态
    public void setGamePlayerStateByIndex(int index, EnumGamePlayerStatus state) {
        playerList.get(index).setStatus(state);
    }

    // 获取下回合出牌玩家下标
    public int getNextPlayerIndex(){
        if (gameDirection == EnumGameDirection.clockwise)
            return (curPlayerIndex + 1) % getPlayerCnt();
        else
            return (curPlayerIndex - 1 + getPlayerCnt()) % getPlayerCnt();
    }

    public String getNextPlayerName(){
        return playerList.get(getNextPlayerIndex()).getPlayerName();
    }

    public String getCurPlayerName(){
        return playerList.get(curPlayerIndex).getPlayerName();
    }
}
