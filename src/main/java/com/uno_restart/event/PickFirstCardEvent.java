package com.uno_restart.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PickFirstCardEvent extends ApplicationEvent {
    private final String playerName;
    public PickFirstCardEvent(String roomID, String playerName) {
        super(roomID);
        this.playerName = playerName;
    }
}
