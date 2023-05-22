package de.muenchen.dave.util.dataimport;

import de.muenchen.dave.TestUtils;
import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.util.DaveConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class GleitenderZeitintervallTest {

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
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)),
                2,
                1,
                2,
                null));
        zeitintervalle.add(TestUtils.createZeitintervall(
                zaehlungId,
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)),
                3,
                1,
                2,
                null));
        zeitintervalle.add(TestUtils.createZeitintervall(
                zaehlungId,
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 15)),
                4,
                1,
                2,
                null));
        zeitintervalle.add(TestUtils.createZeitintervall(
                zaehlungId,
                LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 30)),
                5,
                1,
                2,
                null));
    }

    @Test
    public void getSummedZeitintervall() {
        Zeitintervall result = GleitenderZeitintervall
                .createInstanceWithIndexParameterAsNewestIndex(zeitintervalle, 0, Zeitblock.ZB_06_10)
                .getSummedZeitintervall();
        Zeitintervall expected = new Zeitintervall();
        expected.setPkw(null);
        expected.setLkw(null);
        expected.setLastzuege(null);
        expected.setBusse(null);
        expected.setKraftraeder(null);
        expected.setFahrradfahrer(null);
        expected.setFussgaenger(null);
        expected.setType(null);
        expected.setHochrechnung(new Hochrechnung());
        assertThat(result, is(expected));

        result = GleitenderZeitintervall
                .createInstanceWithIndexParameterAsNewestIndex(zeitintervalle, 0, Zeitblock.ZB_10_15)
                .getSummedZeitintervall();
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        expected.setPkw(1);
        expected.setLkw(1);
        expected.setLastzuege(1);
        expected.setBusse(1);
        expected.setKraftraeder(1);
        expected.setFahrradfahrer(1);
        expected.setFussgaenger(1);
        assertThat(result, is(expected));

        result = GleitenderZeitintervall
                .createInstanceWithIndexParameterAsNewestIndex(zeitintervalle, 1, Zeitblock.ZB_10_15)
                .getSummedZeitintervall();
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 0)));
        expected.setPkw(3);
        expected.setLkw(3);
        expected.setLastzuege(3);
        expected.setBusse(3);
        expected.setKraftraeder(3);
        expected.setFahrradfahrer(3);
        expected.setFussgaenger(3);
        assertThat(result, is(expected));

        result = GleitenderZeitintervall
                .createInstanceWithIndexParameterAsNewestIndex(zeitintervalle, 2, Zeitblock.ZB_10_15)
                .getSummedZeitintervall();
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 15)));
        expected.setPkw(6);
        expected.setLkw(6);
        expected.setLastzuege(6);
        expected.setBusse(6);
        expected.setKraftraeder(6);
        expected.setFahrradfahrer(6);
        expected.setFussgaenger(6);
        assertThat(result, is(expected));

        result = GleitenderZeitintervall
                .createInstanceWithIndexParameterAsNewestIndex(zeitintervalle, 3, Zeitblock.ZB_10_15)
                .getSummedZeitintervall();
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 30)));
        expected.setPkw(10);
        expected.setLkw(10);
        expected.setLastzuege(10);
        expected.setBusse(10);
        expected.setKraftraeder(10);
        expected.setFahrradfahrer(10);
        expected.setFussgaenger(10);
        assertThat(result, is(expected));

        result = GleitenderZeitintervall
                .createInstanceWithIndexParameterAsNewestIndex(zeitintervalle, 4, Zeitblock.ZB_10_15)
                .getSummedZeitintervall();
        expected.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));
        expected.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(11, 45)));
        expected.setPkw(14);
        expected.setLkw(14);
        expected.setLastzuege(14);
        expected.setBusse(14);
        expected.setKraftraeder(14);
        expected.setFahrradfahrer(14);
        expected.setFussgaenger(14);
        assertThat(result, is(expected));
    }

    @Test
    public void calcNumberOfZeitintervallePerHour() {
        final List<Zeitintervall> zeitintervalle = new ArrayList<>();
        assertThat(GleitenderZeitintervall.calcNumberOfZeitintervallePerHour(zeitintervalle), is(4));

        final Zeitintervall zeitintervall = new Zeitintervall();
        zeitintervall.setStartUhrzeit(LocalDateTime.of(2020, 1, 1, 15, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(2020, 1, 1, 15, 5));
        zeitintervalle.add(zeitintervall);

        assertThat(GleitenderZeitintervall.calcNumberOfZeitintervallePerHour(zeitintervalle), is(12));

        zeitintervall.setStartUhrzeit(LocalDateTime.of(2020, 1, 1, 15, 0));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(2020, 1, 1, 15, 6));

        assertThat(GleitenderZeitintervall.calcNumberOfZeitintervallePerHour(zeitintervalle), is(10));
    }

}
