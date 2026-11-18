package de.muenchen.dave.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import de.muenchen.dave.domain.enums.Wetter;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.relational.zaehlstellen.KnotenarmEntity;
import de.muenchen.dave.relational.zaehlstellen.ZaehlstelleEntity;
import de.muenchen.dave.relational.zaehlstellen.ZaehlungEntity;
import de.muenchen.dave.services.IndexServiceUtils;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class SuchwortUtilTests {

    @Test
    public void testGenerateSuchworteOfZaehlstelle() {
        final ZaehlstelleEntity zaehlstelle = new ZaehlstelleEntity();
        zaehlstelle.setStadtbezirk("Pasing-Obermenzing");
        final List<String> expected = Arrays.asList("Pasing", "Obermenzing", "Pasing-Obermenzing");
        final Set<String> suchworte = SuchwortUtil.generateSuchworteOfZaehlstelle(zaehlstelle);
        assertThat(suchworte, containsInAnyOrder(expected.toArray(new String[0])));
    }

    @Test
    public void testGenerateSuchworteOfZaehlung() {
        final ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setWetter(Wetter.CLOUDY);
        zaehlung.setZaehldauer(Zaehldauer.DAUER_24_STUNDEN);
        final LocalDate datum = LocalDate.of(2000, Month.JANUARY, 1);
        final String datumAsString = datum.format(IndexServiceUtils.DDMMYYYY);
        zaehlung.setDatum(datum);
        final KnotenarmEntity k1 = new KnotenarmEntity();
        k1.setNummer(0);
        k1.setStrassenname("Knotenarm 1");
        final KnotenarmEntity k2 = new KnotenarmEntity();
        k2.setNummer(1);
        k2.setStrassenname("Knotenarm 2");
        final List<KnotenarmEntity> knotenarme = Arrays.asList(k1, k2);
        zaehlung.setKnotenarme(knotenarme);

        zaehlung.setGeographie(Arrays.asList("QI", "Isar", "Isarschnitt"));
        zaehlung.setJahreszeit("Sommer");
        zaehlung.setProjektName("Projektname");
        zaehlung.setJahr("2000");
        zaehlung.setMonat("Januar");
        zaehlung.setTagesTyp("Werktag");
        zaehlung.setKreuzungsname("Kreuzungsname");
        zaehlung.setKreisverkehr(true);
        zaehlung.setSonderzaehlung(true);

        final List<String> expected = Arrays.asList("QI", "Isar", "Isarschnitt", "Sommer", "Projektname",
                "2000", "Januar", "Werktag", "Kreuzungsname", "Kreisverkehr", "Sonderzählung",
                "bewölkt", "wolkig", datumAsString, datum.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.GERMANY),
                "Tageszählung", "Ganztageszählung", "Tag", "24-Stundenzählung", "24Stundenzählung", "24h", "24Stunden",
                "Knotenarm 1", "Knotenarm 2", "Querschnitt", "zweiarmig");

        final Set<String> suchworte = SuchwortUtil.generateSuchworteOfZaehlung(zaehlung);
        assertThat(suchworte, containsInAnyOrder(expected.toArray(new String[0])));
    }

    @Test
    public void testGetSuchworteOfWetter() {
        final List<String> expected = Arrays.asList("bewölkt", "wolkig");
        final List<String> suchworte = SuchwortUtil.getSuchworteOfWetter(Wetter.CLOUDY.name());
        assertThat(suchworte, containsInAnyOrder(expected.toArray(new String[0])));
    }

    @Test
    public void testGetSuchworteOfZaehldauer() {
        final List<String> expected = Arrays.asList("Tageszählung", "Ganztageszählung", "Tag", "24-Stundenzählung", "24Stundenzählung", "24h", "24Stunden");
        final List<String> suchworte = SuchwortUtil.getSuchworteOfZaehldauer(Zaehldauer.DAUER_24_STUNDEN.name());
        assertThat(suchworte, containsInAnyOrder(expected.toArray(new String[0])));
    }

    @Test
    public void testGetSuchworteOfDatum() {
        final LocalDate datum = LocalDate.of(2000, Month.JANUARY, 1);
        final String datumAsString = datum.format(IndexServiceUtils.DDMMYYYY);
        final List<String> expected = Arrays.asList(datumAsString, datum.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.GERMANY));
        final List<String> suchworte = SuchwortUtil.getSuchworteOfDatum(datum);
        assertThat(suchworte, containsInAnyOrder(expected.toArray(new String[0])));
    }

    @Test
    public void testGetSuchworteOfKnotenarme() {
        final KnotenarmEntity k1 = new KnotenarmEntity();
        k1.setNummer(0);
        k1.setStrassenname("Knotenarm 1");
        final KnotenarmEntity k2 = new KnotenarmEntity();
        k2.setNummer(1);
        k2.setStrassenname("Knotenarm 2");
        final List<KnotenarmEntity> knotenarme = Arrays.asList(k1, k2);
        final List<String> expected = Arrays.asList("Knotenarm 1", "Knotenarm 2", "Querschnitt", "zweiarmig");
        final List<String> suchworte = SuchwortUtil.getSuchworteOfKnotenarme(knotenarme);
        assertThat(suchworte, containsInAnyOrder(expected.toArray(new String[0])));
    }
}
