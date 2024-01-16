package de.muenchen.dave.domain.dtos.messstelle;

import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
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
