package com.uno_restart.types.player;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class PlayerInfoFeedback{
    @NotNull
    private Boolean success;
    private String message;
}
