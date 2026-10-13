package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
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

    Verkehrsbeziehung dto2Bean(final BearbeiteVerkehrsbeziehungDTO dto);

    BearbeiteVerkehrsbeziehungDTO bean2Dto(final Verkehrsbeziehung bean);

    Laengsverkehr dto2Bean(final BearbeiteLaengsverkehrDTO dto);

    BearbeiteLaengsverkehrDTO bean2Dto(final Laengsverkehr bean);

    Querungsverkehr dto2Bean(final BearbeiteQuerungsverkehrDTO dto);

    BearbeiteQuerungsverkehrDTO bean2Dto(final Querungsverkehr bean);
}
