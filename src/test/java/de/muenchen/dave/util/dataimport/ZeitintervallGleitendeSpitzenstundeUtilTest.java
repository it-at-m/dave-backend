package de.muenchen.dave.util.dataimport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.Hochrechnung;
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

public class ZeitintervallGleitendeSpitzenstundeUtilTest {

    @Test
    public void getGleitendeSpitzenstundenByBewegungsbeziehung_AllTypeZeitintervall() {
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

        List<Zeitintervall> result = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstundenByBewegungsbeziehung(
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
    public void getGleitendeSpitzenstundenByBewegungsbeziehung_TypeZeitintervallKfz() {
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

        List<Zeitintervall> result = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstundenByBewegungsbeziehung(
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
    public void getGleitendeSpitzenstundenByBewegungsbeziehung_TypeZeitintervallRad() {
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

        List<Zeitintervall> result = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstundenByBewegungsbeziehung(
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
    public void getGleitendeSpitzenstundenByBewegungsbeziehung_TypeZeitintervallFuss() {
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

        List<Zeitintervall> result = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstundenByBewegungsbeziehung(
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
}
