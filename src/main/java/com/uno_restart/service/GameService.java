package com.uno_restart.service;

import com.uno_restart.event.DrawCardEvent;
import com.uno_restart.types.enums.EnumGameDirection;
import com.uno_restart.types.enums.EnumGamePlayerStatus;
import com.uno_restart.types.game.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class GameService {
    // 记录游戏对象
    // key: roomID value: game
    private HashMap<String, Game> games;
    @Autowired
    ApplicationEventPublisher eventPublisher;


    // 创建游戏, 初始化牌堆, 确定出牌次序
    public void gameInit(GameSettings settings){
        Game game = new Game(settings);
        String roomID = settings.getRoomInfo().getRoomID();
        games.put(roomID, game);

        int currentPlayerIndex = game.getCurrentPlayerIndex();
        List<GamePlayerState> playerList = game.getPlayerList();
        for (int i = 0; i < playerList.size(); ++i) {
            String playerName = playerList.get(currentPlayerIndex).getPlayerName();
            for (int j = 0; j < Game.INITIAL_NUMBER_OF_CARDS; j++) {
                drawACard(roomID, playerName);
            }

            eventPublisher.publishEvent(new DrawCardEvent(roomID, playerName, Game.INITIAL_NUMBER_OF_CARDS));


            if (game.getGameDirection() == (EnumGameDirection.clockwise))
                currentPlayerIndex = (currentPlayerIndex + 1) % playerList.size();
            else
                currentPlayerIndex = (currentPlayerIndex - 1 + playerList.size()) % playerList.size();
        }

        log.debug("game " + roomID + " init");
    }

    // 抽一张牌, 需要指定玩家与抽牌数量
    public void drawACard(String roomID, String playerName) {
        Game game = games.get(roomID);
        GamePlayerInfo gamePlayerInfo = game.getHandCards().get(playerName);
        LinkedList<GameCard> drawPile = game.getDrawPile(); // 抽牌堆
        LinkedList<GameCard> discardPile = game.getDiscardPile(); // 弃牌堆
        // 抽牌堆卡牌不足, 将弃牌堆重新洗入
        if (drawPile.isEmpty()) {
            // 先对弃牌堆洗牌, 这样不会影响抽牌堆原先剩余卡牌的牌序
            Collections.shuffle(discardPile);
            drawPile.addAll(discardPile);
            discardPile.clear();
            log.info("shuffle discard pile");
        }

        GameCard card = drawPile.removeFirst();
        gamePlayerInfo.getCards().put(card.getCardID(), card);
        gamePlayerInfo.setRemainingCardCnt(1);
        log.trace("player " + playerName + " draw card " + card + ", number of remaining cards " + gamePlayerInfo.getRemainingCardCnt());
    }

    // 打出一张牌
    // 这里面识别牌的种类, 将牌移入弃牌堆后根据种类调用私有方法完成卡牌逻辑
    public void sendACard(String roomID, String playerName, GameCard card) {
        movieCardToDiscardPile(roomID, playerName, card);
        switch (card.getCardType()) {

        }
    }

//    public void checkFirstCard(String roomID) {
//        games.get(roomID)
//    }

    // 获取所有玩家的游戏状态
    public List<GamePlayerState> getPlayerList(String roomID) {
        return games.get(roomID).getPlayerList();
    }

    // 获取指定玩家的游戏状态
    public EnumGamePlayerStatus getPlayerState(String roomID, String playerName) {
        return games.get(roomID).getPlayerList()
                .stream()
                .filter(playerState -> playerState.getPlayerName().equals(playerName))
                .findFirst()
                .get() // 一定可以找到值
                .getStatus();
    }

    // 将牌移入弃牌堆
    private void movieCardToDiscardPile(String roomID, String playerName, GameCard card) {
        Game game = games.get(roomID);
        GamePlayerInfo gamePlayerInfo = game.getHandCards().get(playerName);
        game.getDiscardPile().add(gamePlayerInfo.getCards().remove(card.getCardID()));
        gamePlayerInfo.setRemainingCardCnt(-1);
    }

    public GameService() {
        games = new HashMap<>();
    }
}
