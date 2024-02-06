package com.uno_restart.dataFetcher;

import cn.dev33.satoken.stp.StpUtil;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsSubscription;
import com.uno_restart.event.*;
import com.uno_restart.exception.GameAbnormalException;
import com.uno_restart.exception.PlayerAbnormalException;
import com.uno_restart.exception.RoomAbnormalException;
import com.uno_restart.service.GameService;
import com.uno_restart.service.RoomService;
import com.uno_restart.types.enums.EnumGameAction;
import com.uno_restart.types.game.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@DgsComponent
public class GameDataFetcher {
    @Autowired
    private GameService gameService;
    @Autowired
    private RoomService roomService;
    @Autowired
    private ConfigurableApplicationContext context;

    @DgsMutation
    public Boolean appointedColor(String roomID, String color) throws
            RoomAbnormalException, PlayerAbnormalException,
            GameAbnormalException {
        StpUtil.checkLogin();
        String playerName = StpUtil.getLoginIdAsString();
        roomService.checkRoomExists(roomID);
        roomService.checkPlayerInRoom(roomID, playerName);
        gameService.checkTurn(roomID, playerName);
        gameService.checkPreCard(roomID);
        if (!gameService.isPreCardBlank(roomID)) return false;

        return gameService.appointedColor(roomID, color);
    }

    @DgsMutation
    public Boolean pickFirstCard(String roomID) throws
            RoomAbnormalException, PlayerAbnormalException,
            GameAbnormalException {
        StpUtil.checkLogin();
        String playerName = StpUtil.getLoginIdAsString();
        roomService.checkRoomExists(roomID);
        roomService.checkPlayerInRoom(roomID, playerName);
        gameService.checkTurn(roomID, playerName);
        if (gameService.isPickedFirstCard(roomID)) return false;

        gameService.pickFirstCard(roomID);

        log.info("room " + roomID + ": " + playerName + " picked first card");

        return Boolean.TRUE;
    }

    @DgsMutation
    public Boolean sendCard(String roomID, Map<Object, Object> cardInput) throws
            RoomAbnormalException, PlayerAbnormalException,
            GameAbnormalException {
        StpUtil.checkLogin();
        String playerName = StpUtil.getLoginIdAsString();
        roomService.checkRoomExists(roomID);
        roomService.checkPlayerInRoom(roomID, playerName);
        gameService.checkTurn(roomID, playerName);
        gameService.checkPreCard(roomID); // 检查是否已经完成抽取第一张牌操作
        gameService.checkPreCardColor(roomID);
        gameService.checkHaveCard(roomID, playerName, (Integer) cardInput.get("cardID")); // 检查手牌中是否有这张牌

        GameCard card;
        try {
            card = GameCard.getCardFromInput(cardInput);
            gameService.checkCard(roomID, card);
        } catch (GameAbnormalException e) {
            gameService.reTry(roomID);
            log.info("room " + roomID + ": " + playerName + " retry the card, reason " + e.getMessage());
            return false; // catch之后的代码依然会运行, 所以需要手动返回
        }

        gameService.sendACard(roomID, playerName, card);
        log.info("room " + roomID + ": " + playerName + " send " + cardInput);

        return true;
    }

    @DgsMutation
    public Boolean noCardToSend(String roomID) throws
            RoomAbnormalException, PlayerAbnormalException,
            GameAbnormalException {
        StpUtil.checkLogin();
        String playerName = StpUtil.getLoginIdAsString();
        roomService.checkRoomExists(roomID);
        roomService.checkPlayerInRoom(roomID, playerName);
        gameService.checkTurn(roomID, playerName);
        gameService.checkHaveCardToSend(roomID, playerName);

        // 没有牌可以出时摸一张牌
        LinkedList<GameCard> card = gameService.drawCard(roomID, playerName, 1);
        if (gameService.isCardLegal(roomID, card.getFirst())) {
            gameService.sendACard(roomID, playerName, card.getFirst()); // 若可以打出则立即打出
            log.info("room " + roomID + ": " + playerName + " draw card and immediately send");
        } else {
            gameService.giveUpSendCard(roomID); // 无法打出则放弃
            log.info("room " + roomID + ": " + playerName + " draw card but can't send");
        }

        return true;
    }

