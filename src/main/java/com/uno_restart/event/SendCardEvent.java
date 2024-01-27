package com.uno_restart.event;

import org.springframework.context.ApplicationEvent;

// 出牌事件
public class SendCardEvent extends ApplicationEvent {
    public SendCardEvent(String roomID) {
        super(roomID);
    }
}
