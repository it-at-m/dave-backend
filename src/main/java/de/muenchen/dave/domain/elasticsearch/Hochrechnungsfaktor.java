package de.muenchen.dave.domain.elasticsearch;

import java.io.Serializable;
import lombok.Data;
import org.springframework.data.annotation.Transient;

@Data
public class Hochrechnungsfaktor implements Serializable {

    @Transient
    String id;

    @Transient
    Long version;

    String matrix;

    Double kfz;

    Double sv;

    Double gv;

    boolean active;

    @Transient
    boolean defaultFaktor;

}
