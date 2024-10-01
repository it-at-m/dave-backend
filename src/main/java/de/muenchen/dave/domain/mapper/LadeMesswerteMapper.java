package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.geodateneai.gen.model.IntervallDto;
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
    LadeMesswerteDTO measurementValuesPerIntervalToLadeMesswerteDTO(final IntervallDto bean);

    @AfterMapping
    default void measurementValuesPerIntervalToLadeMesswerteDTO(
            final IntervallDto bean,
            @MappingTarget final LadeMesswerteDTO target) {
        target.setStartUhrzeit(bean.getDatumUhrzeitVon().toLocalTime());
        target.setEndeUhrzeit(bean.getDatumUhrzeitBis().toLocalTime());
    }
}
