package de.muenchen.dave.domain.elasticsearch;

import java.io.Serializable;
import lombok.Data;

@Data
public class Hochrechnungsfaktor implements Serializable {

    String matrix;

    Double kfz;

    Double sv;

    Double gv;

    boolean active;

}
