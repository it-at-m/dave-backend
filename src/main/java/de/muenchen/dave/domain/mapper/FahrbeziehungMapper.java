package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteFahrbeziehungDTO;
import de.muenchen.dave.domain.elasticsearch.Fahrbeziehung;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FahrbeziehungMapper {

    /**
     * bearbeite auf bean (für Kreuzung)
     *
     * @param dto BearbeiteFahrbeziehungDTO
     * @return gemappte Fahrbeziehung
     */
    Fahrbeziehung bearbeiteFahrbeziehungDto2bean(BearbeiteFahrbeziehungDTO dto);

    /**
     * bean auf bearbeite (für Kreuzung)
     *
     * @param bean Fahrbeziehung
     * @return gemapptes BearbeiteFahrbeziehungDTO
     */
    BearbeiteFahrbeziehungDTO bean2bearbeiteFahrbeziehunDto(Fahrbeziehung bean);

}
