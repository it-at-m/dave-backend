package de.muenchen.dave.services;

import de.muenchen.dave.domain.OptionsmenueSettings;
import de.muenchen.dave.domain.dtos.OptionsmenueSettingsDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessfaehigkeitDTO;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.mapper.OptionsmenueSettingsMapper;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.repositories.relationaldb.OptionsmenueSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OptionsmenueSettingsService {

    private final OptionsmenueSettingsRepository optionsmenueSettingsRepository;

    private final OptionsmenueSettingsMapper optionsmenueSettingsMapper;

    public OptionsmenueSettingsDTO getByReadMessfaehigkeit(final ReadMessfaehigkeitDTO readMessfaehigkeit) {
        final var fahrzeugklasse = readMessfaehigkeit.getFahrzeugklassen();
        final var intervall = readMessfaehigkeit.getIntervall();
        return this.getByFahrzeugklasseAndIntervall(fahrzeugklasse, intervall);
    }

    public OptionsmenueSettingsDTO getByFahrzeugklasseAndIntervall(final Fahrzeugklasse fahrzeugklasse, final ZaehldatenIntervall intervall) {
        var optionsmenueSettings = optionsmenueSettingsRepository
                .findByFahrzeugklasseAndIntervall(fahrzeugklasse, intervall)
                .orElseGet(() -> {
                    log.error(
                            "Die für die Messfähigkeit gesuchten Einstellungen des Optionsmenüs wurden nicht gefunden. Es wird stattdessen ein Fluchtwert zurückgegeben.");
                    return this.getDefaultOptionsmenueSettings();
                });
        return optionsmenueSettingsMapper.toDto(optionsmenueSettings);
    }

    protected OptionsmenueSettings getDefaultOptionsmenueSettings() {
        return optionsmenueSettingsRepository
                .findByFahrzeugklasseIsNullAndIntervallIsNull()
                .orElseThrow(() -> new ResourceNotFoundException("Der Fluchtwert für Einstellungen des Optionsmenüs wurde nicht gefunden."));
    }
}
