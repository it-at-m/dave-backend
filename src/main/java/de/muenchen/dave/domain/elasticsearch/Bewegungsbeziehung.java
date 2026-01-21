package de.muenchen.dave.domain.elasticsearch;

import lombok.Data;

import java.io.Serializable;

@Data
public abstract class Bewegungsbeziehung implements Serializable {

    private String id;

}
