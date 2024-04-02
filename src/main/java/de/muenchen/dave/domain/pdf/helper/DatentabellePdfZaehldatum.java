package de.muenchen.dave.domain.pdf.helper;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class DatentabellePdfZaehldatum {

    private String type;

    private String startUhrzeit;

    private String endeUhrzeit;

    private Integer pkw;

    private Integer lkw;

    private Integer lastzuege;

    private Integer lfw;

    private Integer busse;

    private Integer kraftraeder;

    private Integer fahrradfahrer;

    private Integer fussgaenger;

    private Integer pkwEinheiten;

    private BigDecimal gesamt;

    private BigDecimal kfz;

    private BigDecimal schwerverkehr;

    private BigDecimal gueterverkehr;

    private BigDecimal anteilSchwerverkehrAnKfzProzent;

    private BigDecimal anteilGueterverkehrAnKfzProzent;

}
