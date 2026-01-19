package de.muenchen.dave.domain.elasticsearch;

import java.io.Serializable;
import lombok.Data;
import org.springframework.data.annotation.Transient;

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
