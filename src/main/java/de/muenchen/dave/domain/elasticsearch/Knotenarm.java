package de.muenchen.dave.domain.elasticsearch;

import java.io.Serializable;
import lombok.Data;

@Data
public class Knotenarm implements Serializable {

    int nummer;

    String Strassenname;

    String filename;
}
