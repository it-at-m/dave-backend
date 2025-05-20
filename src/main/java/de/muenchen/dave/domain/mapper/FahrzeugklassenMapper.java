package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.geodateneai.gen.model.MessfaehigkeitDto;
import de.muenchen.dave.geodateneai.gen.model.MessstelleDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FahrzeugklassenMapper {

    default Fahrzeugklasse map(final MessstelleDto.FahrzeugklasseEnum fahrzeugklasse) {
        final Fahrzeugklasse mappingTarget;
        if (MessstelleDto.FahrzeugklasseEnum.ZWEI_PLUS_EINS == fahrzeugklasse) {
            mappingTarget = Fahrzeugklasse.ZWEI_PLUS_EINS;
        } else if (MessstelleDto.FahrzeugklasseEnum.ACHT_PLUS_EINS == fahrzeugklasse) {
            mappingTarget = Fahrzeugklasse.ACHT_PLUS_EINS;
        } else if (MessstelleDto.FahrzeugklasseEnum.SUMME_KFZ == fahrzeugklasse) {
            mappingTarget = Fahrzeugklasse.SUMME_KFZ;
        } else if (MessstelleDto.FahrzeugklasseEnum.RAD == fahrzeugklasse) {
            mappingTarget = Fahrzeugklasse.RAD;
        } else {
            mappingTarget = null;
        }
        return mappingTarget;
    }

    default Fahrzeugklasse map(final MessfaehigkeitDto.FahrzeugklasseEnum fahrzeugklasse) {
        final Fahrzeugklasse mappingTarget;
        if (MessfaehigkeitDto.FahrzeugklasseEnum.ZWEI_PLUS_EINS == fahrzeugklasse) {
            mappingTarget = Fahrzeugklasse.ZWEI_PLUS_EINS;
        } else if (MessfaehigkeitDto.FahrzeugklasseEnum.ACHT_PLUS_EINS == fahrzeugklasse) {
            mappingTarget = Fahrzeugklasse.ACHT_PLUS_EINS;
        } else if (MessfaehigkeitDto.FahrzeugklasseEnum.SUMME_KFZ == fahrzeugklasse) {
            mappingTarget = Fahrzeugklasse.SUMME_KFZ;
        } else if (MessfaehigkeitDto.FahrzeugklasseEnum.RAD == fahrzeugklasse) {
            mappingTarget = Fahrzeugklasse.RAD;
        } else {
            mappingTarget = null;
        }
        return mappingTarget;
    }

}
