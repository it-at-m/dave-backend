package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import de.muenchen.dave.util.messstelle.MesswerteSortingIndexUtil;
import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LadeMesswerteMapper {

    @Mapping(target = "type", constant = "")
    @Mapping(target = "sortingIndex", ignore = true)
    @Mapping(target = "fussgaenger", ignore = true)
    @Mapping(target = "pkw", source = "summeAllePkw")
    @Mapping(target = "lkw", source = "anzahlLkw")
    @Mapping(target = "lfw", source = "anzahlLfw")
    @Mapping(target = "lastzuege", source = "summeLastzug")
    @Mapping(target = "busse", source = "anzahlBus")
    @Mapping(target = "kraftraeder", source = "anzahlKrad")
    @Mapping(target = "fahrradfahrer", source = "anzahlRad")
    @Mapping(target = "kfz", source = "summeKraftfahrzeugverkehr")
    @Mapping(target = "schwerverkehr", source = "summeSchwerverkehr")
    @Mapping(target = "gueterverkehr", source = "summeGueterverkehr")
    @Mapping(target = "anteilSchwerverkehrAnKfzProzent", source = "prozentSchwerverkehr")
    @Mapping(target = "anteilGueterverkehrAnKfzProzent", source = "prozentGueterverkehr")
    LadeMesswerteDTO interval2LadeMesswerte(final IntervalDto dto);

    @AfterMapping
    default void interval2LadeMesswerte(
            final IntervalDto source,
            @MappingTarget final LadeMesswerteDTO target) {
        target.setStartUhrzeit(source.getDatumUhrzeitVon().toLocalTime());
        target.setEndeUhrzeit(source.getDatumUhrzeitBis().toLocalTime());
    }

    default List<LadeMesswerteDTO> interval2LadeMesswerte(final List<IntervalDto> intervals, final ZaehldatenIntervall zeitintervall) {
        return intervals.stream()
                .map(interval -> {
                    final var dto = interval2LadeMesswerte(interval);
                    final var sortingIndex = MesswerteSortingIndexUtil.getSortingIndexWithinBlock(dto, zeitintervall.getTypeZeitintervall());
                    dto.setSortingIndex(sortingIndex);
                    return dto;
                })
                .toList();
    }
}
