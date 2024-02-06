package com.uno_restart.types.game;

import com.uno_restart.types.enums.EnumGameDirection;
import com.uno_restart.types.enums.EnumGamePlayerStatus;
import com.uno_restart.types.player.PlayerInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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
    // 玩家名称链表, 配合currentPlayerIndex以及gameDirection实现出牌顺序
    private List<String> playerList;
    // 记录玩家状态
    // key: playerName, value: GamePlayerInfo
    private Map<String, GamePlayerState> gamePlayerStates;
    // 记录玩家手牌
    // key: playerName, value: GamePlayerInfo
    private Map<String, GamePlayerInfo> gamePlayerInfos;
    // 抽牌堆, 使用list, 可以打乱顺序
    private LinkedList<GameCard> drawPile;
    // 弃牌堆
    private LinkedList<GameCard> discardPile;
    // 上一张牌
    private GameCard preCard;

    // 记录是否所有玩家已订阅'gameWaitNextReaction'
    // 初始化为玩家数量, 减至0代表所有玩家已连接
    private AtomicInteger playerConnectCnt;

    public Game(GameSettings settings) {
        int playerCnt = settings.getPlayers().size();
        roomID = settings.getRoomInfo().getRoomID();
        gameDirection = EnumGameDirection.clockwise;
        curPlayerIndex = random.nextInt(playerCnt);
        drawPile = new LinkedList<>(DECK.values());
        Collections.shuffle(drawPile); // 洗牌
        discardPile = new LinkedList<>();
        playerList = settings.getPlayers()
                .stream()
                .map(PlayerInfo::getPlayerName)
                .collect(Collectors.toCollection(LinkedList::new));
        gamePlayerStates = settings.getPlayers()
                .stream()
                .map(playerInfo -> new GamePlayerState(playerInfo.getPlayerName(), EnumGamePlayerStatus.watching))
                .collect(Collectors.toMap(GamePlayerState::getPlayerName, Function.identity()));
        gamePlayerInfos = settings.getPlayers()
                .stream()
                .collect(Collectors.toMap(PlayerInfo::getPlayerName,
                        player -> new GamePlayerInfo(player, new TreeMap<>())));
        // 确定当前回合与下回合出牌玩家
        setGamePlayerStateByIndex(curPlayerIndex, EnumGamePlayerStatus.onTurns);
        setGamePlayerStateByIndex(getNextPlayerIndex(), EnumGamePlayerStatus.nextTurns);

        preCard = null;
        playerConnectCnt = new AtomicInteger(playerCnt);
    }

    // 获取指定玩家游戏信息
    public GamePlayerInfo getGamePlayerInfo(String playerName) {
        return gamePlayerInfos.get(playerName);
    }

    // 获取指定玩家游戏状态
    public GamePlayerState getGamePlayerState(String playerName) {
        return gamePlayerStates.get(playerName);
    }

    // 获取当前回合玩家游戏状态
    public GamePlayerState getCurGamePlayerState(){
        return gamePlayerStates.get(playerList.get(curPlayerIndex));
    }

    // 根据下标修改玩家状态
    public void setGamePlayerStateByIndex(int index, EnumGamePlayerStatus state) {
        gamePlayerStates.get(playerList.get(index)).setState(state);
    }

    // 获取下回合出牌玩家下标
    public int getNextPlayerIndex(){
        if (gameDirection == EnumGameDirection.clockwise)
            return (curPlayerIndex + 1) % playerList.size();
        else
            return (curPlayerIndex - 1 + playerList.size()) % playerList.size();
    }

    public void moveIndex() {
        if (gameDirection == EnumGameDirection.clockwise)
            this.curPlayerIndex = (curPlayerIndex + 1) % playerList.size();
        else
            this.curPlayerIndex = (curPlayerIndex - 1 + playerList.size()) % playerList.size();
    }

    public String getNextPlayerName(){
        return playerList.get(getNextPlayerIndex());
    }

    public String getCurPlayerName(){
        return playerList.get(curPlayerIndex);
    }

    // 修改计数器, 表示玩家加入游戏
    public void playerConnect(){
        playerConnectCnt.decrementAndGet();
    }

    // 是否所有玩家已连接
    public boolean isAllPlayerConnect() {
        return playerConnectCnt.get() == 0;
    }

}
