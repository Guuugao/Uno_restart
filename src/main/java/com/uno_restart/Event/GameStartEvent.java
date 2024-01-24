package com.uno_restart.Event;

import org.springframework.context.ApplicationEvent;

public class GameStartEvent extends ApplicationEvent {
    public GameStartEvent(Object source) {
        super(source);
    }
}
