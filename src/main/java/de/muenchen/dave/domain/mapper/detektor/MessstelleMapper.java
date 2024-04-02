package de.muenchen.dave.domain.mapper.detektor;

import de.muenchen.dave.domain.dtos.messstelle.EditMessquerschnittDTO;
import de.muenchen.dave.domain.dtos.messstelle.EditMessstelleDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOverviewDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessquerschnittDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessstelleInfoDTO;
import de.muenchen.dave.domain.dtos.suche.SucheMessstelleSuggestDTO;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.MessstelleStatus;
import de.muenchen.dave.domain.enums.Stadtbezirk;
import de.muenchen.dave.util.SuchwortUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MessstelleMapper {

    ReadMessstelleInfoDTO bean2readDto(Messstelle bean);

    @AfterMapping
    default void bean2readDtoAfterMapping(@MappingTarget ReadMessstelleInfoDTO dto, Messstelle bean) {
        dto.setLatitude(bean.getPunkt().getLat());
        dto.setLongitude(bean.getPunkt().getLon());
        dto.setStadtbezirk(Stadtbezirk.bezeichnungOf(bean.getStadtbezirkNummer()));
    }

    EditMessstelleDTO bean2editDto(Messstelle bean);

    @AfterMapping
    default void bean2editDtoAfterMapping(@MappingTarget EditMessstelleDTO dto, Messstelle bean) {
        dto.setLatitude(bean.getPunkt().getLat());
        dto.setLongitude(bean.getPunkt().getLon());
        dto.setStadtbezirk(Stadtbezirk.bezeichnungOf(bean.getStadtbezirkNummer()));
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mstId", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "realisierungsdatum", ignore = true)
    @Mapping(target = "abbaudatum", ignore = true)
    @Mapping(target = "stadtbezirkNummer", ignore = true)
    @Mapping(target = "bemerkung", ignore = true)
    @Mapping(target = "hersteller", ignore = true)
    @Mapping(target = "fahrzeugKlassen", ignore = true)
    @Mapping(target = "detektierteVerkehrsarten", ignore = true)
    @Mapping(target = "datumLetztePlausibleMessung", ignore = true)
    @Mapping(target = "punkt", ignore = true)
    @Mapping(target = "suchwoerter", ignore = true)
    @Mapping(target = "messquerschnitte", ignore = true)
    Messstelle updateMessstelle(@MappingTarget Messstelle actual, EditMessstelleDTO dto);

    default void updateMessquerschnitt(Messquerschnitt actual, EditMessquerschnittDTO dto) {
        actual.setStandort(dto.getStandort());
    }

    @AfterMapping
    default void updateMessstelleAfterMapping(@MappingTarget Messstelle actual, EditMessstelleDTO dto) {
        if (!MessstelleStatus.IN_PLANUNG.equals(actual.getStatus())) {
            actual.setGeprueft(true);
        }
        // Suchworte setzen
        final Set<String> generatedSuchwoerter = SuchwortUtil.generateSuchworteOfMessstelle(actual);

        actual.setSuchwoerter(new ArrayList<>());
        if (CollectionUtils.isNotEmpty(generatedSuchwoerter)) {
            actual.getSuchwoerter().addAll(generatedSuchwoerter);
        }

        if (CollectionUtils.isNotEmpty(dto.getCustomSuchwoerter())) {
            actual.getSuchwoerter().addAll(dto.getCustomSuchwoerter());
        }

        actual.getMessquerschnitte().forEach(messquerschnitt -> dto.getMessquerschnitte().forEach(dto1 -> {
            if (dto1.getId().equalsIgnoreCase(messquerschnitt.getId())) {
                updateMessquerschnitt(messquerschnitt, dto1);
            }
        }));
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

    SucheMessstelleSuggestDTO bean2SucheMessstelleSuggestDto(Messstelle bean);

    @AfterMapping
    default void toSucheMessstelleSuggestDto(@MappingTarget SucheMessstelleSuggestDTO dto, Messstelle bean) {
        dto.setText(bean.getMstId() + StringUtils.SPACE + bean.getName());
    }

    MessstelleOverviewDTO bean2overviewDto(Messstelle bean);

    List<MessstelleOverviewDTO> bean2overviewDto(List<Messstelle> bean);

    @AfterMapping
    default void bean2overviewDtoAftermapping(@MappingTarget MessstelleOverviewDTO dto, Messstelle bean) {
        dto.setStadtbezirkNummer(String.valueOf(bean.getStadtbezirkNummer()));
        dto.setStadtbezirk(Stadtbezirk.bezeichnungOf(bean.getStadtbezirkNummer()));
    }

}
