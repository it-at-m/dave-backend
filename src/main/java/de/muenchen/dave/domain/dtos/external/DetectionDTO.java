package de.muenchen.dave.domain.dtos.external;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class DetectionDTO {

    UUID zaehlungId;

    LocalDateTime startUhrzeit;

    LocalDateTime endeUhrzeit;

    Integer pkw;

    Integer lkw;

    Integer lastzuege;

    Integer busse;

    Integer kraftraeder;

    Integer fahrradfahrer;

    Integer fussgaenger;

    Integer von;

    Integer nach;

}
