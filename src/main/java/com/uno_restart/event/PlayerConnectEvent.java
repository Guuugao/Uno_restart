package com.uno_restart.event;

import org.springframework.context.ApplicationEvent;

public class PlayerConnectEvent extends ApplicationEvent {
    String playerName;
    public PlayerConnectEvent(String roomID, String playerName) {
        super(roomID);
        this.playerName = playerName;
    }
}
