package de.muenchen.dave.domain.dtos;

import lombok.Data;

import java.util.UUID;

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
