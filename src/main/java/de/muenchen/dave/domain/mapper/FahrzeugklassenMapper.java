package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.geodateneai.gen.model.MessfaehigkeitDto;
import de.muenchen.dave.geodateneai.gen.model.MessstelleDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FahrzeugklassenMapper {

    default Fahrzeugklasse map(final MessstelleDto.FahrzeugKlassenEnum fahrzeugklasse) {
        final Fahrzeugklasse mappingTarget;
        if (MessstelleDto.FahrzeugKlassenEnum.ZWEI_PLUS_EINS == fahrzeugklasse) {
            mappingTarget = Fahrzeugklasse.ZWEI_PLUS_EINS;
        } else if (MessstelleDto.FahrzeugKlassenEnum.ACHT_PLUS_EINS == fahrzeugklasse) {
            mappingTarget = Fahrzeugklasse.ACHT_PLUS_EINS;
        } else if (MessstelleDto.FahrzeugKlassenEnum.SUMME_KFZ == fahrzeugklasse) {
            mappingTarget = Fahrzeugklasse.SUMME_KFZ;
        } else if (MessstelleDto.FahrzeugKlassenEnum.RAD == fahrzeugklasse) {
            mappingTarget = Fahrzeugklasse.RAD;
        } else {
            mappingTarget = null;
        }
        return mappingTarget;
    }

    default Fahrzeugklasse map(final MessfaehigkeitDto.FahrzeugklassenEnum fahrzeugklasse) {
        final Fahrzeugklasse mappingTarget;
        if (MessfaehigkeitDto.FahrzeugklassenEnum.ZWEI_PLUS_EINS == fahrzeugklasse) {
            mappingTarget = Fahrzeugklasse.ZWEI_PLUS_EINS;
        } else if (MessfaehigkeitDto.FahrzeugklassenEnum.ACHT_PLUS_EINS == fahrzeugklasse) {
            mappingTarget = Fahrzeugklasse.ACHT_PLUS_EINS;
        } else if (MessfaehigkeitDto.FahrzeugklassenEnum.SUMME_KFZ == fahrzeugklasse) {
            mappingTarget = Fahrzeugklasse.SUMME_KFZ;
        } else if (MessfaehigkeitDto.FahrzeugklassenEnum.RAD == fahrzeugklasse) {
            mappingTarget = Fahrzeugklasse.RAD;
        } else {
            mappingTarget = null;
        }
        return mappingTarget;
    }

}
