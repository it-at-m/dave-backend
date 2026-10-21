package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.PkwEinheit;
import de.muenchen.dave.domain.dtos.PkwEinheitDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PkwEinheitMapper {

    PkwEinheitDTO entity2bearbeiteDto(PkwEinheit entity);

}
