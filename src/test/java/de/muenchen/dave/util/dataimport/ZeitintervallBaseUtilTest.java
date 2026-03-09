package de.muenchen.dave.util.dataimport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.TestUtils;
import de.muenchen.dave.domain.Bewegungsbeziehung;
import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Laengsverkehr;
import de.muenchen.dave.domain.Querungsverkehr;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.Bewegungsrichtung;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.util.DaveConstants;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ZeitintervallBaseUtilTest {

    private List<Zeitintervall> zeitintervalle;

    @BeforeEach
    public void beforeEach() {
        final UUID zaehlungId = UUID.randomUUID();
        zeitintervalle = new ArrayList<>();
        zeitintervalle.add(TestUtils.createZeitintervall(
                zaehlungId,
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)),
                1,
                1,
                2,
                null));
        zeitintervalle.add(TestUtils.createZeitintervall(
                zaehlungId,
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)),
                2,
                1,
                3,
                null));
        zeitintervalle.add(TestUtils.createZeitintervall(
                zaehlungId,
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)),
                3,
                2,
                1,
                null));
        zeitintervalle.add(TestUtils.createZeitintervall(
                zaehlungId,
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)),
                4,
                2,
                3,
                null));
        zeitintervalle.add(TestUtils.createZeitintervall(
                zaehlungId,
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)),
                5,
                3,
                1,
                null));
        zeitintervalle.add(TestUtils.createZeitintervall(
                zaehlungId,
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)),
                6,
                1,
                2,
                null));
        zeitintervalle.add(TestUtils.createZeitintervall(
                zaehlungId,
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)),
                7,
                1,
                3,
                null));
        zeitintervalle.add(TestUtils.createZeitintervall(
                zaehlungId,
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)),
                8,
                2,
                1,
                null));
        zeitintervalle.add(TestUtils.createZeitintervall(
                zaehlungId,
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)),
                9,
                2,
                3,
                null));
        zeitintervalle.add(TestUtils.createZeitintervall(
                zaehlungId,
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)),
                10,
                3,
                1,
                null));
    }

    @Test
    public void createByIntervallGroupedZeitintervalle() {
        final Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> result = ZeitintervallBaseUtil.createByIntervallGroupedZeitintervalle(zeitintervalle);

        final ZeitintervallBaseUtil.Intervall firstIntervall = new ZeitintervallBaseUtil.Intervall(
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        final ZeitintervallBaseUtil.Intervall secondIntervall = new ZeitintervallBaseUtil.Intervall(
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));

        final TreeSet<ZeitintervallBaseUtil.Intervall> expectedKeys = new TreeSet<>();
        expectedKeys.add(firstIntervall);
        expectedKeys.add(secondIntervall);

        assertThat(result.keySet(), is(expectedKeys));
        assertThat(result.get(firstIntervall), is(zeitintervalle.subList(0, 5)));
        assertThat(result.get(secondIntervall), is(zeitintervalle.subList(5, 10)));
    }

    @Test
    public void summation() {
        zeitintervalle.get(zeitintervalle.size() - 1).setPkw(null);
        zeitintervalle.get(zeitintervalle.size() - 1).setLkw(null);
        zeitintervalle.get(zeitintervalle.size() - 2).setLkw(null);

        final Zeitintervall summed = TestUtils.privateStaticMethodCall(
                "summation",
                ZeitintervallBaseUtil.class,
                ArrayUtils.toArray(Zeitintervall.class, Zeitintervall.class),
                ArrayUtils.toArray(zeitintervalle.get(zeitintervalle.size() - 1), zeitintervalle.get(zeitintervalle.size() - 2)),
                Zeitintervall.class);

        final Zeitintervall expected = new Zeitintervall();
        expected.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        expected.setPkw(9);
        expected.setLkw(null);
        expected.setLastzuege(19);
        expected.setBusse(19);
        expected.setKraftraeder(19);
        expected.setFahrradfahrer(19);
        expected.setFussgaenger(19);
        expected.setType(TypeZeitintervall.STUNDE_VIERTEL);
        expected.setHochrechnung(new Hochrechnung());
        expected.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(19));
        expected.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(19));
        expected.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(19));
        expected.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.getVerkehrsbeziehung().setVon(3);
        expected.getVerkehrsbeziehung().setNach(1);

        assertThat(summed, is(expected));
    }

    @Test
    public void getZeitintervalleForBewegungsbeziehung() {
        Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> zeitintervalleGroupedByIntervall = new TreeMap<>();
        ZeitintervallBaseUtil.Intervall intervall = new ZeitintervallBaseUtil.Intervall(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 0)),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 15)));
        zeitintervalleGroupedByIntervall.put(intervall, zeitintervalle.subList(0, 5));
        intervall = new ZeitintervallBaseUtil.Intervall(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 15)),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        zeitintervalleGroupedByIntervall.put(intervall, zeitintervalle.subList(5, 10));

        Verkehrsbeziehung verkehrsbeziehung = new Verkehrsbeziehung();
        verkehrsbeziehung.setVon(2);
        verkehrsbeziehung.setNach(3);
        verkehrsbeziehung.setFahrbewegungKreisverkehr(null);

        List<Zeitintervall> result = ZeitintervallBaseUtil.getZeitintervalleForBewegungsbeziehung(verkehrsbeziehung, zeitintervalleGroupedByIntervall);

        List<Zeitintervall> expected = Arrays.asList(
                zeitintervalle.get(3),
                zeitintervalle.get(8));
        assertThat(result, is(expected));
    }

    @Test
    public void isZeitintervallWithinZeitblock() {
        Zeitintervall zeitintervall = new Zeitintervall();
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(7, 0)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(7, 30)));
        boolean result = ZeitintervallBaseUtil.isZeitintervallWithinZeitblock(zeitintervall, Zeitblock.ZB_06_10);
        assertThat(result, is(true));

        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 30)));
        result = ZeitintervallBaseUtil.isZeitintervallWithinZeitblock(zeitintervall, Zeitblock.ZB_06_10);
        assertThat(result, is(true));

        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(9, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 0)));
        result = ZeitintervallBaseUtil.isZeitintervallWithinZeitblock(zeitintervall, Zeitblock.ZB_06_10);
        assertThat(result, is(true));

        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 0)));
        result = ZeitintervallBaseUtil.isZeitintervallWithinZeitblock(zeitintervall, Zeitblock.ZB_06_10);
        assertThat(result, is(true));

        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(5, 45)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 15)));
        result = ZeitintervallBaseUtil.isZeitintervallWithinZeitblock(zeitintervall, Zeitblock.ZB_06_10);
        assertThat(result, is(false));

        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(5, 45)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)));
        result = ZeitintervallBaseUtil.isZeitintervallWithinZeitblock(zeitintervall, Zeitblock.ZB_06_10);
        assertThat(result, is(false));

        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(9, 45)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 15)));
        result = ZeitintervallBaseUtil.isZeitintervallWithinZeitblock(zeitintervall, Zeitblock.ZB_06_10);
        assertThat(result, is(false));

        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 0)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        result = ZeitintervallBaseUtil.isZeitintervallWithinZeitblock(zeitintervall, Zeitblock.ZB_06_10);
        assertThat(result, is(false));

        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 30)));
        result = ZeitintervallBaseUtil.isZeitintervallWithinZeitblock(zeitintervall, Zeitblock.ZB_06_10);
        assertThat(result, is(false));
    }

    @Test
    public void checkAndCorrectEndeuhrzeitForLastZeitintervallOfDayIfNecessary() {
        final Zeitintervall input = new Zeitintervall();
        input.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 45)));
        input.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 58)));
        Zeitintervall result = ZeitintervallBaseUtil.checkAndCorrectEndeuhrzeitForLastZeitintervallOfDayIfNecessary(input);
        Zeitintervall expected = new Zeitintervall();
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 45)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 58)));
        assertThat(result, is(expected));

        input.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 45)));
        input.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 59)));
        result = ZeitintervallBaseUtil.checkAndCorrectEndeuhrzeitForLastZeitintervallOfDayIfNecessary(input);
        expected = new Zeitintervall();
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 45)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 59)));
        assertThat(result, is(expected));

        input.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 45)));
        input.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.MAX));
        result = ZeitintervallBaseUtil.checkAndCorrectEndeuhrzeitForLastZeitintervallOfDayIfNecessary(input);
        expected = new Zeitintervall();
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 45)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 59)));
        assertThat(result, is(expected));

        input.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 45)));
        input.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.MIN));
        result = ZeitintervallBaseUtil.checkAndCorrectEndeuhrzeitForLastZeitintervallOfDayIfNecessary(input);
        expected = new Zeitintervall();
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 45)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 59)));
        assertThat(result, is(expected));
    }

    @Test
    public void isSameBewegungsbeziehungAndBothBewegungsbeziehung_AreNotNull_OrBothNull_variousCases() {
        // beide null -> true
        assertThat(ZeitintervallBaseUtil.isSameBewegungsbeziehungOrBothNull(null, null), is(true));

        // ein Argument ist null -> false
        final Laengsverkehr l1 = new Laengsverkehr();
        l1.setRichtung(Bewegungsrichtung.EIN);
        l1.setStrassenseite(Himmelsrichtung.N);
        assertThat(ZeitintervallBaseUtil.isSameBewegungsbeziehungOrBothNull(l1, null), is(false));
        assertThat(ZeitintervallBaseUtil.isSameBewegungsbeziehungOrBothNull(null, l1), is(false));

        // gleiche Referenz -> true
        assertThat(ZeitintervallBaseUtil.isSameBewegungsbeziehungOrBothNull(l1, l1), is(true));

        // unterschiedliche Instanzen mit gleichen Werten -> true
        final Laengsverkehr l2 = new Laengsverkehr();
        l2.setRichtung(Bewegungsrichtung.EIN);
        l2.setStrassenseite(Himmelsrichtung.N);
        assertThat(ZeitintervallBaseUtil.isSameBewegungsbeziehungOrBothNull(l1, l2), is(true));

        // unterschiedliche konkrete Typen -> false
        final Querungsverkehr q1 = new Querungsverkehr();
        q1.setRichtung(Himmelsrichtung.N);
        assertThat(ZeitintervallBaseUtil.isSameBewegungsbeziehungOrBothNull(l1, q1), is(false));

        // unterschiedliche Feldwerte -> false
        final Laengsverkehr l3 = new Laengsverkehr();
        l3.setRichtung(Bewegungsrichtung.AUS);
        l3.setStrassenseite(Himmelsrichtung.N);
        assertThat(ZeitintervallBaseUtil.isSameBewegungsbeziehungOrBothNull(l1, l3), is(false));
    }

    @Test
    public void areZeitintervallWithSameBewegungsbeziehung_variousCases() {
        // gleiche Bewegungsbeziehungen vorbereiten
        final Verkehrsbeziehung v1 = new Verkehrsbeziehung();
        v1.setVon(1);
        v1.setNach(2);
        v1.setFahrbewegungKreisverkehr(null);
        final Verkehrsbeziehung v2 = new Verkehrsbeziehung();
        v2.setVon(1);
        v2.setNach(2);
        v2.setFahrbewegungKreisverkehr(null);

        final Laengsverkehr l1 = new Laengsverkehr();
        l1.setRichtung(Bewegungsrichtung.EIN);
        l1.setStrassenseite(Himmelsrichtung.N);
        final Laengsverkehr l2 = new Laengsverkehr();
        l2.setRichtung(Bewegungsrichtung.EIN);
        l2.setStrassenseite(Himmelsrichtung.N);

        final Querungsverkehr q1 = new Querungsverkehr();
        q1.setRichtung(Himmelsrichtung.N);
        final Querungsverkehr q2 = new Querungsverkehr();
        q2.setRichtung(Himmelsrichtung.N);

        final Zeitintervall z1 = new Zeitintervall();
        z1.setVerkehrsbeziehung(v1);
        z1.setLaengsverkehr(l1);
        z1.setQuerungsverkehr(q1);

        final Zeitintervall z2 = new Zeitintervall();
        z2.setVerkehrsbeziehung(v2);
        z2.setLaengsverkehr(l2);
        z2.setQuerungsverkehr(q2);

        // alle drei gleich -> true
        assertThat(ZeitintervallBaseUtil.areZeitintervallWithSameBewegungsbeziehung(z1, z2), is(true));

        // laengsverkehr unterscheidet sich -> false (alle drei müssen gleich sein)
        l2.setRichtung(Bewegungsrichtung.AUS);
        assertThat(ZeitintervallBaseUtil.areZeitintervallWithSameBewegungsbeziehung(z1, z2), is(false));

        // Szenario: in einem Zeitintervall ist immer nur entweder Laengsverkehr, Querungsverkehr oder Verkehrsbeziehung gesetzt.
        // Nur Laengsverkehr in beiden gesetzt und gleich -> true
        z1.setVerkehrsbeziehung(null);
        z2.setVerkehrsbeziehung(null);
        z1.setQuerungsverkehr(null);
        z2.setQuerungsverkehr(null);
        l2.setRichtung(Bewegungsrichtung.EIN);
        z1.setLaengsverkehr(l1);
        z2.setLaengsverkehr(l2);
        assertThat(ZeitintervallBaseUtil.areZeitintervallWithSameBewegungsbeziehung(z1, z2), is(true));

        // Nur Verkehrsbeziehung in beiden gesetzt und gleich -> true
        z1.setLaengsverkehr(null);
        z2.setLaengsverkehr(null);
        z1.setVerkehrsbeziehung(v1);
        z2.setVerkehrsbeziehung(v2);
        assertThat(ZeitintervallBaseUtil.areZeitintervallWithSameBewegungsbeziehung(z1, z2), is(true));

        // Nur Querungsverkehr in beiden gesetzt und gleich -> true
        z1.setVerkehrsbeziehung(null);
        z2.setVerkehrsbeziehung(null);
        z1.setQuerungsverkehr(q1);
        z2.setQuerungsverkehr(q2);
        assertThat(ZeitintervallBaseUtil.areZeitintervallWithSameBewegungsbeziehung(z1, z2), is(true));

        // Alle drei null in beiden -> true (Nulls werden als gleich betrachtet)
        z1.setQuerungsverkehr(null);
        z2.setQuerungsverkehr(null);
        assertThat(ZeitintervallBaseUtil.areZeitintervallWithSameBewegungsbeziehung(z1, z2), is(true));

        // Unterschiedliche Typen über die beiden Intervalle (z1 hat Laengsverkehr, z2 hat Verkehrsbeziehung) -> false
        z1.setLaengsverkehr(l1);
        z1.setVerkehrsbeziehung(null);
        z2.setLaengsverkehr(null);
        z2.setVerkehrsbeziehung(v1);
        assertThat(ZeitintervallBaseUtil.areZeitintervallWithSameBewegungsbeziehung(z1, z2), is(false));

        // Ein Bewegungsbeziehung gleich, eine andere null vs non-null -> false
        z1.setLaengsverkehr(l1);
        z2.setLaengsverkehr(null);
        z1.setVerkehrsbeziehung(null);
        z2.setVerkehrsbeziehung(null);
        z1.setQuerungsverkehr(null);
        z2.setQuerungsverkehr(null);
        assertThat(ZeitintervallBaseUtil.areZeitintervallWithSameBewegungsbeziehung(z1, z2), is(false));
    }

    @Test
    public void getAllPossibleBewegungsbeziehungen_variousCases() {
        // Leere Liste -> leeres Set
        assertThat(ZeitintervallBaseUtil.getAllPossibleBewegungsbeziehungen(List.of()), is(Set.of()));

        // Zeitintervalle mit jeweils genau einer Bewegungsbeziehung
        final Verkehrsbeziehung v = new Verkehrsbeziehung();
        v.setVon(1);
        v.setNach(2);
        v.setFahrbewegungKreisverkehr(null);

        final Laengsverkehr l = new Laengsverkehr();
        l.setRichtung(Bewegungsrichtung.EIN);
        l.setStrassenseite(Himmelsrichtung.N);

        final Querungsverkehr q = new Querungsverkehr();
        q.setRichtung(Himmelsrichtung.N);

        final Zeitintervall zV = new Zeitintervall();
        zV.setVerkehrsbeziehung(v);

        final Zeitintervall zL = new Zeitintervall();
        zL.setLaengsverkehr(l);

        final Zeitintervall zQ = new Zeitintervall();
        zQ.setQuerungsverkehr(q);

        final Set<Bewegungsbeziehung> result = ZeitintervallBaseUtil.getAllPossibleBewegungsbeziehungen(List.of(zV, zL, zQ));
        final Set<Bewegungsbeziehung> expected = new HashSet<>();
        expected.add(v);
        expected.add(l);
        expected.add(q);
        assertThat(result, is(expected));

        // Duplikate werden entfernt (zwei gleiche Verkehrsbeziehungen)
        final Verkehrsbeziehung vDup = new Verkehrsbeziehung();
        vDup.setVon(1);
        vDup.setNach(2);
        vDup.setFahrbewegungKreisverkehr(null);
        final Zeitintervall zVDup = new Zeitintervall();
        zVDup.setVerkehrsbeziehung(vDup);

        final Set<Bewegungsbeziehung> resultWithDup = ZeitintervallBaseUtil.getAllPossibleBewegungsbeziehungen(List.of(zV, zVDup));
        final Set<Bewegungsbeziehung> expectedWithDup = new HashSet<>();
        expectedWithDup.add(v);
        assertThat(resultWithDup, is(expectedWithDup));

        // Intervalle ohne Bewegungsbeziehung werden ignoriert
        final Zeitintervall zEmpty = new Zeitintervall();
        final Set<Bewegungsbeziehung> resultIgnore = ZeitintervallBaseUtil.getAllPossibleBewegungsbeziehungen(List.of(zEmpty, zV));
        assertThat(resultIgnore, is(expectedWithDup));

        // Falls ein Zeitintervall mehrere Bewegungsbeziehungen gesetzt hat, werden alle nicht-null gesammelt
        final Zeitintervall zAll = new Zeitintervall();
        zAll.setVerkehrsbeziehung(v);
        zAll.setLaengsverkehr(l);
        zAll.setQuerungsverkehr(q);
        final Set<Bewegungsbeziehung> resultAll = ZeitintervallBaseUtil.getAllPossibleBewegungsbeziehungen(List.of(zAll));
        final Set<Bewegungsbeziehung> expectedAll = new HashSet<>();
        expectedAll.add(v);
        expectedAll.add(l);
        expectedAll.add(q);
        assertThat(resultAll, is(expectedAll));
    }

}
