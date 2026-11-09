package de.muenchen.dave.domain.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
public class StadtbezirkDTO {

    @NotNull
    private Integer nummer;

    @NotNull
    private String name;

}
