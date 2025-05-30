package de.muenchen.dave.services.messstelle;

import static org.mockito.ArgumentMatchers.any;

import de.muenchen.dave.domain.Kalendertag;
import de.muenchen.dave.domain.mapper.detektor.MessstelleReceiverMapperImpl;
import de.muenchen.dave.geodateneai.gen.api.MessstelleApi;
import de.muenchen.dave.repositories.relationaldb.KalendertagRepository;
import de.muenchen.dave.repositories.relationaldb.UnauffaelligeTageRepository;
import java.time.LocalDate;
import java.util.Optional;
import net.javacrumbs.shedlock.core.LockAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UnauffaelligeTageReceiverTest {

    private UnauffaelligeTageService unauffaelligeTageService;

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
        this.unauffaelligeTageService = new UnauffaelligeTageService(
                unauffaelligeTageRepository,
                kalendertagRepository,
                new MessstelleReceiverMapperImpl(),
                messstelleApi,
                messstelleService);
        Mockito.reset(unauffaelligeTageRepository, kalendertagRepository, messstelleApi);
    }

    @Test
    void loadUnauffaelligeTageCron() {
        final var unauffaelligeTageReceiver = new UnauffaelligeTageReceiver(unauffaelligeTageService);
        final var unauffaelligeTageReceiverSpy = Mockito.spy(unauffaelligeTageReceiver);
        final var today = new Kalendertag();
        today.setDatum(LocalDate.now());
        today.setNextStartDateToLoadUnauffaelligeTage(null);
        Mockito.when(kalendertagRepository.findByNextStartDateToLoadUnauffaelligeTageIsTrue()).thenReturn(Optional.of(today));

        LockAssert.TestHelper.makeAllAssertsPass(true);
        unauffaelligeTageReceiverSpy.loadUnauffaelligeTageCron();
        LockAssert.TestHelper.makeAllAssertsPass(false);

        Mockito.verify(unauffaelligeTageReceiverSpy, Mockito.times(1)).loadUnauffaelligeTageCron();
        Mockito.verify(kalendertagRepository, Mockito.times(1)).findByNextStartDateToLoadUnauffaelligeTageIsTrue();
        Mockito.verify(messstelleApi, Mockito.times(0)).getUnauffaelligeTageForEachMessstelleWithHttpInfo(any(LocalDate.class), any(LocalDate.class));
        Mockito.verify(unauffaelligeTageRepository, Mockito.times(1)).saveAllAndFlush(any());
        Mockito.verify(kalendertagRepository, Mockito.times(1)).save(today);
        Mockito.verify(kalendertagRepository, Mockito.times(1)).findByDatum(today.getDatum());
        today.setNextStartDateToLoadUnauffaelligeTage(true);
        Mockito.verify(kalendertagRepository, Mockito.times(0)).saveAndFlush(today);
    }
}
