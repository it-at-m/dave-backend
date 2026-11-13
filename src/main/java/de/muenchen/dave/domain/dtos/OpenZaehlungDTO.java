package de.muenchen.dave.domain.dtos;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class OpenZaehlungDTO implements Serializable {

    String id;

    String zaehlstellenNummer;

    String zaehlstellenId;

    Integer stadtbezirkNummer;

    Date datum;

    String zaehlart;

    String projektNummer;

    String projektName;

    String status;
}
