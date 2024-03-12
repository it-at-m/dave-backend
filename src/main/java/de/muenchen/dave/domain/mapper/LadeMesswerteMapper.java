package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesPerInterval;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
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
    LadeMesswerteDTO measurementValuesPerIntervalToLadeMesswerteDTO(final MeasurementValuesPerInterval bean);
}
