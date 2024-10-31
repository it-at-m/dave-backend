package de.muenchen.dave.domain.mapper.detektor;

import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungResponse;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatDto;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuswertungMapper {

    @Mapping(target = "zeitraum", ignore = true)
    AuswertungResponse tagesaggregatDto2AuswertungResponse(TagesaggregatResponseDto dto);

    //    List<AuswertungResponse> tagesaggregatDto2AuswertungResponse(List<TagesaggregatDto> dto);
}
