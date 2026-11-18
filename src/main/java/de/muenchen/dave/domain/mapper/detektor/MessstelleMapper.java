package de.muenchen.dave.domain.mapper.detektor;

import de.muenchen.dave.domain.dtos.messstelle.EditMessfaehigkeitDTO;
import de.muenchen.dave.domain.dtos.messstelle.EditMessquerschnittDTO;
import de.muenchen.dave.domain.dtos.messstelle.EditMessstelleDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOverviewDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessfaehigkeitDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessquerschnittDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessstelleInfoDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessquerschnittAuswertungDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungDTO;
import de.muenchen.dave.domain.dtos.suche.SucheMessstelleSuggestDTO;
import de.muenchen.dave.domain.enums.MessstelleStatus;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
import de.muenchen.dave.relational.messtellen.MessfaehigkeitEntity;
import de.muenchen.dave.relational.messtellen.MessquerschnittEntity;
import de.muenchen.dave.relational.messtellen.MessstelleEntity;
import de.muenchen.dave.util.DaveConstants;
import de.muenchen.dave.util.SuchwortUtil;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MessstelleMapper {

    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DaveConstants.DATE_FORMAT);

    ReadMessstelleInfoDTO bean2readDto(MessstelleEntity bean, @Context StadtbezirkMapper stadtbezirkMapper);

    @AfterMapping
    default void bean2readDtoAfterMapping(@MappingTarget ReadMessstelleInfoDTO dto, MessstelleEntity bean, @Context StadtbezirkMapper stadtbezirkMapper) {
        dto.setLatitude(bean.getLatitude());
        dto.setLongitude(bean.getLongitude());
        dto.setStadtbezirk(stadtbezirkMapper.bezeichnungOf(bean.getStadtbezirkNummer()));
    }

    EditMessstelleDTO bean2editDto(MessstelleEntity bean, @Context StadtbezirkMapper stadtbezirkMapper);

    @AfterMapping
    default void bean2editDtoAfterMapping(@MappingTarget EditMessstelleDTO dto, MessstelleEntity bean, @Context StadtbezirkMapper stadtbezirkMapper) {
        dto.setLatitude(bean.getLatitude());
        dto.setLongitude(bean.getLongitude());
        dto.setStadtbezirk(stadtbezirkMapper.bezeichnungOf(bean.getStadtbezirkNummer()));
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
    @Mapping(target = "fahrzeugklasse", ignore = true)
    @Mapping(target = "detektierteVerkehrsart", ignore = true)
    @Mapping(target = "suchwoerter", ignore = true)
    @Mapping(target = "messquerschnitte", ignore = true)
    @Mapping(target = "messfaehigkeiten", ignore = true)
    @Mapping(target = "lageplanVorhanden", ignore = true)
    MessstelleEntity updateMessstelle(@MappingTarget MessstelleEntity actual, EditMessstelleDTO dto, @Context StadtbezirkMapper stadtbezirkMapper);

    default void updateMessquerschnitt(MessquerschnittEntity actual, EditMessquerschnittDTO dto) {
        actual.setStandort(dto.getStandort());
    }

    @AfterMapping
    default void updateMessstelleAfterMapping(@MappingTarget MessstelleEntity actual, EditMessstelleDTO dto, @Context StadtbezirkMapper stadtbezirkMapper) {
        if (!MessstelleStatus.IN_PLANUNG.equals(actual.getStatus())) {
            actual.setGeprueft(true);
        }

        // Suchworte setzen
        final Set<String> generatedSuchwoerter = SuchwortUtil.generateSuchworteOfMessstelle(actual, stadtbezirkMapper);

        actual.setSuchwoerter(new ArrayList<>());
        if (CollectionUtils.isNotEmpty(generatedSuchwoerter)) {
            actual.getSuchwoerter().addAll(generatedSuchwoerter);
        }

        if (CollectionUtils.isNotEmpty(dto.getCustomSuchwoerter())) {
            actual.getSuchwoerter().addAll(dto.getCustomSuchwoerter());
        }

        actual.getMessquerschnitte().forEach(messquerschnitt -> dto.getMessquerschnitte().forEach(dto1 -> {
            if (dto1.getId().equalsIgnoreCase(String.valueOf(messquerschnitt.getId()))) {
                updateMessquerschnitt(messquerschnitt, dto1);
            }
        }));

        actual.setLatitude(dto.getLatitude());
        actual.setLongitude(dto.getLongitude());
    }

    ReadMessquerschnittDTO bean2readDto(MessquerschnittEntity bean);

    List<ReadMessquerschnittDTO> bean2readDto(List<MessquerschnittEntity> bean);

    @AfterMapping
    default void bean2readDtoAfterMapping(@MappingTarget ReadMessquerschnittDTO dto, MessquerschnittEntity bean) {
        dto.setLatitude(bean.getLatitude());
        dto.setLongitude(bean.getLongitude());
    }

    EditMessquerschnittDTO bean2editDto(MessquerschnittEntity bean);

    List<EditMessquerschnittDTO> bean2editDto(List<MessquerschnittEntity> bean);

    @AfterMapping
    default void bean2editDtoAfterMapping(@MappingTarget EditMessquerschnittDTO dto, MessquerschnittEntity bean) {
        dto.setLatitude(bean.getLatitude());
        dto.setLongitude(bean.getLongitude());
    }

    SucheMessstelleSuggestDTO bean2SucheMessstelleSuggestDto(MessstelleEntity bean);

    @AfterMapping
    default void toSucheMessstelleSuggestDto(@MappingTarget SucheMessstelleSuggestDTO dto, MessstelleEntity bean) {
        dto.setText(bean.getMstId() + StringUtils.SPACE + bean.getName());
    }

    MessstelleOverviewDTO bean2overviewDto(MessstelleEntity bean, @Context StadtbezirkMapper stadtbezirkMapper);

    List<MessstelleOverviewDTO> bean2overviewDto(List<MessstelleEntity> bean, @Context StadtbezirkMapper stadtbezirkMapper);

    @AfterMapping
    default void bean2overviewDtoAftermapping(@MappingTarget MessstelleOverviewDTO dto, MessstelleEntity bean, @Context StadtbezirkMapper stadtbezirkMapper) {
        dto.setStadtbezirkNummer(String.valueOf(bean.getStadtbezirkNummer()));
        dto.setStadtbezirk(stadtbezirkMapper.bezeichnungOf(bean.getStadtbezirkNummer()));
    }

    List<MessstelleAuswertungDTO> bean2auswertungDto(List<MessstelleEntity> bean);

    List<MessquerschnittAuswertungDTO> bean2auswertungMqDto(List<MessquerschnittEntity> bean);

    EditMessfaehigkeitDTO messfaehigkeitBean2EditMessfaehigkeitDto(MessfaehigkeitEntity bean);

    List<EditMessfaehigkeitDTO> messfaehigkeitBean2EditMessfaehigkeitDto(List<MessfaehigkeitEntity> bean);

    @AfterMapping
    default void messfaehigkeitBean2MessfaehigkeitDtoAftermapping(@MappingTarget EditMessfaehigkeitDTO dto, MessfaehigkeitEntity bean) {
        dto.setGueltigAb(bean.getGueltigAb().format(DATE_TIME_FORMATTER));
        if (LocalDate.now().isAfter(bean.getGueltigBis())) {
            dto.setGueltigBis(bean.getGueltigBis().format(DATE_TIME_FORMATTER));
        } else {
            dto.setGueltigBis("");
        }
    }

    ReadMessfaehigkeitDTO messfaehigkeitBean2ReadMessfaehigkeitDto(MessfaehigkeitEntity bean);

    List<ReadMessfaehigkeitDTO> messfaehigkeitBean2ReadMessfaehigkeitDto(List<MessfaehigkeitEntity> bean);

}
