package de.muenchen.dave.domain.dtos;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class LeseZaehlstelleDTO implements Serializable {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

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
