package com.uno_restart.types.game;

import com.uno_restart.types.player.PlayerInfo;
import com.uno_restart.types.room.GameRoomInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Data
@AllArgsConstructor
public class GameSettings {
    @NotNull
    private ArrayList<PlayerInfo> players;
    private GameRoomInfo roomInfo;
}
