package com.uno_restart.event;

import org.springframework.context.ApplicationEvent;

// 房间关闭时取消订阅
public class RoomCloseEvent extends ApplicationEvent {
    public RoomCloseEvent(String roomID) {
        super(roomID);
    }
}
