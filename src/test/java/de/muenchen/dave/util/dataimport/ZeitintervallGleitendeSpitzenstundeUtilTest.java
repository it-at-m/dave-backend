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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ZeitintervallGleitendeSpitzenstundeUtilTest {

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
    public void getGleitendeSpitzenstunden() {
        zeitintervalle12.get(8).setPkw(2);

        zeitintervalle12.get(zeitintervalle12.size() - 2).setPkw(5);

        final List<Zeitintervall> result = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstunden(zeitintervalle);

        assertThat(result.size(), is(36));

        Zeitintervall expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(0, 0)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 0)));
        expected.setSortingIndex(12000000);
        expected.setPkw(8);
        expected.setLkw(8);
        expected.setLastzuege(8);
        expected.setBusse(8);
        expected.setKraftraeder(8);
        expected.setFahrradfahrer(8);
        expected.setFussgaenger(8);
        expected.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        expected.setHochrechnung(new Hochrechnung());
        expected.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.getVerkehrsbeziehung().setVon(2);
        expected.getVerkehrsbeziehung().setNach(1);

        assertThat(result.get(0), is(expected));

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(0, 0)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 0)));
        expected.setSortingIndex(13000000);
        expected.setPkw(8);
        expected.setLkw(8);
        expected.setLastzuege(8);
        expected.setBusse(8);
        expected.setKraftraeder(8);
        expected.setFahrradfahrer(8);
        expected.setFussgaenger(8);
        expected.setType(TypeZeitintervall.SPITZENSTUNDE_RAD);
        expected.setHochrechnung(new Hochrechnung());
        expected.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.getVerkehrsbeziehung().setVon(2);
        expected.getVerkehrsbeziehung().setNach(1);

        assertThat(result.get(1), is(expected));

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(0, 0)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 0)));
        expected.setSortingIndex(14000000);
        expected.setPkw(8);
        expected.setLkw(8);
        expected.setLastzuege(8);
        expected.setBusse(8);
        expected.setKraftraeder(8);
        expected.setFahrradfahrer(8);
        expected.setFussgaenger(8);
        expected.setType(TypeZeitintervall.SPITZENSTUNDE_FUSS);
        expected.setHochrechnung(new Hochrechnung());
        expected.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.getVerkehrsbeziehung().setVon(2);
        expected.getVerkehrsbeziehung().setNach(1);

        assertThat(result.get(2), is(expected));

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 15)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(2, 15)));
        expected.setSortingIndex(12000000);
        expected.setPkw(5);
        expected.setLkw(4);
        expected.setLastzuege(4);
        expected.setBusse(4);
        expected.setKraftraeder(4);
        expected.setFahrradfahrer(4);
        expected.setFussgaenger(4);
        expected.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        expected.setHochrechnung(new Hochrechnung());
        expected.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.getVerkehrsbeziehung().setVon(1);
        expected.getVerkehrsbeziehung().setNach(2);

        assertThat(result.get(result.size() / 2), is(expected));

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(0, 0)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 0)));
        expected.setSortingIndex(13000000);
        expected.setPkw(4);
        expected.setLkw(4);
        expected.setLastzuege(4);
        expected.setBusse(4);
        expected.setKraftraeder(4);
        expected.setFahrradfahrer(4);
        expected.setFussgaenger(4);
        expected.setType(TypeZeitintervall.SPITZENSTUNDE_RAD);
        expected.setHochrechnung(new Hochrechnung());
        expected.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.getVerkehrsbeziehung().setVon(1);
        expected.getVerkehrsbeziehung().setNach(2);

        assertThat(result.get(result.size() / 2 + 1), is(expected));

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(0, 0)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 0)));
        expected.setSortingIndex(14000000);
        expected.setPkw(4);
        expected.setLkw(4);
        expected.setLastzuege(4);
        expected.setBusse(4);
        expected.setKraftraeder(4);
        expected.setFahrradfahrer(4);
        expected.setFussgaenger(4);
        expected.setType(TypeZeitintervall.SPITZENSTUNDE_FUSS);
        expected.setHochrechnung(new Hochrechnung());
        expected.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.getVerkehrsbeziehung().setVon(1);
        expected.getVerkehrsbeziehung().setNach(2);

        assertThat(result.get(result.size() / 2 + 2), is(expected));

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(22, 45)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 45)));
        expected.setSortingIndex(52000000);
        expected.setPkw(8);
        expected.setLkw(4);
        expected.setLastzuege(4);
        expected.setBusse(4);
        expected.setKraftraeder(4);
        expected.setFahrradfahrer(4);
        expected.setFussgaenger(4);
        expected.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        expected.setHochrechnung(new Hochrechnung());
        expected.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.getVerkehrsbeziehung().setVon(1);
        expected.getVerkehrsbeziehung().setNach(2);

        assertThat(result.get(result.size() - 6), is(expected));

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 0)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(20, 0)));
        expected.setSortingIndex(53000000);
        expected.setPkw(4);
        expected.setLkw(4);
        expected.setLastzuege(4);
        expected.setBusse(4);
        expected.setKraftraeder(4);
        expected.setFahrradfahrer(4);
        expected.setFussgaenger(4);
        expected.setType(TypeZeitintervall.SPITZENSTUNDE_RAD);
        expected.setHochrechnung(new Hochrechnung());
        expected.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.getVerkehrsbeziehung().setVon(1);
        expected.getVerkehrsbeziehung().setNach(2);

        assertThat(result.get(result.size() - 5), is(expected));

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 0)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(20, 0)));
        expected.setSortingIndex(54000000);
        expected.setPkw(4);
        expected.setLkw(4);
        expected.setLastzuege(4);
        expected.setBusse(4);
        expected.setKraftraeder(4);
        expected.setFahrradfahrer(4);
        expected.setFussgaenger(4);
        expected.setType(TypeZeitintervall.SPITZENSTUNDE_FUSS);
        expected.setHochrechnung(new Hochrechnung());
        expected.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.getVerkehrsbeziehung().setVon(1);
        expected.getVerkehrsbeziehung().setNach(2);

        assertThat(result.get(result.size() - 4), is(expected));

    }

    @Test
    public void getGleitendeSpitzenstundenForVerkehrsbeziehung() {
        zeitintervalle12.get(8).setPkw(2);

        zeitintervalle12.get(zeitintervalle12.size() - 2).setPkw(5);

        final Map<ZeitintervallBaseUtil.Intervall, List<Zeitintervall>> zeitintervalleGroupedByIntervall = ZeitintervallBaseUtil
                .createByIntervallGroupedZeitintervalle(zeitintervalle);
        final Verkehrsbeziehung verkehrsbeziehung = new Verkehrsbeziehung();
        verkehrsbeziehung.setVon(1);
        verkehrsbeziehung.setNach(2);

        List<Zeitintervall> result = TestUtils.privateStaticMethodCall(
                "getGleitendeSpitzenstundenForVerkehrsbeziehung",
                ZeitintervallGleitendeSpitzenstundeUtil.class,
                ArrayUtils.toArray(Verkehrsbeziehung.class, Map.class),
                ArrayUtils.toArray(verkehrsbeziehung, zeitintervalleGroupedByIntervall),
                List.class);

        assertThat(result.size(), is(18));

        Zeitintervall expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 15)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(2, 15)));
        expected.setSortingIndex(12000000);
        expected.setPkw(5);
        expected.setLkw(4);
        expected.setLastzuege(4);
        expected.setBusse(4);
        expected.setKraftraeder(4);
        expected.setFahrradfahrer(4);
        expected.setFussgaenger(4);
        expected.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        expected.setHochrechnung(new Hochrechnung());
        expected.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.getVerkehrsbeziehung().setVon(1);
        expected.getVerkehrsbeziehung().setNach(2);

        assertThat(result.get(0), is(expected));

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(0, 0)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 0)));
        expected.setSortingIndex(13000000);
        expected.setPkw(4);
        expected.setLkw(4);
        expected.setLastzuege(4);
        expected.setBusse(4);
        expected.setKraftraeder(4);
        expected.setFahrradfahrer(4);
        expected.setFussgaenger(4);
        expected.setType(TypeZeitintervall.SPITZENSTUNDE_RAD);
        expected.setHochrechnung(new Hochrechnung());
        expected.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.getVerkehrsbeziehung().setVon(1);
        expected.getVerkehrsbeziehung().setNach(2);

        assertThat(result.get(1), is(expected));

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(0, 0)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 0)));
        expected.setSortingIndex(14000000);
        expected.setPkw(4);
        expected.setLkw(4);
        expected.setLastzuege(4);
        expected.setBusse(4);
        expected.setKraftraeder(4);
        expected.setFahrradfahrer(4);
        expected.setFussgaenger(4);
        expected.setType(TypeZeitintervall.SPITZENSTUNDE_FUSS);
        expected.setHochrechnung(new Hochrechnung());
        expected.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.getVerkehrsbeziehung().setVon(1);
        expected.getVerkehrsbeziehung().setNach(2);

        assertThat(result.get(2), is(expected));

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(22, 45)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 45)));
        expected.setSortingIndex(52000000);
        expected.setPkw(8);
        expected.setLkw(4);
        expected.setLastzuege(4);
        expected.setBusse(4);
        expected.setKraftraeder(4);
        expected.setFahrradfahrer(4);
        expected.setFussgaenger(4);
        expected.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        expected.setHochrechnung(new Hochrechnung());
        expected.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.getVerkehrsbeziehung().setVon(1);
        expected.getVerkehrsbeziehung().setNach(2);

        assertThat(result.get(12), is(expected));

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 0)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(20, 0)));
        expected.setSortingIndex(53000000);
        expected.setPkw(4);
        expected.setLkw(4);
        expected.setLastzuege(4);
        expected.setBusse(4);
        expected.setKraftraeder(4);
        expected.setFahrradfahrer(4);
        expected.setFussgaenger(4);
        expected.setType(TypeZeitintervall.SPITZENSTUNDE_RAD);
        expected.setHochrechnung(new Hochrechnung());
        expected.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.getVerkehrsbeziehung().setVon(1);
        expected.getVerkehrsbeziehung().setNach(2);

        assertThat(result.get(13), is(expected));

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 0)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(20, 0)));
        expected.setSortingIndex(54000000);
        expected.setPkw(4);
        expected.setLkw(4);
        expected.setLastzuege(4);
        expected.setBusse(4);
        expected.setKraftraeder(4);
        expected.setFahrradfahrer(4);
        expected.setFussgaenger(4);
        expected.setType(TypeZeitintervall.SPITZENSTUNDE_FUSS);
        expected.setHochrechnung(new Hochrechnung());
        expected.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.getVerkehrsbeziehung().setVon(1);
        expected.getVerkehrsbeziehung().setNach(2);

        assertThat(result.get(14), is(expected));

    }

    @Test
    public void berechneGleitendeSpitzenstunde() {
        zeitintervalle12.sort((zeitintervall1, zeitintervall2) -> {
            return zeitintervall1.getStartUhrzeit().compareTo(zeitintervall1.getStartUhrzeit())
                    + zeitintervall2.getEndeUhrzeit().compareTo(zeitintervall2.getEndeUhrzeit());
        });

        // Change Zeitintervall 02:00 - 02:15 to provoke Spitzenstunde from 01:15 - 02:15
        zeitintervalle12.get(8).setPkw(2);

        // Change Zeitintervall 03:00 - 03:15 to provoke Spitzenstunde from 02:15 - 03:15
        zeitintervalle12.get(12).setFahrradfahrer(3);

        // Change Zeitintervall 04:00 - 04:15 to provoke Spitzenstunde from 03:15 - 04:15
        zeitintervalle12.get(16).setFussgaenger(4);

        final Verkehrsbeziehung verkehrsbeziehung = new Verkehrsbeziehung();
        verkehrsbeziehung.setVon(1);
        verkehrsbeziehung.setNach(2);

        ZeitintervallGleitendeSpitzenstundeUtil.GleitendeSpstdZeitintervallKfzRadFuss result = TestUtils.privateStaticMethodCall(
                "berechneGleitendeSpitzenstunde",
                ZeitintervallGleitendeSpitzenstundeUtil.class,
                ArrayUtils.toArray(UUID.class, Zeitblock.class, Verkehrsbeziehung.class, List.class),
                ArrayUtils.toArray(zaehlungId, Zeitblock.ZB_00_24, verkehrsbeziehung, zeitintervalle12),
                ZeitintervallGleitendeSpitzenstundeUtil.GleitendeSpstdZeitintervallKfzRadFuss.class);

        Zeitintervall expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 15)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(2, 15)));
        expected.setSortingIndex(60000000);
        expected.setPkw(5);
        expected.setLkw(4);
        expected.setLastzuege(4);
        expected.setBusse(4);
        expected.setKraftraeder(4);
        expected.setFahrradfahrer(4);
        expected.setFussgaenger(4);
        expected.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        expected.setHochrechnung(new Hochrechnung());
        expected.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.getVerkehrsbeziehung().setVon(1);
        expected.getVerkehrsbeziehung().setNach(2);

        assertThat(result.getGleitendeSpitzenstundeKfz(), is(Optional.of(expected)));

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(2, 15)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(3, 15)));
        expected.setSortingIndex(70000000);
        expected.setPkw(4);
        expected.setLkw(4);
        expected.setLastzuege(4);
        expected.setBusse(4);
        expected.setKraftraeder(4);
        expected.setFahrradfahrer(6);
        expected.setFussgaenger(4);
        expected.setType(TypeZeitintervall.SPITZENSTUNDE_RAD);
        expected.setHochrechnung(new Hochrechnung());
        expected.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.getVerkehrsbeziehung().setVon(1);
        expected.getVerkehrsbeziehung().setNach(2);

        assertThat(result.getGleitendeSpitzenstundeRad(), is(Optional.of(expected)));

        expected = new Zeitintervall();
        expected.setZaehlungId(zaehlungId);
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(3, 15)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(4, 15)));
        expected.setSortingIndex(80000000);
        expected.setPkw(4);
        expected.setLkw(4);
        expected.setLastzuege(4);
        expected.setBusse(4);
        expected.setKraftraeder(4);
        expected.setFahrradfahrer(4);
        expected.setFussgaenger(7);
        expected.setType(TypeZeitintervall.SPITZENSTUNDE_FUSS);
        expected.setHochrechnung(new Hochrechnung());
        expected.setVerkehrsbeziehung(new Verkehrsbeziehung());
        expected.getVerkehrsbeziehung().setVon(1);
        expected.getVerkehrsbeziehung().setNach(2);

        assertThat(result.getGleitendeSpitzenstundeFuss(), is(Optional.of(expected)));
    }

}
