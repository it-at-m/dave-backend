package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.geodateneai.gen.model.GetMeasurementValuesAggregateRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GeodatenEaiMapper {
    GetMeasurementValuesAggregateRequest.TagesTypEnum backendToEai(TagesTyp backend);

    TagesTyp eaiToBackend(GetMeasurementValuesAggregateRequest.TagesTypEnum eai);
}
