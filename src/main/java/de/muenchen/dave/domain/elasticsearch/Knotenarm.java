package de.muenchen.dave.domain.elasticsearch;

import lombok.Data;

import java.io.Serializable;

@Data
public class Knotenarm implements Serializable {

    int nummer;

    String Strassenname;

    String filename;
}
