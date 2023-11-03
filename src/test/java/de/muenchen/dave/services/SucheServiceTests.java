package de.muenchen.dave.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.Lists;
import de.muenchen.dave.domain.dtos.ZaehlartenKarteDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.util.SuchwortUtil;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Slf4j
public class SucheServiceTests {

    private SucheService service = new SucheService(
            null,
            null,
            null,
            null);

    @Test
    public void mapZaehlungenToZaehlartenKarte() {
        List<Zaehlung> zaehlungen = null;
        assertThat(SucheService.mapZaehlungenToZaehlartenKarte(zaehlungen), is(new HashSet<>()));

        zaehlungen = new ArrayList<>();
        assertThat(SucheService.mapZaehlungenToZaehlartenKarte(zaehlungen), is(new HashSet<>()));

        Zaehlung zaehlung = new Zaehlung();
        zaehlung.setZaehlart(Zaehlart.getZaehlartkürzel(Zaehlart.H));
        zaehlung.setPunkt(new GeoPoint(1, 2));
        zaehlungen.add(zaehlung);

        zaehlung = new Zaehlung();
        zaehlung.setZaehlart(Zaehlart.getZaehlartkürzel(Zaehlart.QR));
        zaehlung.setPunkt(new GeoPoint(3, 4));
        zaehlungen.add(zaehlung);

        zaehlung = new Zaehlung();
        zaehlung.setZaehlart(Zaehlart.getZaehlartkürzel(Zaehlart.N));
        zaehlung.setPunkt(new GeoPoint(3, 4));
        zaehlungen.add(zaehlung);

        zaehlung = new Zaehlung();
        zaehlung.setZaehlart(Zaehlart.getZaehlartkürzel(Zaehlart.T));
        zaehlung.setPunkt(new GeoPoint(3, 4));
        zaehlungen.add(zaehlung);

        Set<ZaehlartenKarteDTO> expected = new HashSet<>();
        ZaehlartenKarteDTO zaehlartenKarte = new ZaehlartenKarteDTO();
        zaehlartenKarte.setLatitude(1.0);
        zaehlartenKarte.setLongitude(2.0);
        zaehlartenKarte.setZaehlarten(new TreeSet<>(Arrays.asList("H")));
        expected.add(zaehlartenKarte);

        zaehlartenKarte = new ZaehlartenKarteDTO();
        zaehlartenKarte.setLatitude(3.0);
        zaehlartenKarte.setLongitude(4.0);
        zaehlartenKarte.setZaehlarten(new TreeSet<>(Arrays.asList("K", "QR", "T")));
        expected.add(zaehlartenKarte);

        assertThat(SucheService.mapZaehlungenToZaehlartenKarte(zaehlungen), is(expected));
    }

    @Test
    public void testIsDate() {
        assertThat(this.service.isDate("01.01.01"), is(true));
        assertThat(this.service.isDate("01.01.2001"), is(true));
        assertThat(this.service.isDate("01.1.01"), is(true));
        assertThat(this.service.isDate("01.1.2001"), is(true));
        assertThat(this.service.isDate("1.01.01"), is(true));
        assertThat(this.service.isDate("1.01.2001"), is(true));
        assertThat(this.service.isDate("1.1.01"), is(true));
        assertThat(this.service.isDate("1.1.2001"), is(true));

        assertThat(this.service.isDate("1.1.2011"), is(true));
        assertThat(this.service.isDate("1.12.11"), is(true));
        assertThat(this.service.isDate("Datum"), is(false));
    }

    @Test
    public void testCreateQueryString() {
        assertThat(this.service.createQueryString("foo bar"), is(equalTo("foo* bar*")));
        assertThat(this.service.createQueryString("foo bar 1.12"), is(equalTo("foo* bar* 01.12.*")));
    }

