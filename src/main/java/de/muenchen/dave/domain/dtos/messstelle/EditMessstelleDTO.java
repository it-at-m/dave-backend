package de.muenchen.dave.domain.dtos.messstelle;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class EditMessstelleDTO implements Serializable {

    private String id;

    private String mstId;
    private String name;
    private String status;
    private String bemerkung;
    private String stadtbezirk;
    private Integer stadtbezirkNummer;

    private String realisierungsdatum;
    private String abbaudatum;
    private String datumLetztePlausibleMessung;

    private String fahrzeugKlassen;
    private String detektierteVerkehrsarten;
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

}
