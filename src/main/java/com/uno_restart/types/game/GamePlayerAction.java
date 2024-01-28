package com.uno_restart.types.game;
import com.uno_restart.types.enums.EnumGameAction;

public class GamePlayerAction {
    private final String who;
    private final EnumGameAction action;
    private final Integer actionTimes;

    public GamePlayerAction(String who, EnumGameAction action) {
        this.who = who;
        this.action = action;
        this.actionTimes = 1;
    }

    public GamePlayerAction(String who, EnumGameAction action, Integer actionTimes) {
        this.who = who;
        this.action = action;
        this.actionTimes = actionTimes;
    }
}
