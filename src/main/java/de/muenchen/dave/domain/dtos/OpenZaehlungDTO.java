package de.muenchen.dave.domain.dtos;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class OpenZaehlungDTO implements Serializable {

    String id;

    String zaehlstellenNummer;

    String zaehlstellenId;

    String stadtbezirk;

    Date datum;

    String zaehlart;

    String projektNummer;

    String projektName;

    String status;
}
