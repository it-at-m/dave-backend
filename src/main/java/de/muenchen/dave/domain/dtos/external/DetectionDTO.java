package de.muenchen.dave.domain.dtos.external;

import lombok.Data;

@Data
public class DetectionDTO {

    String zaehlungId;

    private String startUhrzeit;

    private String endeUhrzeit;

    private Integer pkw;

    private Integer lkw;

    private Integer lastzuege;

    private Integer busse;

    private Integer kraftraeder;

    private Integer fahrradfahrer;

    private Integer fussgaenger;

    private Integer von;

    private Integer nach;

}
