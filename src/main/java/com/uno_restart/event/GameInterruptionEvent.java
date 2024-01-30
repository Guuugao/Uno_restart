package com.uno_restart.event;

import org.springframework.context.ApplicationEvent;

// 游戏进行时中断
public class GameInterruptionEvent extends ApplicationEvent {
    public GameInterruptionEvent(String roomID) {
        super(roomID);
    }
}
