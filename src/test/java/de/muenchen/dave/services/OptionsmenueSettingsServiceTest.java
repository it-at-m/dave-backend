package de.muenchen.dave.services;

import de.muenchen.dave.domain.OptionsmenueSettings;
import de.muenchen.dave.domain.dtos.OptionsmenueSettingsDTO;
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
    void getByReadMessfaehigkeit() {
        final var optionsmenueSettings1 = new OptionsmenueSettings();
        optionsmenueSettings1.setIntervall(ZaehldatenIntervall.STUNDE_KOMPLETT);
        optionsmenueSettings1.setFahrzeugklasse(Fahrzeugklasse.ACHT_PLUS_EINS);
        optionsmenueSettings1.setKraftfahrzeugverkehrChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_HALB, ZaehldatenIntervall.STUNDE_KOMPLETT));

        final var optionsmenueSettings2 = new OptionsmenueSettings();
        optionsmenueSettings2.setIntervall(ZaehldatenIntervall.STUNDE_HALB);
        optionsmenueSettings2.setFahrzeugklasse(Fahrzeugklasse.ACHT_PLUS_EINS);
        optionsmenueSettings2.setKraftfahrzeugverkehrChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_VIERTEL));

        final var optionsmenueSettings3 = new OptionsmenueSettings();
        optionsmenueSettings3.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL_EINGESCHRAENKT);
        optionsmenueSettings3.setFahrzeugklasse(Fahrzeugklasse.ZWEI_PLUS_EINS);
        optionsmenueSettings3.setBusseChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_VIERTEL));

        final var optionsmenueSettings4 = new OptionsmenueSettings();
        optionsmenueSettings4.setIntervall(null);
        optionsmenueSettings4.setFahrzeugklasse(null);
        optionsmenueSettings4.setRadverkehrChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_HALB));
        optionsmenueSettings4.setLastkraftwagenChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_HALB));

        Mockito.when(optionsmenueSettingsRepository.findAll())
                .thenReturn(List.of(optionsmenueSettings1, optionsmenueSettings2, optionsmenueSettings3, optionsmenueSettings4));

        final var expectedOptionsmenueSettings1 = new OptionsmenueSettingsDTO();
        expectedOptionsmenueSettings1.setIntervall(ZaehldatenIntervall.STUNDE_KOMPLETT);
        expectedOptionsmenueSettings1.setFahrzeugklasse(Fahrzeugklasse.ACHT_PLUS_EINS);
        expectedOptionsmenueSettings1.setKraftfahrzeugverkehrChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_HALB, ZaehldatenIntervall.STUNDE_KOMPLETT));

        final var expectedOptionsmenueSettings2 = new OptionsmenueSettingsDTO();
        expectedOptionsmenueSettings2.setIntervall(ZaehldatenIntervall.STUNDE_HALB);
        expectedOptionsmenueSettings2.setFahrzeugklasse(Fahrzeugklasse.ACHT_PLUS_EINS);
        expectedOptionsmenueSettings2.setKraftfahrzeugverkehrChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_VIERTEL));

        final var expectedOptionsmenueSettings3 = new OptionsmenueSettingsDTO();
        expectedOptionsmenueSettings3.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL_EINGESCHRAENKT);
        expectedOptionsmenueSettings3.setFahrzeugklasse(Fahrzeugklasse.ZWEI_PLUS_EINS);
        expectedOptionsmenueSettings3.setBusseChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_VIERTEL));

        final var expectedOptionsmenueSettings4 = new OptionsmenueSettingsDTO();
        expectedOptionsmenueSettings4.setIntervall(null);
        expectedOptionsmenueSettings4.setFahrzeugklasse(null);
        expectedOptionsmenueSettings4.setRadverkehrChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_HALB));
        expectedOptionsmenueSettings4.setLastkraftwagenChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_HALB));

        final var result = optionsmenueSettingsService.getAllOptionsmenueSettingsForMessstellen();

        Assertions.assertThat(result)
                .isEqualTo(List.of(expectedOptionsmenueSettings1, expectedOptionsmenueSettings2, expectedOptionsmenueSettings3, expectedOptionsmenueSettings4));

        Mockito.verify(optionsmenueSettingsRepository, Mockito.times(1)).findAll();
    }
}
