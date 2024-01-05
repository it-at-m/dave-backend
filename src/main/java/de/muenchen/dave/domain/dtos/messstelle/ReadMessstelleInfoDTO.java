package de.muenchen.dave.domain.dtos.messstelle;

import java.io.Serializable;
import lombok.Data;

@Data
public class ReadMessstelleInfoDTO implements Serializable {

    private String id;
    private String mstId;
    private String stadtbezirk;
    private Integer stadtbezirkNummer;
    private Double longitude;
    private Double latitude;
}
