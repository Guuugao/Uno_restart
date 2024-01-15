package com.uno_restart.types.player;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public class PlayerInfoEditFeedback {
    @NotNull
    private Boolean success;
    private String message;
}
