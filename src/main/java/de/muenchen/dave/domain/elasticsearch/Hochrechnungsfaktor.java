package de.muenchen.dave.domain.elasticsearch;

import lombok.Data;

import java.io.Serializable;


@Data
public class Hochrechnungsfaktor implements Serializable {

    String matrix;

    Double kfz;

    Double sv;

    Double gv;

    boolean active;

}
