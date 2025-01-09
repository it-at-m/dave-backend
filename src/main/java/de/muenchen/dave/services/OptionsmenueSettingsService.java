package de.muenchen.dave.services;

import de.muenchen.dave.domain.dtos.OptionsmenueSettingsDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessfaehigkeitDTO;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.mapper.OptionsmenueSettingsMapper;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.repositories.relationaldb.OptionsmenueSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OptionsmenueSettingsService {

    private final OptionsmenueSettingsRepository optionsmenueSettingsRepository;

    private final OptionsmenueSettingsMapper optionsmenueSettingsMapper;

    public OptionsmenueSettingsDTO getByReadMessfaehigkeit(final ReadMessfaehigkeitDTO readMessfaehigkeit) {
        final var fahrzeugklasse = EnumUtils.getEnum(Fahrzeugklasse.class, readMessfaehigkeit.getFahrzeugklassen());
        final var intervall = readMessfaehigkeit.getIntervall();
        return this.getByFahrzeugklasseAndIntervall(fahrzeugklasse, intervall);
    }

    public OptionsmenueSettingsDTO getByFahrzeugklasseAndIntervall(final Fahrzeugklasse fahrzeugklasse, final ZaehldatenIntervall intervall) {
        var optionsmenueSettings = optionsmenueSettingsRepository
                .findByFahrzeugklasseAndIntervall(fahrzeugklasse, intervall)
                .orElseThrow(() -> new ResourceNotFoundException("Die für die Messfähigkeit gesuchten Einstellungen des Optionsmenüs wurden nicht gefunden."));
        return optionsmenueSettingsMapper.toDto(optionsmenueSettings);
    }

}
