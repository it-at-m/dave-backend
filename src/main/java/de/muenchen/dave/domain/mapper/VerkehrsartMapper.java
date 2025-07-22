package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.enums.Verkehrsart;
import de.muenchen.dave.geodateneai.gen.model.MessstelleDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VerkehrsartMapper {

    default Verkehrsart map(final MessstelleDto.DetektierteVerkehrsartEnum verkehrsart) {
        final Verkehrsart mappingTarget;
        if (MessstelleDto.DetektierteVerkehrsartEnum.KFZ == verkehrsart) {
            mappingTarget = Verkehrsart.KFZ;
        } else if (MessstelleDto.DetektierteVerkehrsartEnum.RAD == verkehrsart) {
            mappingTarget = Verkehrsart.RAD;
        } else if (MessstelleDto.DetektierteVerkehrsartEnum.UNBEKANNT == verkehrsart) {
            mappingTarget = Verkehrsart.UNBEKANNT;
        } else {
            mappingTarget = null;
        }
        return mappingTarget;
    }
}
