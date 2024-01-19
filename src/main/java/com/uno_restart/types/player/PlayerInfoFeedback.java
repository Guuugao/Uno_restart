package com.uno_restart.types.player;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Data
@Accessors(chain = true)
public class PlayerInfoFeedback{
    @NotNull
    private Boolean success;
    private String message;
}
