package de.muenchen.relationalimpl.mapper;

import de.muenchen.dave.domain.elasticsearch.detektor.Messfaehigkeit;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MessfaehigkeitRelationalMapper {

    de.muenchen.dave.domain.analytics.detektor.Messfaehigkeit elastic2analytics(
            @MappingTarget de.muenchen.dave.domain.analytics.detektor.Messfaehigkeit analytics,
            Messfaehigkeit elastic);

    Messfaehigkeit analytics2elastic(de.muenchen.dave.domain.analytics.detektor.Messfaehigkeit analytics);
}
