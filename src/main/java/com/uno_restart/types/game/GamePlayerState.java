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
    private EnumGamePlayerStatus status;

    Integer totalScore;

    @NotNull
    private Boolean sayUno; // 记录仅剩一张牌时是否说Uno

    public GamePlayerState(@NotNull String playerName, @NotNull EnumGamePlayerStatus status) {
        this.playerName = playerName;
        this.status = status;
        this.totalScore = 0;
        this.sayUno = false;
    }


}
