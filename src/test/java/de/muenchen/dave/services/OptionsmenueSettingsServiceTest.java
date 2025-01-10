package de.muenchen.dave.services;

import de.muenchen.dave.domain.OptionsmenueSettings;
import de.muenchen.dave.domain.dtos.OptionsmenueSettingsDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessfaehigkeitDTO;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.mapper.OptionsmenueSettingsMapperImpl;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
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
import java.util.Optional;

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
        final var optionsmenueSettings = new OptionsmenueSettings();
        optionsmenueSettings.setFahrzeugklasse(Fahrzeugklasse.ACHT_PLUS_EINS);
        optionsmenueSettings.setIntervall(ZaehldatenIntervall.STUNDE_KOMPLETT);
        optionsmenueSettings.setKraftfahrzeugverkehrChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_KOMPLETT, ZaehldatenIntervall.STUNDE_HALB));

        final var readMessfaehigkeit = new ReadMessfaehigkeitDTO();
        readMessfaehigkeit.setIntervall(ZaehldatenIntervall.STUNDE_KOMPLETT);
        readMessfaehigkeit.setFahrzeugklassen(Fahrzeugklasse.ACHT_PLUS_EINS);

        Mockito
                .when(optionsmenueSettingsRepository.findByFahrzeugklasseAndIntervall(Fahrzeugklasse.ACHT_PLUS_EINS, ZaehldatenIntervall.STUNDE_KOMPLETT))
                .thenReturn(Optional.of(optionsmenueSettings));

        final var result = optionsmenueSettingsService.getByReadMessfaehigkeit(readMessfaehigkeit);

        final var expected = new OptionsmenueSettingsDTO();
        expected.setFahrzeugklasse(Fahrzeugklasse.ACHT_PLUS_EINS);
        expected.setIntervall(ZaehldatenIntervall.STUNDE_KOMPLETT);
        expected.setKraftfahrzeugverkehrChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_KOMPLETT, ZaehldatenIntervall.STUNDE_HALB));

        Assertions.assertThat(result).isEqualTo(expected);

        Mockito
                .verify(optionsmenueSettingsRepository, Mockito.times(1))
                .findByFahrzeugklasseAndIntervall(Fahrzeugklasse.ACHT_PLUS_EINS, ZaehldatenIntervall.STUNDE_KOMPLETT);
    }

    @Test
    void getByReadMessfaehigkeitWithDefaultOptionsmenueSettings() {
        final var readMessfaehigkeit = new ReadMessfaehigkeitDTO();
        readMessfaehigkeit.setIntervall(ZaehldatenIntervall.STUNDE_KOMPLETT);
        readMessfaehigkeit.setFahrzeugklassen(Fahrzeugklasse.ACHT_PLUS_EINS);

        Mockito
                .when(optionsmenueSettingsRepository.findByFahrzeugklasseAndIntervall(Fahrzeugklasse.ACHT_PLUS_EINS, ZaehldatenIntervall.STUNDE_KOMPLETT))
                .thenReturn(Optional.empty());

        final var defaultOptionsmenueSettings = new OptionsmenueSettings();
        defaultOptionsmenueSettings.setBusseChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_HALB));
        Mockito
                .when(optionsmenueSettingsRepository.findByFahrzeugklasseIsNullAndIntervallIsNull())
                .thenReturn(Optional.of(defaultOptionsmenueSettings));

        final var result = optionsmenueSettingsService.getByReadMessfaehigkeit(readMessfaehigkeit);

        final var expected = new OptionsmenueSettingsDTO();
        expected.setBusseChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_HALB));

        Assertions.assertThat(result).isEqualTo(expected);

        Mockito
                .verify(optionsmenueSettingsRepository, Mockito.times(1))
                .findByFahrzeugklasseAndIntervall(Fahrzeugklasse.ACHT_PLUS_EINS, ZaehldatenIntervall.STUNDE_KOMPLETT);

        Mockito
                .verify(optionsmenueSettingsRepository, Mockito.times(1))
                .findByFahrzeugklasseIsNullAndIntervallIsNull();
    }

    @Test
    void getByReadMessfaehigkeitExceptionBecauseNoDefaultOptionsmenueSettingsIsDefined() {
        final var readMessfaehigkeit = new ReadMessfaehigkeitDTO();
        readMessfaehigkeit.setIntervall(ZaehldatenIntervall.STUNDE_KOMPLETT);
        readMessfaehigkeit.setFahrzeugklassen(Fahrzeugklasse.ACHT_PLUS_EINS);

        Mockito
                .when(optionsmenueSettingsRepository.findByFahrzeugklasseAndIntervall(Fahrzeugklasse.ACHT_PLUS_EINS, ZaehldatenIntervall.STUNDE_KOMPLETT))
                .thenReturn(Optional.empty());

        Mockito
                .when(optionsmenueSettingsRepository.findByFahrzeugklasseIsNullAndIntervallIsNull())
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(() -> optionsmenueSettingsService.getByReadMessfaehigkeit(readMessfaehigkeit))
                .isInstanceOf(ResourceNotFoundException.class);

        Mockito
                .verify(optionsmenueSettingsRepository, Mockito.times(1))
                .findByFahrzeugklasseAndIntervall(Fahrzeugklasse.ACHT_PLUS_EINS, ZaehldatenIntervall.STUNDE_KOMPLETT);

        Mockito
                .verify(optionsmenueSettingsRepository, Mockito.times(1))
                .findByFahrzeugklasseIsNullAndIntervallIsNull();
    }

    @Test
    void getByFahrzeugklasseAndIntervall() {
        final var optionsmenueSettings = new OptionsmenueSettings();
        optionsmenueSettings.setFahrzeugklasse(Fahrzeugklasse.ACHT_PLUS_EINS);
        optionsmenueSettings.setIntervall(ZaehldatenIntervall.STUNDE_KOMPLETT);
        optionsmenueSettings.setKraftfahrzeugverkehrChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_KOMPLETT, ZaehldatenIntervall.STUNDE_HALB));

        Mockito
                .when(optionsmenueSettingsRepository.findByFahrzeugklasseAndIntervall(Fahrzeugklasse.ACHT_PLUS_EINS, ZaehldatenIntervall.STUNDE_KOMPLETT))
                .thenReturn(Optional.of(optionsmenueSettings));

        final var result = optionsmenueSettingsService.getByFahrzeugklasseAndIntervall(Fahrzeugklasse.ACHT_PLUS_EINS, ZaehldatenIntervall.STUNDE_KOMPLETT);

        final var expected = new OptionsmenueSettingsDTO();
        expected.setFahrzeugklasse(Fahrzeugklasse.ACHT_PLUS_EINS);
        expected.setIntervall(ZaehldatenIntervall.STUNDE_KOMPLETT);
        expected.setKraftfahrzeugverkehrChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_KOMPLETT, ZaehldatenIntervall.STUNDE_HALB));

        Assertions.assertThat(result).isEqualTo(expected);

        Mockito
                .verify(optionsmenueSettingsRepository, Mockito.times(1))
                .findByFahrzeugklasseAndIntervall(Fahrzeugklasse.ACHT_PLUS_EINS, ZaehldatenIntervall.STUNDE_KOMPLETT);
    }

    @Test
    void getByFahrzeugklasseAndIntervallWithDefaultOptionsmenueSettings() {
        Mockito
                .when(optionsmenueSettingsRepository.findByFahrzeugklasseAndIntervall(Fahrzeugklasse.ACHT_PLUS_EINS, ZaehldatenIntervall.STUNDE_KOMPLETT))
                .thenReturn(Optional.empty());

        final var defaultOptionsmenueSettings = new OptionsmenueSettings();
        defaultOptionsmenueSettings.setBusseChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_HALB));
        Mockito
                .when(optionsmenueSettingsRepository.findByFahrzeugklasseIsNullAndIntervallIsNull())
                .thenReturn(Optional.of(defaultOptionsmenueSettings));

        final var result = optionsmenueSettingsService.getByFahrzeugklasseAndIntervall(Fahrzeugklasse.ACHT_PLUS_EINS, ZaehldatenIntervall.STUNDE_KOMPLETT);

        final var expected = new OptionsmenueSettingsDTO();
        expected.setBusseChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_HALB));

        Assertions.assertThat(result).isEqualTo(expected);

        Mockito
                .verify(optionsmenueSettingsRepository, Mockito.times(1))
                .findByFahrzeugklasseAndIntervall(Fahrzeugklasse.ACHT_PLUS_EINS, ZaehldatenIntervall.STUNDE_KOMPLETT);

        Mockito
                .verify(optionsmenueSettingsRepository, Mockito.times(1))
                .findByFahrzeugklasseIsNullAndIntervallIsNull();
    }

    @Test
    void getByFahrzeugklasseAndIntervallExceptionBecauseNoDefaultOptionsmenueSettingsIsDefined() {
        Mockito
                .when(optionsmenueSettingsRepository.findByFahrzeugklasseAndIntervall(Fahrzeugklasse.ACHT_PLUS_EINS, ZaehldatenIntervall.STUNDE_KOMPLETT))
                .thenReturn(Optional.empty());

        Mockito
                .when(optionsmenueSettingsRepository.findByFahrzeugklasseIsNullAndIntervallIsNull())
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(
                        () -> optionsmenueSettingsService.getByFahrzeugklasseAndIntervall(Fahrzeugklasse.ACHT_PLUS_EINS, ZaehldatenIntervall.STUNDE_KOMPLETT))
                .isInstanceOf(ResourceNotFoundException.class);

        Mockito
                .verify(optionsmenueSettingsRepository, Mockito.times(1))
                .findByFahrzeugklasseAndIntervall(Fahrzeugklasse.ACHT_PLUS_EINS, ZaehldatenIntervall.STUNDE_KOMPLETT);

        Mockito
                .verify(optionsmenueSettingsRepository, Mockito.times(1))
                .findByFahrzeugklasseIsNullAndIntervallIsNull();
    }

    @Test
    void getDefaultOptionsmenueSettings() {
        final var defaultOptionsmenueSettings = new OptionsmenueSettings();
        defaultOptionsmenueSettings.setBusseChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_HALB));
        Mockito
                .when(optionsmenueSettingsRepository.findByFahrzeugklasseIsNullAndIntervallIsNull())
                .thenReturn(Optional.of(defaultOptionsmenueSettings));

        final var result = optionsmenueSettingsService.getDefaultOptionsmenueSettings();

        final var expected = new OptionsmenueSettings();
        expected.setBusseChoosableIntervals(List.of(ZaehldatenIntervall.STUNDE_HALB));

        Assertions.assertThat(result).isEqualTo(expected);

        Mockito
                .verify(optionsmenueSettingsRepository, Mockito.times(1))
                .findByFahrzeugklasseIsNullAndIntervallIsNull();
    }

    @Test
    void getDefaultOptionsmenueSettingsExceptionBecauseNoDefaultOptionsmenueSettingsIsDefined() {
        Mockito
                .when(optionsmenueSettingsRepository.findByFahrzeugklasseIsNullAndIntervallIsNull())
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(
                        () -> optionsmenueSettingsService.getDefaultOptionsmenueSettings())
                .isInstanceOf(ResourceNotFoundException.class);

        Mockito
                .verify(optionsmenueSettingsRepository, Mockito.times(1))
                .findByFahrzeugklasseIsNullAndIntervallIsNull();
    }
}
