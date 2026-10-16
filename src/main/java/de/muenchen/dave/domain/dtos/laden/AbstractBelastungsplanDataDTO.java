package de.muenchen.dave.domain.dtos.laden;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Data;

@Data
public abstract class AbstractBelastungsplanDataDTO implements Serializable {

    @NotNull
    private String label;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean filled;

}
