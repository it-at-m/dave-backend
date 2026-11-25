package de.muenchen.dave.domain.dtos.laden;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class LadeZaehldatenTableDto implements Serializable {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    List<LadeZaehldatumDto> zaehldaten;

}
