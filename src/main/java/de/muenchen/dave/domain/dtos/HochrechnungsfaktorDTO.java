package de.muenchen.dave.domain.dtos;

import java.util.UUID;
import lombok.Data;

@Data
public class HochrechnungsfaktorDTO {

    UUID id;

    Long entityVersion;

    String matrix;

    Double kfz;

    Double sv;

    Double gv;

    boolean active;

    boolean defaultFaktor;

}
