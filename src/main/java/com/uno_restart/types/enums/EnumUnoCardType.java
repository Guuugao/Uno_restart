package com.uno_restart.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnumUnoCardType {
    N0(0),N1(1), N2(2),
    N3(3), N4(4), N5(5),
    N6(6), N7(7), N8(8),
    N9(9), SKIP(20), REVERSE(20),
    ADD2(20), WILD(50), ADD4(50);

    final int score;
}
