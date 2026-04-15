package de.muenchen.dave.domain.dtos.laden;

import java.io.Serializable;
import lombok.Data;

@Data
public abstract class AbstractBelastungsplanDataDTO implements Serializable {

    private String label;

    private boolean filled;

}
