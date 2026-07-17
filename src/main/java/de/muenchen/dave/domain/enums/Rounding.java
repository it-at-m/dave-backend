package de.muenchen.dave.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Rounding {

    NONE(0),

    R10(10),

    R100(100);

    private final int value;

}
