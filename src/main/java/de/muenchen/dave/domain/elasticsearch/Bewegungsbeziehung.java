package de.muenchen.dave.domain.elasticsearch;

import java.io.Serializable;
import lombok.Data;

@Data
public abstract class Bewegungsbeziehung implements Serializable {

    private String id;

}
