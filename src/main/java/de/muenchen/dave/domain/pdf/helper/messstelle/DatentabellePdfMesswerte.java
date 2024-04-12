package de.muenchen.dave.domain.pdf.helper.messstelle;

import lombok.Data;

@Data
public class DatentabellePdfMesswerte {

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

    private Integer gesamt;

    private Integer kfz;

    private Integer schwerverkehr;

    private Integer gueterverkehr;

    private Double anteilSchwerverkehrAnKfzProzent;

    private Double anteilGueterverkehrAnKfzProzent;

}
