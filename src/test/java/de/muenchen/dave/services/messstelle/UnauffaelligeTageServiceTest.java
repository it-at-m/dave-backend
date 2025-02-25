package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.Kalendertag;
import de.muenchen.dave.domain.UnauffaelligerTag;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.domain.mapper.detektor.MessstelleReceiverMapperImpl;
import de.muenchen.dave.geodateneai.gen.api.MessstelleApi;
import de.muenchen.dave.geodateneai.gen.model.UnauffaelligerTagDto;
import de.muenchen.dave.repositories.relationaldb.KalendertagRepository;
import de.muenchen.dave.repositories.relationaldb.UnauffaelligeTageRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UnauffaelligeTageServiceTest {

    private UnauffaelligeTageService unauffaelligeTageService;

    @Mock
    private UnauffaelligeTageRepository unauffaelligeTageRepository;

    @Mock
    private KalendertagRepository kalendertagRepository;

    @Mock
    private MessstelleApi messstelleApi;

    @BeforeEach
    void beforeEach() {
        this.unauffaelligeTageService = new UnauffaelligeTageService(
                unauffaelligeTageRepository,
                kalendertagRepository,
                new MessstelleReceiverMapperImpl(),
                messstelleApi);
        Mockito.reset(unauffaelligeTageRepository, kalendertagRepository, messstelleApi);
    }

    @Test
    void mapDto2Entity() {
        final var unauffaelligerTagDto = new UnauffaelligerTagDto();
        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 25));
        unauffaelligerTagDto.setMstId(1234);

        final var kalendertag = new Kalendertag();
        kalendertag.setDatum(LocalDate.of(2025, 2, 25));
        kalendertag.setTagestyp(TagesTyp.MO_SO);

        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 25))).thenReturn(Optional.of(kalendertag));

        final var result = unauffaelligeTageService.mapDto2Entity(unauffaelligerTagDto);

        final var expected = new UnauffaelligerTag();
        expected.setMstId(1234);
        expected.setKalendertag(kalendertag);

        Assertions.assertEquals(expected, result);

        Mockito.verify(kalendertagRepository, Mockito.times(1)).findByDatum(LocalDate.of(2025, 2, 25));

    }

    @Test
    void mapDto2EntityNoKalendertagFound() {
        final var unauffaelligerTagDto = new UnauffaelligerTagDto();
        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 25));
        unauffaelligerTagDto.setMstId(1234);

        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 25))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> unauffaelligeTageService.mapDto2Entity(unauffaelligerTagDto));

        Mockito.verify(kalendertagRepository, Mockito.times(1)).findByDatum(LocalDate.of(2025, 2, 25));
    }
}