    @DgsMutation
    public Boolean sayUno(String roomID) throws
            RoomAbnormalException, PlayerAbnormalException,
            GameAbnormalException {
        StpUtil.checkLogin();
        String playerName = StpUtil.getLoginIdAsString();
        roomService.checkRoomExists(roomID);
        roomService.checkPlayerInRoom(roomID, playerName);
        gameService.checkTurn(roomID, playerName);
        gameService.checkOnlyOneCard(roomID, playerName);

        gameService.sayUno(roomID);
        log.info("room " + roomID + ": " + playerName + " say Uno!");

        return true;
    }

    @DgsMutation
    public Boolean whoForgetSayUno(String roomID, String who) throws
            RoomAbnormalException, PlayerAbnormalException,
            GameAbnormalException {
        StpUtil.checkLogin();
        String playerName = StpUtil.getLoginIdAsString();
        roomService.checkRoomExists(roomID);
        roomService.checkPlayerInRoom(roomID, playerName);
        roomService.checkPlayerInRoom(roomID, who);
        gameService.checkOnlyOneCard(roomID, who);

        log.info("room " + roomID + ": " + playerName + " think " + who + " forget say Uno");
        if (!gameService.didYouSayUno(roomID, who)) {
            gameService.drawCard(roomID, who, 2); // 没说则抽两张牌
            log.info("room " + roomID + ": Oh " + who + " forget say Uno");
        }
        log.info("room " + roomID + ": but " + who + " already said Uno");

        return true;
    }

