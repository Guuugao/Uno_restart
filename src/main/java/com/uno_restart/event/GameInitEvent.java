package com.uno_restart.event;

import com.uno_restart.types.game.GameSettings;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

// 通知房间开始初始化
@Getter
public class GameInitEvent extends ApplicationEvent {
    GameSettings settings;
    public GameInitEvent(String roomID, GameSettings settings) {
        super(roomID);
        this.settings = settings;
    }
}
