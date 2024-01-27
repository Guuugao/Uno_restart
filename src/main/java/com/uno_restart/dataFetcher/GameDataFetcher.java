package com.uno_restart.dataFetcher;

import cn.dev33.satoken.stp.StpUtil;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsSubscription;
import com.uno_restart.event.DrawCardEvent;
import com.uno_restart.exception.PlayerNotInRoomException;
import com.uno_restart.exception.playerNotLoginException;
import com.uno_restart.service.GameService;
import com.uno_restart.service.RoomService;
import com.uno_restart.types.enums.EnumGamePlayerStatus;
import com.uno_restart.types.game.GamePlayerState;
import com.uno_restart.types.game.GameTurnsFeedback;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import reactor.core.publisher.Flux;

import java.util.Collections;
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
//    public Boolean gameSendCard(String roomID, GameCard card) {
//        StpUtil.checkLogin();
//
//    }
//
    @DgsSubscription
    public Flux<GameTurnsFeedback> gameWaitNextReaction(String roomID, String token){
        String playerName = StpUtil.getLoginIdByToken(token).toString();
        if (!StpUtil.isLogin(playerName)) {
            return Flux.error(new playerNotLoginException("未能读取到有效 token"));
        } else if (roomService.isPlayerNotJoinRoom(playerName)) {
            return Flux.error(new PlayerNotInRoomException("玩家未加入当前房间"));
        }else {
            return Flux.create(sink -> {
                    // 监听到出牌动作, 则告知所有玩家
                    context.addApplicationListener((ApplicationListener<DrawCardEvent>) event -> {
                        if (roomID.equals(event.getSource())) { // 忽略其他信号
                            // TODO 这里出牌状态是写死的, 不会修改玩家实际状态, 是否保留需要看情况, 因为摸牌后可以立即打出
                            GameTurnsFeedback feedback = new GameTurnsFeedback(
                                    true, EnumGamePlayerStatus.drawCard, gameService.getPlayerList(roomID));
                            sink.next(feedback);
                        }
                    });
                }
            );
        }
    }
}
