package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OptionsMapper {

    OptionsDTO deepCopy(final OptionsDTO options);

}
