//package de.muenchen.dave.services.messstelle;
//
//import de.muenchen.dave.domain.Kalendertag;
//import de.muenchen.dave.domain.UnauffaelligerTag;
//import de.muenchen.dave.domain.enums.TagesTyp;
//import de.muenchen.dave.domain.mapper.detektor.MessstelleReceiverMapperImpl;
//import de.muenchen.dave.geodateneai.gen.api.MessstelleApi;
//import de.muenchen.dave.geodateneai.gen.model.UnauffaelligerTagDto;
//import de.muenchen.dave.repositories.relationaldb.KalendertagRepository;
//import de.muenchen.dave.repositories.relationaldb.UnauffaelligeTageRepository;
//import jakarta.persistence.EntityNotFoundException;
//import net.javacrumbs.shedlock.core.LockAssert;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//import org.springframework.http.ResponseEntity;
//import reactor.core.publisher.Mono;
//
//import java.time.LocalDate;
//import java.time.temporal.ChronoUnit;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.concurrent.atomic.AtomicReference;
//
//import static org.mockito.ArgumentMatchers.any;
//
//@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
//class UnauffaelligeTageReceiverTest {
//
//    private UnauffaelligeTageReceiver unauffaelligeTageReceiver;
//
//    @Mock
//    private UnauffaelligeTageRepository unauffaelligeTageRepository;
//
//    @Mock
//    private KalendertagRepository kalendertagRepository;
//
//    @Mock
//    private MessstelleApi messstelleApi;
//
//    @BeforeEach
//    void beforeEach() {
//        this.unauffaelligeTageReceiver = new UnauffaelligeTageReceiver(
//                unauffaelligeTageRepository,
//                kalendertagRepository,
//                new MessstelleReceiverMapperImpl(),
//                messstelleApi);
//        Mockito.reset(unauffaelligeTageRepository, kalendertagRepository, messstelleApi);
//    }
//
//    @Test
//    void loadMessstellenCron() {
//        final var unauffaelligeTageServiceSpy = Mockito.spy(this.unauffaelligeTageReceiver);
//
//        final var kalendertag20250202 = new Kalendertag();
//        kalendertag20250202.setDatum(LocalDate.of(2025, 2, 2));
//        kalendertag20250202.setTagestyp(TagesTyp.MO_SO);
//
//        final var kalendertag20250203 = new Kalendertag();
//        kalendertag20250203.setDatum(LocalDate.of(2025, 2, 3));
//        kalendertag20250203.setTagestyp(TagesTyp.MO_SO);
//
//        final var unauffaelligeTage = new ArrayList<UnauffaelligerTag>();
//        var unauffaelligerTag = new UnauffaelligerTag();
//        unauffaelligerTag.setMstId("1234");
//        unauffaelligerTag.setKalendertag(kalendertag20250202);
//        unauffaelligeTage.add(unauffaelligerTag);
//        unauffaelligerTag = new UnauffaelligerTag();
//        unauffaelligerTag.setMstId("1234");
//        unauffaelligerTag.setKalendertag(kalendertag20250203);
//        unauffaelligeTage.add(unauffaelligerTag);
//        unauffaelligerTag = new UnauffaelligerTag();
//        unauffaelligerTag.setMstId("4321");
//        unauffaelligerTag.setKalendertag(kalendertag20250203);
//        unauffaelligeTage.add(unauffaelligerTag);
//
//        Mockito.doReturn(unauffaelligeTage).when(unauffaelligeTageServiceSpy).loadAndSaveUnauffaelligeTageForEachMessstelle();
//
//        LockAssert.TestHelper.makeAllAssertsPass(true);
//        unauffaelligeTageServiceSpy.loadMessstellenCron();
//        LockAssert.TestHelper.makeAllAssertsPass(false);
//
//        Mockito.verify(unauffaelligeTageServiceSpy, Mockito.times(1)).loadAndSaveUnauffaelligeTageForEachMessstelle();
//
//        Mockito.verify(unauffaelligeTageRepository, Mockito.times(1)).saveAllAndFlush(unauffaelligeTage);
//    }
//
//    @Test
//    void loadMessstellenCronEntityNotFoundExceptionThrown() {
//        final var unauffaelligeTageServiceSpy = Mockito.spy(this.unauffaelligeTageReceiver);
//
//        Mockito.doThrow(new EntityNotFoundException("test")).when(unauffaelligeTageServiceSpy).loadAndSaveUnauffaelligeTageForEachMessstelle();
//
//        LockAssert.TestHelper.makeAllAssertsPass(true);
//        Assertions.assertThrows(EntityNotFoundException.class, unauffaelligeTageServiceSpy::loadAndSaveUnauffaelligeTageForEachMessstelle);
//        LockAssert.TestHelper.makeAllAssertsPass(false);
//
//        Mockito.verify(unauffaelligeTageServiceSpy, Mockito.times(1)).loadAndSaveUnauffaelligeTageForEachMessstelle();
//
//        Mockito.verify(unauffaelligeTageRepository, Mockito.times(0)).saveAllAndFlush(any());
//    }
//
//    @Test
//    void loadAndSaveUnauffaelligeTageForEachMessstelleWithDataInDatabase() {
//        final var kalenderTagForYoungestSavedUnauffaelligerTag = new Kalendertag();
//        kalenderTagForYoungestSavedUnauffaelligerTag.setDatum(LocalDate.of(2025, 2, 1));
//
//        final var youngestSavedUnauffaelligerTag = new UnauffaelligerTag();
//        youngestSavedUnauffaelligerTag.setKalendertag(kalenderTagForYoungestSavedUnauffaelligerTag);
//        youngestSavedUnauffaelligerTag.setMstId("1234");
//
//        Mockito.when(unauffaelligeTageRepository.findTopByOrderByKalendertagDatumDesc()).thenReturn(Optional.of(youngestSavedUnauffaelligerTag));
//
//        final var unauffaelligeTag20250202 = new ArrayList<UnauffaelligerTagDto>();
//        final var unauffaelligeTag20250203 = new ArrayList<UnauffaelligerTagDto>();
//        var unauffaelligerTagDto = new UnauffaelligerTagDto();
//        unauffaelligerTagDto.setMstId("1234");
//        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 2));
//        unauffaelligeTag20250202.add(unauffaelligerTagDto);
//        unauffaelligerTagDto = new UnauffaelligerTagDto();
//        unauffaelligerTagDto.setMstId("1234");
//        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 3));
//        unauffaelligeTag20250203.add(unauffaelligerTagDto);
//        unauffaelligerTagDto = new UnauffaelligerTagDto();
//        unauffaelligerTagDto.setMstId("4321");
//        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 3));
//        unauffaelligeTag20250203.add(unauffaelligerTagDto);
//        final var mono20250202 = Mono.just(ResponseEntity.of(Optional.of((List<UnauffaelligerTagDto>) unauffaelligeTag20250202)));
//        final var mono20250203 = Mono.just(ResponseEntity.of(Optional.of((List<UnauffaelligerTagDto>) unauffaelligeTag20250203)));
//        final var emptyMono = Mono.just(ResponseEntity.of(Optional.of((List<UnauffaelligerTagDto>) new ArrayList<UnauffaelligerTagDto>())));
//
//        Mockito.when(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(
//                LocalDate.of(2025, 2, 2), LocalDate.of(2025, 2, 2))).thenReturn(mono20250202);
//        Mockito.when(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(
//                LocalDate.of(2025, 2, 3), LocalDate.of(2025, 2, 3))).thenReturn(mono20250203);
//
//        final AtomicReference<LocalDate> arg1 = new AtomicReference<>();
//        Mockito.when(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(
//                Mockito.argThat(argument -> {
//                    arg1.set(argument);
//                    return argument.isAfter(kalenderTagForYoungestSavedUnauffaelligerTag.getDatum()) && argument.isBefore(LocalDate.now());
//                }),
//                Mockito.argThat(argument -> argument.isEqual(arg1.get()) && !argument.isEqual(LocalDate.of(2025, 2, 2))
//                        && !argument.isEqual(LocalDate.of(2025, 2, 3)))))
//                .thenReturn(emptyMono);
//
//        final var kalendertag20250202 = new Kalendertag();
//        kalendertag20250202.setDatum(LocalDate.of(2025, 2, 2));
//        kalendertag20250202.setTagestyp(TagesTyp.MO_SO);
//        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 2))).thenReturn(Optional.of(kalendertag20250202));
//
//        final var kalendertag20250203 = new Kalendertag();
//        kalendertag20250203.setDatum(LocalDate.of(2025, 2, 3));
//        kalendertag20250203.setTagestyp(TagesTyp.MO_SO);
//        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 3))).thenReturn(Optional.of(kalendertag20250203));
//
//        unauffaelligeTageReceiver.loadAndSaveUnauffaelligeTageForEachMessstelle();
//
//        Mockito.verify(messstelleApi,
//                Mockito.times((int) ChronoUnit.DAYS.between(kalenderTagForYoungestSavedUnauffaelligerTag.getDatum().plusDays(1), LocalDate.now()) - 2))
//                .getUnauffaelligeTageForEachMessstelleWithHttpInfo(
//                        Mockito.argThat(argument -> {
//                            arg1.set(argument);
//                            return argument.isAfter(kalenderTagForYoungestSavedUnauffaelligerTag.getDatum()) && argument.isBefore(LocalDate.now());
//                        }),
//                        Mockito.argThat(argument -> argument.isEqual(arg1.get()) && !argument.isEqual(LocalDate.of(2025, 2, 2))
//                                && !argument.isEqual(LocalDate.of(2025, 2, 3))));
//        Mockito.verify(messstelleApi, Mockito.times(1))
//                .getUnauffaelligeTageForEachMessstelleWithHttpInfo(LocalDate.of(2025, 2, 2), LocalDate.of(2025, 2, 2));
//        Mockito.verify(messstelleApi, Mockito.times(1))
//                .getUnauffaelligeTageForEachMessstelleWithHttpInfo(LocalDate.of(2025, 2, 3), LocalDate.of(2025, 2, 3));
//
//        Mockito.verify(kalendertagRepository, Mockito.times(1))
//                .findByDatum(LocalDate.of(2025, 2, 2));
//
//        Mockito.verify(kalendertagRepository, Mockito.times(2))
//                .findByDatum(LocalDate.of(2025, 2, 3));
//
//        Mockito.verify(unauffaelligeTageRepository, Mockito.times(1))
//                .flush();
//        Mockito.verify(unauffaelligeTageRepository, Mockito.times(3))
//                .save(Mockito.any(UnauffaelligerTag.class));
//    }
//
//    @Test
//    void loadAndSaveUnauffaelligeTageForEachMessstelleWithoutDataInDatabase() {
//        Mockito.when(unauffaelligeTageRepository.findTopByOrderByKalendertagDatumDesc()).thenReturn(Optional.empty());
//
//        final var unauffaelligeTag20250202 = new ArrayList<UnauffaelligerTagDto>();
//        final var unauffaelligeTag20250203 = new ArrayList<UnauffaelligerTagDto>();
//        var unauffaelligerTagDto = new UnauffaelligerTagDto();
//        unauffaelligerTagDto.setMstId("1234");
//        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 2));
//        unauffaelligeTag20250202.add(unauffaelligerTagDto);
//        unauffaelligerTagDto = new UnauffaelligerTagDto();
//        unauffaelligerTagDto.setMstId("1234");
//        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 3));
//        unauffaelligeTag20250203.add(unauffaelligerTagDto);
//        unauffaelligerTagDto = new UnauffaelligerTagDto();
//        unauffaelligerTagDto.setMstId("4321");
//        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 3));
//        unauffaelligeTag20250203.add(unauffaelligerTagDto);
//        final var mono20250202 = Mono.just(ResponseEntity.of(Optional.of((List<UnauffaelligerTagDto>) unauffaelligeTag20250202)));
//        final var mono20250203 = Mono.just(ResponseEntity.of(Optional.of((List<UnauffaelligerTagDto>) unauffaelligeTag20250203)));
//        final var emptyMono = Mono.just(ResponseEntity.of(Optional.of((List<UnauffaelligerTagDto>) new ArrayList<UnauffaelligerTagDto>())));
//
//        Mockito.when(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(
//                LocalDate.of(2025, 2, 2), LocalDate.of(2025, 2, 2))).thenReturn(mono20250202);
//        Mockito.when(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(
//                LocalDate.of(2025, 2, 3), LocalDate.of(2025, 2, 3))).thenReturn(mono20250203);
//
//        final AtomicReference<LocalDate> arg1 = new AtomicReference<>();
//        Mockito.when(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(
//                Mockito.argThat(argument -> {
//                    arg1.set(argument);
//                    return argument.isAfter(LocalDate.of(2005, 12, 31)) && argument.isBefore(LocalDate.now());
//                }),
//                Mockito.argThat(argument -> argument.isEqual(arg1.get()) && !argument.isEqual(LocalDate.of(2025, 2, 2))
//                        && !argument.isEqual(LocalDate.of(2025, 2, 3)))))
//                .thenReturn(emptyMono);
//
//        final var kalendertag20250202 = new Kalendertag();
//        kalendertag20250202.setDatum(LocalDate.of(2025, 2, 2));
//        kalendertag20250202.setTagestyp(TagesTyp.MO_SO);
//        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 2))).thenReturn(Optional.of(kalendertag20250202));
//
//        final var kalendertag20250203 = new Kalendertag();
//        kalendertag20250203.setDatum(LocalDate.of(2025, 2, 3));
//        kalendertag20250203.setTagestyp(TagesTyp.MO_SO);
//        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 3))).thenReturn(Optional.of(kalendertag20250203));
//
//        final var result = unauffaelligeTageReceiver.loadAndSaveUnauffaelligeTageForEachMessstelle();
//
//        final var expected = new ArrayList<UnauffaelligerTag>();
//        var unauffaelligerTag = new UnauffaelligerTag();
//        unauffaelligerTag.setMstId("1234");
//        unauffaelligerTag.setKalendertag(kalendertag20250202);
//        expected.add(unauffaelligerTag);
//        unauffaelligerTag = new UnauffaelligerTag();
//        unauffaelligerTag.setMstId("1234");
//        unauffaelligerTag.setKalendertag(kalendertag20250203);
//        expected.add(unauffaelligerTag);
//        unauffaelligerTag = new UnauffaelligerTag();
//        unauffaelligerTag.setMstId("4321");
//        unauffaelligerTag.setKalendertag(kalendertag20250203);
//        expected.add(unauffaelligerTag);
//
//        Assertions.assertEquals(expected, result);
//
//        Mockito.verify(messstelleApi, Mockito.times((int) ChronoUnit.DAYS.between(LocalDate.of(2006, 1, 1), LocalDate.now()) - 2))
//                .getUnauffaelligeTageForEachMessstelleWithHttpInfo(
//                        Mockito.argThat(argument -> {
//                            arg1.set(argument);
//                            return argument.isAfter(LocalDate.of(2005, 12, 31)) && argument.isBefore(LocalDate.now());
//                        }),
//                        Mockito.argThat(argument -> argument.isEqual(arg1.get()) && !argument.isEqual(LocalDate.of(2025, 2, 2))
//                                && !argument.isEqual(LocalDate.of(2025, 2, 3))));
//        Mockito.verify(messstelleApi, Mockito.times(1))
//                .getUnauffaelligeTageForEachMessstelleWithHttpInfo(LocalDate.of(2025, 2, 2), LocalDate.of(2025, 2, 2));
//        Mockito.verify(messstelleApi, Mockito.times(1))
//                .getUnauffaelligeTageForEachMessstelleWithHttpInfo(LocalDate.of(2025, 2, 3), LocalDate.of(2025, 2, 3));
//
//        Mockito.verify(kalendertagRepository, Mockito.times(1))
//                .findByDatum(LocalDate.of(2025, 2, 2));
//
//        Mockito.verify(kalendertagRepository, Mockito.times(2))
//                .findByDatum(LocalDate.of(2025, 2, 3));
//    }
//
//    @Test
//    void loadAndSaveUnauffaelligeTageForEachMessstelleNoKaledertagForDatumFound() {
//        Mockito.when(unauffaelligeTageRepository.findTopByOrderByKalendertagDatumDesc()).thenReturn(Optional.empty());
//
//        final var unauffaelligeTag20250202 = new ArrayList<UnauffaelligerTagDto>();
//        final var unauffaelligeTag20250203 = new ArrayList<UnauffaelligerTagDto>();
//        var unauffaelligerTagDto = new UnauffaelligerTagDto();
//        unauffaelligerTagDto.setMstId("1234");
//        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 2));
//        unauffaelligeTag20250202.add(unauffaelligerTagDto);
//        unauffaelligerTagDto = new UnauffaelligerTagDto();
//        unauffaelligerTagDto.setMstId("1234");
//        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 3));
//        unauffaelligeTag20250203.add(unauffaelligerTagDto);
//        unauffaelligerTagDto = new UnauffaelligerTagDto();
//        unauffaelligerTagDto.setMstId("4321");
//        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 3));
//        unauffaelligeTag20250203.add(unauffaelligerTagDto);
//        final var mono20250202 = Mono.just(ResponseEntity.of(Optional.of((List<UnauffaelligerTagDto>) unauffaelligeTag20250202)));
//        final var mono20250203 = Mono.just(ResponseEntity.of(Optional.of((List<UnauffaelligerTagDto>) unauffaelligeTag20250203)));
//        final var emptyMono = Mono.just(ResponseEntity.of(Optional.of((List<UnauffaelligerTagDto>) new ArrayList<UnauffaelligerTagDto>())));
//
//        Mockito.when(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(
//                LocalDate.of(2025, 2, 2), LocalDate.of(2025, 2, 2))).thenReturn(mono20250202);
//        Mockito.when(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(
//                LocalDate.of(2025, 2, 3), LocalDate.of(2025, 2, 3))).thenReturn(mono20250203);
//
//        final AtomicReference<LocalDate> arg1 = new AtomicReference<>();
//        Mockito.when(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(
//                Mockito.argThat(argument -> {
//                    arg1.set(argument);
//                    return argument.isAfter(LocalDate.of(2005, 12, 31)) && argument.isBefore(LocalDate.now());
//                }),
//                Mockito.argThat(argument -> argument.isEqual(arg1.get()) && !argument.isEqual(LocalDate.of(2025, 2, 2))
//                        && !argument.isEqual(LocalDate.of(2025, 2, 3)))))
//                .thenReturn(emptyMono);
//
//        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 2))).thenReturn(Optional.empty());
//
//        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 3))).thenReturn(Optional.empty());
//
//        Assertions.assertThrows(EntityNotFoundException.class, unauffaelligeTageReceiver::loadAndSaveUnauffaelligeTageForEachMessstelle);
//
//        Mockito.verify(messstelleApi, Mockito.times((int) ChronoUnit.DAYS.between(LocalDate.of(2006, 1, 1), LocalDate.now()) - 2))
//                .getUnauffaelligeTageForEachMessstelleWithHttpInfo(
//                        Mockito.argThat(argument -> {
//                            arg1.set(argument);
//                            return argument.isAfter(LocalDate.of(2005, 12, 31)) && argument.isBefore(LocalDate.now());
//                        }),
//                        Mockito.argThat(argument -> argument.isEqual(arg1.get()) && !argument.isEqual(LocalDate.of(2025, 2, 2))
//                                && !argument.isEqual(LocalDate.of(2025, 2, 3))));
//        Mockito.verify(messstelleApi, Mockito.times(1))
//                .getUnauffaelligeTageForEachMessstelleWithHttpInfo(LocalDate.of(2025, 2, 2), LocalDate.of(2025, 2, 2));
//        Mockito.verify(messstelleApi, Mockito.times(1))
//                .getUnauffaelligeTageForEachMessstelleWithHttpInfo(LocalDate.of(2025, 2, 3), LocalDate.of(2025, 2, 3));
//
//        Mockito.verify(kalendertagRepository, Mockito.times(1))
//                .findByDatum(LocalDate.of(2025, 2, 2));
//
//        Mockito.verify(kalendertagRepository, Mockito.times(0))
//                .findByDatum(LocalDate.of(2025, 2, 3));
//    }
//
//    @Test
//    void loadAndSaveUnauffaelligeTageForEachMessstelleYesterdayIsNotAfterLastUnauffaelligerTag() {
//        final var kalenderTagForYoungestSavedUnauffaelligerTag = new Kalendertag();
//        kalenderTagForYoungestSavedUnauffaelligerTag.setDatum(LocalDate.now().minusDays(1));
//
//        final var youngestSavedUnauffaelligerTag = new UnauffaelligerTag();
//        youngestSavedUnauffaelligerTag.setKalendertag(kalenderTagForYoungestSavedUnauffaelligerTag);
//        youngestSavedUnauffaelligerTag.setMstId("1234");
//
//        Mockito.when(unauffaelligeTageRepository.findTopByOrderByKalendertagDatumDesc()).thenReturn(Optional.of(youngestSavedUnauffaelligerTag));
//
//        final var result = unauffaelligeTageReceiver.loadAndSaveUnauffaelligeTageForEachMessstelle();
//
//        Assertions.assertEquals(List.of(), result);
//
//        Mockito.verify(unauffaelligeTageRepository, Mockito.times(1))
//                .findTopByOrderByKalendertagDatumDesc();
//
//        Mockito.verify(messstelleApi, Mockito.times(0))
//                .getUnauffaelligeTageForEachMessstelleWithHttpInfo(any(), any());
//    }
//
//    @Test
//    void mapDto2Entity() {
//        final var unauffaelligerTagDto = new UnauffaelligerTagDto();
//        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 25));
//        unauffaelligerTagDto.setMstId("1234");
//
//        final var kalendertag = new Kalendertag();
//        kalendertag.setDatum(LocalDate.of(2025, 2, 25));
//        kalendertag.setTagestyp(TagesTyp.MO_SO);
//
//        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 25))).thenReturn(Optional.of(kalendertag));
//
//        final var result = unauffaelligeTageReceiver.mapDto2Entity(unauffaelligerTagDto);
//
//        final var expected = new UnauffaelligerTag();
//        expected.setMstId("1234");
//        expected.setKalendertag(kalendertag);
//
//        Assertions.assertEquals(expected, result);
//
//        Mockito.verify(kalendertagRepository, Mockito.times(1)).findByDatum(LocalDate.of(2025, 2, 25));
//
//    }
//
//    @Test
//    void mapDto2EntityNoKalendertagFound() {
//        final var unauffaelligerTagDto = new UnauffaelligerTagDto();
//        unauffaelligerTagDto.setDatum(LocalDate.of(2025, 2, 25));
//        unauffaelligerTagDto.setMstId("1234");
//
//        Mockito.when(kalendertagRepository.findByDatum(LocalDate.of(2025, 2, 25))).thenReturn(Optional.empty());
//
//        Assertions.assertThrows(EntityNotFoundException.class, () -> unauffaelligeTageReceiver.mapDto2Entity(unauffaelligerTagDto));
//
//        Mockito.verify(kalendertagRepository, Mockito.times(1)).findByDatum(LocalDate.of(2025, 2, 25));
//    }
//}
