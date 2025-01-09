package de.muenchen.dave.services;

import de.muenchen.dave.domain.OptionsmenueSettings;
import de.muenchen.dave.domain.OptionsmenueSettingsKey;
import de.muenchen.dave.domain.dtos.OptionsmenueSettingsDTO;
import de.muenchen.dave.domain.dtos.OptionsmenueSettingsKeyDTO;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.mapper.OptionsmenueSettingsMapperImpl;
import de.muenchen.dave.repositories.relationaldb.OptionsmenueSettingsRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OptionsmenueSettingsServiceTest {

    private OptionsmenueSettingsService optionsmenueSettingsService;

    @Mock
    private OptionsmenueSettingsRepository optionsmenueSettingsRepository;

    @BeforeEach
    void setup() {
        optionsmenueSettingsService = new OptionsmenueSettingsService(optionsmenueSettingsRepository, new OptionsmenueSettingsMapperImpl());
        Mockito.reset(optionsmenueSettingsRepository);
    }

    @Test
    void getAllOptionsmenueSettings() {

        var optionsmenueSettingsKey1 = new OptionsmenueSettingsKey();
        optionsmenueSettingsKey1.setFahrzeugklasse(Fahrzeugklasse.ACHT_PLUS_EINS);
        optionsmenueSettingsKey1.setIntervall(ZaehldatenIntervall.STUNDE_KOMPLETT);
        var optionsmenueSettings1 = new OptionsmenueSettings();
        optionsmenueSettings1.setFahrzeugklassenAndIntervall(optionsmenueSettingsKey1);
        optionsmenueSettings1.setKraftfahrzeugverkehrChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_KOMPLETT, ZaehldatenIntervall.STUNDE_HALB));

        var optionsmenueSettingsKey2 = new OptionsmenueSettingsKey();
        optionsmenueSettingsKey2.setFahrzeugklasse(Fahrzeugklasse.ZWEI_PLUS_EINS);
        optionsmenueSettingsKey2.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL_EINGESCHRAENKT);
        var optionsmenueSettings2 = new OptionsmenueSettings();
        optionsmenueSettings2.setFahrzeugklassenAndIntervall(optionsmenueSettingsKey2);
        optionsmenueSettings2.setKraftraederChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_KOMPLETT));
        optionsmenueSettings2.setRadverkehrChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_VIERTEL));

        Mockito.when(optionsmenueSettingsRepository.findAll()).thenReturn(List.of(optionsmenueSettings1, optionsmenueSettings2));

        final var result = optionsmenueSettingsService.getAllOptionsmenueSettings();

        var optionsmenueSettingsKeyDto1 = new OptionsmenueSettingsKeyDTO();
        optionsmenueSettingsKeyDto1.setFahrzeugklasse(Fahrzeugklasse.ACHT_PLUS_EINS);
        optionsmenueSettingsKeyDto1.setIntervall(ZaehldatenIntervall.STUNDE_KOMPLETT);
        var optionsmenueSettingsDto1 = new OptionsmenueSettingsDTO();
        optionsmenueSettingsDto1.setFahrzeugklassenAndIntervall(optionsmenueSettingsKeyDto1);
        optionsmenueSettingsDto1.setKraftfahrzeugverkehrChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_KOMPLETT, ZaehldatenIntervall.STUNDE_HALB));

        var optionsmenueSettingsKeyDto2 = new OptionsmenueSettingsKeyDTO();
        optionsmenueSettingsKeyDto2.setFahrzeugklasse(Fahrzeugklasse.ZWEI_PLUS_EINS);
        optionsmenueSettingsKeyDto2.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL_EINGESCHRAENKT);
        var optionsmenueSettingsDto2 = new OptionsmenueSettingsDTO();
        optionsmenueSettingsDto2.setFahrzeugklassenAndIntervall(optionsmenueSettingsKeyDto2);
        optionsmenueSettingsDto2.setKraftraederChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_KOMPLETT));
        optionsmenueSettingsDto2.setRadverkehrChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_VIERTEL));

        final var expected = List.of(optionsmenueSettingsDto1, optionsmenueSettingsDto2);

        Assertions.assertThat(result)
                .isNotEmpty()
                .containsExactlyInAnyOrderElementsOf(expected);
    }
}
