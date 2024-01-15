package com.uno_restart.types.room;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Data
public class GameRoomPage {
    @NotNull
    private Integer maxPageCount;
    @NotNull
    private Integer currentPageCount;
    @NotNull
    ArrayList<GameRoomInfo> pageContent;
}
