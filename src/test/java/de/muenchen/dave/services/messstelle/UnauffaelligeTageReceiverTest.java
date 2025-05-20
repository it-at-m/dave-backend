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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UnauffaelligeTageReceiverTest {

    private UnauffaelligeTageReceiver unauffaelligeTageReceiver;

    @Mock
    private UnauffaelligeTageRepository unauffaelligeTageRepository;

    @Mock
    private KalendertagRepository kalendertagRepository;

    @Mock
    private MessstelleApi messstelleApi;

    @Mock
    private MessstelleService messstelleService;

    @BeforeEach
    void beforeEach() {
        this.unauffaelligeTageReceiver = new UnauffaelligeTageReceiver(
                unauffaelligeTageRepository,
                kalendertagRepository,
                new MessstelleReceiverMapperImpl(),
                messstelleApi,
                messstelleService);
        Mockito.reset(unauffaelligeTageRepository, kalendertagRepository, messstelleApi);
    }

    @Test
    void loadUnauffaelligeTageCron() {
        final var unauffaelligeTageServiceSpy = Mockito.spy(this.unauffaelligeTageReceiver);
        final var today = new Kalendertag();
        today.setDatum(LocalDate.now());
        today.setNextStartDateToLoadUnauffaelligeTage(null);
        Mockito.when(kalendertagRepository.findByNextStartDateToLoadUnauffaelligeTageIsTrue()).thenReturn(Optional.of(today));

        LockAssert.TestHelper.makeAllAssertsPass(true);
        unauffaelligeTageServiceSpy.loadUnauffaelligeTageCron();
        LockAssert.TestHelper.makeAllAssertsPass(false);

        Mockito.verify(unauffaelligeTageServiceSpy, Mockito.times(1)).loadAndSaveUnauffaelligeTageForEachMessstelle();
        Mockito.verify(kalendertagRepository, Mockito.times(1)).findByNextStartDateToLoadUnauffaelligeTageIsTrue();
        Mockito.verify(messstelleApi, Mockito.times(0)).getUnauffaelligeTageForEachMessstelleWithHttpInfo(any(LocalDate.class), any(LocalDate.class));
        Mockito.verify(unauffaelligeTageRepository, Mockito.times(1)).saveAllAndFlush(any());
        Mockito.verify(kalendertagRepository, Mockito.times(1)).save(today);
        Mockito.verify(kalendertagRepository, Mockito.times(1)).findByDatum(today.getDatum());
        today.setNextStartDateToLoadUnauffaelligeTage(true);
        Mockito.verify(kalendertagRepository, Mockito.times(0)).saveAndFlush(today);
    }

    @Test
    void loadMessstellenCronEntityNotFoundExceptionThrown() {
        final var unauffaelligeTageServiceSpy = Mockito.spy(this.unauffaelligeTageReceiver);

        Mockito.doThrow(new EntityNotFoundException("test")).when(unauffaelligeTageServiceSpy).loadAndSaveUnauffaelligeTageForEachMessstelle();

        LockAssert.TestHelper.makeAllAssertsPass(true);
        Assertions.assertThrows(EntityNotFoundException.class, unauffaelligeTageServiceSpy::loadAndSaveUnauffaelligeTageForEachMessstelle);
        LockAssert.TestHelper.makeAllAssertsPass(false);

        Mockito.verify(unauffaelligeTageServiceSpy, Mockito.times(1)).loadAndSaveUnauffaelligeTageForEachMessstelle();

        Mockito.verify(unauffaelligeTageRepository, Mockito.times(0)).saveAllAndFlush(any());
    }

    @Test
    void loadAndSaveUnauffaelligeTageForEachMessstelleWithDataInDatabase() {
        final var nextStartDate = new Kalendertag();
        nextStartDate.setDatum(LocalDate.of(2025, 2, 2));

        Mockito.when(kalendertagRepository.findByNextStartDateToLoadUnauffaelligeTageIsTrue()).thenReturn(Optional.of(nextStartDate));
        final var today = new Kalendertag();
        today.setDatum(LocalDate.now());
        Mockito.when(kalendertagRepository.findByDatum(LocalDate.now())).thenReturn(Optional.of(today));

        final var unauffaelligeTag20250202 = new ArrayList<UnauffaelligerTagDto>();
        final var unauffaelligeTag20250203 = new ArrayList<UnauffaelligerTagDto>();
        var unauffaelligerTagDto = new UnauffaelligerTagDto();
        unauffaelligerTagDto.setMstId("1234");
        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 2));
        unauffaelligeTag20250202.add(unauffaelligerTagDto);
        unauffaelligerTagDto = new UnauffaelligerTagDto();
        unauffaelligerTagDto.setMstId("1234");
        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 3));
        unauffaelligeTag20250203.add(unauffaelligerTagDto);
        unauffaelligerTagDto = new UnauffaelligerTagDto();
        unauffaelligerTagDto.setMstId("4321");
        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 3));
        unauffaelligeTag20250203.add(unauffaelligerTagDto);
        final var mono20250202 = Mono.just(ResponseEntity.of(Optional.of((List<UnauffaelligerTagDto>) unauffaelligeTag20250202)));
        final var mono20250203 = Mono.just(ResponseEntity.of(Optional.of((List<UnauffaelligerTagDto>) unauffaelligeTag20250203)));
        final var emptyMono = Mono.just(ResponseEntity.of(Optional.of((List<UnauffaelligerTagDto>) new ArrayList<UnauffaelligerTagDto>())));

        Mockito.when(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(
                LocalDate.of(2025, 2, 2), LocalDate.of(2025, 2, 2))).thenReturn(mono20250202);
        Mockito.when(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(
                LocalDate.of(2025, 2, 3), LocalDate.of(2025, 2, 3))).thenReturn(mono20250203);

        final AtomicReference<LocalDate> arg1 = new AtomicReference<>();
        Mockito.when(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(
                Mockito.argThat(argument -> {
                    arg1.set(argument);
                    return argument.isAfter(nextStartDate.getDatum()) && argument.isBefore(LocalDate.now());
                }),
                Mockito.argThat(argument -> argument.isEqual(arg1.get()) && !argument.isEqual(LocalDate.of(2025, 2, 2))
                        && !argument.isEqual(LocalDate.of(2025, 2, 3)))))
                .thenReturn(emptyMono);

        final var kalendertag20250202 = new Kalendertag();
        kalendertag20250202.setDatum(LocalDate.of(2025, 2, 2));
        kalendertag20250202.setTagestyp(TagesTyp.MO_SO);
        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 2))).thenReturn(Optional.of(kalendertag20250202));

        final var kalendertag20250203 = new Kalendertag();
        kalendertag20250203.setDatum(LocalDate.of(2025, 2, 3));
        kalendertag20250203.setTagestyp(TagesTyp.MO_SO);
        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 3))).thenReturn(Optional.of(kalendertag20250203));

        unauffaelligeTageReceiver.loadAndSaveUnauffaelligeTageForEachMessstelle();

        Mockito.verify(messstelleApi,
                Mockito.times((int) ChronoUnit.DAYS.between(nextStartDate.getDatum(), LocalDate.now()) - 2))
                .getUnauffaelligeTageForEachMessstelleWithHttpInfo(
                        Mockito.argThat(argument -> {
                            arg1.set(argument);
                            return argument.isAfter(nextStartDate.getDatum()) && argument.isBefore(LocalDate.now());
                        }),
                        Mockito.argThat(argument -> argument.isEqual(arg1.get()) && !argument.isEqual(LocalDate.of(2025, 2, 2))
                                && !argument.isEqual(LocalDate.of(2025, 2, 3))));

        Mockito.verify(messstelleApi, Mockito.times(1))
                .getUnauffaelligeTageForEachMessstelleWithHttpInfo(LocalDate.of(2025, 2, 2), LocalDate.of(2025, 2, 2));
        Mockito.verify(messstelleApi, Mockito.times(1))
                .getUnauffaelligeTageForEachMessstelleWithHttpInfo(LocalDate.of(2025, 2, 3), LocalDate.of(2025, 2, 3));

        Mockito.verify(kalendertagRepository, Mockito.times(1))
                .findByDatum(LocalDate.of(2025, 2, 2));

        Mockito.verify(kalendertagRepository, Mockito.times(2))
                .findByDatum(LocalDate.of(2025, 2, 3));

        Mockito.verify(unauffaelligeTageRepository, Mockito.times(1))
                .saveAllAndFlush(any());
        Mockito.verify(kalendertagRepository, Mockito.times(1))
                .findByDatum(today.getDatum());
        Mockito.verify(kalendertagRepository, Mockito.times(1))
                .save(any(Kalendertag.class));
        Mockito.verify(kalendertagRepository, Mockito.times(1))
                .saveAndFlush(any(Kalendertag.class));
    }

    @Test
    void loadAndSaveUnauffaelligeTageForEachMessstelleWithoutDataInDatabase() {
        Mockito.when(kalendertagRepository.findByNextStartDateToLoadUnauffaelligeTageIsTrue()).thenReturn(Optional.empty());
        final var today = new Kalendertag();
        today.setDatum(LocalDate.now());
        Mockito.when(kalendertagRepository.findByDatum(LocalDate.now())).thenReturn(Optional.of(today));

        final var unauffaelligeTag20250202 = new ArrayList<UnauffaelligerTagDto>();
        final var unauffaelligeTag20250203 = new ArrayList<UnauffaelligerTagDto>();
        var unauffaelligerTagDto = new UnauffaelligerTagDto();
        unauffaelligerTagDto.setMstId("1234");
        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 2));
        unauffaelligeTag20250202.add(unauffaelligerTagDto);
        unauffaelligerTagDto = new UnauffaelligerTagDto();
        unauffaelligerTagDto.setMstId("1234");
        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 3));
        unauffaelligeTag20250203.add(unauffaelligerTagDto);
        unauffaelligerTagDto = new UnauffaelligerTagDto();
        unauffaelligerTagDto.setMstId("4321");
        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 3));
        unauffaelligeTag20250203.add(unauffaelligerTagDto);
        final var mono20250202 = Mono.just(ResponseEntity.of(Optional.of((List<UnauffaelligerTagDto>) unauffaelligeTag20250202)));
        final var mono20250203 = Mono.just(ResponseEntity.of(Optional.of((List<UnauffaelligerTagDto>) unauffaelligeTag20250203)));
        final var emptyMono = Mono.just(ResponseEntity.of(Optional.of((List<UnauffaelligerTagDto>) new ArrayList<UnauffaelligerTagDto>())));

        Mockito.when(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(
                LocalDate.of(2025, 2, 2), LocalDate.of(2025, 2, 2))).thenReturn(mono20250202);
        Mockito.when(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(
                LocalDate.of(2025, 2, 3), LocalDate.of(2025, 2, 3))).thenReturn(mono20250203);

        final AtomicReference<LocalDate> arg1 = new AtomicReference<>();
        Mockito.when(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(
                Mockito.argThat(argument -> {
                    arg1.set(argument);
                    return argument.isAfter(LocalDate.of(2005, 12, 31)) && argument.isBefore(LocalDate.now());
                }),
                Mockito.argThat(argument -> argument.isEqual(arg1.get()) && !argument.isEqual(LocalDate.of(2025, 2, 2))
                        && !argument.isEqual(LocalDate.of(2025, 2, 3)))))
                .thenReturn(emptyMono);

        final var kalendertag20250202 = new Kalendertag();
        kalendertag20250202.setDatum(LocalDate.of(2025, 2, 2));
        kalendertag20250202.setTagestyp(TagesTyp.MO_SO);
        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 2))).thenReturn(Optional.of(kalendertag20250202));

        final var kalendertag20250203 = new Kalendertag();
        kalendertag20250203.setDatum(LocalDate.of(2025, 2, 3));
        kalendertag20250203.setTagestyp(TagesTyp.MO_SO);
        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 3))).thenReturn(Optional.of(kalendertag20250203));

        unauffaelligeTageReceiver.loadAndSaveUnauffaelligeTageForEachMessstelle();

        Mockito.verify(messstelleApi, Mockito.times((int) ChronoUnit.DAYS.between(LocalDate.of(2006, 1, 1), LocalDate.now()) - 2))
                .getUnauffaelligeTageForEachMessstelleWithHttpInfo(
                        Mockito.argThat(argument -> {
                            arg1.set(argument);
                            return argument.isAfter(LocalDate.of(2005, 12, 31)) && argument.isBefore(LocalDate.now());
                        }),
                        Mockito.argThat(argument -> argument.isEqual(arg1.get()) && !argument.isEqual(LocalDate.of(2025, 2, 2))
                                && !argument.isEqual(LocalDate.of(2025, 2, 3))));
        Mockito.verify(messstelleApi, Mockito.times(1))
                .getUnauffaelligeTageForEachMessstelleWithHttpInfo(LocalDate.of(2025, 2, 2), LocalDate.of(2025, 2, 2));
        Mockito.verify(messstelleApi, Mockito.times(1))
                .getUnauffaelligeTageForEachMessstelleWithHttpInfo(LocalDate.of(2025, 2, 3), LocalDate.of(2025, 2, 3));

        Mockito.verify(kalendertagRepository, Mockito.times(1))
                .findByDatum(LocalDate.of(2025, 2, 2));

        Mockito.verify(kalendertagRepository, Mockito.times(2))
                .findByDatum(LocalDate.of(2025, 2, 3));

        Mockito.verify(unauffaelligeTageRepository, Mockito.times(1))
                .saveAllAndFlush(any());
        Mockito.verify(kalendertagRepository, Mockito.times(1))
                .findByDatum(today.getDatum());
        Mockito.verify(kalendertagRepository, Mockito.times(0))
                .save(any(Kalendertag.class));
        Mockito.verify(kalendertagRepository, Mockito.times(1))
                .saveAndFlush(any(Kalendertag.class));
    }

    @Test
    void loadAndSaveUnauffaelligeTageForEachMessstelleNoKalendertagForDatumFound() {
        Mockito.when(kalendertagRepository.findByNextStartDateToLoadUnauffaelligeTageIsTrue()).thenReturn(Optional.empty());
        final var today = new Kalendertag();
        today.setDatum(LocalDate.now());
        Mockito.when(kalendertagRepository.findByDatum(LocalDate.now())).thenReturn(Optional.of(today));

        final var unauffaelligeTag20250202 = new ArrayList<UnauffaelligerTagDto>();
        final var unauffaelligeTag20250203 = new ArrayList<UnauffaelligerTagDto>();
        var unauffaelligerTagDto = new UnauffaelligerTagDto();
        unauffaelligerTagDto.setMstId("1234");
        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 2));
        unauffaelligeTag20250202.add(unauffaelligerTagDto);
        unauffaelligerTagDto = new UnauffaelligerTagDto();
        unauffaelligerTagDto.setMstId("1234");
        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 3));
        unauffaelligeTag20250203.add(unauffaelligerTagDto);
        unauffaelligerTagDto = new UnauffaelligerTagDto();
        unauffaelligerTagDto.setMstId("4321");
        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 3));
        unauffaelligeTag20250203.add(unauffaelligerTagDto);
        final var mono20250202 = Mono.just(ResponseEntity.of(Optional.of((List<UnauffaelligerTagDto>) unauffaelligeTag20250202)));
        final var mono20250203 = Mono.just(ResponseEntity.of(Optional.of((List<UnauffaelligerTagDto>) unauffaelligeTag20250203)));
        final var emptyMono = Mono.just(ResponseEntity.of(Optional.of((List<UnauffaelligerTagDto>) new ArrayList<UnauffaelligerTagDto>())));

        Mockito.when(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(
                LocalDate.of(2025, 2, 2), LocalDate.of(2025, 2, 2))).thenReturn(mono20250202);
        Mockito.when(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(
                LocalDate.of(2025, 2, 3), LocalDate.of(2025, 2, 3))).thenReturn(mono20250203);

        final AtomicReference<LocalDate> arg1 = new AtomicReference<>();
        Mockito.when(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(
                Mockito.argThat(argument -> {
                    arg1.set(argument);
                    return argument.isAfter(LocalDate.of(2005, 12, 31)) && argument.isBefore(LocalDate.now());
                }),
                Mockito.argThat(argument -> argument.isEqual(arg1.get()) && !argument.isEqual(LocalDate.of(2025, 2, 2))
                        && !argument.isEqual(LocalDate.of(2025, 2, 3)))))
                .thenReturn(emptyMono);

        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 2))).thenReturn(Optional.empty());

        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 3))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, unauffaelligeTageReceiver::loadAndSaveUnauffaelligeTageForEachMessstelle);

        Mockito.verify(unauffaelligeTageRepository, Mockito.times(0))
                .saveAllAndFlush(any());
        Mockito.verify(kalendertagRepository, Mockito.times(0))
                .findByDatum(today.getDatum());
        Mockito.verify(kalendertagRepository, Mockito.times(0))
                .save(any(Kalendertag.class));
        Mockito.verify(kalendertagRepository, Mockito.times(0))
                .saveAndFlush(any(Kalendertag.class));
    }

    @Test
    void loadAndSaveUnauffaelligeTageForEachMessstelleYesterdayIsNotAfterLastUnauffaelligerTag() {
        final var today = new Kalendertag();
        today.setDatum(LocalDate.now());

        Mockito.when(kalendertagRepository.findByNextStartDateToLoadUnauffaelligeTageIsTrue()).thenReturn(Optional.of(today));
        Mockito.when(kalendertagRepository.findByDatum(LocalDate.now())).thenReturn(Optional.of(today));

        unauffaelligeTageReceiver.loadAndSaveUnauffaelligeTageForEachMessstelle();

        Mockito.verify(kalendertagRepository, Mockito.times(1))
                .findByNextStartDateToLoadUnauffaelligeTageIsTrue();

        Mockito.verify(messstelleApi, Mockito.times(0))
                .getUnauffaelligeTageForEachMessstelleWithHttpInfo(any(), any());

        Mockito.verify(unauffaelligeTageRepository, Mockito.times(1))
                .saveAllAndFlush(List.of());

        Mockito.verify(kalendertagRepository, Mockito.times(1))
                .save(any(Kalendertag.class));

        Mockito.verify(kalendertagRepository, Mockito.times(1))
                .findByDatum(today.getDatum());

        Mockito.verify(kalendertagRepository, Mockito.times(1))
                .saveAndFlush(any(Kalendertag.class));
    }

    @Test
    void mapDto2Entity() {
        final var unauffaelligerTagDto = new UnauffaelligerTagDto();
        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 25));
        unauffaelligerTagDto.setMstId("1234");

        final var kalendertag = new Kalendertag();
        kalendertag.setDatum(LocalDate.of(2025, 2, 25));
        kalendertag.setTagestyp(TagesTyp.MO_SO);

        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 25))).thenReturn(Optional.of(kalendertag));

        final var result = unauffaelligeTageReceiver.mapDto2Entity(unauffaelligerTagDto);

        final var expected = new UnauffaelligerTag();
        expected.setMstId("1234");
        expected.setKalendertag(kalendertag);

        Assertions.assertEquals(expected, result);

        Mockito.verify(kalendertagRepository, Mockito.times(1)).findByDatum(LocalDate.of(2025, 2, 25));

    }

    @Test
    void mapDto2EntityNoKalendertagFound() {
        final var unauffaelligerTagDto = new UnauffaelligerTagDto();
        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 25));
        unauffaelligerTagDto.setMstId("1234");

        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 25))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> unauffaelligeTageReceiver.mapDto2Entity(unauffaelligerTagDto));

        Mockito.verify(kalendertagRepository, Mockito.times(1)).findByDatum(LocalDate.of(2025, 2, 25));
    }
}
