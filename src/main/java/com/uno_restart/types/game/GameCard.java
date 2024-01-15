package com.uno_restart.types.game;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class GameCard {
    @NotNull
    private EnumUnoCardType cardType;
    @NotNull
    private EnumUnoCardColor cardColor;
}
