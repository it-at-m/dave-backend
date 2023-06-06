package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.PkwEinheitDTO;
import de.muenchen.dave.domain.elasticsearch.PkwEinheit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PkwEinheitMapper {

    de.muenchen.dave.domain.PkwEinheit bearbeiteDto2entity(PkwEinheitDTO dto);

    PkwEinheitDTO entity2bearbeiteDto(de.muenchen.dave.domain.PkwEinheit entity);

    de.muenchen.dave.domain.PkwEinheit elastic2Entity(PkwEinheit elastic);

}
