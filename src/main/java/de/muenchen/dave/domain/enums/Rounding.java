package de.muenchen.dave.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Schema(enumAsRef = true)
public enum Rounding {

    NONE(0),

    R10(10),

    R100(100);

    private final int value;

}
