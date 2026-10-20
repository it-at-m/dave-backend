package de.muenchen.dave.domain.dtos.messstelle;

import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.Verkehrsart;
import java.io.Serializable;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EditMessstelleDTO implements Serializable {

    private String id;

    @NotNull
    private String mstId;
    private String name;
    @NotNull
    private String status;
    private String bemerkung;
    private String stadtbezirk;
    private Integer stadtbezirkNummer;

    private String realisierungsdatum;
    private String abbaudatum;
    private String datumLetztePlausibleMessung;

    private Fahrzeugklasse fahrzeugklasse;
    private Verkehrsart detektierteVerkehrsart;
    private String hersteller;

    private Double longitude;
    private Double latitude;

    private Boolean sichtbarDatenportal;
    private Boolean geprueft;
    private String kommentar;
    private String standort;
    private List<String> customSuchwoerter;

    private List<EditMessquerschnittDTO> messquerschnitte;
    private List<EditMessfaehigkeitDTO> messfaehigkeiten;

    private Boolean lageplanVorhanden;
}
