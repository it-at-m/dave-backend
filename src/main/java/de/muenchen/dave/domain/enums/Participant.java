package de.muenchen.dave.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Participant {

    // Dienstleister
    DIENSTLEISTER(1, "Dienstleister"),
    // Mobilitätsreferat
    MOBILITAETSREFERAT(2, "Mobilitätsreferat");

    private final int participantId;
    private final String name;
}
