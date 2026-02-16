package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.external.DetectionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DetectorMapper {

    @Mapping(target = "von", source = "fahrbeziehung.von")
    @Mapping(target = "nach", source = "fahrbeziehung.nach")
    DetectionDTO bean2DetectionDTO(Zeitintervall zi);

    @Mapping(target = "fahrbeziehung.von", source = "von")
    @Mapping(target = "fahrbeziehung.nach", source = "nach")
    @Mapping(target = "type", constant = "STUNDE_VIERTEL")
    @Mapping(target = "fahrbeziehungId", ignore = true)
    @Mapping(target = "sortingIndex", ignore = true)
    @Mapping(target = "zaehlungId", source = "zaehlungId")
    @Mapping(target = "hochrechnung", ignore = true)
    Zeitintervall detectionDTO2Bean(DetectionDTO dto);

}
