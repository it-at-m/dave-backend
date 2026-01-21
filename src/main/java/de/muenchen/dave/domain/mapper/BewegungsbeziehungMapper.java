package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteBewegungsbeziehungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.elasticsearch.Bewegungsbeziehung;
import de.muenchen.dave.domain.elasticsearch.Laengsverkehr;
import de.muenchen.dave.domain.elasticsearch.Querungsverkehr;
import de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.SubclassMapping;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BewegungsbeziehungMapper {

    @SubclassMapping(source = BearbeiteVerkehrsbeziehungDTO.class, target = Verkehrsbeziehung.class)
    @SubclassMapping(source = BearbeiteLaengsverkehrDTO.class, target = Laengsverkehr.class)
    @SubclassMapping(source = BearbeiteQuerungsverkehrDTO.class, target = Querungsverkehr.class)
    Bewegungsbeziehung dtoBewegungsbeziehung2BeanBewegungsbeziehung(final BearbeiteBewegungsbeziehungDTO dto);

    @SubclassMapping(source = Verkehrsbeziehung.class, target = BearbeiteVerkehrsbeziehungDTO.class)
    @SubclassMapping(source = Laengsverkehr.class, target = BearbeiteLaengsverkehrDTO.class)
    @SubclassMapping(source = Querungsverkehr.class, target = BearbeiteQuerungsverkehrDTO.class)
    BearbeiteBewegungsbeziehungDTO beanBewegungsbeziehung2DtoBewegungsbeziehung(final Bewegungsbeziehung dto);

    Verkehrsbeziehung dto2Bean(final BearbeiteVerkehrsbeziehungDTO dto);

    BearbeiteVerkehrsbeziehungDTO bean2Dto(final Verkehrsbeziehung bean);

    Laengsverkehr dto2Bean(final BearbeiteLaengsverkehrDTO dto);

    BearbeiteLaengsverkehrDTO bean2Dto(final Laengsverkehr bean);

    Querungsverkehr dto2Bean(final BearbeiteQuerungsverkehrDTO dto);

    BearbeiteQuerungsverkehrDTO bean2Dto(final Querungsverkehr bean);
}
