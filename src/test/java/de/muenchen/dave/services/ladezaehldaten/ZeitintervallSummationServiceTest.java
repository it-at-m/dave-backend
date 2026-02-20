package de.muenchen.dave.services.ladezaehldaten;

import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.util.dataimport.ZeitintervallBaseUtil;
import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Bewegungsbeziehung;
import de.muenchen.dave.domain.Laengsverkehr;
import de.muenchen.dave.domain.Querungsverkehr;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZeitintervallSummationServiceTest {

    private final ZeitintervallSummationService service = new ZeitintervallSummationService();

    @Test
    void testNullSafeSummation_createsHochrechnungAndDelegatesToUtil() {
        // Prüft, dass nullSafeSummation fehlende Hochrechnung ergänzt und an die Utility delegiert.
        final var zi1 = new Zeitintervall();
        zi1.setPkw(1);
        zi1.setHochrechnung(null);

        final var zi2 = new Zeitintervall();
        zi2.setPkw(2);
        zi2.setHochrechnung(null);

        // Mock der statischen Utility-Methode und Rückgabe eines einfachen Ergebnisses
        try (MockedStatic<ZeitintervallBaseUtil> utilities = mockStatic(ZeitintervallBaseUtil.class)) {
            utilities.when(() -> ZeitintervallBaseUtil.summation(any(Zeitintervall.class), any(Zeitintervall.class)))
                    .thenAnswer(invocation -> {
                        final var a = invocation.getArgument(0, Zeitintervall.class);
                        final var b = invocation.getArgument(1, Zeitintervall.class);
                        final var res = new Zeitintervall();
                        res.setPkw((a.getPkw() == null ? 0 : a.getPkw()) + (b.getPkw() == null ? 0 : b.getPkw()));
                        return res;
                    });

            final var result = service.nullSafeSummation(zi1, zi2);

            // Nach Aufruf sollten die input-Objekte eine Hochrechnung besitzen
            assertNotNull(zi1.getHochrechnung());
            assertNotNull(zi2.getHochrechnung());

            // Das Ergebnis kommt aus der Utility (gemockt) und enthält die summierten PKW
            assertNotNull(result);
            assertEquals(3, result.getPkw());

            // Utility wurde genau einmal aufgerufen
            utilities.verify(() -> ZeitintervallBaseUtil.summation(any(Zeitintervall.class), any(Zeitintervall.class)), times(1));
        }
    }

    @Test
    void testSumZeitintervelleOverBewegungsbeziehung_groupsAndSumsCorrectly() {
        // Bereite mehrere Bewegungsbeziehungen mit Zeitintervallen vor, die nach sortingIndex
        // gruppiert und addiert werden sollen.
        final var zbId = UUID.randomUUID();
        final var start = LocalDateTime.now().minusHours(1);
        final var ende = LocalDateTime.now();

        // Erzeuge Zeitintervalle für zwei verschiedene Bewegungsbeziehungen
        final var vb1 = new Verkehrsbeziehung();
        vb1.setVon(1);
        final var vb2 = new Verkehrsbeziehung();
        vb2.setVon(2);

        final var zi1 = Zeitintervall.builder().zaehlungId(zbId).startUhrzeit(start).endeUhrzeit(ende).sortingIndex(1).pkw(1).type(TypeZeitintervall.GESAMT).build();
        final var zi2 = Zeitintervall.builder().zaehlungId(zbId).startUhrzeit(start).endeUhrzeit(ende).sortingIndex(2).pkw(2).type(TypeZeitintervall.GESAMT).build();
        final var zi3 = Zeitintervall.builder().zaehlungId(zbId).startUhrzeit(start).endeUhrzeit(ende).sortingIndex(1).pkw(10).type(TypeZeitintervall.GESAMT).build();
        final var zi4 = Zeitintervall.builder().zaehlungId(zbId).startUhrzeit(start).endeUhrzeit(ende).sortingIndex(3).pkw(3).type(TypeZeitintervall.GESAMT).build();

        final var input = Map.of((Bewegungsbeziehung) vb1, List.of(zi1, zi2), (Bewegungsbeziehung) vb2, List.of(zi3, zi4));

        // Mock the static summation to perform numeric sums of PKW fields
        try (MockedStatic<ZeitintervallBaseUtil> utilities = mockStatic(ZeitintervallBaseUtil.class)) {
            utilities.when(() -> ZeitintervallBaseUtil.summation(any(Zeitintervall.class), any(Zeitintervall.class)))
                    .thenAnswer(invocation -> {
                        final var a = invocation.getArgument(0, Zeitintervall.class);
                        final var b = invocation.getArgument(1, Zeitintervall.class);
                        final var res = new Zeitintervall();
                        res.setPkw((a.getPkw() == null ? 0 : a.getPkw()) + (b.getPkw() == null ? 0 : b.getPkw()));
                        // type/start/ende/zaehlungId werden vom aufrufenden Code gesetzt, sortingIndex ebenfalls später
                        return res;
                    });

            final var result = service.sumZeitintervelleOverBewegungsbeziehung(input);

            // Es sollten drei Gruppen (sortingIndex 1,2,3) zurückgeliefert werden
            assertNotNull(result);
            assertEquals(3, result.size());

            // Ergebnis ist nach sortingIndex sortiert
            assertEquals(1, result.get(0).getSortingIndex());
            assertEquals(2, result.get(1).getSortingIndex());
            assertEquals(3, result.get(2).getSortingIndex());

            // PKW-Summen: index 1 -> 1 + 10 = 11, index 2 -> 2, index 3 -> 3
            assertEquals(11, result.get(0).getPkw());
            assertEquals(2, result.get(1).getPkw());
            assertEquals(3, result.get(2).getPkw());

            // Die Utility sollte genau so oft aufgerufen worden sein, wie es Zeitintervalle gibt (4 Aufrufe)
            utilities.verify(() -> ZeitintervallBaseUtil.summation(any(Zeitintervall.class), any(Zeitintervall.class)), times(4));
        }
    }

    @Test
    void testSumZeitintervelleOverBewegungsbeziehung_emptyInput_returnsEmptyList() {
        // Edge Case: Leere Eingabemap -> leere Ergebnisliste
        final var result = service.sumZeitintervelleOverBewegungsbeziehung(Map.of());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
