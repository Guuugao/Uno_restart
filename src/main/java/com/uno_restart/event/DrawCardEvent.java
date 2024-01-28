package com.uno_restart.event;


import lombok.Getter;
import org.springframework.context.ApplicationEvent;

// 抽牌事件
@Getter
public class DrawCardEvent extends ApplicationEvent {
    private final int drawCardCnt;
    private final String playerName;
    public DrawCardEvent(String roomID, String playerName, int drawCardCnt) {
        super(roomID);
        this.playerName = playerName;
        this.drawCardCnt = drawCardCnt;
    }

}
