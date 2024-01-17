package com.uno_restart.types.player;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class PlayerAvatarFeedback {
    @NotNull
    private Boolean success;
    private String message;
    String avatarPath;
}