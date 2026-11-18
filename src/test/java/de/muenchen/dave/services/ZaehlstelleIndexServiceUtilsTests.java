package de.muenchen.dave.services;

import de.muenchen.dave.domain.enums.Status;
import de.muenchen.dave.relational.zaehlstellen.KnotenarmEntity;
import de.muenchen.dave.relational.zaehlstellen.ZaehlungEntity;
import java.time.LocalDate;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@Slf4j
public class ZaehlstelleIndexServiceUtilsTests {

    @Test
    public void testJahreszeitenDetector() {
        LocalDate w1 = LocalDate.of(2019, 1, 1);
        LocalDate w2 = LocalDate.of(2019, 12, 1);
        LocalDate f = LocalDate.of(2019, 3, 1);
        LocalDate s = LocalDate.of(2019, 6, 1);
        LocalDate h = LocalDate.of(2019, 9, 1);

        assertThat(IndexServiceUtils.jahreszeitenDetector(w1), is(equalTo(IndexServiceUtils.WINTER)));
        assertThat(IndexServiceUtils.jahreszeitenDetector(w2), is(equalTo(IndexServiceUtils.WINTER)));
        assertThat(IndexServiceUtils.jahreszeitenDetector(f), is(equalTo(IndexServiceUtils.FRUEHLING)));
        assertThat(IndexServiceUtils.jahreszeitenDetector(s), is(equalTo(IndexServiceUtils.SOMMER)));
        assertThat(IndexServiceUtils.jahreszeitenDetector(h), is(equalTo(IndexServiceUtils.HERBST)));

    }

    @Test
    public void testGetLetzteAktiveZaehlung() {
        ZaehlungEntity z1 = new ZaehlungEntity();
        z1.setDatum(LocalDate.of(2010, 1, 1));
        z1.setStatus(Status.ACTIVE);

        ZaehlungEntity z2 = new ZaehlungEntity();
        z2.setDatum(LocalDate.of(2015, 1, 1));
        z2.setStatus(Status.ACTIVE);

        ZaehlungEntity z3 = new ZaehlungEntity();
        z3.setDatum(LocalDate.of(2020, 1, 1));
        z3.setStatus(Status.ACTIVE);

        assertThat(IndexServiceUtils.getLetzteAktiveZaehlung(Lists.newArrayList(z1, z2, z3)), is(equalTo(z3)));

        z3.setStatus(Status.INACTIVE);
        assertThat(IndexServiceUtils.getLetzteAktiveZaehlung(Lists.newArrayList(z1, z2, z3)), is(equalTo(z2)));
    }

    @Test
    public void createKreuzungsname() {
        String kreuzungsname = "";
        final ZaehlungEntity zaehlung = new ZaehlungEntity();
        final KnotenarmEntity knotenarm1 = new KnotenarmEntity();
        knotenarm1.setNummer(1);
        knotenarm1.setStrassenname("strasse1");
        final KnotenarmEntity knotenarm2 = new KnotenarmEntity();
        knotenarm2.setNummer(2);
        knotenarm2.setStrassenname("strasse2");
        final KnotenarmEntity knotenarm3 = new KnotenarmEntity();
        knotenarm3.setNummer(3);
        knotenarm3.setStrassenname("strasse3");
        final KnotenarmEntity knotenarm4 = new KnotenarmEntity();
        knotenarm4.setNummer(4);
        knotenarm4.setStrassenname("strasse2");
        zaehlung.setKnotenarme(Arrays.asList(knotenarm1, knotenarm2, knotenarm3, knotenarm4));

        String result = IndexServiceUtils.createKreuzungsname(kreuzungsname, zaehlung);
        assertThat(result, is(equalTo("strasse1 - strasse2 - strasse3")));

        kreuzungsname = "test";
        result = IndexServiceUtils.createKreuzungsname(kreuzungsname, zaehlung);
        assertThat(result, is(equalTo("test")));

        kreuzungsname = "";
        knotenarm2.setNummer(3);
        knotenarm3.setNummer(2);
        result = IndexServiceUtils.createKreuzungsname(kreuzungsname, zaehlung);
        assertThat(result, is(equalTo("strasse1 - strasse3 - strasse2")));
    }

}
