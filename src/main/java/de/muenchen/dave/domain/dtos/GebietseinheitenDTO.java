package de.muenchen.dave.domain.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class GebietseinheitenDTO {

    @NotNull
    private List<StadtbezirkDTO> stadtbezirke;

    @NotNull
    private List<StadtbezirksviertelDTO> stadtbezirksviertel;

}
