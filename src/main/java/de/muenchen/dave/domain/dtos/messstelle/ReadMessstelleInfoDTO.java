package de.muenchen.dave.domain.dtos.messstelle;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class ReadMessstelleInfoDTO implements Serializable {

    private String id;
    private String mstId;
    private String stadtbezirk;
    private Integer stadtbezirkNummer;
    private String standort;
    private Double longitude;
    private Double latitude;
    private String datumLetztePlausibleMessung;
    private String realisierungsdatum;
    private String abbaudatum;
    private List<ReadMessquerschnittDTO> messquerschnitte;
}
