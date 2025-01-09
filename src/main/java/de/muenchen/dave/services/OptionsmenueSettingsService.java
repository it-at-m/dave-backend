package de.muenchen.dave.services;

import de.muenchen.dave.domain.dtos.OptionsmenueSettingsDTO;
import de.muenchen.dave.domain.mapper.OptionsmenueSettingsMapper;
import de.muenchen.dave.repositories.relationaldb.OptionsmenueSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OptionsmenueSettingsService {

    private final OptionsmenueSettingsRepository optionsmenueSettingsRepository;

    private final OptionsmenueSettingsMapper optionsmenueSettingsMapper;

    public List<OptionsmenueSettingsDTO> getAllOptionsmenueSettings() {
        return optionsmenueSettingsRepository
                .findAll()
                .stream()
                .map(optionsmenueSettingsMapper::toDto)
                .toList();
    }

}
