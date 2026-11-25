package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteLaengsverkehrDto;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteQuerungsverkehrDto;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDto;
import de.muenchen.dave.domain.elasticsearch.Laengsverkehr;
import de.muenchen.dave.domain.elasticsearch.Querungsverkehr;
import de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.SubclassExhaustiveStrategy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION
)
public interface BewegungsbeziehungMapper {

    Verkehrsbeziehung dto2Bean(final BearbeiteVerkehrsbeziehungDto dto);

    BearbeiteVerkehrsbeziehungDto bean2Dto(final Verkehrsbeziehung bean);

    Laengsverkehr dto2Bean(final BearbeiteLaengsverkehrDto dto);

    BearbeiteLaengsverkehrDto bean2Dto(final Laengsverkehr bean);

    Querungsverkehr dto2Bean(final BearbeiteQuerungsverkehrDto dto);

    BearbeiteQuerungsverkehrDto bean2Dto(final Querungsverkehr bean);
}
