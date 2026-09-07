package de.muenchen.dave.services.persist;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.KIPredictionResult;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.exceptions.PredictionFailedException;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import de.muenchen.dave.services.hochrechnung.HochrechnungsService;
import de.muenchen.dave.util.DaveConstants;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class ZeitintervallPersistierungsServiceTest {

    @Test
    void test_With13HoursPrediction_PersistsKiAndGesamtHochrechnung() throws PredictionFailedException {
        final ZeitintervallRepository zeitintervallRepository = Mockito.mock(ZeitintervallRepository.class);
        final HochrechnungsService hochrechnungsService = Mockito.mock(HochrechnungsService.class);
        when(hochrechnungsService.berechneRadhochrechnung(any(), any())).thenReturn(List.of(new KIPredictionResult(123)));
        final ZeitintervallPersistierungsService service = new ZeitintervallPersistierungsService(zeitintervallRepository, hochrechnungsService);

        service.aufbereitenUndPersistieren(Zaehldauer.DAUER_13_STUNDEN, createZeitintervalle(52), true);

        assertKiHochrechnung(zeitintervallRepository);
    }

    @Test
    void test_With16HoursPrediction_PersistsKiAndGesamtHochrechnung() throws PredictionFailedException {
        final ZeitintervallRepository zeitintervallRepository = Mockito.mock(ZeitintervallRepository.class);
        final HochrechnungsService hochrechnungsService = Mockito.mock(HochrechnungsService.class);
        when(hochrechnungsService.berechneRadhochrechnung(any(), any())).thenReturn(List.of(new KIPredictionResult(123)));
        final ZeitintervallPersistierungsService service = new ZeitintervallPersistierungsService(zeitintervallRepository, hochrechnungsService);

        service.aufbereitenUndPersistieren(Zaehldauer.DAUER_16_STUNDEN, createZeitintervalle(64), true);

        assertKiHochrechnung(zeitintervallRepository);
    }

    private void assertKiHochrechnung(final ZeitintervallRepository zeitintervallRepository) {
        final ArgumentCaptor<List<Zeitintervall>> captor = ArgumentCaptor.forClass(List.class);
        verify(zeitintervallRepository).saveAllAndFlush(captor.capture());
        final Zeitintervall kiZeitintervall = captor.getValue().stream()
                .filter(zeitintervall -> TypeZeitintervall.GESAMT_KI.equals(zeitintervall.getType()))
                .findFirst()
                .orElseThrow();
        final Zeitintervall gesamtZeitintervall = captor.getValue().stream()
                .filter(zeitintervall -> TypeZeitintervall.GESAMT.equals(zeitintervall.getType()))
                .findFirst()
                .orElseThrow();

        assertThat(kiZeitintervall.getFahrradfahrer(), equalTo(123));
        assertThat(gesamtZeitintervall.getHochrechnung(), notNullValue());
        assertThat(gesamtZeitintervall.getHochrechnung().getHochrechnungRad(), equalTo(123));
    }

    private List<Zeitintervall> createZeitintervalle(final int anzahl) {
        final UUID zaehlungId = UUID.randomUUID();
        final UUID bewegungsbeziehungId = UUID.randomUUID();
        final Verkehrsbeziehung verkehrsbeziehung = new Verkehrsbeziehung();
        verkehrsbeziehung.setVon(1);
        verkehrsbeziehung.setNach(2);
        final List<Zeitintervall> zeitintervalle = new ArrayList<>();
        final LocalDateTime start = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0));
        for (int index = 0; index < anzahl; index++) {
            final LocalDateTime startUhrzeit = start.plusMinutes(index * 15L);
            zeitintervalle.add(Zeitintervall.builder()
                    .zaehlungId(zaehlungId)
                    .bewegungsbeziehungId(bewegungsbeziehungId)
                    .verkehrsbeziehung(verkehrsbeziehung)
                    .startUhrzeit(startUhrzeit)
                    .endeUhrzeit(startUhrzeit.plusMinutes(15))
                    .fahrradfahrer(index)
                    .hochrechnung(new Hochrechnung())
                    .build());
        }
        return zeitintervalle;
    }

}
