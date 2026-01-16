package de.muenchen.relationalimpl.mapper;

import de.muenchen.dave.domain.elasticsearch.Fahrbeziehung;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FahrbeziehungRelationalMapper {


    de.muenchen.dave.domain.analytics.Fahrbeziehung elastic2analytics(@MappingTarget de.muenchen.dave.domain.analytics.Fahrbeziehung analytics,
            Fahrbeziehung elastic);
}
