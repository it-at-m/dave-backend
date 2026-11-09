package de.muenchen.dave.domain.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
public class StadtbezirkViertelDTO {

    @NotNull
    private String nummer;
    private String name;
    @NotNull
    private Integer stadtbezirkNummer;

}
