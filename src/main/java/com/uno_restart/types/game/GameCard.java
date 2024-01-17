package com.uno_restart.types.game;

import com.uno_restart.types.interfaces_enum.EnumUnoCardColor;
import com.uno_restart.types.interfaces_enum.EnumUnoCardType;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class GameCard {
    @NotNull
    private EnumUnoCardType cardType;
    @NotNull
    private EnumUnoCardColor cardColor;
}
