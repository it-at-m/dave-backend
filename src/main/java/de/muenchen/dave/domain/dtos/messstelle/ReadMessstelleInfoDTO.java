package de.muenchen.dave.domain.dtos.messstelle;

import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class ReadMessstelleInfoDTO implements Serializable {

    private String id;
    private String mstId;
    private String standort;
    private String stadtbezirk;
    private Integer stadtbezirkNummer;
    private Fahrzeugklasse fahrzeugklasse;
    private String detektierteVerkehrsarten;
    private String hersteller;
    private Double longitude;
    private Double latitude;
    private LocalDate datumLetztePlausibleMessung;
    private LocalDate realisierungsdatum;
    private LocalDate abbaudatum;
    private String kommentar;
    private List<ReadMessquerschnittDTO> messquerschnitte;
    private List<ReadMessfaehigkeitDTO> messfaehigkeiten;
}
