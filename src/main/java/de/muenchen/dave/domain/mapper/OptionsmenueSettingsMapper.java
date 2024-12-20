package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.OptionsmenueSettings;
import de.muenchen.dave.domain.dtos.OptionsmenueSettingsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OptionsmenueSettingsMapper {

    OptionsmenueSettingsDTO toDto(final OptionsmenueSettings optionsmenueSettings);


}
