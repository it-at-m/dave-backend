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
import net.javacrumbs.shedlock.core.LockAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    void loadMessstellenCron() {
        final var unauffaelligeTageServiceSpy = Mockito.spy(this.unauffaelligeTageService);

        final var kalendertag20250202 = new Kalendertag();
        kalendertag20250202.setDatum(LocalDate.of(2025, 2, 2));
        kalendertag20250202.setTagestyp(TagesTyp.MO_SO);

        final var kalendertag20250203 = new Kalendertag();
        kalendertag20250203.setDatum(LocalDate.of(2025, 2, 3));
        kalendertag20250203.setTagestyp(TagesTyp.MO_SO);

        final var unauffaelligeTage = new ArrayList<UnauffaelligerTag>();
        var unauffaelligerTag = new UnauffaelligerTag();
        unauffaelligerTag.setMstId(1234);
        unauffaelligerTag.setKalendertag(kalendertag20250202);
        unauffaelligeTage.add(unauffaelligerTag);
        unauffaelligerTag = new UnauffaelligerTag();
        unauffaelligerTag.setMstId(1234);
        unauffaelligerTag.setKalendertag(kalendertag20250203);
        unauffaelligeTage.add(unauffaelligerTag);
        unauffaelligerTag = new UnauffaelligerTag();
        unauffaelligerTag.setMstId(4321);
        unauffaelligerTag.setKalendertag(kalendertag20250203);
        unauffaelligeTage.add(unauffaelligerTag);

        Mockito.doReturn(unauffaelligeTage).when(unauffaelligeTageServiceSpy).loadUnauffaelligeTageForEachMessstelle();

        LockAssert.TestHelper.makeAllAssertsPass(true);
        unauffaelligeTageServiceSpy.loadMessstellenCron();
        LockAssert.TestHelper.makeAllAssertsPass(false);

        Mockito.verify(unauffaelligeTageServiceSpy, Mockito.times(1)).loadUnauffaelligeTageForEachMessstelle();

        Mockito.verify(unauffaelligeTageRepository, Mockito.times(1)).saveAllAndFlush(unauffaelligeTage);
    }

    @Test
    void loadUnauffaelligeTageForEachMessstelleWithDataInDatabase() {
        final var kalenderTagForYoungestSavedUnauffaelligerTag = new Kalendertag();
        kalenderTagForYoungestSavedUnauffaelligerTag.setDatum(LocalDate.of(2025, 2, 1));

        final var youngestSavedUnauffaelligerTag = new UnauffaelligerTag();
        youngestSavedUnauffaelligerTag.setKalendertag(kalenderTagForYoungestSavedUnauffaelligerTag);
        youngestSavedUnauffaelligerTag.setMstId(1234);

        Mockito.when(unauffaelligeTageRepository.findTopByOrderByDatumDesc()).thenReturn(Optional.of(youngestSavedUnauffaelligerTag));

        final var unauffaelligeTage = new ArrayList<UnauffaelligerTagDto>();
        var unauffaelligerTagDto = new UnauffaelligerTagDto();
        unauffaelligerTagDto.setMstId(1234);
        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 2));
        unauffaelligeTage.add(unauffaelligerTagDto);
        unauffaelligerTagDto = new UnauffaelligerTagDto();
        unauffaelligerTagDto.setMstId(1234);
        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 3));
        unauffaelligeTage.add(unauffaelligerTagDto);
        unauffaelligerTagDto = new UnauffaelligerTagDto();
        unauffaelligerTagDto.setMstId(4321);
        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 3));
        unauffaelligeTage.add(unauffaelligerTagDto);
        final var mobidamResponseEntity = ResponseEntity.of(Optional.of((List<UnauffaelligerTagDto>) unauffaelligeTage));
        final var mono = Mono.just(mobidamResponseEntity);
        Mockito.when(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(LocalDate.of(2025, 2, 2), LocalDate.now().minusDays(1))).thenReturn(mono);

        final var kalendertag20250202 = new Kalendertag();
        kalendertag20250202.setDatum(LocalDate.of(2025, 2, 2));
        kalendertag20250202.setTagestyp(TagesTyp.MO_SO);
        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 2))).thenReturn(Optional.of(kalendertag20250202));

        final var kalendertag20250203 = new Kalendertag();
        kalendertag20250203.setDatum(LocalDate.of(2025, 2, 3));
        kalendertag20250203.setTagestyp(TagesTyp.MO_SO);
        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 3))).thenReturn(Optional.of(kalendertag20250203));

        final var result = unauffaelligeTageService.loadUnauffaelligeTageForEachMessstelle();

        final var expected = new ArrayList<UnauffaelligerTag>();
        var unauffaelligerTag = new UnauffaelligerTag();
        unauffaelligerTag.setMstId(1234);
        unauffaelligerTag.setKalendertag(kalendertag20250202);
        expected.add(unauffaelligerTag);
        unauffaelligerTag = new UnauffaelligerTag();
        unauffaelligerTag.setMstId(1234);
        unauffaelligerTag.setKalendertag(kalendertag20250203);
        expected.add(unauffaelligerTag);
        unauffaelligerTag = new UnauffaelligerTag();
        unauffaelligerTag.setMstId(4321);
        unauffaelligerTag.setKalendertag(kalendertag20250203);
        expected.add(unauffaelligerTag);

        Assertions.assertEquals(expected, result);

        Mockito.verify(messstelleApi, Mockito.times(1))
                .getUnauffaelligeTageForEachMessstelleWithHttpInfo(LocalDate.of(2025, 2, 2), LocalDate.now().minusDays(1));

        Mockito.verify(kalendertagRepository, Mockito.times(1))
                .findByDatum(LocalDate.of(2025, 2, 2));

        Mockito.verify(kalendertagRepository, Mockito.times(2))
                .findByDatum(LocalDate.of(2025, 2, 3));
    }

    @Test
    void loadUnauffaelligeTageForEachMessstelleWithoutDataInDatabase() {
        Mockito.when(unauffaelligeTageRepository.findTopByOrderByDatumDesc()).thenReturn(Optional.empty());

        final var unauffaelligeTage = new ArrayList<UnauffaelligerTagDto>();
        var unauffaelligerTagDto = new UnauffaelligerTagDto();
        unauffaelligerTagDto.setMstId(1234);
        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 2));
        unauffaelligeTage.add(unauffaelligerTagDto);
        unauffaelligerTagDto = new UnauffaelligerTagDto();
        unauffaelligerTagDto.setMstId(1234);
        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 3));
        unauffaelligeTage.add(unauffaelligerTagDto);
        unauffaelligerTagDto = new UnauffaelligerTagDto();
        unauffaelligerTagDto.setMstId(4321);
        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 3));
        unauffaelligeTage.add(unauffaelligerTagDto);
        final var mobidamResponseEntity = ResponseEntity.of(Optional.of((List<UnauffaelligerTagDto>) unauffaelligeTage));
        final var mono = Mono.just(mobidamResponseEntity);
        Mockito.when(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(LocalDate.of(2006, 1, 1), LocalDate.now().minusDays(1))).thenReturn(mono);

        final var kalendertag20250202 = new Kalendertag();
        kalendertag20250202.setDatum(LocalDate.of(2025, 2, 2));
        kalendertag20250202.setTagestyp(TagesTyp.MO_SO);
        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 2))).thenReturn(Optional.of(kalendertag20250202));

        final var kalendertag20250203 = new Kalendertag();
        kalendertag20250203.setDatum(LocalDate.of(2025, 2, 3));
        kalendertag20250203.setTagestyp(TagesTyp.MO_SO);
        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 3))).thenReturn(Optional.of(kalendertag20250203));

        final var result = unauffaelligeTageService.loadUnauffaelligeTageForEachMessstelle();

        final var expected = new ArrayList<UnauffaelligerTag>();
        var unauffaelligerTag = new UnauffaelligerTag();
        unauffaelligerTag.setMstId(1234);
        unauffaelligerTag.setKalendertag(kalendertag20250202);
        expected.add(unauffaelligerTag);
        unauffaelligerTag = new UnauffaelligerTag();
        unauffaelligerTag.setMstId(1234);
        unauffaelligerTag.setKalendertag(kalendertag20250203);
        expected.add(unauffaelligerTag);
        unauffaelligerTag = new UnauffaelligerTag();
        unauffaelligerTag.setMstId(4321);
        unauffaelligerTag.setKalendertag(kalendertag20250203);
        expected.add(unauffaelligerTag);

        Assertions.assertEquals(expected, result);

        Mockito.verify(messstelleApi, Mockito.times(1))
                .getUnauffaelligeTageForEachMessstelleWithHttpInfo(LocalDate.of(2006, 1, 1), LocalDate.now().minusDays(1));

        Mockito.verify(kalendertagRepository, Mockito.times(1))
                .findByDatum(LocalDate.of(2025, 2, 2));

        Mockito.verify(kalendertagRepository, Mockito.times(2))
                .findByDatum(LocalDate.of(2025, 2, 3));
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
