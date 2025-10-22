package de.muenchen.dave.services;

import de.muenchen.dave.configuration.CachingConfiguration;
import de.muenchen.dave.domain.dtos.OptionsmenueSettingsDTO;
import de.muenchen.dave.domain.mapper.OptionsmenueSettingsMapper;
import de.muenchen.dave.repositories.relationaldb.OptionsmenueSettingsRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OptionsmenueSettingsService {

    private final OptionsmenueSettingsRepository optionsmenueSettingsRepository;

    private final OptionsmenueSettingsMapper optionsmenueSettingsMapper;

    @Cacheable(value = CachingConfiguration.OPTIONSMENUE_SETTINGS_FOR_MESSSTELLEN)
    public List<OptionsmenueSettingsDTO> getAllOptionsmenueSettingsForMessstellen() {
        return optionsmenueSettingsRepository
                .findAll()
                .stream()
                .map(optionsmenueSettingsMapper::toDto)
                .toList();
    }
}
