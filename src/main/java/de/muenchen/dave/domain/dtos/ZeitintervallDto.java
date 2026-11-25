package de.muenchen.dave.domain.dtos;

import lombok.Data;

@Data
public class ZeitintervallDto {

    private String startUhrzeit;

    private String endeUhrzeit;

    private Integer pkw;

    private Integer lkw;

    private Integer lastzuege;

    private Integer busse;

    private Integer kraftraeder;

    private Integer fahrradfahrer;

    private Integer fussgaenger;

}
