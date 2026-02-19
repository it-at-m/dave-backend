package de.muenchen.dave.services.ladezaehldaten;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.TestUtils;
import de.muenchen.dave.domain.*;
import de.muenchen.dave.util.DaveConstants;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import org.junit.jupiter.api.Test;

public class ZaehldatenSummationServeTest {

    private final ZeitintervallSummationService zeitintervallSummationService = new ZeitintervallSummationService();

    private final LocalDateTime time1 = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0));
    private final LocalDateTime time2 = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 15));

    @Test
    void sumZeitintervelleOverBewegungsbeziehung() {
        final var intervall11 = TestUtils.createZeitintervall(UUID.randomUUID(), time1, 20, 1, 2, null);
        final var intervall12 = TestUtils.createZeitintervall(UUID.randomUUID(), time2, 50, 1, 3, null);

        final var intervall21 = TestUtils.createZeitintervall(UUID.randomUUID(), time1, 30, 1, 2, null);
        final var intervall22 = TestUtils.createZeitintervall(UUID.randomUUID(), time2, 70, 1, 3, null);

        final var laengsverkehr1 = new Laengsverkehr();
        laengsverkehr1.setKnotenarm(1);
        final var laengsverkehr2 = new Laengsverkehr();
        laengsverkehr2.setKnotenarm(2);

        intervall11.setSortingIndex(11008006);
        intervall11.setLaengsverkehr(laengsverkehr1);
        intervall21.setSortingIndex(11008006);
        intervall21.setLaengsverkehr(laengsverkehr2);

        intervall12.setSortingIndex(11008000);
        intervall12.setLaengsverkehr(laengsverkehr1);
        ;
        intervall22.setSortingIndex(11008000);
        intervall22.setLaengsverkehr(laengsverkehr2);

        final var zeitintervalleByBewegungsbeziehung = new HashMap<Bewegungsbeziehung, List<Zeitintervall>>();
        zeitintervalleByBewegungsbeziehung.put(laengsverkehr1, List.of(intervall11, intervall12));
        zeitintervalleByBewegungsbeziehung.put(laengsverkehr2, List.of(intervall21, intervall22));

        final var result = zeitintervallSummationService.sumZeitintervelleOverBewegungsbeziehung(zeitintervalleByBewegungsbeziehung);

        final var expectedIntervall1 = TestUtils.createZeitintervall(intervall11.getZaehlungId(), time1, 50, 1, 2, null);
        expectedIntervall1.setSortingIndex(11008006);
        expectedIntervall1.setBewegungsbeziehungId(null);
        expectedIntervall1.setVerkehrsbeziehung(null);
        final var expectedIntervall2 = TestUtils.createZeitintervall(intervall12.getZaehlungId(), time2, 120, 1, 2, null);
        expectedIntervall2.setSortingIndex(11008000);
        expectedIntervall2.setBewegungsbeziehungId(null);
        expectedIntervall2.setVerkehrsbeziehung(null);
        final var expected = List.of(expectedIntervall2, expectedIntervall1);

        assertThat(result, is(expected));
    }

    @Test
    void nullSafeSummationWithHochrechnung() {
        final var intervall1 = TestUtils.createZeitintervall(UUID.randomUUID(), time1, 20, 1, 2, null);
        final var intervall2 = TestUtils.createZeitintervall(UUID.randomUUID(), time2, 50, 1, 3, null);

        final var result = zeitintervallSummationService.nullSafeSummation(intervall1, intervall2);

        final var expected = TestUtils.createZeitintervall(intervall1.getZaehlungId(), time1, 70, 1, 2, null);
        expected.setBewegungsbeziehungId(null);

        assertThat(result, is(expected));
    }

    @Test
    void nullSafeSummationWithHochrechnungOnlyInFirstIntervall() {
        final var intervall1 = TestUtils.createZeitintervall(UUID.randomUUID(), time1, 20, 1, 2, null);
        final var intervall2 = TestUtils.createZeitintervall(UUID.randomUUID(), time2, 50, 1, 3, null);
        intervall2.setHochrechnung(null);

        final var result = zeitintervallSummationService.nullSafeSummation(intervall1, intervall2);

        final var expected = TestUtils.createZeitintervall(intervall1.getZaehlungId(), time1, 70, 1, 2, null);
        final var expectedHochrechnung = new Hochrechnung();
        expectedHochrechnung.setHochrechnungKfz(BigDecimal.valueOf(20));
        expectedHochrechnung.setHochrechnungSv(BigDecimal.valueOf(20));
        expectedHochrechnung.setHochrechnungGv(BigDecimal.valueOf(20));
        expected.setHochrechnung(expectedHochrechnung);
        expected.setBewegungsbeziehungId(null);

        assertThat(result, is(expected));
    }

    @Test
    void nullSafeSummationWithHochrechnungOnlyInSecondIntervall() {
        final var intervall1 = TestUtils.createZeitintervall(UUID.randomUUID(), time1, 20, 1, 2, null);
        intervall1.setHochrechnung(null);
        final var intervall2 = TestUtils.createZeitintervall(UUID.randomUUID(), time2, 50, 1, 3, null);

        final var result = zeitintervallSummationService.nullSafeSummation(intervall1, intervall2);

        final var expected = TestUtils.createZeitintervall(intervall1.getZaehlungId(), time1, 70, 1, 2, null);
        final var expectedHochrechnung = new Hochrechnung();
        expectedHochrechnung.setHochrechnungKfz(BigDecimal.valueOf(50));
        expectedHochrechnung.setHochrechnungSv(BigDecimal.valueOf(50));
        expectedHochrechnung.setHochrechnungGv(BigDecimal.valueOf(50));
        expected.setHochrechnung(expectedHochrechnung);
        expected.setBewegungsbeziehungId(null);

        assertThat(result, is(expected));
    }

    @Test
    void nullSafeSummationWithoutHochrechnung() {
        final var intervall1 = TestUtils.createZeitintervall(UUID.randomUUID(), time1, 20, 1, 2, null);
        intervall1.setHochrechnung(null);
        final var intervall2 = TestUtils.createZeitintervall(UUID.randomUUID(), time2, 50, 1, 3, null);
        intervall2.setHochrechnung(null);

        final var result = zeitintervallSummationService.nullSafeSummation(intervall1, intervall2);

        final var expected = TestUtils.createZeitintervall(intervall1.getZaehlungId(), time1, 70, 1, 2, null);
        expected.setHochrechnung(new Hochrechnung());
        expected.setBewegungsbeziehungId(null);

        assertThat(result, is(expected));
    }

    @Test
    void invertMap() {
        final var intervall11 = TestUtils.createZeitintervall(UUID.randomUUID(), time1, 20, 1, 2, null);
        final var intervall12 = TestUtils.createZeitintervall(UUID.randomUUID(), time2, 50, 1, 3, null);

        final var intervall21 = TestUtils.createZeitintervall(UUID.randomUUID(), time1, 30, 1, 2, null);
        final var intervall22 = TestUtils.createZeitintervall(UUID.randomUUID(), time2, 70, 1, 3, null);

        final var laengsverkehr1 = new Laengsverkehr();
        laengsverkehr1.setKnotenarm(1);
        final var laengsverkehr2 = new Laengsverkehr();
        laengsverkehr2.setKnotenarm(2);

        intervall11.setSortingIndex(11008006);
        intervall11.setLaengsverkehr(laengsverkehr1);
        intervall21.setSortingIndex(11008006);
        intervall21.setLaengsverkehr(laengsverkehr2);

        intervall12.setSortingIndex(11008000);
        intervall12.setLaengsverkehr(laengsverkehr1);
        ;
        intervall22.setSortingIndex(11008000);
        intervall22.setLaengsverkehr(laengsverkehr2);

        final var zeitintervalleByBewegungsbeziehung = new HashMap<Bewegungsbeziehung, List<Zeitintervall>>();
        zeitintervalleByBewegungsbeziehung.put(laengsverkehr1, List.of(intervall11, intervall12));
        zeitintervalleByBewegungsbeziehung.put(laengsverkehr2, List.of(intervall21, intervall22));

        final var result = zeitintervallSummationService.invertMap(zeitintervalleByBewegungsbeziehung);

        final var expected = new HashMap<Integer, List<Zeitintervall>>();
        expected.put(11008000, List.of(intervall12, intervall22));
        expected.put(11008006, List.of(intervall11, intervall21));

        assertThat(result, is(expected));
    }
}
