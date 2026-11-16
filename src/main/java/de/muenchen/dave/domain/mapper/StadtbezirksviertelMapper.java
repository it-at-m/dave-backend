package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.StadtbezirksviertelEntity;
import de.muenchen.dave.domain.dtos.StadtbezirksviertelDTO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StadtbezirksviertelMapper {

    @Mapping(target = "stadtbezirkNummer", source = "stadtbezirk.nummer")
    StadtbezirksviertelDTO entity2Dto(StadtbezirksviertelEntity entity);

    List<StadtbezirksviertelDTO> entities2Dtos(List<StadtbezirksviertelEntity> entities);

}
