package de.muenchen.dave.migrationespg.domain.mapper;

import de.muenchen.dave.migrationespg.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.pdf.helper.ZeitreiheTable;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ZeitreiheTableOptionsMapperMig {

    // @MappingTarget bewirkt, dass das übergebene ZeitreiheTable-Objekt nur "geupdated" wird und kein neues Objekt erzeugt wird.
    ZeitreiheTable options2zeitreiheTable(@MappingTarget ZeitreiheTable zeitreiheTable, OptionsDTO options);
}
