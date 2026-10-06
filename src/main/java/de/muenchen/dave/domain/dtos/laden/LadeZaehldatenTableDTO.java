package de.muenchen.dave.domain.dtos.laden;

import java.io.Serializable;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LadeZaehldatenTableDTO implements Serializable {

    @NotNull
    List<LadeZaehldatumDTO> zaehldaten;

}
