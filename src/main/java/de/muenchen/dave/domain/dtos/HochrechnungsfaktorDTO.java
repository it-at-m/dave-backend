package de.muenchen.dave.domain.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class HochrechnungsfaktorDTO {

    UUID id;

    Long entityVersion;

    @NotNull
    String matrix;

    Double kfz;

    Double sv;

    Double gv;

    @NotNull
    Boolean active;

    @NotNull
    Boolean defaultFaktor;

}
