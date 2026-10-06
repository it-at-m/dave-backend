package de.muenchen.dave.domain.dtos.laden;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public abstract class AbstractBelastungsplanDataDTO implements Serializable {

    @NotNull
    private String label;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean filled;

}
