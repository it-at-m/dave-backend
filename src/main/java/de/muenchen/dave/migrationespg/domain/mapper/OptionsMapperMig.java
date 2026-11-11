package de.muenchen.dave.migrationespg.domain.mapper;

import de.muenchen.dave.migrationespg.domain.dtos.OptionsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OptionsMapperMig {

    OptionsDTO deepCopy(final OptionsDTO options);

}
