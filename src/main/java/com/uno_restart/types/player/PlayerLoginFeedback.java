package com.uno_restart.types.player;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
public class PlayerLoginFeedback {
    @NotNull
    private Boolean success;
    @NotNull
    private String authcation;
    private String message;
}
