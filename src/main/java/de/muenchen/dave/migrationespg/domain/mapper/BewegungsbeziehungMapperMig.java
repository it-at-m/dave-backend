package de.muenchen.dave.migrationespg.domain.mapper;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.LaengsverkehrEntity;
import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.QuerungsverkehrEntity;
import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.VerkehrsbeziehungEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.SubclassExhaustiveStrategy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION
)
public interface BewegungsbeziehungMapperMig {

    VerkehrsbeziehungEntity dto2Bean(final BearbeiteVerkehrsbeziehungDTO dto);

    BearbeiteVerkehrsbeziehungDTO bean2Dto(final VerkehrsbeziehungEntity bean);

    LaengsverkehrEntity dto2Bean(final BearbeiteLaengsverkehrDTO dto);

    BearbeiteLaengsverkehrDTO bean2Dto(final LaengsverkehrEntity bean);

    QuerungsverkehrEntity dto2Bean(final BearbeiteQuerungsverkehrDTO dto);

    BearbeiteQuerungsverkehrDTO bean2Dto(final QuerungsverkehrEntity bean);
}
