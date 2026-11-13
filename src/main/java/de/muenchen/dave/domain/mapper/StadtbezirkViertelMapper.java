package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.StadtbezirkViertelEntity;
import de.muenchen.dave.domain.dtos.StadtbezirkViertelDTO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StadtbezirkViertelMapper {

    @Mapping(target = "stadtbezirkNummer", source = "stadtbezirk.nummer")
    StadtbezirkViertelDTO entity2Dto(StadtbezirkViertelEntity entity);

    List<StadtbezirkViertelDTO> entities2Dtos(List<StadtbezirkViertelEntity> entities);

}
