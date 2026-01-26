package de.muenchen.dave.util.dataimport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.TestUtils;
import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
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
    public void getAllPossibleFahrbeziehungen() {
        final Set<Verkehrsbeziehung> result = ZeitintervallBaseUtil.getAllPossibleFahrbeziehungen(zeitintervalle);

        final Set<Verkehrsbeziehung> expected = new HashSet<>();
        Verkehrsbeziehung fahrbeziehung = new Verkehrsbeziehung();
        fahrbeziehung.setVon(1);
        fahrbeziehung.setNach(2);
        fahrbeziehung.setFahrbewegungKreisverkehr(null);
        expected.add(fahrbeziehung);
        fahrbeziehung = new Verkehrsbeziehung();
        fahrbeziehung.setVon(1);
        fahrbeziehung.setNach(3);
        fahrbeziehung.setFahrbewegungKreisverkehr(null);
        expected.add(fahrbeziehung);
        fahrbeziehung = new Verkehrsbeziehung();
        fahrbeziehung.setVon(2);
        fahrbeziehung.setNach(1);
        fahrbeziehung.setFahrbewegungKreisverkehr(null);
        expected.add(fahrbeziehung);
        fahrbeziehung = new Verkehrsbeziehung();
        fahrbeziehung.setVon(2);
        fahrbeziehung.setNach(3);
        fahrbeziehung.setFahrbewegungKreisverkehr(null);
        expected.add(fahrbeziehung);
        fahrbeziehung = new Verkehrsbeziehung();
        fahrbeziehung.setVon(3);
        fahrbeziehung.setNach(1);
        fahrbeziehung.setFahrbewegungKreisverkehr(null);
        expected.add(fahrbeziehung);

        assertThat(result, is(expected));
    }

    @Test
    public void getZeitintervalleForFahrbeziehung() {
        Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> zeitintervalleGroupedByIntervall = new TreeMap<>();
        ZeitintervallBaseUtil.Intervall intervall = new ZeitintervallBaseUtil.Intervall(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 0)),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 15)));
        zeitintervalleGroupedByIntervall.put(intervall, zeitintervalle.subList(0, 5));
        intervall = new ZeitintervallBaseUtil.Intervall(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 15)),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        zeitintervalleGroupedByIntervall.put(intervall, zeitintervalle.subList(5, 10));

        Verkehrsbeziehung fahrbeziehung = new Verkehrsbeziehung();
        fahrbeziehung.setVon(2);
        fahrbeziehung.setNach(3);
        fahrbeziehung.setFahrbewegungKreisverkehr(null);

        List<Zeitintervall> result = ZeitintervallBaseUtil.getZeitintervalleForFahrbeziehung(fahrbeziehung, zeitintervalleGroupedByIntervall);

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

    //    @Test
    //    public void getPkwEinheit() {
    //        List<Zeitintervall> input = Arrays.asList(new Zeitintervall(), new Zeitintervall(), new Zeitintervall());
    //        PkwEinheit result = ZeitintervallBaseUtil.getPkwEinheit(input);
    //        assertThat(result, IsNull.nullValue());
    //
    //        final PkwEinheit expected = new PkwEinheit();
    //        expected.setFahrradfahrer(BigDecimal.TEN);
    //        input = Arrays.asList(new Zeitintervall(), new Zeitintervall(), new Zeitintervall());
    //        result = ZeitintervallBaseUtil.getPkwEinheit(input);
    //        assertThat(result, is(result));
    //    }

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

}
