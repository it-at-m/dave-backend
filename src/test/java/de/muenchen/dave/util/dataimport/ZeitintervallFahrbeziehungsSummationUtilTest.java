package de.muenchen.dave.util.dataimport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.TestUtils;
import de.muenchen.dave.domain.Fahrbeziehung;
import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.util.DaveConstants;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ZeitintervallFahrbeziehungsSummationUtilTest {

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
    public void createDataStructureForSummation() {
        final Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> result = TestUtils.privateStaticMethodCall(
                "createDataStructureForSummation",
                ZeitintervallFahrbeziehungsSummationUtil.class,
                ArrayUtils.toArray(List.class),
                ArrayUtils.toArray(zeitintervalle),
                Map.class);

        ZeitintervallBaseUtil.Intervall intervall1030 = new ZeitintervallBaseUtil.Intervall(
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        ZeitintervallBaseUtil.Intervall intervall1045 = new ZeitintervallBaseUtil.Intervall(
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        final Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> expected = new HashMap<>();
        expected.put(intervall1030, zeitintervalle.subList(0, 5));
        expected.put(intervall1045, zeitintervalle.subList(5, 10));

        assertThat(result, is(expected));

    }

    @Test
    public void filterValidFahrbeziehung() {
        Zeitintervall zeitintervall = new Zeitintervall();
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());

        // Fahrbeziehung Kreuzung
        zeitintervall.getFahrbeziehung().setVon(1);
        zeitintervall.getFahrbeziehung().setNach(2);
        zeitintervall.getFahrbeziehung().setFahrbewegungKreisverkehr(null);
        boolean result = TestUtils.privateStaticMethodCall(
                "filterValidFahrbeziehung",
                ZeitintervallFahrbeziehungsSummationUtil.class,
                ArrayUtils.toArray(Zeitintervall.class),
                ArrayUtils.toArray(zeitintervall),
                Boolean.class);
        assertThat(result, is(true));

        // Fahrbeziehung Kreisverkehr
        zeitintervall.getFahrbeziehung().setVon(1);
        zeitintervall.getFahrbeziehung().setNach(null);
        zeitintervall.getFahrbeziehung().setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.HINEIN);
        result = TestUtils.privateStaticMethodCall(
                "filterValidFahrbeziehung",
                ZeitintervallFahrbeziehungsSummationUtil.class,
                ArrayUtils.toArray(Zeitintervall.class),
                ArrayUtils.toArray(zeitintervall),
                Boolean.class);
        assertThat(result, is(true));

        // Keine gültige konkrete Fahrbeziehung
        zeitintervall.getFahrbeziehung().setVon(1);
        zeitintervall.getFahrbeziehung().setNach(null);
        zeitintervall.getFahrbeziehung().setFahrbewegungKreisverkehr(null);
        result = TestUtils.privateStaticMethodCall(
                "filterValidFahrbeziehung",
                ZeitintervallFahrbeziehungsSummationUtil.class,
                ArrayUtils.toArray(Zeitintervall.class),
                ArrayUtils.toArray(zeitintervall),
                Boolean.class);
        assertThat(result, is(false));

        // Keine gültige konkrete Fahrbeziehung
        zeitintervall.getFahrbeziehung().setVon(null);
        zeitintervall.getFahrbeziehung().setNach(null);
        zeitintervall.getFahrbeziehung().setFahrbewegungKreisverkehr(null);
        result = TestUtils.privateStaticMethodCall(
                "filterValidFahrbeziehung",
                ZeitintervallFahrbeziehungsSummationUtil.class,
                ArrayUtils.toArray(Zeitintervall.class),
                ArrayUtils.toArray(zeitintervall),
                Boolean.class);
        assertThat(result, is(false));

        // Keine gültige Fahrbeziehung
        zeitintervall.getFahrbeziehung().setVon(null);
        zeitintervall.getFahrbeziehung().setNach(1);
        zeitintervall.getFahrbeziehung().setFahrbewegungKreisverkehr(null);
        result = TestUtils.privateStaticMethodCall(
                "filterValidFahrbeziehung",
                ZeitintervallFahrbeziehungsSummationUtil.class,
                ArrayUtils.toArray(Zeitintervall.class),
                ArrayUtils.toArray(zeitintervall),
                Boolean.class);
        assertThat(result, is(false));

        // Keine gültige Fahrbeziehung
        zeitintervall.getFahrbeziehung().setVon(null);
        zeitintervall.getFahrbeziehung().setNach(null);
        zeitintervall.getFahrbeziehung().setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.HINEIN);
        result = TestUtils.privateStaticMethodCall(
                "filterValidFahrbeziehung",
                ZeitintervallFahrbeziehungsSummationUtil.class,
                ArrayUtils.toArray(Zeitintervall.class),
                ArrayUtils.toArray(zeitintervall),
                Boolean.class);
        assertThat(result, is(false));

        // Keine gültige Fahrbeziehung
        zeitintervall.getFahrbeziehung().setVon(null);
        zeitintervall.getFahrbeziehung().setNach(1);
        zeitintervall.getFahrbeziehung().setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.HINEIN);
        result = TestUtils.privateStaticMethodCall(
                "filterValidFahrbeziehung",
                ZeitintervallFahrbeziehungsSummationUtil.class,
                ArrayUtils.toArray(Zeitintervall.class),
                ArrayUtils.toArray(zeitintervall),
                Boolean.class);
        assertThat(result, is(false));
    }

    @Test
    public void getUeberFahrbeziehungSummierteZeitintervalleList() {
        final List<Zeitintervall> result = ZeitintervallFahrbeziehungsSummationUtil.getUeberFahrbeziehungSummierteZeitintervalle(zeitintervalle);

        final List<Zeitintervall> expected = new ArrayList<>();

        // Fahrbeziehungen ab 10:30: Alle nach alle
        Zeitintervall zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setSortingIndex(31043042);
        zeitintervall.setPkw(15);
        zeitintervall.setLkw(15);
        zeitintervall.setLastzuege(15);
        zeitintervall.setBusse(15);
        zeitintervall.setKraftraeder(15);
        zeitintervall.setFahrradfahrer(15);
        zeitintervall.setFussgaenger(15);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(15));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(15));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(15));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:30: 1 nach alle
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setSortingIndex(31043042);
        zeitintervall.setPkw(3);
        zeitintervall.setLkw(3);
        zeitintervall.setLastzuege(3);
        zeitintervall.setBusse(3);
        zeitintervall.setKraftraeder(3);
        zeitintervall.setFahrradfahrer(3);
        zeitintervall.setFussgaenger(3);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(3));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(3));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(3));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setVon(1);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:30: 2 nach alle
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setSortingIndex(31043042);
        zeitintervall.setPkw(7);
        zeitintervall.setLkw(7);
        zeitintervall.setLastzuege(7);
        zeitintervall.setBusse(7);
        zeitintervall.setKraftraeder(7);
        zeitintervall.setFahrradfahrer(7);
        zeitintervall.setFussgaenger(7);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(7));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(7));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(7));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setVon(2);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:30: 3 nach alle
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setSortingIndex(31043042);
        zeitintervall.setPkw(5);
        zeitintervall.setLkw(5);
        zeitintervall.setLastzuege(5);
        zeitintervall.setBusse(5);
        zeitintervall.setKraftraeder(5);
        zeitintervall.setFahrradfahrer(5);
        zeitintervall.setFussgaenger(5);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(5));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(5));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(5));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setVon(3);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:45: Alle nach alle
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        zeitintervall.setSortingIndex(31044043);
        zeitintervall.setPkw(40);
        zeitintervall.setLkw(40);
        zeitintervall.setLastzuege(40);
        zeitintervall.setBusse(40);
        zeitintervall.setKraftraeder(40);
        zeitintervall.setFahrradfahrer(40);
        zeitintervall.setFussgaenger(40);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(40));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(40));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(40));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:45: 1 nach alle
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        zeitintervall.setSortingIndex(31044043);
        zeitintervall.setPkw(13);
        zeitintervall.setLkw(13);
        zeitintervall.setLastzuege(13);
        zeitintervall.setBusse(13);
        zeitintervall.setKraftraeder(13);
        zeitintervall.setFahrradfahrer(13);
        zeitintervall.setFussgaenger(13);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(13));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(13));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(13));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setVon(1);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:45: 2 nach alle
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        zeitintervall.setSortingIndex(31044043);
        zeitintervall.setPkw(17);
        zeitintervall.setLkw(17);
        zeitintervall.setLastzuege(17);
        zeitintervall.setBusse(17);
        zeitintervall.setKraftraeder(17);
        zeitintervall.setFahrradfahrer(17);
        zeitintervall.setFussgaenger(17);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(17));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(17));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(17));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setVon(2);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:45: 3 nach alle
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        zeitintervall.setSortingIndex(31044043);
        zeitintervall.setPkw(10);
        zeitintervall.setLkw(10);
        zeitintervall.setLastzuege(10);
        zeitintervall.setBusse(10);
        zeitintervall.setKraftraeder(10);
        zeitintervall.setFahrradfahrer(10);
        zeitintervall.setFussgaenger(10);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(10));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(10));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(10));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setVon(3);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:30: alle nach 1
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setSortingIndex(31043042);
        zeitintervall.setPkw(8);
        zeitintervall.setLkw(8);
        zeitintervall.setLastzuege(8);
        zeitintervall.setBusse(8);
        zeitintervall.setKraftraeder(8);
        zeitintervall.setFahrradfahrer(8);
        zeitintervall.setFussgaenger(8);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(8));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(8));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(8));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setNach(1);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:30: alle nach 2
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setSortingIndex(31043042);
        zeitintervall.setPkw(1);
        zeitintervall.setLkw(1);
        zeitintervall.setLastzuege(1);
        zeitintervall.setBusse(1);
        zeitintervall.setKraftraeder(1);
        zeitintervall.setFahrradfahrer(1);
        zeitintervall.setFussgaenger(1);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(1));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(1));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(1));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setNach(2);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:30: alle nach 3
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setSortingIndex(31043042);
        zeitintervall.setPkw(6);
        zeitintervall.setLkw(6);
        zeitintervall.setLastzuege(6);
        zeitintervall.setBusse(6);
        zeitintervall.setKraftraeder(6);
        zeitintervall.setFahrradfahrer(6);
        zeitintervall.setFussgaenger(6);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(6));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(6));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(6));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setNach(3);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:45: alle nach 1
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        zeitintervall.setSortingIndex(31044043);
        zeitintervall.setPkw(18);
        zeitintervall.setLkw(18);
        zeitintervall.setLastzuege(18);
        zeitintervall.setBusse(18);
        zeitintervall.setKraftraeder(18);
        zeitintervall.setFahrradfahrer(18);
        zeitintervall.setFussgaenger(18);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(18));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(18));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(18));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setNach(1);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:45: alle nach 2
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        zeitintervall.setSortingIndex(31044043);
        zeitintervall.setPkw(6);
        zeitintervall.setLkw(6);
        zeitintervall.setLastzuege(6);
        zeitintervall.setBusse(6);
        zeitintervall.setKraftraeder(6);
        zeitintervall.setFahrradfahrer(6);
        zeitintervall.setFussgaenger(6);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(6));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(6));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(6));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setNach(2);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:45: alle nach 3
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        zeitintervall.setSortingIndex(31044043);
        zeitintervall.setPkw(16);
        zeitintervall.setLkw(16);
        zeitintervall.setLastzuege(16);
        zeitintervall.setBusse(16);
        zeitintervall.setKraftraeder(16);
        zeitintervall.setFahrradfahrer(16);
        zeitintervall.setFussgaenger(16);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(16));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(16));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(16));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setNach(3);
        expected.add(zeitintervall);

        assertThat(result.size(), is(expected.size()));
        assertThat(result.size(), is(new HashSet<>(result).size()));
        assertThat(expected.size(), is(new HashSet<>(expected).size()));
        assertThat(new HashSet<>(result), is(new HashSet<>(expected)));

    }

    @Test
    public void getUeberFahrbeziehungSummierteZeitintervalleMap() {
        ZeitintervallBaseUtil.Intervall intervall1030 = new ZeitintervallBaseUtil.Intervall(
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        ZeitintervallBaseUtil.Intervall intervall1045 = new ZeitintervallBaseUtil.Intervall(
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        final Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> zeitintervalleGroupedByIntervall = new HashMap<>();
        zeitintervalleGroupedByIntervall.put(intervall1030, zeitintervalle.subList(0, 5));
        zeitintervalleGroupedByIntervall.put(intervall1045, zeitintervalle.subList(5, 10));

        final List<Zeitintervall> result = TestUtils.privateStaticMethodCall(
                "getUeberFahrbeziehungSummierteZeitintervalle",
                ZeitintervallFahrbeziehungsSummationUtil.class,
                ArrayUtils.toArray(Map.class),
                ArrayUtils.toArray(zeitintervalleGroupedByIntervall),
                List.class);

        final List<Zeitintervall> expected = new ArrayList<>();

        // Fahrbeziehungen ab 10:45: Alle nach alle
        Zeitintervall zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        zeitintervall.setSortingIndex(31044043);
        zeitintervall.setPkw(40);
        zeitintervall.setLkw(40);
        zeitintervall.setLastzuege(40);
        zeitintervall.setBusse(40);
        zeitintervall.setKraftraeder(40);
        zeitintervall.setFahrradfahrer(40);
        zeitintervall.setFussgaenger(40);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(40));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(40));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(40));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:45: 1 nach alle
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        zeitintervall.setSortingIndex(31044043);
        zeitintervall.setPkw(13);
        zeitintervall.setLkw(13);
        zeitintervall.setLastzuege(13);
        zeitintervall.setBusse(13);
        zeitintervall.setKraftraeder(13);
        zeitintervall.setFahrradfahrer(13);
        zeitintervall.setFussgaenger(13);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(13));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(13));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(13));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setVon(1);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:45: 2 nach alle
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        zeitintervall.setSortingIndex(31044043);
        zeitintervall.setPkw(17);
        zeitintervall.setLkw(17);
        zeitintervall.setLastzuege(17);
        zeitintervall.setBusse(17);
        zeitintervall.setKraftraeder(17);
        zeitintervall.setFahrradfahrer(17);
        zeitintervall.setFussgaenger(17);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(17));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(17));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(17));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setVon(2);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:45: 3 nach alle
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        zeitintervall.setSortingIndex(31044043);
        zeitintervall.setPkw(10);
        zeitintervall.setLkw(10);
        zeitintervall.setLastzuege(10);
        zeitintervall.setBusse(10);
        zeitintervall.setKraftraeder(10);
        zeitintervall.setFahrradfahrer(10);
        zeitintervall.setFussgaenger(10);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(10));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(10));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(10));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setVon(3);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:30: Alle nach alle
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setSortingIndex(31043042);
        zeitintervall.setPkw(15);
        zeitintervall.setLkw(15);
        zeitintervall.setLastzuege(15);
        zeitintervall.setBusse(15);
        zeitintervall.setKraftraeder(15);
        zeitintervall.setFahrradfahrer(15);
        zeitintervall.setFussgaenger(15);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(15));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(15));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(15));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:30: 1 nach alle
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setSortingIndex(31043042);
        zeitintervall.setPkw(3);
        zeitintervall.setLkw(3);
        zeitintervall.setLastzuege(3);
        zeitintervall.setBusse(3);
        zeitintervall.setKraftraeder(3);
        zeitintervall.setFahrradfahrer(3);
        zeitintervall.setFussgaenger(3);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(3));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(3));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(3));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setVon(1);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:30: 2 nach alle
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setSortingIndex(31043042);
        zeitintervall.setPkw(7);
        zeitintervall.setLkw(7);
        zeitintervall.setLastzuege(7);
        zeitintervall.setBusse(7);
        zeitintervall.setKraftraeder(7);
        zeitintervall.setFahrradfahrer(7);
        zeitintervall.setFussgaenger(7);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(7));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(7));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(7));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setVon(2);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:30: 3 nach alle
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setSortingIndex(31043042);
        zeitintervall.setPkw(5);
        zeitintervall.setLkw(5);
        zeitintervall.setLastzuege(5);
        zeitintervall.setBusse(5);
        zeitintervall.setKraftraeder(5);
        zeitintervall.setFahrradfahrer(5);
        zeitintervall.setFussgaenger(5);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(5));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(5));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(5));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setVon(3);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:30: alle nach 1
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setSortingIndex(31043042);
        zeitintervall.setPkw(8);
        zeitintervall.setLkw(8);
        zeitintervall.setLastzuege(8);
        zeitintervall.setBusse(8);
        zeitintervall.setKraftraeder(8);
        zeitintervall.setFahrradfahrer(8);
        zeitintervall.setFussgaenger(8);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(8));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(8));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(8));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setNach(1);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:30: alle nach 2
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setSortingIndex(31043042);
        zeitintervall.setPkw(1);
        zeitintervall.setLkw(1);
        zeitintervall.setLastzuege(1);
        zeitintervall.setBusse(1);
        zeitintervall.setKraftraeder(1);
        zeitintervall.setFahrradfahrer(1);
        zeitintervall.setFussgaenger(1);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(1));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(1));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(1));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setNach(2);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:30: alle nach 3
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setSortingIndex(31043042);
        zeitintervall.setPkw(6);
        zeitintervall.setLkw(6);
        zeitintervall.setLastzuege(6);
        zeitintervall.setBusse(6);
        zeitintervall.setKraftraeder(6);
        zeitintervall.setFahrradfahrer(6);
        zeitintervall.setFussgaenger(6);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(6));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(6));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(6));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setNach(3);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:45: alle nach 1
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        zeitintervall.setSortingIndex(31044043);
        zeitintervall.setPkw(18);
        zeitintervall.setLkw(18);
        zeitintervall.setLastzuege(18);
        zeitintervall.setBusse(18);
        zeitintervall.setKraftraeder(18);
        zeitintervall.setFahrradfahrer(18);
        zeitintervall.setFussgaenger(18);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(18));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(18));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(18));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setNach(1);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:45: alle nach 2
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        zeitintervall.setSortingIndex(31044043);
        zeitintervall.setPkw(6);
        zeitintervall.setLkw(6);
        zeitintervall.setLastzuege(6);
        zeitintervall.setBusse(6);
        zeitintervall.setKraftraeder(6);
        zeitintervall.setFahrradfahrer(6);
        zeitintervall.setFussgaenger(6);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(6));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(6));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(6));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setNach(2);
        expected.add(zeitintervall);

        // Fahrbeziehungen ab 10:45: alle nach 3
        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        zeitintervall.setSortingIndex(31044043);
        zeitintervall.setPkw(16);
        zeitintervall.setLkw(16);
        zeitintervall.setLastzuege(16);
        zeitintervall.setBusse(16);
        zeitintervall.setKraftraeder(16);
        zeitintervall.setFahrradfahrer(16);
        zeitintervall.setFussgaenger(16);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(16));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(16));
        zeitintervall.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(16));
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setNach(3);
        expected.add(zeitintervall);

        assertThat(result.size(), is(expected.size()));
        assertThat(result.size(), is(new HashSet<>(result).size()));
        assertThat(expected.size(), is(new HashSet<>(expected).size()));
        assertThat(new HashSet<>(result), is(new HashSet<>(expected)));
    }

    @Test
    public void getSummedZeitintervallForAllFahrbeziehungen() {
        ZeitintervallBaseUtil.Intervall intervall = new ZeitintervallBaseUtil.Intervall(
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));

        Zeitintervall result = TestUtils.privateStaticMethodCall(
                "getSummedZeitintervallForAllFahrbeziehungen",
                ZeitintervallFahrbeziehungsSummationUtil.class,
                ArrayUtils.toArray(ZeitintervallBaseUtil.Intervall.class, List.class),
                ArrayUtils.toArray(intervall, zeitintervalle.subList(0, 5)),
                Zeitintervall.class);

        Zeitintervall expected = new Zeitintervall();
        expected.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        expected.setPkw(15);
        expected.setLkw(15);
        expected.setLastzuege(15);
        expected.setBusse(15);
        expected.setKraftraeder(15);
        expected.setFahrradfahrer(15);
        expected.setFussgaenger(15);
        expected.setType(TypeZeitintervall.STUNDE_VIERTEL);
        expected.setHochrechnung(new Hochrechnung());
        expected.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(15));
        expected.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(15));
        expected.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(15));
        expected.setFahrbeziehung(new Fahrbeziehung());

        assertThat(result, is(expected));

        intervall = new ZeitintervallBaseUtil.Intervall(
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));

        result = TestUtils.privateStaticMethodCall(
                "getSummedZeitintervallForAllFahrbeziehungen",
                ZeitintervallFahrbeziehungsSummationUtil.class,
                ArrayUtils.toArray(ZeitintervallBaseUtil.Intervall.class, List.class),
                ArrayUtils.toArray(intervall, zeitintervalle.subList(5, 10)),
                Zeitintervall.class);

        expected = new Zeitintervall();
        expected.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        expected.setPkw(40);
        expected.setLkw(40);
        expected.setLastzuege(40);
        expected.setBusse(40);
        expected.setKraftraeder(40);
        expected.setFahrradfahrer(40);
        expected.setFussgaenger(40);
        expected.setType(TypeZeitintervall.STUNDE_VIERTEL);
        expected.setHochrechnung(new Hochrechnung());
        expected.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(40));
        expected.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(40));
        expected.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(40));
        expected.setFahrbeziehung(new Fahrbeziehung());

        assertThat(result, is(expected));
    }

    @Test
    public void getSummedZeitintervallForCertainVonFahrbeziehungen() {
        ZeitintervallBaseUtil.Intervall intervall = new ZeitintervallBaseUtil.Intervall(
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));

        Zeitintervall result = TestUtils.privateStaticMethodCall(
                "getSummedZeitintervallForCertainVonFahrbeziehungen",
                ZeitintervallFahrbeziehungsSummationUtil.class,
                ArrayUtils.toArray(Integer.class, ZeitintervallBaseUtil.Intervall.class, List.class),
                ArrayUtils.toArray(2, intervall, zeitintervalle.subList(0, 5)),
                Zeitintervall.class);

        Zeitintervall expected = new Zeitintervall();
        expected.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        expected.setPkw(7);
        expected.setLkw(7);
        expected.setLastzuege(7);
        expected.setBusse(7);
        expected.setKraftraeder(7);
        expected.setFahrradfahrer(7);
        expected.setFussgaenger(7);
        expected.setType(TypeZeitintervall.STUNDE_VIERTEL);
        expected.setHochrechnung(new Hochrechnung());
        expected.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(7));
        expected.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(7));
        expected.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(7));
        expected.setFahrbeziehung(new Fahrbeziehung());
        expected.getFahrbeziehung().setVon(2);

        assertThat(result, is(expected));

        intervall = new ZeitintervallBaseUtil.Intervall(
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));

        result = TestUtils.privateStaticMethodCall(
                "getSummedZeitintervallForCertainVonFahrbeziehungen",
                ZeitintervallFahrbeziehungsSummationUtil.class,
                ArrayUtils.toArray(Integer.class, ZeitintervallBaseUtil.Intervall.class, List.class),
                ArrayUtils.toArray(2, intervall, zeitintervalle.subList(5, 10)),
                Zeitintervall.class);

        expected = new Zeitintervall();
        expected.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        expected.setPkw(17);
        expected.setLkw(17);
        expected.setLastzuege(17);
        expected.setBusse(17);
        expected.setKraftraeder(17);
        expected.setFahrradfahrer(17);
        expected.setFussgaenger(17);
        expected.setType(TypeZeitintervall.STUNDE_VIERTEL);
        expected.setHochrechnung(new Hochrechnung());
        expected.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(17));
        expected.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(17));
        expected.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(17));
        expected.setFahrbeziehung(new Fahrbeziehung());
        expected.getFahrbeziehung().setVon(2);

        assertThat(result, is(expected));
    }

    @Test
    public void getSummedZeitintervallForCertainNachFahrbeziehungen() {
        ZeitintervallBaseUtil.Intervall intervall = new ZeitintervallBaseUtil.Intervall(
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));

        Zeitintervall result = TestUtils.privateStaticMethodCall(
                "getSummedZeitintervallForCertainNachFahrbeziehungen",
                ZeitintervallFahrbeziehungsSummationUtil.class,
                ArrayUtils.toArray(Integer.class, ZeitintervallBaseUtil.Intervall.class, List.class),
                ArrayUtils.toArray(3, intervall, zeitintervalle.subList(0, 5)),
                Zeitintervall.class);

        Zeitintervall expected = new Zeitintervall();
        expected.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        expected.setPkw(6);
        expected.setLkw(6);
        expected.setLastzuege(6);
        expected.setBusse(6);
        expected.setKraftraeder(6);
        expected.setFahrradfahrer(6);
        expected.setFussgaenger(6);
        expected.setType(TypeZeitintervall.STUNDE_VIERTEL);
        expected.setHochrechnung(new Hochrechnung());
        expected.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(6));
        expected.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(6));
        expected.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(6));
        expected.setFahrbeziehung(new Fahrbeziehung());
        expected.getFahrbeziehung().setNach(3);

        assertThat(result, is(expected));

        intervall = new ZeitintervallBaseUtil.Intervall(
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));

        result = TestUtils.privateStaticMethodCall(
                "getSummedZeitintervallForCertainNachFahrbeziehungen",
                ZeitintervallFahrbeziehungsSummationUtil.class,
                ArrayUtils.toArray(Integer.class, ZeitintervallBaseUtil.Intervall.class, List.class),
                ArrayUtils.toArray(3, intervall, zeitintervalle.subList(5, 10)),
                Zeitintervall.class);

        expected = new Zeitintervall();
        expected.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        expected.setPkw(16);
        expected.setLkw(16);
        expected.setLastzuege(16);
        expected.setBusse(16);
        expected.setKraftraeder(16);
        expected.setFahrradfahrer(16);
        expected.setFussgaenger(16);
        expected.setType(TypeZeitintervall.STUNDE_VIERTEL);
        expected.setHochrechnung(new Hochrechnung());
        expected.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(16));
        expected.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(16));
        expected.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(16));
        expected.setFahrbeziehung(new Fahrbeziehung());
        expected.getFahrbeziehung().setNach(3);

        assertThat(result, is(expected));
    }

    @Test
    public void getSummedZeitintervallOverAllGivenZeitintervalle() {
        ZeitintervallBaseUtil.Intervall intervall = new ZeitintervallBaseUtil.Intervall(
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));

        Zeitintervall result = TestUtils.privateStaticMethodCall(
                "getSummedZeitintervallOverAllGivenZeitintervalle",
                ZeitintervallFahrbeziehungsSummationUtil.class,
                ArrayUtils.toArray(ZeitintervallBaseUtil.Intervall.class, List.class),
                ArrayUtils.toArray(intervall, zeitintervalle.subList(0, 5)),
                Zeitintervall.class);

        Zeitintervall expected = new Zeitintervall();
        expected.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        expected.setPkw(15);
        expected.setLkw(15);
        expected.setLastzuege(15);
        expected.setBusse(15);
        expected.setKraftraeder(15);
        expected.setFahrradfahrer(15);
        expected.setFussgaenger(15);
        expected.setType(TypeZeitintervall.STUNDE_VIERTEL);
        expected.setHochrechnung(new Hochrechnung());
        expected.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(15));
        expected.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(15));
        expected.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(15));
        expected.setFahrbeziehung(new Fahrbeziehung());

        assertThat(result, is(expected));

        intervall = new ZeitintervallBaseUtil.Intervall(
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)),
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));

        result = TestUtils.privateStaticMethodCall(
                "getSummedZeitintervallOverAllGivenZeitintervalle",
                ZeitintervallFahrbeziehungsSummationUtil.class,
                ArrayUtils.toArray(ZeitintervallBaseUtil.Intervall.class, List.class),
                ArrayUtils.toArray(intervall, zeitintervalle.subList(5, 10)),
                Zeitintervall.class);

        expected = new Zeitintervall();
        expected.setZaehlungId(zeitintervalle.get(zeitintervalle.size() - 1).getZaehlungId());
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        expected.setPkw(40);
        expected.setLkw(40);
        expected.setLastzuege(40);
        expected.setBusse(40);
        expected.setKraftraeder(40);
        expected.setFahrradfahrer(40);
        expected.setFussgaenger(40);
        expected.setType(TypeZeitintervall.STUNDE_VIERTEL);
        expected.setHochrechnung(new Hochrechnung());
        expected.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(40));
        expected.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(40));
        expected.getHochrechnung().setHochrechnungSv(BigDecimal.valueOf(40));
        expected.setFahrbeziehung(new Fahrbeziehung());

        assertThat(result, is(expected));
    }

    @Test
    public void getAllVonFahrbeziehungen() {
        Set<Integer> vonFahrbeziehungen = TestUtils.privateStaticMethodCall(
                "getAllVonFahrbeziehungen",
                ZeitintervallFahrbeziehungsSummationUtil.class,
                ArrayUtils.toArray(List.class),
                ArrayUtils.toArray(zeitintervalle),
                Set.class);

        Set<Integer> expected = new HashSet<>();
        expected.addAll(Arrays.asList(1, 2, 3));
        assertThat(vonFahrbeziehungen, is(expected));
    }

    @Test
    public void getAllNachFahrbeziehungen() {
        Set<Integer> vonFahrbeziehungen = TestUtils.privateStaticMethodCall(
                "getAllNachFahrbeziehungen",
                ZeitintervallFahrbeziehungsSummationUtil.class,
                ArrayUtils.toArray(List.class),
                ArrayUtils.toArray(zeitintervalle),
                Set.class);

        Set<Integer> expected = new HashSet<>();
        expected.addAll(Arrays.asList(1, 2, 3));
        assertThat(vonFahrbeziehungen, is(expected));

        final UUID zaehlungId = UUID.randomUUID();
        List<Zeitintervall> zeitintervallKreisverkehr = new ArrayList<>();
        zeitintervallKreisverkehr.add(TestUtils.createZeitintervall(
                zaehlungId,
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)),
                2,
                1,
                99,
                null));
        zeitintervallKreisverkehr.add(TestUtils.createZeitintervall(
                zaehlungId,
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)),
                2,
                1,
                null,
                FahrbewegungKreisverkehr.HERAUS));
        zeitintervallKreisverkehr.add(TestUtils.createZeitintervall(
                zaehlungId,
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)),
                2,
                1,
                100,
                FahrbewegungKreisverkehr.HERAUS));

        vonFahrbeziehungen = TestUtils.privateStaticMethodCall(
                "getAllNachFahrbeziehungen",
                ZeitintervallFahrbeziehungsSummationUtil.class,
                ArrayUtils.toArray(List.class),
                ArrayUtils.toArray(zeitintervallKreisverkehr),
                Set.class);

        expected = new HashSet<>();
        expected.addAll(List.of(99));
        assertThat(vonFahrbeziehungen, is(expected));

    }

}
