package com.uno_restart.types.player;

import lombok.*;

@Getter
@ToString
// 游戏历史数据
public class PlayerHistory {
    Integer winTimes;
    Integer failTimes;
    Integer totalGames;

    public PlayerHistory() {
        winTimes = 0;
        failTimes = 0;
        totalGames = 0;
    }

    public PlayerHistory(Integer winTimes, Integer failTimes) {
        this.winTimes = winTimes;
        this.failTimes = failTimes;
        this.totalGames = winTimes + failTimes;
    }

    public void setWinTimes(final Integer winTimes) {
        this.winTimes = winTimes;
        this.totalGames = winTimes + this.failTimes;
    }

    public void setFailTimes(final Integer failTimes) {
        this.failTimes = failTimes;
        this.totalGames = this.winTimes + failTimes;
    }
}