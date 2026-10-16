package de.muenchen.dave.domain.dtos;

import java.io.Serializable;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeseZaehlstelleDTO implements Serializable {

    @NotNull
    String id;
    @NotNull
    String nummer;
    @NotNull
    String name;
    @NotNull
    String stadtbezirk;
    @NotNull
    Integer stadtbezirkNummer;
    @NotNull
    Double lat;
    @NotNull
    Double lng;
    @NotNull
    String kommentar;
    List<String> suchwoerter;
    @NotNull
    List<LeseZaehlungDTO> zaehlungen;
    Boolean sichtbarDatenportal;
}
