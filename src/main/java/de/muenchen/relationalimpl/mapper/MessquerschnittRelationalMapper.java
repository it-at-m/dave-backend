package de.muenchen.relationalimpl.mapper;

import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MessquerschnittRelationalMapper {

    de.muenchen.dave.domain.analytics.detektor.Messquerschnitt elastic2analytics(
            @MappingTarget de.muenchen.dave.domain.analytics.detektor.Messquerschnitt analytics,
            Messquerschnitt elastic);

    Messquerschnitt analytics2elastic(de.muenchen.dave.domain.analytics.detektor.Messquerschnitt analytics);
}
