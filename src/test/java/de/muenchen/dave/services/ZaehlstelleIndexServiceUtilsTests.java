package de.muenchen.dave.services;

import de.muenchen.dave.domain.elasticsearch.Knotenarm;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@Slf4j
public class ZaehlstelleIndexServiceUtilsTests {

    @Test
    public void testSplitStrings() {
        String s = "a, b, , c,  d  ,e";
        assertThat(IndexServiceUtils.splitStrings(s), containsInAnyOrder("a", "b", "c", "d", "e"));
    }

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
    public void testGetZaehljahre() {
        Zaehlung z1 = new Zaehlung();
        z1.setJahr("2010");

        Zaehlung z2 = new Zaehlung();
        z2.setJahr("2015");

        Zaehlung z3 = new Zaehlung();
        z3.setJahr("2020");

        assertThat(IndexServiceUtils.getZaehljahre(Lists.newArrayList(z1, z2, z3)), containsInAnyOrder(2010, 2015, 2020));
    }

    @Test
    public void testGetLetzteAktiveZaehlung() {
        Zaehlung z1 = new Zaehlung();
        z1.setDatum(LocalDate.of(2010, 1, 1));
        z1.setStatus(Status.ACTIVE.name());

        Zaehlung z2 = new Zaehlung();
        z2.setDatum(LocalDate.of(2015, 1, 1));
        z2.setStatus(Status.ACTIVE.name());

        Zaehlung z3 = new Zaehlung();
        z3.setDatum(LocalDate.of(2020, 1, 1));
        z3.setStatus(Status.ACTIVE.name());

        assertThat(IndexServiceUtils.getLetzteAktiveZaehlung(Lists.newArrayList(z1, z2, z3)), is(equalTo(z3)));

        z3.setStatus(Status.INACTIVE.name());
        assertThat(IndexServiceUtils.getLetzteAktiveZaehlung(Lists.newArrayList(z1, z2, z3)), is(equalTo(z2)));
    }

    @Test
    public void createKreuzungsname() {
        String kreuzungsname = "";
        final Zaehlung zaehlung = new Zaehlung();
        final Knotenarm knotenarm1 = new Knotenarm();
        knotenarm1.setNummer(1);
        knotenarm1.setStrassenname("strasse1");
        final Knotenarm knotenarm2 = new Knotenarm();
        knotenarm2.setNummer(2);
        knotenarm2.setStrassenname("strasse2");
        final Knotenarm knotenarm3 = new Knotenarm();
        knotenarm3.setNummer(3);
        knotenarm3.setStrassenname("strasse3");
        final Knotenarm knotenarm4 = new Knotenarm();
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
