package com.uno_restart.event;

import org.springframework.context.ApplicationEvent;

// 游戏开始事件
public class GameStartEvent extends ApplicationEvent {
    public GameStartEvent(String roomID) {
        super(roomID);
    }
}
