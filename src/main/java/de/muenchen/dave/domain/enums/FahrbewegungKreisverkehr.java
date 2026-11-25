package de.muenchen.dave.domain.enums;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDto;
import de.muenchen.dave.domain.dtos.external.ExternalVerkehrsbeziehungDto;
import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;

public enum FahrbewegungKreisverkehr {

    HINEIN,
    HERAUS,
    VORBEI;

    public static Optional<FahrbewegungKreisverkehr> createEnumFrom(final BearbeiteVerkehrsbeziehungDto verkehrsbeziehung) {
        final Optional<FahrbewegungKreisverkehr> fahrbewegungKreisverkehrOptional;
        if (BooleanUtils.isTrue(verkehrsbeziehung.getHinein())) {
            fahrbewegungKreisverkehrOptional = Optional.of(HINEIN);
        } else if (BooleanUtils.isTrue(verkehrsbeziehung.getHeraus())) {
            fahrbewegungKreisverkehrOptional = Optional.of(HERAUS);
        } else if (BooleanUtils.isTrue(verkehrsbeziehung.getVorbei())) {
            fahrbewegungKreisverkehrOptional = Optional.of(VORBEI);
        } else {
            fahrbewegungKreisverkehrOptional = Optional.empty();
        }
        return fahrbewegungKreisverkehrOptional;
    }

    public static Optional<FahrbewegungKreisverkehr> createEnumFrom(final ExternalVerkehrsbeziehungDto verkehrsbeziehung) {
        final Optional<FahrbewegungKreisverkehr> fahrbewegungKreisverkehrOptional;
        if (BooleanUtils.isTrue(verkehrsbeziehung.getHinein())) {
            fahrbewegungKreisverkehrOptional = Optional.of(HINEIN);
        } else if (BooleanUtils.isTrue(verkehrsbeziehung.getHeraus())) {
            fahrbewegungKreisverkehrOptional = Optional.of(HERAUS);
        } else if (BooleanUtils.isTrue(verkehrsbeziehung.getVorbei())) {
            fahrbewegungKreisverkehrOptional = Optional.of(VORBEI);
        } else {
            fahrbewegungKreisverkehrOptional = Optional.empty();
        }
        return fahrbewegungKreisverkehrOptional;
    }

}
