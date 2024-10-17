package de.muenchen.dave.util.dataimport;

import de.muenchen.dave.TestUtils;
import de.muenchen.dave.domain.Fahrbeziehung;
import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.util.DaveConstants;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ZeitintervallZeitblockSummationUtilTest {

    private List<Zeitintervall> zeitintervalle;

    private List<Zeitintervall> zeitintervalle12;

    private List<Zeitintervall> zeitintervalle21;

    private UUID zaehlungId;

    @BeforeEach
    public void beforeEach() {
        zaehlungId = UUID.randomUUID();
        zeitintervalle12 = new ArrayList<>();
        zeitintervalle21 = new ArrayList<>();
        LocalDateTime startTime = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(0, 0));
        for (int index = 0; index < 96; index++) {
            zeitintervalle12.add(TestUtils.createZeitintervall(
                    zaehlungId,
                    startTime,
                    1,
                    1,
                    2,
                    null));
            zeitintervalle21.add(TestUtils.createZeitintervall(
                    zaehlungId,
                    startTime,
                    2,
                    2,
                    1,
                    null));
            startTime = startTime.plusMinutes(15);
        }
        zeitintervalle = new ArrayList<>();
        zeitintervalle.addAll(zeitintervalle12);
        zeitintervalle.addAll(zeitintervalle21);
    }

    @Test
    public void getSummen() {
        final List<Zeitintervall> result = ZeitintervallZeitblockSummationUtil.getSummen(zeitintervalle);
        // Zeitblock.values().length * 2 - 4 -> Anzahl der Zeitblöcke je Fahrbeziehung
        // abzüglich der beiden ZB_06_19 und ZB_06_22 je Fahrbeziehung.
        assertThat(result.size(), is(Zeitblock.values().length * 2 - 4));

        // List for each Fahrbeziehung in result has same sorting as Zeitblock-Enum entries

        Zeitintervall expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(Zeitblock.ZB_10_15.getStart());
        expected.setEndeUhrzeit(Zeitblock.ZB_10_15.getEnd());
        expected.setSortingIndex(35000000);
        expected.setPkw(40);
        expected.setLkw(40);
        expected.setLastzuege(40);
        expected.setBusse(40);
        expected.setKraftraeder(40);
        expected.setFahrradfahrer(40);
        expected.setFussgaenger(40);
        expected.setType(TypeZeitintervall.BLOCK);
        expected.setHochrechnung(new Hochrechnung());
        expected.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(40));
        expected.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(40));
        expected.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(40));
        expected.setFahrbeziehung(new Fahrbeziehung());
        expected.getFahrbeziehung().setVon(2);
        expected.getFahrbeziehung().setNach(1);

        assertThat(result.get(2), is(expected));

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(Zeitblock.ZB_2300_2330.getStart());
        expected.setEndeUhrzeit(Zeitblock.ZB_2300_2330.getEnd());
        expected.setSortingIndex(51094092);
        expected.setPkw(4);
        expected.setLkw(4);
        expected.setLastzuege(4);
        expected.setBusse(4);
        expected.setKraftraeder(4);
        expected.setFahrradfahrer(4);
        expected.setFussgaenger(4);
        expected.setType(TypeZeitintervall.STUNDE_HALB);
        expected.setHochrechnung(new Hochrechnung());
        expected.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(4));
        expected.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(4));
        expected.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(4));
        expected.setFahrbeziehung(new Fahrbeziehung());
        expected.getFahrbeziehung().setVon(2);
        expected.getFahrbeziehung().setNach(1);

        assertThat(result.get(result.size() / 2 - 2), is(expected));

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(Zeitblock.ZB_10_15.getStart());
        expected.setEndeUhrzeit(Zeitblock.ZB_10_15.getEnd());
        expected.setSortingIndex(35000000);
        expected.setPkw(20);
        expected.setLkw(20);
        expected.setLastzuege(20);
        expected.setBusse(20);
        expected.setKraftraeder(20);
        expected.setFahrradfahrer(20);
        expected.setFussgaenger(20);
        expected.setType(TypeZeitintervall.BLOCK);
        expected.setHochrechnung(new Hochrechnung());
        expected.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(20));
        expected.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(20));
        expected.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(20));
        expected.setFahrbeziehung(new Fahrbeziehung());
        expected.getFahrbeziehung().setVon(1);
        expected.getFahrbeziehung().setNach(2);

        assertThat(result.get(result.size() / 2 + 2), is(expected));

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(Zeitblock.ZB_2300_2330.getStart());
        expected.setEndeUhrzeit(Zeitblock.ZB_2300_2330.getEnd());
        expected.setSortingIndex(51094092);
        expected.setPkw(2);
        expected.setLkw(2);
        expected.setLastzuege(2);
        expected.setBusse(2);
        expected.setKraftraeder(2);
        expected.setFahrradfahrer(2);
        expected.setFussgaenger(2);
        expected.setType(TypeZeitintervall.STUNDE_HALB);
        expected.setHochrechnung(new Hochrechnung());
        expected.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(2));
        expected.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(2));
        expected.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(2));
        expected.setFahrbeziehung(new Fahrbeziehung());
        expected.getFahrbeziehung().setVon(1);
        expected.getFahrbeziehung().setNach(2);

        assertThat(result.get(result.size() - 2), is(expected));
    }

    @Test
    public void getSummenForFahrbeziehung() {
        final Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> zeitintervalleGroupedByIntervall = ZeitintervallBaseUtil
                .createByIntervallGroupedZeitintervalle(zeitintervalle);
        final Fahrbeziehung fahrbeziehung = new Fahrbeziehung();
        fahrbeziehung.setVon(2);
        fahrbeziehung.setNach(1);

        List<Zeitintervall> result = TestUtils.privateStaticMethodCall(
                "getSummenForFahrbeziehung",
                ZeitintervallZeitblockSummationUtil.class,
                ArrayUtils.toArray(Fahrbeziehung.class, Map.class),
                ArrayUtils.toArray(fahrbeziehung, zeitintervalleGroupedByIntervall),
                List.class);

        // Anzahl der Zeitblöcke abzüglich der ZB_06_19 und ZB_06_22
        assertThat(result.size(), is(Zeitblock.values().length - 2));

        // List in result has same sorting as Zeitblock-Enum entries

        Zeitintervall expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(Zeitblock.ZB_10_15.getStart());
        expected.setEndeUhrzeit(Zeitblock.ZB_10_15.getEnd());
        expected.setSortingIndex(35000000);
        expected.setPkw(40);
        expected.setLkw(40);
        expected.setLastzuege(40);
        expected.setBusse(40);
        expected.setKraftraeder(40);
        expected.setFahrradfahrer(40);
        expected.setFussgaenger(40);
        expected.setType(TypeZeitintervall.BLOCK);
        expected.setHochrechnung(new Hochrechnung());
        expected.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(40));
        expected.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(40));
        expected.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(40));
        expected.setFahrbeziehung(new Fahrbeziehung());
        expected.getFahrbeziehung().setVon(2);
        expected.getFahrbeziehung().setNach(1);

        assertThat(result.get(2), is(expected));

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(Zeitblock.ZB_2300_2330.getStart());
        expected.setEndeUhrzeit(Zeitblock.ZB_2300_2330.getEnd());
        expected.setSortingIndex(51094092);
        expected.setPkw(4);
        expected.setLkw(4);
        expected.setLastzuege(4);
        expected.setBusse(4);
        expected.setKraftraeder(4);
        expected.setFahrradfahrer(4);
        expected.setFussgaenger(4);
        expected.setType(TypeZeitintervall.STUNDE_HALB);
        expected.setHochrechnung(new Hochrechnung());
        expected.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(4));
        expected.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(4));
        expected.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(4));
        expected.setFahrbeziehung(new Fahrbeziehung());
        expected.getFahrbeziehung().setVon(2);
        expected.getFahrbeziehung().setNach(1);

        assertThat(result.get(result.size() - 2), is(expected));
    }

    @Test
    public void getSumme() {
        final Fahrbeziehung fahrbeziehung = new Fahrbeziehung();
        fahrbeziehung.setVon(1);
        fahrbeziehung.setNach(2);
        Optional<Zeitintervall> result = TestUtils.privateStaticMethodCall(
                "getSumme",
                ZeitintervallZeitblockSummationUtil.class,
                ArrayUtils.toArray(UUID.class, Zeitblock.class, Fahrbeziehung.class, List.class),
                ArrayUtils.toArray(zaehlungId, Zeitblock.ZB_00_24, fahrbeziehung, zeitintervalle12),
                Optional.class);

        Zeitintervall expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(Zeitblock.ZB_00_24.getStart());
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 59)));
        expected.setSortingIndex(90000000);
        expected.setPkw(96);
        expected.setLkw(96);
        expected.setLastzuege(96);
        expected.setBusse(96);
        expected.setKraftraeder(96);
        expected.setFahrradfahrer(96);
        expected.setFussgaenger(96);
        expected.setType(TypeZeitintervall.GESAMT);
        expected.setHochrechnung(new Hochrechnung());
        expected.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(96));
        expected.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(96));
        expected.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(96));
        expected.setFahrbeziehung(new Fahrbeziehung());
        expected.getFahrbeziehung().setVon(1);
        expected.getFahrbeziehung().setNach(2);

        assertThat(result.get(), is(expected));

        result = TestUtils.privateStaticMethodCall(
                "getSumme",
                ZeitintervallZeitblockSummationUtil.class,
                ArrayUtils.toArray(UUID.class, Zeitblock.class, Fahrbeziehung.class, List.class),
                ArrayUtils.toArray(zaehlungId, Zeitblock.ZB_06_10, fahrbeziehung, zeitintervalle12),
                Optional.class);

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(Zeitblock.ZB_06_10.getStart());
        expected.setEndeUhrzeit(Zeitblock.ZB_06_10.getEnd());
        expected.setSortingIndex(25000000);
        expected.setPkw(16);
        expected.setLkw(16);
        expected.setLastzuege(16);
        expected.setBusse(16);
        expected.setKraftraeder(16);
        expected.setFahrradfahrer(16);
        expected.setFussgaenger(16);
        expected.setType(TypeZeitintervall.BLOCK);
        expected.setHochrechnung(new Hochrechnung());
        expected.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(16));
        expected.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(16));
        expected.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(16));
        expected.setFahrbeziehung(new Fahrbeziehung());
        expected.getFahrbeziehung().setVon(1);
        expected.getFahrbeziehung().setNach(2);

        assertThat(result.get(), is(expected));

        result = TestUtils.privateStaticMethodCall(
                "getSumme",
                ZeitintervallZeitblockSummationUtil.class,
                ArrayUtils.toArray(UUID.class, Zeitblock.class, Fahrbeziehung.class, List.class),
                ArrayUtils.toArray(zaehlungId, Zeitblock.ZB_0500_0530, fahrbeziehung, zeitintervalle12),
                Optional.class);

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(Zeitblock.ZB_0500_0530.getStart());
        expected.setEndeUhrzeit(Zeitblock.ZB_0500_0530.getEnd());
        expected.setSortingIndex(11022020);
        expected.setPkw(2);
        expected.setLkw(2);
        expected.setLastzuege(2);
        expected.setBusse(2);
        expected.setKraftraeder(2);
        expected.setFahrradfahrer(2);
        expected.setFussgaenger(2);
        expected.setType(TypeZeitintervall.STUNDE_HALB);
        expected.setHochrechnung(new Hochrechnung());
        expected.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(2));
        expected.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(2));
        expected.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(2));
        expected.setFahrbeziehung(new Fahrbeziehung());
        expected.getFahrbeziehung().setVon(1);
        expected.getFahrbeziehung().setNach(2);

        assertThat(result.get(), is(expected));

        result = TestUtils.privateStaticMethodCall(
                "getSumme",
                ZeitintervallZeitblockSummationUtil.class,
                ArrayUtils.toArray(UUID.class, Zeitblock.class, Fahrbeziehung.class, List.class),
                ArrayUtils.toArray(zaehlungId, Zeitblock.ZB_01_02, fahrbeziehung, zeitintervalle12.subList(0, 7)),
                Optional.class);

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(Zeitblock.ZB_01_02.getStart());
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 45)));
        expected.setSortingIndex(11008008);
        expected.setPkw(3);
        expected.setLkw(3);
        expected.setLastzuege(3);
        expected.setBusse(3);
        expected.setKraftraeder(3);
        expected.setFahrradfahrer(3);
        expected.setFussgaenger(3);
        expected.setType(TypeZeitintervall.STUNDE_KOMPLETT);
        expected.setHochrechnung(new Hochrechnung());
        expected.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(3));
        expected.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(3));
        expected.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(3));
        expected.setFahrbeziehung(new Fahrbeziehung());
        expected.getFahrbeziehung().setVon(1);
        expected.getFahrbeziehung().setNach(2);

        assertThat(result.get(), is(expected));

        result = TestUtils.privateStaticMethodCall(
                "getSumme",
                ZeitintervallZeitblockSummationUtil.class,
                ArrayUtils.toArray(UUID.class, Zeitblock.class, Fahrbeziehung.class, List.class),
                ArrayUtils.toArray(zaehlungId, Zeitblock.ZB_10_15, fahrbeziehung, zeitintervalle12.subList(0, 16)),
                Optional.class);

        assertThat(result.isPresent(), is(false));

    }

    @Test
    public void shouldZeitblockBeCreated() {
        final ZeitintervallZeitblockSummationUtil.StartEndeUhrzeit startEndeUhrzeit = new ZeitintervallZeitblockSummationUtil.StartEndeUhrzeit();

        startEndeUhrzeit.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 6, 0));
        startEndeUhrzeit.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 19, 0));
        boolean result = TestUtils.privateStaticMethodCall(
                "shouldZeitblockBeCreated",
                ZeitintervallZeitblockSummationUtil.class,
                ArrayUtils.toArray(Zeitblock.class, ZeitintervallZeitblockSummationUtil.StartEndeUhrzeit.class),
                ArrayUtils.toArray(Zeitblock.ZB_06_19, startEndeUhrzeit),
                Boolean.class);
        assertThat(result, is(true));

        startEndeUhrzeit.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 6, 0));
        startEndeUhrzeit.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 22, 0));
        result = TestUtils.privateStaticMethodCall(
                "shouldZeitblockBeCreated",
                ZeitintervallZeitblockSummationUtil.class,
                ArrayUtils.toArray(Zeitblock.class, ZeitintervallZeitblockSummationUtil.StartEndeUhrzeit.class),
                ArrayUtils.toArray(Zeitblock.ZB_06_22, startEndeUhrzeit),
                Boolean.class);
        assertThat(result, is(true));

        startEndeUhrzeit.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 5, 0));
        startEndeUhrzeit.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 19, 0));
        result = TestUtils.privateStaticMethodCall(
                "shouldZeitblockBeCreated",
                ZeitintervallZeitblockSummationUtil.class,
                ArrayUtils.toArray(Zeitblock.class, ZeitintervallZeitblockSummationUtil.StartEndeUhrzeit.class),
                ArrayUtils.toArray(Zeitblock.ZB_06_19, startEndeUhrzeit),
                Boolean.class);
        assertThat(result, is(false));

        startEndeUhrzeit.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 6, 0));
        startEndeUhrzeit.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 20, 0));
        result = TestUtils.privateStaticMethodCall(
                "shouldZeitblockBeCreated",
                ZeitintervallZeitblockSummationUtil.class,
                ArrayUtils.toArray(Zeitblock.class, ZeitintervallZeitblockSummationUtil.StartEndeUhrzeit.class),
                ArrayUtils.toArray(Zeitblock.ZB_06_19, startEndeUhrzeit),
                Boolean.class);
        assertThat(result, is(false));

        startEndeUhrzeit.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 5, 0));
        startEndeUhrzeit.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 22, 0));
        result = TestUtils.privateStaticMethodCall(
                "shouldZeitblockBeCreated",
                ZeitintervallZeitblockSummationUtil.class,
                ArrayUtils.toArray(Zeitblock.class, ZeitintervallZeitblockSummationUtil.StartEndeUhrzeit.class),
                ArrayUtils.toArray(Zeitblock.ZB_06_22, startEndeUhrzeit),
                Boolean.class);
        assertThat(result, is(false));

        startEndeUhrzeit.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 6, 0));
        startEndeUhrzeit.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 21, 0));
        result = TestUtils.privateStaticMethodCall(
                "shouldZeitblockBeCreated",
                ZeitintervallZeitblockSummationUtil.class,
                ArrayUtils.toArray(Zeitblock.class, ZeitintervallZeitblockSummationUtil.StartEndeUhrzeit.class),
                ArrayUtils.toArray(Zeitblock.ZB_06_22, startEndeUhrzeit),
                Boolean.class);
        assertThat(result, is(false));

        startEndeUhrzeit.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 6, 0));
        startEndeUhrzeit.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 21, 0));
        result = TestUtils.privateStaticMethodCall(
                "shouldZeitblockBeCreated",
                ZeitintervallZeitblockSummationUtil.class,
                ArrayUtils.toArray(Zeitblock.class, ZeitintervallZeitblockSummationUtil.StartEndeUhrzeit.class),
                ArrayUtils.toArray(Zeitblock.ZB_00_06, startEndeUhrzeit),
                Boolean.class);
        assertThat(result, is(true));

        startEndeUhrzeit.setStartUhrzeit(null);
        startEndeUhrzeit.setEndeUhrzeit(null);
        result = TestUtils.privateStaticMethodCall(
                "shouldZeitblockBeCreated",
                ZeitintervallZeitblockSummationUtil.class,
                ArrayUtils.toArray(Zeitblock.class, ZeitintervallZeitblockSummationUtil.StartEndeUhrzeit.class),
                ArrayUtils.toArray(Zeitblock.ZB_00_06, startEndeUhrzeit),
                Boolean.class);
        assertThat(result, is(true));

    }

    @Test
    public void getStartAndEndeuhrzeit() {
        // Zeitintervalle von 3:15 bis 4:45
        List<Zeitintervall> zeitintervalle = zeitintervalle12.subList(13, 19);

        ZeitintervallZeitblockSummationUtil.StartEndeUhrzeit result = TestUtils.privateStaticMethodCall(
                "getStartAndEndeuhrzeit",
                ZeitintervallZeitblockSummationUtil.class,
                ArrayUtils.toArray(List.class),
                ArrayUtils.toArray(zeitintervalle),
                ZeitintervallZeitblockSummationUtil.StartEndeUhrzeit.class);

        ZeitintervallZeitblockSummationUtil.StartEndeUhrzeit expected = new ZeitintervallZeitblockSummationUtil.StartEndeUhrzeit();
        expected.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 3, 15));
        expected.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 4, 45));

        assertThat(result, is(expected));

        // Zeitintervall von 03:15 bis 03:30
        zeitintervalle = zeitintervalle12.subList(13, 14);

        result = TestUtils.privateStaticMethodCall(
                "getStartAndEndeuhrzeit",
                ZeitintervallZeitblockSummationUtil.class,
                ArrayUtils.toArray(List.class),
                ArrayUtils.toArray(zeitintervalle),
                ZeitintervallZeitblockSummationUtil.StartEndeUhrzeit.class);

        expected = new ZeitintervallZeitblockSummationUtil.StartEndeUhrzeit();
        expected.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 3, 15));
        expected.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 3, 30));

        assertThat(result, is(expected));

        // Zeitintervall von 00:15 bis 00:45
        zeitintervalle = zeitintervalle12.subList(1, 3);

        result = TestUtils.privateStaticMethodCall(
                "getStartAndEndeuhrzeit",
                ZeitintervallZeitblockSummationUtil.class,
                ArrayUtils.toArray(List.class),
                ArrayUtils.toArray(zeitintervalle),
                ZeitintervallZeitblockSummationUtil.StartEndeUhrzeit.class);

        expected = new ZeitintervallZeitblockSummationUtil.StartEndeUhrzeit();
        expected.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 0, 15));
        expected.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 0, 45));

        assertThat(result, is(expected));

        // Leere Liste
        zeitintervalle = new ArrayList<>();

        result = TestUtils.privateStaticMethodCall(
                "getStartAndEndeuhrzeit",
                ZeitintervallZeitblockSummationUtil.class,
                ArrayUtils.toArray(List.class),
                ArrayUtils.toArray(zeitintervalle),
                ZeitintervallZeitblockSummationUtil.StartEndeUhrzeit.class);

        expected = new ZeitintervallZeitblockSummationUtil.StartEndeUhrzeit();

        assertThat(result, is(expected));

    }

}
