package com.uno_restart.event;

import com.uno_restart.types.game.GameCard;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

// 出牌事件
@Getter
public class SendCardEvent extends ApplicationEvent {
    private final GameCard sendCard;
    private final String playerName;

    public SendCardEvent(String roomID, GameCard sendCard, String playerName) {
        super(roomID);
        this.sendCard = sendCard;
        this.playerName = playerName;
    }
}