    @Test
    public void testCleanseDate() {
        String d1 = this.service.cleanseDate("12.1.16");
        assertThat(d1, is(equalTo("12.01.2016")));

        String d2 = this.service.cleanseDate("foo");
        assertThat(d2, is(equalTo("foo")));

        String d3 = this.service.cleanseDate("12.3");
        assertThat(d3, is(equalTo("12.03.")));

        String d4 = this.service.cleanseDate("12");
        assertThat(d4, is(equalTo("12")));

        String d5 = this.service.cleanseDate("1");
        assertThat(d5, is(equalTo("1")));

        String d6 = this.service.cleanseDate("3.");
        assertThat(d6, is(equalTo("03.")));

        String d7 = this.service.cleanseDate("12.1.90");
        assertThat(d7, is(equalTo("12.01.1990")));
    }

    @Test
    public void testFilterZaehlung() {
        Zaehlung z1 = new Zaehlung();
        z1.setDatum(LocalDate.parse("2017-04-03"));
        z1.setProjektName("Foobla");
        z1.setSuchwoerter(new ArrayList<>(SuchwortUtil.generateSuchworteOfZaehlung(z1)));

        ArrayList<String> w1 = Lists.newArrayList("Moosach", "Foo");
        assertThat(this.service.filterZaehlung(w1, z1), is(true));

        ArrayList<String> w2 = Lists.newArrayList("Moosach", "Bar", "3.");
        assertThat(this.service.filterZaehlung(w2, z1), is(true));

        ArrayList<String> w3 = Lists.newArrayList("Moosach", "Bar", "3");
        assertThat(this.service.filterZaehlung(w3, z1), is(false));

        ArrayList<String> w4 = Lists.newArrayList("Moosach", "Bar", "4.");
        assertThat(this.service.filterZaehlung(w4, z1), is(false));
    }

    @Test
    public void testCheckZaehlstelleForZaehlung() {
        Zaehlstelle zs1 = new Zaehlstelle();
        Zaehlung z1 = new Zaehlung();
        z1.setDatum(LocalDate.parse("2017-04-03"));
        z1.setProjektName("Foobla");
        z1.setSuchwoerter(new ArrayList<>(SuchwortUtil.generateSuchworteOfZaehlung(z1)));
        Zaehlung z2 = new Zaehlung();
        z2.setDatum(LocalDate.parse("2014-07-20"));
        z2.setProjektName("foo");
        z2.setSuchwoerter(new ArrayList<>(SuchwortUtil.generateSuchworteOfZaehlung(z2)));
        Zaehlung z3 = new Zaehlung();
        z3.setDatum(LocalDate.parse("2019-08-04"));
        z3.setProjektName("bar");
        z3.setSuchwoerter(new ArrayList<>(SuchwortUtil.generateSuchworteOfZaehlung(z3)));
        zs1.setZaehlungen(Lists.newArrayList(z1, z2, z3));

        // Test mit Datum
        Optional<Zaehlung> optionalZaehlung1 = this.service.checkZaehlstelleForZaehlung(zs1, "Nymphenburg 20 bla");
        assertThat(optionalZaehlung1.isPresent(), is(true));
        assertThat(optionalZaehlung1.get(), is(equalTo(z2)));

        // Test mit falschem Datum
        Optional<Zaehlung> optionalZaehlung2 = this.service.checkZaehlstelleForZaehlung(zs1, "Nymphenburg 27.03.2018");
        assertThat(optionalZaehlung2.isPresent(), is(false));

        // Test mit "Foo" (groß)
        Optional<Zaehlung> optionalZaehlung3 = this.service.checkZaehlstelleForZaehlung(zs1, "Nymphenburg Foo Bla");
        assertThat(optionalZaehlung3.isPresent(), is(true));
        assertThat(optionalZaehlung3.get(), is(equalTo(z1)));

        // Test mit "foo" (klein)
        Optional<Zaehlung> optionalZaehlung4 = this.service.checkZaehlstelleForZaehlung(zs1, "Nymphenburg foo Bla");
        assertThat(optionalZaehlung4.isPresent(), is(true));
        assertThat(optionalZaehlung4.get(), is(equalTo(z2)));

        // Test mit falschem Text
        Optional<Zaehlung> optionalZaehlung5 = this.service.checkZaehlstelleForZaehlung(zs1, "Nymphenburg Foobar Bla");
        assertThat(optionalZaehlung5.isPresent(), is(false));
    }

}
