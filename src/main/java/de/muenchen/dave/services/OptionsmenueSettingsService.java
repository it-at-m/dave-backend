package de.muenchen.dave.services;

import de.muenchen.dave.domain.dtos.OptionsmenueSettingsDTO;
import de.muenchen.dave.domain.dtos.OptionsmenueSettingsKeyDTO;
import de.muenchen.dave.domain.mapper.OptionsmenueSettingsMapper;
import de.muenchen.dave.repositories.relationaldb.OptionsmenueSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OptionsmenueSettingsService {

    private final OptionsmenueSettingsRepository optionsmenueSettingsRepository;

    private final OptionsmenueSettingsMapper optionsmenueSettingsMapper;

    public Map<OptionsmenueSettingsKeyDTO, OptionsmenueSettingsDTO> getOptionsmenueSettings() {
        return optionsmenueSettingsRepository
                .findAll()
                .stream()
                .map(optionsmenueSettingsMapper::toDto)
                .collect(Collectors.toMap(OptionsmenueSettingsDTO::getFahrzeugklassenAndIntervall, Function.identity()));
    }


}
