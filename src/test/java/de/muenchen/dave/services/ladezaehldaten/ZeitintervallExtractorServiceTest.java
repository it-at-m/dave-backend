package de.muenchen.dave.services.ladezaehldaten;

import de.muenchen.dave.domain.Laengsverkehr;
import de.muenchen.dave.domain.Querungsverkehr;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.external.ExternalLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.external.ExternalQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.external.ExternalVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.enums.Bewegungsrichtung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZeitintervallExtractorServiceTest {

    @Mock
    private ZeitintervallRepository zeitintervallRepository;

    private ZeitintervallExtractorService service;

    @BeforeEach
    void setUp() {
        // Mock an den Konstruktor übergeben wie in der Produktivklasse
        final var mockedRepo = zeitintervallRepository;
        service = new ZeitintervallExtractorService(mockedRepo);
    }

    @Test
    void testExtractZeitintervalle_FJS_callsRepositoryAndGroupsByLaengsverkehr() {
        // Testet den Pfad für Zaehlart.FJS. Es wird geprüft, dass die Repository-Methode mit den
        // erwarteten Parametern aufgerufen wird und die zurückgegebenen Zeitintervalle nach
        // Laengsverkehr gruppiert werden.
        final var zaehlungId = UUID.randomUUID();
        final var start = LocalDateTime.of(2022, 1, 1, 0, 0);
        final var ende = LocalDateTime.of(2022, 1, 1, 23, 59);

        final var chosen = new ExternalLaengsverkehrDTO();
        chosen.setKnotenarm(1);
        chosen.setRichtung(Bewegungsrichtung.EIN);
        chosen.setStrassenseite(Himmelsrichtung.N);

        final var options = new OptionsDTO();
        options.setChosenLaengsverkehre(List.of(chosen));

        final var lv = new Laengsverkehr();
        lv.setKnotenarm(1);
        lv.setRichtung(Bewegungsrichtung.EIN);
        lv.setStrassenseite(Himmelsrichtung.N);

        final var zi = Zeitintervall.builder()
                .zaehlungId(zaehlungId)
                .startUhrzeit(start)
                .endeUhrzeit(ende)
                .type(TypeZeitintervall.STUNDE_KOMPLETT)
                .laengsverkehr(lv)
                .build();

        final var types = Set.of(TypeZeitintervall.STUNDE_KOMPLETT);

        // Stub das Repository für die erwartete Methode
        when(zeitintervallRepository
                .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndLaengsverkehrKnotenarmAndLaengsverkehrRichtungAndLaengsverkehrStrassenseiteAndTypeInOrderBySortingIndexAsc(
                        eq(zaehlungId), eq(start), eq(ende), eq(1), eq(Bewegungsrichtung.EIN), eq(Himmelsrichtung.N), eq(types)))
                .thenReturn(List.of(zi));

        final var result = service.extractZeitintervalle(zaehlungId, Zaehlart.FJS, start, ende, false, options, types);

        // Erwartet eine Grouping-Map mit genau einem Key (der Laengsverkehr) und einer Liste mit
        // dem einen Zeitintervall
        assertNotNull(result);
        assertEquals(1, result.size());
        final var key = result.keySet().iterator().next();
        assertTrue(key instanceof Laengsverkehr);
        final var list = result.get(key);
        assertEquals(1, list.size());
        assertEquals(zi, list.get(0));

        // Prüfe, dass das Repository genau einmal mit den erwarteten Parametern aufgerufen wurde
        verify(zeitintervallRepository, times(1))
                .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndLaengsverkehrKnotenarmAndLaengsverkehrRichtungAndLaengsverkehrStrassenseiteAndTypeInOrderBySortingIndexAsc(
                        eq(zaehlungId), eq(start), eq(ende), eq(1), eq(Bewegungsrichtung.EIN), eq(Himmelsrichtung.N), eq(types));
        verifyNoMoreInteractions(zeitintervallRepository);
    }

    @Test
    void testExtractZeitintervalle_FJS_emptyChosenList_noRepositoryCall() {
        // Edge Case: Wenn keine Laengsverkehre gewählt wurden, darf das Repository nicht
        // aufgerufen werden und das Ergebnis muss leer sein.
        final var zaehlungId = UUID.randomUUID();
        final var start = LocalDateTime.now();
        final var ende = start.plusHours(1);

        final var options = new OptionsDTO();
        options.setChosenLaengsverkehre(null); // bewusst null

        final var types = Set.of(TypeZeitintervall.BLOCK);

        final var result = service.extractZeitintervalle(zaehlungId, Zaehlart.FJS, start, ende, false, options, types);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Repository darf in diesem Fall gar nicht verwendet werden
        verifyNoInteractions(zeitintervallRepository);
    }

    @Test
    void testExtractZeitintervalle_QU_callsRepositoryAndGroupsByQuerungsverkehr() {
        // Testet den Pfad für Zaehlart.QU: Querungsverkehr wird aus Options gelesen und
        // die passende Repository-Methode aufgerufen.
        final var zaehlungId = UUID.randomUUID();
        final var start = LocalDateTime.now().minusDays(1);
        final var ende = LocalDateTime.now();

        final var chosen = new ExternalQuerungsverkehrDTO();
        chosen.setKnotenarm(2);
        chosen.setRichtung(Himmelsrichtung.O);

        final var options = new OptionsDTO();
        options.setChosenQuerungsverkehre(List.of(chosen));

        final var qv = new Querungsverkehr();
        qv.setKnotenarm(2);
        qv.setRichtung(Himmelsrichtung.O);

        final var zi = Zeitintervall.builder()
                .zaehlungId(zaehlungId)
                .startUhrzeit(start)
                .endeUhrzeit(ende)
                .type(TypeZeitintervall.GESAMT)
                .querungsverkehr(qv)
                .build();

        final var types = Set.of(TypeZeitintervall.GESAMT);

        when(zeitintervallRepository
                .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndQuerungsverkehrKnotenarmAndQuerungsverkehrRichtungAndTypeInOrderBySortingIndexAsc(
                        eq(zaehlungId), eq(start), eq(ende), eq(2), eq(Himmelsrichtung.O), eq(types)))
                .thenReturn(List.of(zi));

        final var result = service.extractZeitintervalle(zaehlungId, Zaehlart.QU, start, ende, false, options, types);

        assertNotNull(result);
        assertEquals(1, result.size());
        final var key = result.keySet().iterator().next();
        assertTrue(key instanceof Querungsverkehr);
        final var list = result.get(key);
        assertEquals(1, list.size());
        assertEquals(zi, list.get(0));

        verify(zeitintervallRepository, times(1))
                .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndQuerungsverkehrKnotenarmAndQuerungsverkehrRichtungAndTypeInOrderBySortingIndexAsc(
                        eq(zaehlungId), eq(start), eq(ende), eq(2), eq(Himmelsrichtung.O), eq(types));
        verifyNoMoreInteractions(zeitintervallRepository);
    }

    @Test
    void testExtractZeitintervalle_QJS_callsRepositoryAndGroupsByVerkehrsbeziehung() {
        // Testet den Pfad für Zaehlart.QJS: Es werden mehrere Verkehrsbeziehungen aus den
        // Options verarbeitet und die Repository-Methode pro Eintrag aufgerufen.
        final var zaehlungId = UUID.randomUUID();
        final var start = LocalDateTime.now().minusHours(2);
        final var ende = LocalDateTime.now();

        final var chosen = new ExternalVerkehrsbeziehungDTO();
        chosen.setVon(3);
        chosen.setNach(4);
        chosen.setStrassenseite(Himmelsrichtung.S);

        final var options = new OptionsDTO();
        options.setChosenVerkehrsbeziehungen(List.of(chosen));

        final var vb = new Verkehrsbeziehung();
        vb.setVon(3);
        vb.setNach(4);
        vb.setStrassenseite(Himmelsrichtung.S);

        final var zi = Zeitintervall.builder()
                .zaehlungId(zaehlungId)
                .startUhrzeit(start)
                .endeUhrzeit(ende)
                .type(TypeZeitintervall.GESAMT)
                .verkehrsbeziehung(vb)
                .build();

        final var types = Set.of(TypeZeitintervall.GESAMT);

        when(zeitintervallRepository
                .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungNachAndTypeInAndVerkehrsbeziehungStrassenseiteOrderBySortingIndexAsc(
                        eq(zaehlungId), eq(start), eq(ende), eq(3), eq(4), eq(types), eq(Himmelsrichtung.S)))
                .thenReturn(List.of(zi));

        final var result = service.extractZeitintervalle(zaehlungId, Zaehlart.QJS, start, ende, false, options, types);

        assertNotNull(result);
        assertEquals(1, result.size());
        final var key = result.keySet().iterator().next();
        assertTrue(key instanceof Verkehrsbeziehung);
        final var list = result.get(key);
        assertEquals(1, list.size());
        assertEquals(zi, list.get(0));

        verify(zeitintervallRepository, times(1))
                .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungNachAndTypeInAndVerkehrsbeziehungStrassenseiteOrderBySortingIndexAsc(
                        eq(zaehlungId), eq(start), eq(ende), eq(3), eq(4), eq(types), eq(Himmelsrichtung.S));
        verifyNoMoreInteractions(zeitintervallRepository);
    }

    @Test
    void testExtractZeitintervalle_Kreisverkehr_hinein() {
        // Kreisverkehr-Fall: Optionen definieren nur `vonKnotenarm`. Es muss die Methode für
        // Kreisverkehr HINEIN mit FahrbewegungKreisverkehr.HINEIN aufgerufen werden.
        final var zaehlungId = UUID.randomUUID();
        final var start = LocalDateTime.now().minusHours(3);
        final var ende = LocalDateTime.now().minusHours(2);

        final var options = new OptionsDTO();
        options.setVonKnotenarm(5);
        options.setNachKnotenarm(null);

        final var vb = new Verkehrsbeziehung();
        vb.setVon(5);
        vb.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.HINEIN);

        final var zi = Zeitintervall.builder()
                .zaehlungId(zaehlungId)
                .startUhrzeit(start)
                .endeUhrzeit(ende)
                .type(TypeZeitintervall.BLOCK)
                .verkehrsbeziehung(vb)
                .build();

        final var types = Set.of(TypeZeitintervall.BLOCK);

        when(zeitintervallRepository
                .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungFahrbewegungKreisverkehrAndTypeInOrderBySortingIndexAsc(
                        eq(zaehlungId), eq(start), eq(ende), eq(5), eq(FahrbewegungKreisverkehr.HINEIN), eq(types)))
                .thenReturn(List.of(zi));

        final var result = service.extractZeitintervalle(zaehlungId, Zaehlart.N, start, ende, true, options, types);

        assertNotNull(result);
        assertEquals(1, result.size());
        final var key = result.keySet().iterator().next();
        assertTrue(key instanceof Verkehrsbeziehung);
        final var list = result.get(key);
        assertEquals(1, list.size());
        assertEquals(zi, list.get(0));

        verify(zeitintervallRepository, times(1))
                .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungFahrbewegungKreisverkehrAndTypeInOrderBySortingIndexAsc(
                        eq(zaehlungId), eq(start), eq(ende), eq(5), eq(FahrbewegungKreisverkehr.HINEIN), eq(types));
        verifyNoMoreInteractions(zeitintervallRepository);
    }

    @Test
    void testExtractZeitintervalle_Kreuzung_vonNach() {
        // Kreuzung-Fall: sowohl von als auch nach gesetzt -> spezifische Methode für von->nach
        final var zaehlungId = UUID.randomUUID();
        final var start = LocalDateTime.now().minusHours(6);
        final var ende = LocalDateTime.now().minusHours(5);

        final var options = new OptionsDTO();
        options.setVonKnotenarm(7);
        options.setNachKnotenarm(8);

        final var vb = new Verkehrsbeziehung();
        vb.setVon(7);
        vb.setNach(8);

        final var zi = Zeitintervall.builder()
                .zaehlungId(zaehlungId)
                .startUhrzeit(start)
                .endeUhrzeit(ende)
                .type(TypeZeitintervall.GESAMT)
                .verkehrsbeziehung(vb)
                .build();

        final var types = Set.of(TypeZeitintervall.GESAMT);

        when(zeitintervallRepository
                .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungNachAndTypeInOrderBySortingIndexAsc(
                        eq(zaehlungId), eq(start), eq(ende), eq(7), eq(8), eq(types)))
                .thenReturn(List.of(zi));

        final var result = service.extractZeitintervalle(zaehlungId, Zaehlart.N, start, ende, false, options, types);

        assertNotNull(result);
        assertEquals(1, result.size());
        final var key = result.keySet().iterator().next();
        assertTrue(key instanceof Verkehrsbeziehung);
        final var list = result.get(key);
        assertEquals(1, list.size());
        assertEquals(zi, list.get(0));

        verify(zeitintervallRepository, times(1))
                .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungNachAndTypeInOrderBySortingIndexAsc(
                        eq(zaehlungId), eq(start), eq(ende), eq(7), eq(8), eq(types));
        verifyNoMoreInteractions(zeitintervallRepository);
    }

}
