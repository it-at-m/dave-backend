package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.PkwEinheit;
import de.muenchen.dave.domain.dtos.PkwEinheitDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PkwEinheitMapper {

    PkwEinheitDto entity2bearbeiteDto(PkwEinheit entity);

}
