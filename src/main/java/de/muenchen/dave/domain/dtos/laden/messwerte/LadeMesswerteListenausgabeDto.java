package de.muenchen.dave.domain.dtos.laden.messwerte;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class LadeMesswerteListenausgabeDto implements Serializable {

    @NotNull
    List<LadeMesswerteDto> zaehldaten = new ArrayList<>();

}
