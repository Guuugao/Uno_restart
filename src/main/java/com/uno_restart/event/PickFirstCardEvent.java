package com.uno_restart.event;

import com.uno_restart.types.game.GameCard;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PickFirstCardEvent extends ApplicationEvent {
    private final String playerName;
    private final GameCard firstCard;
    public PickFirstCardEvent(String roomID, String playerName, GameCard firstCard) {
        super(roomID);
        this.playerName = playerName;
        this.firstCard = firstCard;
    }
}
