package de.muenchen.dave.domain.mapper.detektor;

import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungMessquerschnitte;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatResponseDto;
import de.muenchen.dave.services.messstelle.Zeitraum;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuswertungMapper {

    @Mapping(target = "zeitraum", expression = "java( zeitraum )")
    @Mapping(target = "mstId", expression = "java( Integer.valueOf(mstId) )")
    AuswertungMessquerschnitte tagesaggregatDto2AuswertungResponse(
            final TagesaggregatResponseDto dto,
            @Context final Zeitraum zeitraum,
            @Context final String mstId);
}
