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

    LocalDateTime time1 = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0));
    LocalDateTime time2 = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 15));

    @Test
    void twoIntervals() {
        Verkehrsbeziehung movement1 = new Verkehrsbeziehung();
        movement1.setVon(1);
        movement1.setNach(2);
        Verkehrsbeziehung movement2 = new Verkehrsbeziehung();
        movement2.setVon(1);
        movement2.setNach(3);
        final UUID id = UUID.randomUUID();
        Zeitintervall intervall11 = TestUtils.createZeitintervall(id, time1, 20, 1, 2, null);
        Zeitintervall intervall12 = TestUtils.createZeitintervall(id, time2, 50, 1, 3, null);

        Zeitintervall intervall21 = TestUtils.createZeitintervall(id, time1, 30, 1, 2, null);
        Zeitintervall intervall22 = TestUtils.createZeitintervall(id, time2, 70, 1, 3, null);

        //nicht korrekt berechnet
        intervall11.setSortingIndex(11008006);
        intervall21.setSortingIndex(11008006);

        intervall12.setSortingIndex(11008000);
        intervall22.setSortingIndex(11008000);

        intervall11.setBewegungsbeziehungId(UUID.randomUUID());
        intervall11.setLaengsverkehr(new Laengsverkehr());
        intervall11.setQuerungsverkehr(new Querungsverkehr());

        Map<Bewegungsbeziehung, List<Zeitintervall>> map = new HashMap<>();
        map.put(movement1, List.of(intervall11, intervall12));
        map.put(movement2, List.of(intervall21, intervall22));

        List<Zeitintervall> testIntervals = zeitintervallSummationService.sumZeitintervelleOverBewegungsbeziehung(map);

        Zeitintervall intervallCompare1 = TestUtils.createZeitintervall(id, time1, 50, 1, 2, null);
        Zeitintervall intervallCompare2 = TestUtils.createZeitintervall(id, time2, 120, 1, 3, null);

        intervallCompare1.setBewegungsbeziehungId(null);
        intervallCompare1.setVerkehrsbeziehung(null);

        intervallCompare2.setBewegungsbeziehungId(null);
        intervallCompare2.setVerkehrsbeziehung(null);

        intervallCompare1.setSortingIndex(11008006);
        intervallCompare2.setSortingIndex(11008000);

        List<Zeitintervall> compare = List.of(intervallCompare1, intervallCompare2);

        //ZählungsID noch fehlerhaft in der zu testenden Klasse
        assertThat(testIntervals, is(compare));
    }

    @Test
    void nullSafeSummationWithHochrechnung() {
        final var intervall1 = TestUtils.createZeitintervall(UUID.randomUUID(), time1, 20, 1, 2, null);
        final var intervall2 = TestUtils.createZeitintervall(UUID.randomUUID(), time2, 50, 1, 3, null);

        final var result = zeitintervallSummationService.nullSafeSummation(intervall1, intervall2);

        final var expected  = TestUtils.createZeitintervall(intervall1.getZaehlungId(), time1, 70, 1, 2, null);
        expected.setBewegungsbeziehungId(null);

        assertThat(result, is(expected));
    }

    @Test
    void nullSafeSummationWithHochrechnungOnlyInFirstIntervall() {
        final var intervall1 = TestUtils.createZeitintervall(UUID.randomUUID(), time1, 20, 1, 2, null);
        final var intervall2 = TestUtils.createZeitintervall(UUID.randomUUID(), time2, 50, 1, 3, null);
        intervall2.setHochrechnung(null);

        final var result = zeitintervallSummationService.nullSafeSummation(intervall1, intervall2);

        final var expected  = TestUtils.createZeitintervall(intervall1.getZaehlungId(), time1, 70, 1, 2, null);
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

        final var expected  = TestUtils.createZeitintervall(intervall1.getZaehlungId(), time1, 70, 1, 2, null);
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

        final var expected  = TestUtils.createZeitintervall(intervall1.getZaehlungId(), time1, 70, 1, 2, null);
        expected.setHochrechnung(new Hochrechnung());
        expected.setBewegungsbeziehungId(null);

        assertThat(result, is(expected));
    }

    @Test
    void invertMap() {
        final var intervall11 = TestUtils.createZeitintervall(UUID.randomUUID(), time1, 20, 1, 2, null);
        final var  intervall12 = TestUtils.createZeitintervall(UUID.randomUUID(), time2, 50, 1, 3, null);

        final var  intervall21 = TestUtils.createZeitintervall(UUID.randomUUID(), time1, 30, 1, 2, null);
        final var  intervall22 = TestUtils.createZeitintervall(UUID.randomUUID(), time2, 70, 1, 3, null);

        final var laengsverkehr1 = new Laengsverkehr();
        laengsverkehr1.setKnotenarm(1);
        final var laengsverkehr2 = new Laengsverkehr();
        laengsverkehr2.setKnotenarm(2);

        intervall11.setSortingIndex(11008006);
        intervall11.setLaengsverkehr(laengsverkehr1);
        intervall21.setSortingIndex(11008006);
        intervall21.setLaengsverkehr(laengsverkehr2);

        intervall12.setSortingIndex(11008000);
        intervall12.setLaengsverkehr(laengsverkehr1);;
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
