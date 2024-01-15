package com.uno_restart.types.player;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
// 游戏历史数据
public class PlayerHistory {
    Integer winTimes;
    Integer failTimes;
    Integer totalGames;

    public PlayerHistory(Integer winTimes, Integer failTimes) {
        this.winTimes = winTimes;
        this.failTimes = failTimes;
        this.totalGames = winTimes + failTimes;
    }

    public void setWinTimes(Integer winTimes) {
        this.winTimes = winTimes;
        this.totalGames = winTimes + failTimes;
    }

    public void setFailTimes(Integer failTimes) {
        this.failTimes = failTimes;
        this.totalGames = winTimes + failTimes;
    }
}