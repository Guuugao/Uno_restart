package com.uno_restart.event;

import com.uno_restart.types.game.GameSettings;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

// 游戏开始事件
@Getter
public class GameStartEvent extends ApplicationEvent {
    GameSettings settings;
    public GameStartEvent(String roomID, GameSettings settings) {
        super(roomID);
        this.settings = settings;
    }
}
