package de.muenchen.dave.domain.mapper.detektor;

import de.muenchen.dave.domain.dtos.messstelle.EditMessfaehigkeitDto;
import de.muenchen.dave.domain.dtos.messstelle.EditMessquerschnittDto;
import de.muenchen.dave.domain.dtos.messstelle.EditMessstelleDto;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOverviewDto;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessfaehigkeitDto;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessquerschnittDto;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessstelleInfoDto;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessquerschnittAuswertungDto;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungDto;
import de.muenchen.dave.domain.dtos.suche.SucheMessstelleSuggestDto;
import de.muenchen.dave.domain.elasticsearch.detektor.Messfaehigkeit;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.MessstelleStatus;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
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
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MessstelleMapper {

    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DaveConstants.DATE_FORMAT);

    ReadMessstelleInfoDto bean2readDto(Messstelle bean, @Context StadtbezirkMapper stadtbezirkMapper);

    @AfterMapping
    default void bean2readDtoAfterMapping(@MappingTarget ReadMessstelleInfoDto dto, Messstelle bean, @Context StadtbezirkMapper stadtbezirkMapper) {
        dto.setLatitude(bean.getPunkt().getLat());
        dto.setLongitude(bean.getPunkt().getLon());
        dto.setStadtbezirk(stadtbezirkMapper.bezeichnungOf(bean.getStadtbezirkNummer()));
    }

    EditMessstelleDto bean2editDto(Messstelle bean, @Context StadtbezirkMapper stadtbezirkMapper);

    @AfterMapping
    default void bean2editDtoAfterMapping(@MappingTarget EditMessstelleDto dto, Messstelle bean, @Context StadtbezirkMapper stadtbezirkMapper) {
        dto.setLatitude(bean.getPunkt().getLat());
        dto.setLongitude(bean.getPunkt().getLon());
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
    @Mapping(target = "punkt", ignore = true)
    @Mapping(target = "suchwoerter", ignore = true)
    @Mapping(target = "messquerschnitte", ignore = true)
    @Mapping(target = "messfaehigkeiten", ignore = true)
    @Mapping(target = "lageplanVorhanden", ignore = true)
    Messstelle updateMessstelle(@MappingTarget Messstelle actual, EditMessstelleDto dto, @Context StadtbezirkMapper stadtbezirkMapper);

    default void updateMessquerschnitt(Messquerschnitt actual, EditMessquerschnittDto dto) {
        actual.setStandort(dto.getStandort());
    }

    @AfterMapping
    default void updateMessstelleAfterMapping(@MappingTarget Messstelle actual, EditMessstelleDto dto, @Context StadtbezirkMapper stadtbezirkMapper) {
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
            if (dto1.getId().equalsIgnoreCase(messquerschnitt.getId())) {
                updateMessquerschnitt(messquerschnitt, dto1);
            }
        }));

        actual.setPunkt(new GeoPoint(dto.getLatitude(), dto.getLongitude()));
    }

    ReadMessquerschnittDto bean2readDto(Messquerschnitt bean);

    List<ReadMessquerschnittDto> bean2readDto(List<Messquerschnitt> bean);

    @AfterMapping
    default void bean2readDtoAfterMapping(@MappingTarget ReadMessquerschnittDto dto, Messquerschnitt bean) {
        dto.setLatitude(bean.getPunkt().getLat());
        dto.setLongitude(bean.getPunkt().getLon());
    }

    EditMessquerschnittDto bean2editDto(Messquerschnitt bean);

    List<EditMessquerschnittDto> bean2editDto(List<Messquerschnitt> bean);

    @AfterMapping
    default void bean2editDtoAfterMapping(@MappingTarget EditMessquerschnittDto dto, Messquerschnitt bean) {
        dto.setLatitude(bean.getPunkt().getLat());
        dto.setLongitude(bean.getPunkt().getLon());
    }

    SucheMessstelleSuggestDto bean2SucheMessstelleSuggestDto(Messstelle bean);

    @AfterMapping
    default void toSucheMessstelleSuggestDto(@MappingTarget SucheMessstelleSuggestDto dto, Messstelle bean) {
        dto.setText(bean.getMstId() + StringUtils.SPACE + bean.getName());
    }

    MessstelleOverviewDto bean2overviewDto(Messstelle bean, @Context StadtbezirkMapper stadtbezirkMapper);

    List<MessstelleOverviewDto> bean2overviewDto(List<Messstelle> bean, @Context StadtbezirkMapper stadtbezirkMapper);

    @AfterMapping
    default void bean2overviewDtoAftermapping(@MappingTarget MessstelleOverviewDto dto, Messstelle bean, @Context StadtbezirkMapper stadtbezirkMapper) {
        dto.setStadtbezirkNummer(String.valueOf(bean.getStadtbezirkNummer()));
        dto.setStadtbezirk(stadtbezirkMapper.bezeichnungOf(bean.getStadtbezirkNummer()));
    }

    List<MessstelleAuswertungDto> bean2auswertungDto(List<Messstelle> bean);

    List<MessquerschnittAuswertungDto> bean2auswertungMqDto(List<Messquerschnitt> bean);

    EditMessfaehigkeitDto messfaehigkeitBean2EditMessfaehigkeitDto(Messfaehigkeit bean);

    List<EditMessfaehigkeitDto> messfaehigkeitBean2EditMessfaehigkeitDto(List<Messfaehigkeit> bean);

    @AfterMapping
    default void messfaehigkeitBean2MessfaehigkeitDtoAftermapping(@MappingTarget EditMessfaehigkeitDto dto, Messfaehigkeit bean) {
        dto.setGueltigAb(bean.getGueltigAb().format(DATE_TIME_FORMATTER));
        if (LocalDate.now().isAfter(bean.getGueltigBis())) {
            dto.setGueltigBis(bean.getGueltigBis().format(DATE_TIME_FORMATTER));
        } else {
            dto.setGueltigBis("");
        }
    }

    ReadMessfaehigkeitDto messfaehigkeitBean2ReadMessfaehigkeitDto(Messfaehigkeit bean);

    List<ReadMessfaehigkeitDto> messfaehigkeitBean2ReadMessfaehigkeitDto(List<Messfaehigkeit> bean);

}
