package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.StadtbezirkEntity;
import de.muenchen.dave.domain.dtos.StadtbezirkDTO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StadtbezirkMapper {

    StadtbezirkDTO entity2Dto(StadtbezirkEntity entity);

    List<StadtbezirkDTO> entities2Dtos(List<StadtbezirkEntity> entities);

}
