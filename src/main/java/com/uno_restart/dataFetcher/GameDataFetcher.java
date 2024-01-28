package com.uno_restart.dataFetcher;

import cn.dev33.satoken.stp.StpUtil;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsSubscription;
import com.uno_restart.event.DrawCardEvent;
import com.uno_restart.exception.PlayerNotInRoomException;
import com.uno_restart.exception.PlayerNotLoginException;
import com.uno_restart.exception.RoomNotExistsException;
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

//    @DgsMutation
//    Boolean pickFirstCard(String roomID)
//            throws RoomNotExistsException, PlayerNotInRoomException {
//
//        roomService.checkRoomExists(roomID);
//        roomService.checkPlayerInRoom(roomID, StpUtil.getLoginIdAsString());
//
//    }

    @DgsMutation
    public Boolean gameSendCard(String roomID, GameCard card)
            throws RoomNotExistsException, PlayerNotInRoomException {
        StpUtil.checkLogin();

        roomService.checkRoomExists(roomID);
        roomService.checkPlayerInRoom(roomID, StpUtil.getLoginIdAsString());

        return true;
    }

    // TODO 游戏开始后, 在玩家订阅之前, 游戏初始化就已经完成, 导致监听器无法向前端反馈初始化过程
    @DgsSubscription
    public Flux<GameTurnsFeedback> gameWaitNextReaction(String roomID, String token) {
        Object playerName = StpUtil.getLoginIdByToken(token);
        if (playerName == null) {
            return Flux.error(new PlayerNotLoginException("未能读取到有效 token"));
        }

        // 订阅使用socket, 所以错误不能直接抛出, 需要转换为错误流
        try {
            roomService.checkRoomExists(roomID);
            roomService.checkPlayerInRoom(roomID, playerName.toString());
        } catch (RoomNotExistsException | PlayerNotInRoomException e) {
            return Flux.error(e);
        }

        return Flux.create(sink -> {
                    // 监听到出牌动作, 则告知所有玩家
                    context.addApplicationListener((ApplicationListener<DrawCardEvent>) event -> {
                        if (roomID.equals(event.getSource())) { // 忽略其他信号
                            List<GamePlayerState> playerList = gameService.getPlayerList(roomID);
                            GamePlayerState yourState = playerList.stream()
                                    .filter(gamePlayerState -> gamePlayerState.getPlayerName().equals(playerName))
                                    .findAny().get();

                            GameTurnsFeedback feedback = new GameTurnsFeedback(
                                    true, yourState.getStatus(), playerList,
                                    new GamePlayerAction(event.getPlayerName(), EnumGameAction.drawCard, event.getDrawCardCnt()));
                            sink.next(feedback);
                        }
                    });
                }
        );
    }
}
