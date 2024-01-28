package com.uno_restart.service;

import com.uno_restart.event.DrawCardEvent;
import com.uno_restart.exception.AbnormalGameException;
import com.uno_restart.types.enums.EnumGameDirection;
import com.uno_restart.types.enums.EnumGamePlayerStatus;
import com.uno_restart.types.enums.EnumUnoCardType;
import com.uno_restart.types.game.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GameService {
    // 记录游戏对象
    // key: roomID value: game
    private HashMap<String, Game> games;
    @Autowired
    ApplicationEventPublisher eventPublisher;


    // 创建游戏, 初始化牌堆, 确定出牌次序
    public void gameInit(GameSettings settings) {
        Game game = new Game(settings);
        String roomID = settings.getRoomInfo().getRoomID();
        games.put(roomID, game);

        int currentPlayerIndex = game.getCurPlayerIndex();
        List<GamePlayerState> playerList = game.getPlayerList();
        for (int i = 0; i < playerList.size(); ++i) {
            String playerName = playerList.get(currentPlayerIndex).getPlayerName();
            drawCard(roomID, playerName, Game.INITIAL_NUMBER_OF_CARDS);

            eventPublisher.publishEvent(new DrawCardEvent(roomID, playerName, Game.INITIAL_NUMBER_OF_CARDS));
            currentPlayerIndex = (currentPlayerIndex + 1) % playerList.size();
        }

        log.debug("game " + roomID + " init");
    }

    // 抽取指定数量卡牌并添加至指定玩家手牌
    public LinkedList<GameCard> drawCard(String roomID, String playerName, int cnt){
        Game game = games.get(roomID);
        GamePlayerInfo gamePlayerInfo = game.getGamePlayers().get(playerName);
        LinkedList<GameCard> drawPile = game.getDrawPile(); // 抽牌堆
        LinkedList<GameCard> discardPile = game.getDiscardPile(); // 弃牌堆
        // 抽牌堆卡牌不足, 将弃牌堆重新洗入
        if (drawPile.size() < cnt) {
            // 先对弃牌堆洗牌, 这样不会影响抽牌堆原先剩余卡牌的牌序
            Collections.shuffle(discardPile);
            drawPile.addAll(discardPile);
            discardPile.clear();
            log.info("shuffle discard pile");
        }

        // 抽取指定数量的卡牌
        LinkedList<GameCard> drawCards = drawPile.stream()
                .limit(cnt).collect(Collectors.toCollection(LinkedList::new));
        // 将卡牌加入指定玩家手牌
        gamePlayerInfo.getHandCards().putAll(drawCards.stream()
                .collect(Collectors.toMap(GameCard::getCardID, Function.identity())));
        // 修改手牌数量
        gamePlayerInfo.setRemainingCardCnt(cnt);

        log.trace("player " + playerName + " draw cards " + drawCards + ", number of remaining cards " + gamePlayerInfo.getRemainingCardCnt());

        // 从抽牌堆移除元素
        while (cnt > 0) {
            drawPile.removeFirst();
            --cnt;
        }

        return drawCards;
    }

    // 打出一张牌
    // 这里面识别牌的种类, 将牌移入弃牌堆后根据种类调用私有方法完成卡牌逻辑
    public void sendACard(String roomID, String playerName, GameCard card) {
        Game game = games.get(roomID);
        movieCardToDiscardPile(roomID, playerName, card); // 将牌移入弃牌堆
        game.setPreviousCard(card); // 记录当前卡牌作为下回合出牌依据

        switch (card.getCardType()) {
            case REVERSE -> reverseGameDirection(game);
            case SKIP -> movieIndex(game); // 额外移动一次下标即代表跳过回合
            case ADD2 -> { // 抽2张牌并跳过
                drawCard(roomID, playerName, 2);
                movieIndex(game);
            }
            case ADD4 -> { // 抽4张牌并跳过, 指定颜色功能在前面的公共代码已经完成
                drawCard(roomID, playerName, 4);
                movieIndex(game);
            }
        }

        movieIndex(game);
    }

    // 当前玩家放弃出牌
    public void giveUpSendCard(String roomID){
        movieIndex(games.get(roomID));
    }

    public GameCard pickFirstCard(String roomID) throws AbnormalGameException {
        Game game = games.get(roomID);
        if (game.getPreviousCard() != null)
            throw new AbnormalGameException("初始卡牌已确定");
        GameCard firstCard = game.getDrawPile().removeFirst();
        game.getDiscardPile().add(firstCard);
        game.setPreviousCard(firstCard);
        return firstCard;
    }

    // 修改玩家状态为喊过Uno
    public void sayUno(String roomID) {
        games.get(roomID).getCurGamePlayerState().setSayUno(true);
    }

    // 获取所有玩家的游戏状态
    public List<GamePlayerState> getPlayerList(String roomID) {
        return games.get(roomID).getPlayerList();
    }

    // 获取指定玩家的游戏状态
    public EnumGamePlayerStatus getPlayerState(String roomID, String playerName) {
        return games.get(roomID).getPlayerList()
                .stream()
                .filter(playerState -> playerState.getPlayerName().equals(playerName))
                .findAny()
                .get() // 一定可以找到值
                .getStatus();
    }

    // 将牌移入弃牌堆
    private void movieCardToDiscardPile(String roomID, String playerName, GameCard card) {
        Game game = games.get(roomID);
        GamePlayerInfo gamePlayerInfo = game.getGamePlayerInfo(playerName);
        game.getDiscardPile().add(gamePlayerInfo.getHandCards().remove(card.getCardID()));
        gamePlayerInfo.setRemainingCardCnt(-1);
    }

    // 将"currentPlayerIndex"下标指向下一个玩家
    private void movieIndex(Game game) {
        game.setGamePlayerStateByIndex(game.getCurPlayerIndex(), EnumGamePlayerStatus.watching);
        if (game.getGameDirection() == (EnumGameDirection.clockwise))
            game.setCurPlayerIndex((game.getCurPlayerIndex() + 1) % game.getPlayerCnt());
        else
            game.setCurPlayerIndex((game.getCurPlayerIndex() - 1 + game.getPlayerCnt()) % game.getPlayerCnt());
        game.setGamePlayerStateByIndex(game.getCurPlayerIndex(), EnumGamePlayerStatus.onTurns);
        game.setGamePlayerStateByIndex(game.getNextPlayerIndex(), EnumGamePlayerStatus.nextTurns);
    }

    // 检查到卡牌不能打出后抛出异常
    public void checkCard(String roomID, GameCard card)
            throws AbnormalGameException {
        // 若为万能牌或者颜色/图案与上一张牌一样才允许出牌
        if (!isCardLegal(roomID, card))
            throw new AbnormalGameException("卡牌颜色或图案不同");
    }

    // 检查卡牌是否可以打出
    public boolean isCardLegal(String roomID, GameCard card){
        GameCard previousCard = games.get(roomID).getPreviousCard();
        // 当卡牌满足 颜色相同 | 图案相同 | 万能牌 时可以打出
        return card.getCardType() == EnumUnoCardType.WILD ||
                card.getCardType() == EnumUnoCardType.ADD4 ||
                card.getCardType() == previousCard.getCardType() ||
                card.getCardColor() == previousCard.getCardColor();
    }

    // 检查是否为指定玩家回合
    public void checkTurn(String roomID, String playerName)
            throws AbnormalGameException {
        if (!games.get(roomID).getCurGamePlayerState().getPlayerName().equals(playerName))
            throw new AbnormalGameException("不是当前玩家回合");
    }

    // 当前出牌不符合规则, 设置当前出牌玩家状态为retryOnTurns
    public void reTry(String roomID) {
        games.get(roomID).getCurGamePlayerState().setStatus(EnumGamePlayerStatus.retryOnTurns);
    }

    // 获取游戏玩家信息集合
    public Collection<GamePlayerInfo> getGamePlayerInfos(String roomID) {
        return games.get(roomID).getGamePlayers().values();
    }

    // 获取指定游戏的上一张牌
    public GameCard getPreviousCard(String roomID) {
        return games.get(roomID).getPreviousCard();
    }

    // 获取下回合出牌玩家的名称
    public String getNextPlayerName(String roomID) {
        return games.get(roomID).getNextPlayerName();
    }

    // 反转游戏出牌方向
    private void reverseGameDirection(Game game) {
        if (game.getGameDirection() == EnumGameDirection.clockwise)
            game.setGameDirection(EnumGameDirection.anticlockwise);
        else
            game.setGameDirection(EnumGameDirection.clockwise);
    }

    // 在玩家还有牌可以打出时抛出异常
    public void checkHasCardToSend(String roomID, String playerName) throws AbnormalGameException {
        if (games.get(roomID).getGamePlayerInfo(playerName)
                .getHandCards().values()
                .stream().anyMatch(card -> isCardLegal(roomID, card)))
            throw new AbnormalGameException("存在卡牌可以打出");
    }

    // 在指定房间当前出牌玩家手牌大于1时抛出异常
    public void checkOnlyOneCard(String roomID, String playerName) throws AbnormalGameException {
        if (games.get(roomID).getGamePlayerInfo(playerName).getHandCards().size() > 1)
            throw new AbnormalGameException("剩余卡牌数量大于1");
    }

    public boolean didYouSayUno(String roomID, String who) {
        return games.get(roomID).getGamePlayerState(who).getSayUno();
    }

    public GameService() {
        games = new HashMap<>();
    }
}
