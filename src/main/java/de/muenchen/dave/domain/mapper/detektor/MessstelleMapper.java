package de.muenchen.dave.domain.mapper.detektor;

import de.muenchen.dave.domain.dtos.messstelle.EditMessquerschnittDTO;
import de.muenchen.dave.domain.dtos.messstelle.EditMessstelleDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessquerschnittDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessstelleDTO;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.services.IndexServiceUtils;
import de.muenchen.dave.util.SuchwortUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MessstelleMapper {

    ReadMessstelleDTO bean2readDto(Messstelle bean);

    @AfterMapping
    default void bean2readDtoAfterMapping(@MappingTarget ReadMessstelleDTO dto, Messstelle bean) {
        dto.setLatitude(bean.getPunkt().getLat());
        dto.setLongitude(bean.getPunkt().getLon());
        dto.setTooltip(IndexServiceUtils.createMessstelleTooltip(bean));
    }

    EditMessstelleDTO bean2editDto(Messstelle bean);

    @AfterMapping
    default void bean2editDtoAfterMapping(@MappingTarget EditMessstelleDTO dto, Messstelle bean) {
        dto.setLatitude(bean.getPunkt().getLat());
        dto.setLongitude(bean.getPunkt().getLon());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mstId", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "realisierungsdatum", ignore = true)
    @Mapping(target = "abbaudatum", ignore = true)
    @Mapping(target = "stadtbezirkNummer", ignore = true)
    @Mapping(target = "bemerkung", ignore = true)
    @Mapping(target = "datumLetztePlausibleMessung", ignore = true)
    @Mapping(target = "punkt", ignore = true)
    @Mapping(target = "suchwoerter", ignore = true)
    @Mapping(target = "messquerschnitte", ignore = true)
    Messstelle updateMessstelle(@MappingTarget Messstelle actual, EditMessstelleDTO dto);

    @AfterMapping
    default void updateMessstelleAfterMapping(@MappingTarget Messstelle actual, EditMessstelleDTO dto) {
        // Suchworte setzen
        final Set<String> generatedSuchwoerter = SuchwortUtil.generateSuchworteOfMessstelle(actual);

        actual.setSuchwoerter(new ArrayList<>());
        if (CollectionUtils.isNotEmpty(generatedSuchwoerter)) {
            actual.getSuchwoerter().addAll(generatedSuchwoerter);
        }

        if (CollectionUtils.isNotEmpty(dto.getCustomSuchwoerter())) {
            actual.getSuchwoerter().addAll(dto.getCustomSuchwoerter());
        }
    }

    ReadMessquerschnittDTO bean2readDto(Messquerschnitt bean);

    List<ReadMessquerschnittDTO> bean2readDto(List<Messquerschnitt> bean);

    @AfterMapping
    default void bean2readDtoAfterMapping(@MappingTarget ReadMessquerschnittDTO dto, Messquerschnitt bean) {
        dto.setLatitude(bean.getPunkt().getLat());
        dto.setLongitude(bean.getPunkt().getLon());
    }

    EditMessquerschnittDTO bean2editDto(Messquerschnitt bean);

    List<EditMessquerschnittDTO> bean2editDto(List<Messquerschnitt> bean);

    @AfterMapping
    default void bean2editDtoAfterMapping(@MappingTarget EditMessquerschnittDTO dto, Messquerschnitt bean) {
        dto.setLatitude(bean.getPunkt().getLat());
        dto.setLongitude(bean.getPunkt().getLon());
    }

}
