package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.pdf.templates.GangliniePdf;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DiagrammPdfOptionsMapper {

    // @MappingTarget bewirkt, dass das übergebene GangliniePdf nur "geupdated" wird und kein neues Objekt erzeugt wird.
    GangliniePdf options2gangliniePdf(@MappingTarget GangliniePdf gangliniePdf, OptionsDTO dto);

    de.muenchen.dave.domain.pdf.templates.messstelle.GangliniePdf options2gangliniePdf(
            @MappingTarget de.muenchen.dave.domain.pdf.templates.messstelle.GangliniePdf gangliniePdf, FahrzeugOptionsDTO dto);

    de.muenchen.dave.domain.pdf.templates.messstelle.GesamtauswertungPdf options2gesamtauswertungPdf(
            @MappingTarget de.muenchen.dave.domain.pdf.templates.messstelle.GesamtauswertungPdf gesamtauswertungPdf, FahrzeugOptionsDTO dto);

}
