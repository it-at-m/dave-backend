package de.muenchen.dave.domain.dtos.external;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DetectionDTO {

    @NotNull
    UUID zaehlungId;

    @NotNull
    LocalDateTime startUhrzeit;

    @NotNull
    LocalDateTime endeUhrzeit;

    Integer pkw;

    Integer lkw;

    Integer lastzuege;

    Integer busse;

    Integer kraftraeder;

    Integer fahrradfahrer;

    Integer fussgaenger;

    @NotNull
    Integer von;

    @NotNull
    Integer nach;

}
