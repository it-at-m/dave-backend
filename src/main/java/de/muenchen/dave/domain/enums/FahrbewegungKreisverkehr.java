package de.muenchen.dave.domain.enums;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.dtos.external.ExternalVerkehrsbeziehungDTO;
import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;

public enum FahrbewegungKreisverkehr {

    HINEIN,
    HERAUS,
    VORBEI;

    public static Optional<FahrbewegungKreisverkehr> createEnumFrom(final BearbeiteVerkehrsbeziehungDTO fahrbeziehungDto) {
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

    public static Optional<FahrbewegungKreisverkehr> createEnumFrom(final ExternalVerkehrsbeziehungDTO fahrbeziehungDto) {
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
