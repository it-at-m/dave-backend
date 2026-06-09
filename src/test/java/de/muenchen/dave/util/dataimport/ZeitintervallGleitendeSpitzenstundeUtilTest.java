package de.muenchen.dave.util.dataimport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Laengsverkehr;
import de.muenchen.dave.domain.Querungsverkehr;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.util.DaveConstants;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class ZeitintervallGleitendeSpitzenstundeUtilTest {

    @Test
    public void getGleitendeSpitzenstundenForEachBewegungsbeziehung_AllTypeZeitintervall() {
        List<Zeitintervall> intervals = createIntervals();

        // Definiere PKW-Peak: Indizes 27..30 -> hohe Werte, sodass die Stundensumme hier maximal ist
        intervals.get(27).setPkw(20);
        intervals.get(28).setPkw(25);
        intervals.get(29).setPkw(30);
        intervals.get(30).setPkw(25);

        // Definiere Rad-Peak: Indizes 50..53
        intervals.get(50).setFahrradfahrer(50);
        intervals.get(51).setFahrradfahrer(60);
        intervals.get(52).setFahrradfahrer(70);
        intervals.get(53).setFahrradfahrer(60);

        // Definiere Fuss-Peak: Indizes 80..83
        intervals.get(80).setFussgaenger(15);
        intervals.get(81).setFussgaenger(20);
        intervals.get(82).setFussgaenger(25);
        intervals.get(83).setFussgaenger(20);

        UUID zaehlungId = UUID.randomUUID();

        Set<TypeZeitintervall> types = Set.of(
                TypeZeitintervall.SPITZENSTUNDE_KFZ,
                TypeZeitintervall.SPITZENSTUNDE_RAD,
                TypeZeitintervall.SPITZENSTUNDE_FUSS);

        List<Zeitintervall> result = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstundenForEachBewegungsbeziehung(
                zaehlungId, Zeitblock.ZB_00_24, Zaehlart.N, intervals, types);

        List<Zeitintervall> expected = new ArrayList<>();

        var zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 0, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 1, 0, 0));
        zeitintervall.setSortingIndex(12000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 0, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 1, 0, 0));
        zeitintervall.setSortingIndex(13000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_RAD);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 0, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 1, 0, 0));
        zeitintervall.setSortingIndex(14000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_FUSS);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 6, 45, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 7, 45, 0));
        zeitintervall.setSortingIndex(22000000);
        zeitintervall.setPkw(100);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 6, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 7, 0, 0));
        zeitintervall.setSortingIndex(23000000);
        zeitintervall.setPkw(23);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_RAD);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 6, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 7, 0, 0));
        zeitintervall.setSortingIndex(24000000);
        zeitintervall.setPkw(23);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_FUSS);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 10, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 11, 0, 0));
        zeitintervall.setSortingIndex(32000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 12, 30, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 13, 30, 0));
        zeitintervall.setSortingIndex(33000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(240);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_RAD);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 10, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 11, 0, 0));
        zeitintervall.setSortingIndex(34000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_FUSS);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 15, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 16, 0, 0));
        zeitintervall.setSortingIndex(42000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 15, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 16, 0, 0));
        zeitintervall.setSortingIndex(43000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_RAD);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 15, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 16, 0, 0));
        zeitintervall.setSortingIndex(44000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_FUSS);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 19, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 20, 0, 0));
        zeitintervall.setSortingIndex(52000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 19, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 20, 0, 0));
        zeitintervall.setSortingIndex(53000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_RAD);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 20, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 21, 0, 0));
        zeitintervall.setSortingIndex(54000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(80);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_FUSS);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 6, 45, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 7, 45, 0));
        zeitintervall.setSortingIndex(60000000);
        zeitintervall.setPkw(100);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 12, 30, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 13, 30, 0));
        zeitintervall.setSortingIndex(70000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(240);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_RAD);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 20, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 21, 0, 0));
        zeitintervall.setSortingIndex(80000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(80);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_FUSS);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        assertThat(result.get(0), is(expected.get(0)));
        assertThat(result.get(1), is(expected.get(1)));
        assertThat(result.get(2), is(expected.get(2)));
        assertThat(result.get(3), is(expected.get(3)));
        assertThat(result.get(4), is(expected.get(4)));
        assertThat(result.get(5), is(expected.get(5)));
        assertThat(result.get(6), is(expected.get(6)));
        assertThat(result.get(7), is(expected.get(7)));
        assertThat(result.get(8), is(expected.get(8)));
        assertThat(result.get(9), is(expected.get(9)));
        assertThat(result.get(10), is(expected.get(10)));
        assertThat(result.get(11), is(expected.get(11)));
        assertThat(result.get(12), is(expected.get(12)));
        assertThat(result.get(13), is(expected.get(13)));
        assertThat(result.get(14), is(expected.get(14)));
        assertThat(result.get(15), is(expected.get(15)));
        assertThat(result.get(16), is(expected.get(16)));
        assertThat(result.get(17), is(expected.get(17)));
    }

    @Test
    public void getGleitendeSpitzenstundenForEachBewegungsbeziehung_TypeZeitintervallKfz() {
        List<Zeitintervall> intervals = createIntervals();

        // Definiere PKW-Peak: Indizes 27..30 -> hohe Werte, sodass die Stundensumme hier maximal ist
        intervals.get(27).setPkw(20);
        intervals.get(28).setPkw(25);
        intervals.get(29).setPkw(30);
        intervals.get(30).setPkw(25);

        // Definiere Rad-Peak: Indizes 50..53
        intervals.get(50).setFahrradfahrer(50);
        intervals.get(51).setFahrradfahrer(60);
        intervals.get(52).setFahrradfahrer(70);
        intervals.get(53).setFahrradfahrer(60);

        // Definiere Fuss-Peak: Indizes 80..83
        intervals.get(80).setFussgaenger(15);
        intervals.get(81).setFussgaenger(20);
        intervals.get(82).setFussgaenger(25);
        intervals.get(83).setFussgaenger(20);

        UUID zaehlungId = UUID.randomUUID();

        Set<TypeZeitintervall> types = Set.of(
                TypeZeitintervall.SPITZENSTUNDE_KFZ);

        List<Zeitintervall> result = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstundenForEachBewegungsbeziehung(
                zaehlungId, Zeitblock.ZB_00_24, Zaehlart.N, intervals, types);

        List<Zeitintervall> expected = new ArrayList<>();

        var zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 0, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 1, 0, 0));
        zeitintervall.setSortingIndex(12000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 6, 45, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 7, 45, 0));
        zeitintervall.setSortingIndex(22000000);
        zeitintervall.setPkw(100);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 10, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 11, 0, 0));
        zeitintervall.setSortingIndex(32000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 15, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 16, 0, 0));
        zeitintervall.setSortingIndex(42000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 19, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 20, 0, 0));
        zeitintervall.setSortingIndex(52000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 6, 45, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 7, 45, 0));
        zeitintervall.setSortingIndex(60000000);
        zeitintervall.setPkw(100);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        assertThat(result.get(0), is(expected.get(0)));
        assertThat(result.get(1), is(expected.get(1)));
        assertThat(result.get(2), is(expected.get(2)));
        assertThat(result.get(3), is(expected.get(3)));
        assertThat(result.get(4), is(expected.get(4)));
        assertThat(result.get(5), is(expected.get(5)));
    }

    @Test
    public void getGleitendeSpitzenstundenForEachBewegungsbeziehung_TypeZeitintervallRad() {
        List<Zeitintervall> intervals = createIntervals();

        // Definiere PKW-Peak: Indizes 27..30 -> hohe Werte, sodass die Stundensumme hier maximal ist
        intervals.get(27).setPkw(20);
        intervals.get(28).setPkw(25);
        intervals.get(29).setPkw(30);
        intervals.get(30).setPkw(25);

        // Definiere Rad-Peak: Indizes 50..53
        intervals.get(50).setFahrradfahrer(50);
        intervals.get(51).setFahrradfahrer(60);
        intervals.get(52).setFahrradfahrer(70);
        intervals.get(53).setFahrradfahrer(60);

        // Definiere Fuss-Peak: Indizes 80..83
        intervals.get(80).setFussgaenger(15);
        intervals.get(81).setFussgaenger(20);
        intervals.get(82).setFussgaenger(25);
        intervals.get(83).setFussgaenger(20);

        UUID zaehlungId = UUID.randomUUID();

        Set<TypeZeitintervall> types = Set.of(
                TypeZeitintervall.SPITZENSTUNDE_RAD);

        List<Zeitintervall> result = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstundenForEachBewegungsbeziehung(
                zaehlungId, Zeitblock.ZB_00_24, Zaehlart.N, intervals, types);

        List<Zeitintervall> expected = new ArrayList<>();

        var zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 0, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 1, 0, 0));
        zeitintervall.setSortingIndex(13000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_RAD);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 6, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 7, 0, 0));
        zeitintervall.setSortingIndex(23000000);
        zeitintervall.setPkw(23);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_RAD);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 12, 30, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 13, 30, 0));
        zeitintervall.setSortingIndex(33000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(240);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_RAD);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 15, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 16, 0, 0));
        zeitintervall.setSortingIndex(43000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_RAD);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 19, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 20, 0, 0));
        zeitintervall.setSortingIndex(53000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_RAD);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 12, 30, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 13, 30, 0));
        zeitintervall.setSortingIndex(70000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(240);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_RAD);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        assertThat(result.get(0), is(expected.get(0)));
        assertThat(result.get(1), is(expected.get(1)));
        assertThat(result.get(2), is(expected.get(2)));
        assertThat(result.get(3), is(expected.get(3)));
        assertThat(result.get(4), is(expected.get(4)));
        assertThat(result.get(5), is(expected.get(5)));
    }

    @Test
    public void getGleitendeSpitzenstundenForEachBewegungsbeziehung_TypeZeitintervallFuss() {
        List<Zeitintervall> intervals = createIntervals();

        // Definiere PKW-Peak: Indizes 27..30 -> hohe Werte, sodass die Stundensumme hier maximal ist
        intervals.get(27).setPkw(20);
        intervals.get(28).setPkw(25);
        intervals.get(29).setPkw(30);
        intervals.get(30).setPkw(25);

        // Definiere Rad-Peak: Indizes 50..53
        intervals.get(50).setFahrradfahrer(50);
        intervals.get(51).setFahrradfahrer(60);
        intervals.get(52).setFahrradfahrer(70);
        intervals.get(53).setFahrradfahrer(60);

        // Definiere Fuss-Peak: Indizes 80..83
        intervals.get(80).setFussgaenger(15);
        intervals.get(81).setFussgaenger(20);
        intervals.get(82).setFussgaenger(25);
        intervals.get(83).setFussgaenger(20);

        UUID zaehlungId = UUID.randomUUID();

        Set<TypeZeitintervall> types = Set.of(
                TypeZeitintervall.SPITZENSTUNDE_FUSS);

        List<Zeitintervall> result = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstundenForEachBewegungsbeziehung(
                zaehlungId, Zeitblock.ZB_00_24, Zaehlart.N, intervals, types);

        List<Zeitintervall> expected = new ArrayList<>();

        var zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 0, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 1, 0, 0));
        zeitintervall.setSortingIndex(14000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_FUSS);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 6, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 7, 0, 0));
        zeitintervall.setSortingIndex(24000000);
        zeitintervall.setPkw(23);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_FUSS);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 10, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 11, 0, 0));
        zeitintervall.setSortingIndex(34000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_FUSS);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 15, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 16, 0, 0));
        zeitintervall.setSortingIndex(44000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(4);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_FUSS);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 20, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 21, 0, 0));
        zeitintervall.setSortingIndex(54000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(80);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_FUSS);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(1941, 5, 12, 20, 0, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(1941, 5, 12, 21, 0, 0));
        zeitintervall.setSortingIndex(80000000);
        zeitintervall.setPkw(4);
        zeitintervall.setFahrradfahrer(4);
        zeitintervall.setFussgaenger(80);
        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_FUSS);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.add(zeitintervall);

        assertThat(result.get(0), is(expected.get(0)));
        assertThat(result.get(1), is(expected.get(1)));
        assertThat(result.get(2), is(expected.get(2)));
        assertThat(result.get(3), is(expected.get(3)));
        assertThat(result.get(4), is(expected.get(4)));
        assertThat(result.get(5), is(expected.get(5)));
    }

    /**
     * Testet, ob die Spitzenstunde auch innerhalb des 06-bis-19-Uhr-Blocks
     * richtig ermittelt wird.
     */
    @Test
    public void getGleitendeSpitzenstunden_Block06_19_AllTypeZeitintervall() {
        List<Zeitintervall> intervals = createIntervals();

        // Definiere RAD-Peak um 07:00 Uhr
        intervals.get(28).setFahrradfahrer(1000);
        intervals.get(29).setFahrradfahrer(1000);
        intervals.get(30).setFahrradfahrer(1000);
        intervals.get(31).setFahrradfahrer(1000);

        UUID zaehlungId = UUID.randomUUID();
        List<Zeitintervall> gleitendeSpitzenstunden = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstunden(zaehlungId, Zeitblock.ZB_06_19,
                intervals, Set.of(TypeZeitintervall.SPITZENSTUNDE_RAD, TypeZeitintervall.STUNDE_VIERTEL));

        // Der Peak muss um 07:00 Uhr mit der Summe der Zählwerte der 4 Intervalle dieser Stunde sein:
        assertThat(gleitendeSpitzenstunden.getFirst().getStartUhrzeit(), is(DaveConstants.DEFAULT_LOCALDATE.atTime(7, 0)));
        assertThat(gleitendeSpitzenstunden.getFirst().getFahrradfahrer(), is(4000));
        assertThat(gleitendeSpitzenstunden.getFirst().getType(), is(TypeZeitintervall.SPITZENSTUNDE_RAD));
    }

    /**
     * Testet, ob die Spitzenstunde auch innerhalb des 06-bis-22-Uhr-Blocks
     * richtig ermittelt wird.
     */
    @Test
    public void getGleitendeSpitzenstunden_Block06_22_AllTypeZeitintervall() {
        List<Zeitintervall> intervals = createIntervals();

        // Definiere RAD-Peak um 07:00 Uhr
        intervals.get(28).setPkw(1000);
        intervals.get(29).setPkw(1000);
        intervals.get(30).setPkw(1000);
        intervals.get(31).setPkw(1000);

        UUID zaehlungId = UUID.randomUUID();
        List<Zeitintervall> gleitendeSpitzenstunden = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstunden(zaehlungId, Zeitblock.ZB_06_22,
                intervals, Set.of(TypeZeitintervall.SPITZENSTUNDE_KFZ, TypeZeitintervall.STUNDE_VIERTEL));

        // Der Peak muss um 07:00 Uhr mit der Summe der Zählwerte der 4 Intervalle dieser Stunde sein:
        assertThat(gleitendeSpitzenstunden.getFirst().getStartUhrzeit(), is(DaveConstants.DEFAULT_LOCALDATE.atTime(7, 0)));
        assertThat(gleitendeSpitzenstunden.getFirst().getPkw(), is(4000));
        assertThat(gleitendeSpitzenstunden.getFirst().getType(), is(TypeZeitintervall.SPITZENSTUNDE_KFZ));
    }

    @Test
    public void setBewegungsbeziehungOnZeitintervall_setsQuerungsverkehr_whenZaehlartIsQU() throws Exception {
        final Zeitintervall zi = new Zeitintervall();
        final Querungsverkehr querungsverkehr = new Querungsverkehr();

        ZeitintervallGleitendeSpitzenstundeUtil.setBewegungsbeziehungOnZeitintervallAccordingZaehlart(zi, querungsverkehr, Zaehlart.QU);

        assertThat(zi.getQuerungsverkehr(), is(querungsverkehr));
        assertThat(zi.getLaengsverkehr(), is(nullValue()));
        assertThat(zi.getVerkehrsbeziehung(), is(nullValue()));
    }

    @Test
    public void setBewegungsbeziehungOnZeitintervall_setsLaengsverkehr_whenZaehlartIsFJS() throws Exception {
        final Zeitintervall zi = new Zeitintervall();
        final Laengsverkehr laengsverkehr = new Laengsverkehr();

        ZeitintervallGleitendeSpitzenstundeUtil.setBewegungsbeziehungOnZeitintervallAccordingZaehlart(zi, laengsverkehr, Zaehlart.FJS);

        assertThat(zi.getLaengsverkehr(), is(laengsverkehr));
        assertThat(zi.getQuerungsverkehr(), is(nullValue()));
        assertThat(zi.getVerkehrsbeziehung(), is(nullValue()));
    }

    @Test
    public void setBewegungsbeziehungOnZeitintervall_setsVerkehrsbeziehung_whenZaehlartIsOther() throws Exception {
        final Zeitintervall zi = new Zeitintervall();
        final Verkehrsbeziehung verkehrsbeziehung = new Verkehrsbeziehung();

        ZeitintervallGleitendeSpitzenstundeUtil.setBewegungsbeziehungOnZeitintervallAccordingZaehlart(zi, verkehrsbeziehung, Zaehlart.N);

        assertThat(zi.getVerkehrsbeziehung(), is(verkehrsbeziehung));
        assertThat(zi.getQuerungsverkehr(), is(nullValue()));
        assertThat(zi.getLaengsverkehr(), is(nullValue()));
    }

    @Test
    public void emptyInput_returnsEmpty_forZeitblock() {
        final List<Zeitintervall> input = new ArrayList<>();
        final var result = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstundenForEachBewegungsbeziehungForZeitblock(
                UUID.randomUUID(), Zeitblock.ZB_06_10, Zaehlart.N, input, Set.of(TypeZeitintervall.SPITZENSTUNDE_KFZ));
        assertThat(result, is(notNullValue()));
        assertThat(result.size(), is(0));
    }

    @Test
    public void emptyTypes_returnsEmpty() {
        final List<Zeitintervall> input = new ArrayList<>();
        final Zeitintervall zi = new Zeitintervall();
        zi.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)));
        zi.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 15)));
        final Verkehrsbeziehung vb = new Verkehrsbeziehung();
        vb.setVon(1);
        zi.setVerkehrsbeziehung(vb);
        input.add(zi);

        final var result = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstundenForEachBewegungsbeziehungForZeitblock(
                UUID.randomUUID(), Zeitblock.ZB_06_10, Zaehlart.N, input, Set.of());
        assertThat(result, is(notNullValue()));
        assertThat(result.size(), is(0));
    }

    @Test
    public void groups_and_setsVerkehrsbeziehung_forNormalZaehlart() {
        final List<Zeitintervall> input = new ArrayList<>();

        // group A
        final Verkehrsbeziehung vbA = new Verkehrsbeziehung();
        vbA.setVon(1);
        for (int i = 0; i < 4; i++) {
            final Zeitintervall zi = new Zeitintervall();
            zi.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)).plusMinutes(15L * i));
            zi.setEndeUhrzeit(zi.getStartUhrzeit().plusMinutes(15));
            zi.setPkw(i + 1);
            zi.setVerkehrsbeziehung(vbA);
            input.add(zi);
        }

        // group B
        final Verkehrsbeziehung vbB = new Verkehrsbeziehung();
        vbB.setVon(2);
        for (int i = 0; i < 4; i++) {
            final Zeitintervall zi = new Zeitintervall();
            zi.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)).plusMinutes(15L * i));
            zi.setEndeUhrzeit(zi.getStartUhrzeit().plusMinutes(15));
            zi.setPkw((i + 1) * 10);
            zi.setVerkehrsbeziehung(vbB);
            input.add(zi);
        }

        final var result = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstundenForEachBewegungsbeziehungForZeitblock(
                UUID.randomUUID(), Zeitblock.ZB_06_10, Zaehlart.N, input, Set.of(TypeZeitintervall.SPITZENSTUNDE_KFZ));

        // two groups -> two resulting spitzenstunden (one per group)
        assertThat(result.size(), is(2));

        // check that for each result the Verkehrsbeziehung is set
        for (final Zeitintervall out : result) {
            assertThat(out.getType(), is(TypeZeitintervall.SPITZENSTUNDE_KFZ));
            assertThat(out.getVerkehrsbeziehung(), notNullValue());
            // other fields for other bewegungsbeziehung types must be null
            assertThat(out.getLaengsverkehr(), is(nullValue()));
            assertThat(out.getQuerungsverkehr(), is(nullValue()));
        }
    }

    @Test
    public void setsLaengsverkehr_forFJSZaehlart() {
        final List<Zeitintervall> input = new ArrayList<>();

        final Laengsverkehr lvA = new Laengsverkehr();
        lvA.setKnotenarm(1);
        for (int i = 0; i < 4; i++) {
            final Zeitintervall zi = new Zeitintervall();
            zi.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)).plusMinutes(15L * i));
            zi.setEndeUhrzeit(zi.getStartUhrzeit().plusMinutes(15));
            zi.setPkw(i + 1);
            zi.setLaengsverkehr(lvA);
            input.add(zi);
        }

        final var result = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstundenForEachBewegungsbeziehungForZeitblock(
                UUID.randomUUID(), Zeitblock.ZB_06_10, Zaehlart.FJS, input, Set.of(TypeZeitintervall.SPITZENSTUNDE_KFZ));

        assertThat(result.size(), is(1));
        final var out = result.get(0);
        assertThat(out.getLaengsverkehr(), is(lvA));
        assertThat(out.getVerkehrsbeziehung(), is(nullValue()));
        assertThat(out.getQuerungsverkehr(), is(nullValue()));
    }

    @Test
    public void setsQuerungsverkehr_forQUZaehlart() {
        final List<Zeitintervall> input = new ArrayList<>();

        final Querungsverkehr qv = new Querungsverkehr();
        qv.setKnotenarm(5);
        for (int i = 0; i < 4; i++) {
            final Zeitintervall zi = new Zeitintervall();
            zi.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)).plusMinutes(15L * i));
            zi.setEndeUhrzeit(zi.getStartUhrzeit().plusMinutes(15));
            zi.setPkw(i + 1);
            zi.setQuerungsverkehr(qv);
            input.add(zi);
        }

        final var result = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstundenForEachBewegungsbeziehungForZeitblock(
                UUID.randomUUID(), Zeitblock.ZB_06_10, Zaehlart.QU, input, Set.of(TypeZeitintervall.SPITZENSTUNDE_KFZ));

        assertThat(result.size(), is(1));
        final var out = result.get(0);
        assertThat(out.getQuerungsverkehr(), is(qv));
        assertThat(out.getVerkehrsbeziehung(), is(nullValue()));
        assertThat(out.getLaengsverkehr(), is(nullValue()));
    }

    @Test
    public void mockedSetBewegungsbeziehung_isCalled_perResultingZeitintervall() {
        final List<Zeitintervall> input = new ArrayList<>();
        final Verkehrsbeziehung vb1 = new Verkehrsbeziehung();
        vb1.setVon(1);
        final Zeitintervall zi1 = new Zeitintervall();
        zi1.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)));
        zi1.setEndeUhrzeit(zi1.getStartUhrzeit().plusMinutes(15));
        zi1.setPkw(20);
        zi1.setVerkehrsbeziehung(vb1);
        input.add(zi1);

        final Verkehrsbeziehung vb2 = new Verkehrsbeziehung();
        vb2.setVon(2);
        final Zeitintervall zi2 = new Zeitintervall();
        zi2.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 15)));
        zi2.setEndeUhrzeit(zi2.getStartUhrzeit().plusMinutes(15));
        zi2.setPkw(10);
        zi2.setVerkehrsbeziehung(vb2);
        input.add(zi2);

        try (MockedStatic<ZeitintervallGleitendeSpitzenstundeUtil> utilMock = Mockito.mockStatic(
                ZeitintervallGleitendeSpitzenstundeUtil.class, Mockito.CALLS_REAL_METHODS)) {
            utilMock.when(() -> ZeitintervallGleitendeSpitzenstundeUtil
                    .setBewegungsbeziehungOnZeitintervallAccordingZaehlart(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenAnswer(invocation -> null);

            final UUID id = UUID.randomUUID();
            final var result = ZeitintervallGleitendeSpitzenstundeUtil
                    .getGleitendeSpitzenstundenForEachBewegungsbeziehungForZeitblock(
                            id, Zeitblock.ZB_06_10, Zaehlart.N, input, Set.of(TypeZeitintervall.SPITZENSTUNDE_KFZ));

            // Expect one result per group
            assertThat(result.size(), is(2));

            // verify that the mocked setter was called for each returned Zeitintervall
            utilMock.verify(() -> ZeitintervallGleitendeSpitzenstundeUtil
                    .setBewegungsbeziehungOnZeitintervallAccordingZaehlart(Mockito.any(), Mockito.any(), Mockito.any()), Mockito.times(2));
        }
    }

    @Test
    public void nullZaehlungId_returnsEmpty() {
        final List<Zeitintervall> input = new ArrayList<>();
        final Zeitintervall zi = new Zeitintervall();
        zi.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)));
        zi.setEndeUhrzeit(zi.getStartUhrzeit().plusMinutes(15));
        final Verkehrsbeziehung vb = new Verkehrsbeziehung();
        vb.setVon(1);
        zi.setVerkehrsbeziehung(vb);
        input.add(zi);

        final var result = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstundenForEachBewegungsbeziehungForZeitblock(
                null, Zeitblock.ZB_06_10, Zaehlart.N, input, Set.of(TypeZeitintervall.SPITZENSTUNDE_KFZ));

        assertThat(result, is(notNullValue()));
        assertThat(result.size(), is(0));
    }

    @Test
    public void intervalsOutsideZeitblock_areIgnored() {
        final List<Zeitintervall> input = new ArrayList<>();

        // outside the Zeitblock ZB_06_10 (05:45)
        final Verkehrsbeziehung vbOutside = new Verkehrsbeziehung();
        vbOutside.setVon(1);
        final Zeitintervall ziOutside = new Zeitintervall();
        ziOutside.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(5, 45)));
        ziOutside.setEndeUhrzeit(ziOutside.getStartUhrzeit().plusMinutes(15));
        ziOutside.setPkw(10);
        ziOutside.setVerkehrsbeziehung(vbOutside);
        input.add(ziOutside);

        // inside the Zeitblock ZB_06_10 (06:00)
        final Verkehrsbeziehung vbInside = new Verkehrsbeziehung();
        vbInside.setVon(2);
        final Zeitintervall ziInside = new Zeitintervall();
        ziInside.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)));
        ziInside.setEndeUhrzeit(ziInside.getStartUhrzeit().plusMinutes(15));
        ziInside.setPkw(20);
        ziInside.setVerkehrsbeziehung(vbInside);
        input.add(ziInside);

        final var result = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstundenForEachBewegungsbeziehungForZeitblock(
                UUID.randomUUID(), Zeitblock.ZB_06_10, Zaehlart.N, input, Set.of(TypeZeitintervall.SPITZENSTUNDE_KFZ));

        // only the group with the interval inside the block should produce a spitzenstunde
        assertThat(result.size(), is(1));
        final var out = result.get(0);
        assertThat(out.getVerkehrsbeziehung(), is(vbInside));
        assertThat(out.getLaengsverkehr(), is(nullValue()));
        assertThat(out.getQuerungsverkehr(), is(nullValue()));
    }

    @Test
    public void mixedGroup_onlyInsideIntervalsContribute() {
        final List<Zeitintervall> input = new ArrayList<>();

        // group that has only intervals outside the block
        final Verkehrsbeziehung vbOnlyOutside = new Verkehrsbeziehung();
        vbOnlyOutside.setVon(9);
        final Zeitintervall ziOnlyOutside = new Zeitintervall();
        ziOnlyOutside.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(5, 30)));
        ziOnlyOutside.setEndeUhrzeit(ziOnlyOutside.getStartUhrzeit().plusMinutes(15));
        ziOnlyOutside.setPkw(50);
        ziOnlyOutside.setVerkehrsbeziehung(vbOnlyOutside);
        input.add(ziOnlyOutside);

        // group that has both outside and inside intervals
        final Verkehrsbeziehung vbMixed = new Verkehrsbeziehung();
        vbMixed.setVon(7);
        // outside interval (should be ignored)
        final Zeitintervall ziMixedOutside = new Zeitintervall();
        ziMixedOutside.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(5, 45)));
        ziMixedOutside.setEndeUhrzeit(ziMixedOutside.getStartUhrzeit().plusMinutes(15));
        ziMixedOutside.setPkw(100);
        ziMixedOutside.setVerkehrsbeziehung(vbMixed);
        input.add(ziMixedOutside);
        // inside intervals (should be considered)
        final Zeitintervall ziMixed1 = new Zeitintervall();
        ziMixed1.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)));
        ziMixed1.setEndeUhrzeit(ziMixed1.getStartUhrzeit().plusMinutes(15));
        ziMixed1.setPkw(1);
        ziMixed1.setVerkehrsbeziehung(vbMixed);
        input.add(ziMixed1);

        final Zeitintervall ziMixed2 = new Zeitintervall();
        ziMixed2.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 15)));
        ziMixed2.setEndeUhrzeit(ziMixed2.getStartUhrzeit().plusMinutes(15));
        ziMixed2.setPkw(2);
        ziMixed2.setVerkehrsbeziehung(vbMixed);
        input.add(ziMixed2);

        final var result = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstundenForEachBewegungsbeziehungForZeitblock(
                UUID.randomUUID(), Zeitblock.ZB_06_10, Zaehlart.N, input, Set.of(TypeZeitintervall.SPITZENSTUNDE_KFZ));

        // Expect only one resulting spitzenstunde (for vbMixed), since vbOnlyOutside has no intervals inside the block
        assertThat(result.size(), is(1));
        final var out = result.get(0);
        assertThat(out.getVerkehrsbeziehung().getVon(), is(vbMixed.getVon()));
        assertThat(out.getLaengsverkehr(), is(nullValue()));
        assertThat(out.getQuerungsverkehr(), is(nullValue()));
    }

    private List<Zeitintervall> createIntervals() {
        // Erzeuge 96 Viertelstundenintervalle vom DaveConstants.DEFAULT_LOCALDATE 00:00 bis 24:00
        List<Zeitintervall> intervals = new ArrayList<>();
        LocalDate day = DaveConstants.DEFAULT_LOCALDATE;

        // Eine einzige Verkehrsbeziehung-Instanz für alle Intervalle, damit die Gruppierung eine Gruppe ergibt
        Verkehrsbeziehung verkehrsbeziehung = new Verkehrsbeziehung();
        for (int i = 0; i < 96; i++) {
            LocalTime start = LocalTime.MIDNIGHT.plusMinutes(15L * i);
            LocalTime end = start.plusMinutes(15);
            Zeitintervall zi = new Zeitintervall();
            zi.setStartUhrzeit(LocalDateTime.of(day, start));
            // Für das letzte Intervall kann das Ende dem Mitternachtspunkt des nächsten Tages entsprechen, die Utils behandeln dies jedoch über LocalTime.MAX
            zi.setEndeUhrzeit(LocalDateTime.of(day, end));
            // Basiszählung: 1 PKW pro Intervall
            zi.setPkw(1);
            // Basiszählung: 1 Fahrradfahrer pro Intervall
            zi.setFahrradfahrer(1);
            // Basiszählung: 1 Fussgänger pro Intervall
            zi.setFussgaenger(1);
            // Gleiche Verkehrsbeziehung zuweisen, damit die Gruppierung eine Gruppe ergibt
            zi.setVerkehrsbeziehung(verkehrsbeziehung);
            intervals.add(zi);
        }
        return intervals;
    }
}
