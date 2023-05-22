package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.pdf.templates.GangliniePdf;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GangliniePdfOptionsMapper {

    // @MappingTarget bewirkt, dass das übergebene GangliniePdf nur "geupdated" wird und kein neues Objekt erzeugt wird.
    GangliniePdf options2gangliniePdf(@MappingTarget GangliniePdf gangliniePdf, OptionsDTO dto);

}
