package com.uno_restart.types.game;

import com.uno_restart.types.enums.EnumGamePlayerStatus;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class GamePlayerState {
    @NotNull
    private String playerName;
    @NotNull
    private EnumGamePlayerStatus state;
    Integer totalScore;
    @NotNull
    private Boolean sayUno; // 记录仅剩一张牌时是否说Uno
    // 剩余卡牌数量
    Integer remainingCardCnt;

    public GamePlayerState(@NotNull String playerName, @NotNull EnumGamePlayerStatus state) {
        this.playerName = playerName;
        this.state = state;
        this.totalScore = 0;
        this.sayUno = false;
        this.remainingCardCnt = 0;
    }

    public void setRemainingCardCnt(int diff) {
        this.remainingCardCnt += diff;
    }

    // 手中是否没有卡牌
    public boolean haveNoCard(){
        return remainingCardCnt == 0;
    }
}
