/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.enums;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteFahrbeziehungDTO;
import de.muenchen.dave.domain.dtos.external.ExternalFahrbeziehungDTO;
import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;

public enum FahrbewegungKreisverkehr {

    HINEIN,
    HERAUS,
    VORBEI;

    public static Optional<FahrbewegungKreisverkehr> createEnumFrom(final BearbeiteFahrbeziehungDTO fahrbeziehungDto) {
        final Optional<FahrbewegungKreisverkehr> fahrbewegungKreisverkehrOptional;
        if (BooleanUtils.isTrue(fahrbeziehungDto.getHinein())) {
            fahrbewegungKreisverkehrOptional = Optional.of(HINEIN);
        } else if (BooleanUtils.isTrue(fahrbeziehungDto.getHeraus())) {
            fahrbewegungKreisverkehrOptional = Optional.of(HERAUS);
        } else if (BooleanUtils.isTrue(fahrbeziehungDto.getVorbei())) {
            fahrbewegungKreisverkehrOptional = Optional.of(VORBEI);
        } else {
            fahrbewegungKreisverkehrOptional = Optional.empty();
        }
        return fahrbewegungKreisverkehrOptional;
    }

    public static Optional<FahrbewegungKreisverkehr> createEnumFrom(final ExternalFahrbeziehungDTO fahrbeziehungDto) {
        final Optional<FahrbewegungKreisverkehr> fahrbewegungKreisverkehrOptional;
        if (BooleanUtils.isTrue(fahrbeziehungDto.getHinein())) {
            fahrbewegungKreisverkehrOptional = Optional.of(HINEIN);
        } else if (BooleanUtils.isTrue(fahrbeziehungDto.getHeraus())) {
            fahrbewegungKreisverkehrOptional = Optional.of(HERAUS);
        } else if (BooleanUtils.isTrue(fahrbeziehungDto.getVorbei())) {
            fahrbewegungKreisverkehrOptional = Optional.of(VORBEI);
        } else {
            fahrbewegungKreisverkehrOptional = Optional.empty();
        }
        return fahrbewegungKreisverkehrOptional;
    }

}
