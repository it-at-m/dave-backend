package de.muenchen.dave.domain.elasticsearch;

import java.io.Serializable;

import org.springframework.data.annotation.Transient;

import lombok.Data;

@Data
public class Knotenarm implements Serializable {

    @Transient
    String id;

    @Transient
    Long version;

    int nummer;

    String Strassenname;

    String filename;
}
