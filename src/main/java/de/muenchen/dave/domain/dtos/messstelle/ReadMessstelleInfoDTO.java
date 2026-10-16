package de.muenchen.dave.domain.dtos.messstelle;

import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.Verkehrsart;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class ReadMessstelleInfoDTO implements Serializable {

    @NotNull
    private String id;
    @NotNull
    private String mstId;
    @NotNull
    private String standort;
    @NotNull
    private String stadtbezirk;
    @NotNull
    private Integer stadtbezirkNummer;
    @NotNull
    private Fahrzeugklasse fahrzeugklasse;
    @NotNull
    private Verkehrsart detektierteVerkehrsart;
    @NotNull
    private String hersteller;
    @NotNull
    private Double longitude;
    @NotNull
    private Double latitude;
    @NotNull
    private LocalDate datumLetztePlausibleMessung;
    @NotNull
    private LocalDate realisierungsdatum;
    @NotNull
    private LocalDate abbaudatum;
    @NotNull
    private String kommentar;
    @NotNull
    private List<ReadMessquerschnittDTO> messquerschnitte;
    @NotNull
    private List<ReadMessfaehigkeitDTO> messfaehigkeiten;
    @NotNull
    private Boolean lageplanVorhanden;
}
