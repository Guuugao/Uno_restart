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
import com.uno_restart.types.game.GameCard;
import com.uno_restart.types.game.GamePlayerAction;
import com.uno_restart.types.game.GamePlayerState;
import com.uno_restart.types.game.GameTurnsFeedback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
    Boolean pickFirstCard(String roomID) throws
            RoomAbnormalException, PlayerAbnormalException,
            GameAbnormalException {
        StpUtil.checkLogin();
        String playerName = StpUtil.getLoginIdAsString();
        roomService.checkRoomExists(roomID);
        roomService.checkPlayerInRoom(roomID, playerName);
        gameService.checkTurn(roomID, playerName);

        gameService.pickFirstCard(roomID);

        return true;
    }

    @DgsMutation
    public Boolean sendCard(String roomID, GameCard card) throws
            RoomAbnormalException, PlayerAbnormalException,
            GameAbnormalException {
        StpUtil.checkLogin();
        String playerName = StpUtil.getLoginIdAsString();
        roomService.checkRoomExists(roomID);
        roomService.checkPlayerInRoom(roomID, playerName);
        gameService.checkTurn(roomID, playerName);
        try {
            gameService.checkCard(roomID, card);
        } catch (GameAbnormalException e) {
            gameService.reTry(roomID);
        }

        gameService.sendACard(roomID, playerName, card);

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
        gameService.checkHasCardToSend(roomID, playerName);

        // 没有牌可以出时摸一张牌
        LinkedList<GameCard> card = gameService.drawCard(roomID,  playerName, 1);
        if (gameService.isCardLegal(roomID, card.getFirst())){
            gameService.sendACard(roomID, playerName, card.getFirst()); // 若可以打出则立即打出
        } else {
            gameService.giveUpSendCard(roomID); // 无法打出则放弃
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

        if (!gameService.didYouSayUno(roomID, who)) {
            gameService.drawCard(roomID, who, 2); // 没说则抽两张牌
        }

        return true;
    }

    // TODO 游戏开始后, 在玩家订阅之前, 游戏初始化就已经完成, 导致监听器无法向前端反馈初始化过程, 可以改为监听器, 监听到玩家订阅后再初始化房间
    // TODO 订阅能不能使用登录拦截器验证登录? 可以试试, 但是改不改不一定
    @DgsSubscription
    public Flux<GameTurnsFeedback> gameWaitNextReaction(String roomID, String token) {
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

        return Flux.create(sink -> {
                    context.addApplicationListener((ApplicationListener<PickFirstCardEvent>) event -> {
                        if (roomID.equals(event.getSource())) {
                            sink.next(new GameTurnsFeedback(true, gameService.getPlayerState(roomID, playerName.toString()),
                                    gameService.getPlayerList(roomID))
                                    .setPlayerActions(List.of(new GamePlayerAction(event.getPlayerName(), EnumGameAction.showFirstCard)))
                                    .setGamePlayerInfo(gameService.getGamePlayerInfos(roomID))
                                    .setLastCard(event.getFirstCard())
                            );
                        }
                    });

                    context.addApplicationListener((ApplicationListener<SendCardEvent>) event -> {
                        if (roomID.equals(event.getSource())) {
                            ArrayList<GamePlayerAction> gamePlayerActions = new ArrayList<>();
                            gamePlayerActions.ensureCapacity(2); // 一张牌最多影响两名玩家, 出牌者与其下家
                            gamePlayerActions.add(new GamePlayerAction(event.getPlayerName(), EnumGameAction.sendCard));

                            GameTurnsFeedback feedback = new GameTurnsFeedback(true, gameService.getPlayerState(roomID, playerName.toString()),
                                    gameService.getPlayerList(roomID))
                                    .setPlayerActions(gamePlayerActions)
                                    .setGamePlayerInfo(gameService.getGamePlayerInfos(roomID))
                                    .setLastCard(event.getSendCard());
                            if (event.getSendCard() == null)
                                feedback.setSuccess(false)
                                        .setMessage("卡牌颜色或图案不同");
                            else {
                                switch (event.getSendCard().getCardType()) {
                                    case SKIP -> gamePlayerActions.add(new GamePlayerAction(gameService.getNextPlayerName(roomID), EnumGameAction.skipTurn)); // 额外移动一次下标即代表跳过回合
                                    case ADD2 -> gamePlayerActions.add(new GamePlayerAction(gameService.getNextPlayerName(roomID), EnumGameAction.skipTurn)); // 抽牌动作已由抽牌方法发布事件完成
                                    case ADD4 -> gamePlayerActions.add(new GamePlayerAction(gameService.getNextPlayerName(roomID), EnumGameAction.skipTurn));
                                }
                            }

                            sink.next(feedback);
                        }
                    });

                    // 监听到出牌动作, 则告知所有玩家
                    context.addApplicationListener((ApplicationListener<DrawCardEvent>) event -> {
                        if (roomID.equals(event.getSource())) { // 忽略其他信号
                            sink.next(new GameTurnsFeedback(true, gameService.getPlayerState(roomID, playerName.toString()),
                                    gameService.getPlayerList(roomID))
                                    .setPlayerActions(List.of(new GamePlayerAction(event.getPlayerName(), EnumGameAction.drawCard, event.getDrawCardCnt())))
                                    .setGamePlayerInfo(gameService.getGamePlayerInfos(roomID))
                                    .setLastCard(gameService.getPreviousCard(roomID))
                            );
                        }
                    });

                    // 游戏中止时结束订阅
                    context.addApplicationListener((ApplicationListener<GameInterruptionEvent>) event -> {
                        if (roomID.equals(event.getSource())) { // 忽略其他信号
                            sink.error(new GameAbnormalException("游戏中止"));
                        }
                    });

                    // 游戏结束时终止订阅
                    context.addApplicationListener((ApplicationListener<GameOverEvent>) event -> {
                        if (roomID.equals(event.getSource())) { // 忽略其他信号
                            sink.complete();
                        }
                    });
                }
        );
    }

    @DgsSubscription
    public Mono<List<GamePlayerState>> gameRanking(String roomID, String token) {
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

        return Mono.create(sink -> {
            // 监听到游戏结束, 返回排名
            context.addApplicationListener((ApplicationListener<GameOverEvent>) event -> {
                if (roomID.equals(event.getSource())) {
                    List<GamePlayerState> rank = gameService.calcGameRank(roomID);
                    roomService.saveRankInfo(roomID, rank);
                    sink.success(rank);
                }
            });

            // 监听到游戏中断, 停止订阅
            context.addApplicationListener((ApplicationListener<GameInterruptionEvent>) event -> {
                if (roomID.equals(event.getSource())) { // 忽略其他信号
                    sink.error(new GameAbnormalException("游戏中止"));
                }
            });
        });
    }
}
