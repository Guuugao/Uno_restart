package com.uno_restart.event;


import org.springframework.context.ApplicationEvent;

// 抽牌事件
public class DrawCardEvent extends ApplicationEvent {
    public DrawCardEvent(String roomID) {
        super(roomID);
    }
}
