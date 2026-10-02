package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.pdf.templates.GangliniePdf;
import de.muenchen.dave.domain.pdf.templates.messstelle.GanglinieMessstellePdf;
import de.muenchen.dave.domain.pdf.templates.messstelle.GesamtauswertungMessstellePdf;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DiagrammPdfOptionsMapper {

    // @MappingTarget bewirkt, dass das übergebene GangliniePdf nur "geupdated" wird und kein neues Objekt erzeugt wird.
    @Mapping(target = "zeitauswahl", ignore = true)
    GangliniePdf options2gangliniePdf(@MappingTarget GangliniePdf gangliniePdf, OptionsDTO dto);

    GanglinieMessstellePdf options2gangliniePdf(
            @MappingTarget GanglinieMessstellePdf gangliniePdf, FahrzeugOptionsDTO dto);

    GesamtauswertungMessstellePdf options2gesamtauswertungPdf(
            @MappingTarget GesamtauswertungMessstellePdf gesamtauswertungPdf, FahrzeugOptionsDTO dto);

}