    @DgsSubscription
    public Flux<GameTurnsFeedback> gameWaitNextReaction(String roomID, String token) {
        token = token.replaceFirst(StpUtil.getTokenName() + "=", "");
        Object playerName = StpUtil.getLoginIdByToken(token);
        if (playerName == null) {
            return Flux.error(new PlayerAbnormalException("未能读取到有效 token"));
        }

        // 订阅使用socket, 所以错误不能直接抛出, 需要转换为错误流
        try {
            roomService.checkRoomExists(roomID);
            roomService.checkPlayerInRoom(roomID, playerName.toString());
        } catch (RoomAbnormalException | PlayerAbnormalException e) {
            return Flux.error(e);
        }

        log.info("room " + roomID + ": " + playerName + " wait for next turn");
        return Flux.create(sink -> {
                    context.addApplicationListener((ApplicationListener<PickFirstCardEvent>) event -> {
                        if (roomID.equals(event.getSource())) {
                            Game game = gameService.getGame(roomID);
                            sink.next(new GameTurnsFeedback(
                                            game.getGamePlayerStates().values(),
                                            game.getGamePlayerInfos().get(playerName.toString()),
                                            game.getPreCard(),
                                            List.of(new GamePlayerAction(event.getPlayerName(), EnumGameAction.showFirstCard))
                                    )
                            );

                            log.debug("room " + roomID + ": event-PickFirstCardEvent-gameWaitNextReaction");
                        }
                    });

                    context.addApplicationListener((ApplicationListener<SendCardEvent>) event -> {
                        if (roomID.equals(event.getSource())) {
                            Game game = gameService.getGame(roomID);
                            ArrayList<GamePlayerAction> gamePlayerActions = new ArrayList<>();
                            gamePlayerActions.ensureCapacity(2); // 一张牌最多影响两名玩家, 出牌者与其下家

                            GameTurnsFeedback feedback = new GameTurnsFeedback(
                                    game.getGamePlayerStates().values(),
                                    game.getGamePlayerInfos().get(playerName.toString()),
                                    game.getPreCard(),
                                    gamePlayerActions);
                            if (event.getSendCard() != null) { // 出牌失败返回null, 不做操作
                                gamePlayerActions.add(new GamePlayerAction(event.getPlayerName(), EnumGameAction.sendCard));
                                switch (event.getSendCard().cardType()) {
                                    case SKIP ->
                                            gamePlayerActions.add(new GamePlayerAction(gameService.getNextPlayerName(roomID), EnumGameAction.skipTurn)); // 额外移动一次下标即代表跳过回合
                                    case ADD2 ->
                                            gamePlayerActions.add(new GamePlayerAction(gameService.getNextPlayerName(roomID), EnumGameAction.skipTurn)); // 抽牌动作已由抽牌方法发布事件完成
                                    case ADD4 ->
                                            gamePlayerActions.add(new GamePlayerAction(gameService.getNextPlayerName(roomID), EnumGameAction.skipTurn));
                                }
                            }
                            sink.next(feedback);

                            log.debug("room " + roomID + ": event-SendCardEvent-gameWaitNextReaction");
                        }
                    });

                    // 监听到出牌动作, 则告知所有玩家
                    context.addApplicationListener((ApplicationListener<DrawCardEvent>) event -> {
                        Game game = gameService.getGame(roomID);
                        if (roomID.equals(event.getSource())) { // 忽略其他信号
                            sink.next(new GameTurnsFeedback(
                                    game.getGamePlayerStates().values(),
                                    game.getGamePlayerInfos().get(playerName.toString()),
                                    game.getPreCard(),
                                            List.of(new GamePlayerAction(event.getPlayerName(), EnumGameAction.drawCard, event.getDrawCardCnt()))
                                    )
                            );

                            log.debug("room " + roomID + ": event-DrawCardEvent-gameWaitNextReaction");
                        }
                    });

                    // 游戏中止时结束订阅
                    context.addApplicationListener((ApplicationListener<GameInterruptionEvent>) event -> {
                        if (roomID.equals(event.getSource())) { // 忽略其他信号
                            sink.error(new GameAbnormalException("游戏中止"));
                            log.debug("room " + roomID + ": event-GameInterruptionEvent-gameWaitNextReaction");
                        }
                    });

                    // 游戏结束时终止订阅
                    context.addApplicationListener((ApplicationListener<GameOverEvent>) event -> {
                        if (roomID.equals(event.getSource())) { // 忽略其他信号
                            sink.complete();
                            log.debug("room " + roomID + ": event-GameOverEvent-gameWaitNextReaction");
                        }
                    });

                    // 标记一名玩家已经连接至游戏
                    context.publishEvent(new PlayerConnectEvent(roomID, playerName.toString()));
                }
        );
    }

    @DgsSubscription
    public Mono<List<GamePlayerState>> gameRanking(String roomID, String token) {
        token = token.replaceFirst(StpUtil.getTokenName() + "=", "");
        Object playerName = StpUtil.getLoginIdByToken(token);
        if (playerName == null) {
            return Mono.error(new PlayerAbnormalException("未能读取到有效 token"));
        }

        try {
            roomService.checkRoomExists(roomID);
            roomService.checkPlayerInRoom(roomID, playerName.toString());
        } catch (RoomAbnormalException | PlayerAbnormalException e) {
            return Mono.error(e);
        }

        log.info("room " + roomID + ": " + playerName + " subscribe game rank");
        return Mono.create(sink -> {
            // 监听到游戏结束, 返回排名
            context.addApplicationListener((ApplicationListener<GameOverEvent>) event -> {
                if (roomID.equals(event.getSource())) {
                    List<GamePlayerState> rank = gameService.calcGameRank(roomID);
                    roomService.saveRankInfo(roomID, rank);
                    sink.success(rank);
                    log.debug("room " + roomID + ": event-GameOverEvent-gameRanking");
                }
            });

            // 监听到游戏中断, 停止订阅
            context.addApplicationListener((ApplicationListener<GameInterruptionEvent>) event -> {
                if (roomID.equals(event.getSource())) { // 忽略其他信号
                    sink.error(new GameAbnormalException("游戏中止"));
                    log.debug("room " + roomID + ": event-GameInterruptionEvent-gameRanking");
                }
            });
        });
    }
}
