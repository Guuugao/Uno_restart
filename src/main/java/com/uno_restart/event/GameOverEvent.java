package com.uno_restart.event;

import org.springframework.context.ApplicationEvent;

// 一局游戏结束事件
public class GameOverEvent extends ApplicationEvent {
    public GameOverEvent(String roomID) {
        super(roomID);
    }
}
