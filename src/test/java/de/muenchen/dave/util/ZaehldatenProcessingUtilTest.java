package de.muenchen.dave.util;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitauswahl;
import de.muenchen.dave.domain.enums.Zeitblock;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ZaehldatenProcessingUtilTest {

    @Test
    public void getStartUhrzeit() {
        final LadeZaehldatumDTO ladeZaehldatum = new LadeZaehldatumDTO();

        ladeZaehldatum.setStartUhrzeit(LocalTime.of(10, 11, 23));
        assertThat(ZaehldatenProcessingUtil.getStartUhrzeit(ladeZaehldatum), is("10:11"));

        ladeZaehldatum.setStartUhrzeit(LocalTime.of(11, 12));
        assertThat(ZaehldatenProcessingUtil.getStartUhrzeit(ladeZaehldatum), is("11:12"));
    }

    @Test
    public void nullsafeCast() {
        assertThat(ZaehldatenProcessingUtil.nullsafeCast(new BigDecimal(10)), is(new Integer(10)));
        assertThat(ZaehldatenProcessingUtil.nullsafeCast(null), is(IsNull.nullValue()));
    }

    @Test
    public void checkAndAddToXAxisWhenNotAvailable() {
        final List<String> xAxisData = new ArrayList<>();
        xAxisData.add("A");
        xAxisData.add("B");

        assertThat(ZaehldatenProcessingUtil.checkAndAddToXAxisWhenNotAvailable(xAxisData, "C").size(), is(3));
        Iterator<String> iterator = xAxisData.iterator();
        assertThat(iterator.next(), is("A"));
        assertThat(iterator.next(), is("B"));
        assertThat(iterator.next(), is("C"));

        assertThat(ZaehldatenProcessingUtil.checkAndAddToXAxisWhenNotAvailable(xAxisData, "B").size(), is(3));
        iterator = xAxisData.iterator();
        assertThat(iterator.next(), is("A"));
        assertThat(iterator.next(), is("B"));
        assertThat(iterator.next(), is("C"));
    }

    @Test
    public void getValueRoundedToHundred() {
        assertThat(ZaehldatenProcessingUtil.getValueRounded(1420, 100), is(1500));
        assertThat(ZaehldatenProcessingUtil.getValueRounded(1, 100), is(100));
        assertThat(ZaehldatenProcessingUtil.getValueRounded(0, 100), is(100));

        assertThat(ZaehldatenProcessingUtil.getValueRounded(BigDecimal.valueOf(1420), 100), is(1500));
        assertThat(ZaehldatenProcessingUtil.getValueRounded(BigDecimal.valueOf(1), 100), is(100));
        assertThat(ZaehldatenProcessingUtil.getValueRounded(BigDecimal.valueOf(0), 100), is(100));
    }

    @Test
    public void getValueRoundedToTen() {
        assertThat(ZaehldatenProcessingUtil.getValueRounded(BigDecimal.valueOf(14.2), 10), is(20));
        assertThat(ZaehldatenProcessingUtil.getValueRounded(BigDecimal.valueOf(0.01), 10), is(10));
        assertThat(ZaehldatenProcessingUtil.getValueRounded(BigDecimal.valueOf(0), 10), is(10));
    }

    @Test
    public void getZeroIfNull() {
        BigDecimal valueToCheck = null;
        assertThat(ZaehldatenProcessingUtil.getZeroIfNull(valueToCheck), is(BigDecimal.ZERO));
        valueToCheck = BigDecimal.TEN;
        assertThat(ZaehldatenProcessingUtil.getZeroIfNull(valueToCheck), is(BigDecimal.TEN));

        Integer valueToCheck2 = null;
        assertThat(ZaehldatenProcessingUtil.getZeroIfNull(valueToCheck2), is(0));
        valueToCheck2 = 10;
        assertThat(ZaehldatenProcessingUtil.getZeroIfNull(valueToCheck2), is(10));
    }

    @Test
    public void createHardcodedOptions() {
        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setKategorien(Arrays.asList(Fahrzeug.KFZ, Fahrzeug.RAD, Fahrzeug.FUSS));
        zaehlung.setZaehldauer("DAUER_2_X_4_STUNDEN");

        final OptionsDTO expected = new OptionsDTO();
        expected.setZaehldauer(Zaehldauer.DAUER_2_X_4_STUNDEN);
        expected.setIntervall(null);
        expected.setZeitblock(Zeitblock.ZB_00_24);
        expected.setZeitauswahl(Zeitauswahl.TAGESWERT.getCapitalizedName());
        expected.setKraftfahrzeugverkehr(true);
        expected.setSchwerverkehr(true);
        expected.setGueterverkehr(true);
        expected.setRadverkehr(true);
        expected.setFussverkehr(true);
        expected.setBlocksumme(true);
        expected.setTagessumme(true);
        expected.setSpitzenstunde(true);
        expected.setSpitzenstundeKfz(true);
        expected.setSpitzenstundeRad(true);
        expected.setSpitzenstundeFuss(true);

        assertThat(ZaehldatenProcessingUtil.createHardcodedOptions(zaehlung), is(expected));
    }

    @Test
    public void getMonatTextuell() {
        assertThat(ZaehldatenProcessingUtil.getMonatTextuell(7), is("Juli"));
        assertThat(ZaehldatenProcessingUtil.getMonatTextuell(LocalDate.of(2021, 7, 27)), is("Juli"));
    }

}
