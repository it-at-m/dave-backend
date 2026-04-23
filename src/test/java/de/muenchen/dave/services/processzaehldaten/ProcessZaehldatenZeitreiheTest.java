package de.muenchen.dave.services.processzaehldaten;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.OptionsLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.OptionsQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.OptionsVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenZeitreiheDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.*;
import de.muenchen.dave.domain.enums.Bewegungsrichtung;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import de.muenchen.dave.domain.enums.Zaehlart;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ProcessZaehldatenZeitreiheTest {

    private static final LocalDate DATE1 = LocalDate.of(2011, 11, 4);
    private static final LocalDate DATE2 = LocalDate.of(2016, 8, 8);
    private static final LocalDate DATE3 = LocalDate.of(2020, 4, 3);
    private static final LocalDate DATE4 = LocalDate.of(2024, 1, 11);
    private static final String ID1 = "1234";
    private static final String ID2 = "5678";
    private static final String ID3 = "90ab";
    private static final String ID4 = "42cd";

    private static Zaehlstelle getZaehlstelleWithZaehlungen() {

        Zaehlung zaehlung1 = new Zaehlung();
        zaehlung1.setId(ID1);
        zaehlung1.setDatum(DATE1);
        zaehlung1.setZaehlart(Zaehlart.N.name());

        Zaehlung zaehlung2 = new Zaehlung();
        zaehlung2.setId(ID2);
        zaehlung2.setDatum(DATE2);
        zaehlung2.setZaehlart(Zaehlart.N.name());

        Zaehlung zaehlung3 = new Zaehlung();
        zaehlung3.setId(ID3);
        zaehlung3.setDatum(DATE3);
        zaehlung3.setZaehlart(Zaehlart.R.name());

        Zaehlung zaehlung4 = new Zaehlung();
        zaehlung4.setId(ID4);
        zaehlung4.setDatum(DATE4);
        zaehlung4.setZaehlart(Zaehlart.N.name());

        List<Zaehlung> zaehlungList = new ArrayList<>();
        zaehlungList.add(zaehlung1);
        zaehlungList.add(zaehlung2);
        zaehlungList.add(zaehlung3);
        zaehlungList.add(zaehlung4);

        Zaehlstelle zaehlstelle = new Zaehlstelle();
        zaehlstelle.setZaehlungen(zaehlungList);

        return zaehlstelle;
    }

    @Test
    public void calculateOldestDate() {
        Zaehlstelle zaehlstelle = getZaehlstelleWithZaehlungen();
        OptionsDTO options = new OptionsDTO();

        LocalDate currentDate = DATE4;
        assertThat(ProcessZaehldatenZeitreiheService.calculateOldestDate(zaehlstelle, currentDate, options),
                is(DATE1));

        options.setIdVergleichszaehlungZeitreihe(ID2);
        assertThat(ProcessZaehldatenZeitreiheService.calculateOldestDate(zaehlstelle, currentDate, options),
                is(DATE2));

        options.setIdVergleichszaehlungZeitreihe(null);
        currentDate = DATE2;
        assertThat(ProcessZaehldatenZeitreiheService.calculateOldestDate(zaehlstelle, currentDate, options),
                is(DATE1));
    }

    @Test
    public void fillLadeZaehldatenZeitreiheDTO() {
        final OptionsDTO options = new OptionsDTO();
        options.setKraftfahrzeugverkehr(true);
        options.setSchwerverkehr(true);
        options.setGueterverkehr(true);
        options.setFussverkehr(true);
        options.setRadverkehr(true);
        options.setSchwerverkehrsanteilProzent(true);
        options.setGueterverkehrsanteilProzent(true);
        options.setZeitreiheGesamt(true);

        final LadeZaehldatumDTO ladeZaehldatumDTO = new LadeZaehldatumDTO();
        ladeZaehldatumDTO.setPkw(100);
        ladeZaehldatumDTO.setLkw(50);
        ladeZaehldatumDTO.setLastzuege(20);
        ladeZaehldatumDTO.setBusse(5);
        ladeZaehldatumDTO.setKraftraeder(25);
        ladeZaehldatumDTO.setFahrradfahrer(40);
        ladeZaehldatumDTO.setFussgaenger(45);
        ladeZaehldatumDTO.setPkwEinheiten(100);

        final LadeZaehldatenZeitreiheDTO ladeZaehldatenZeitreiheDTO1 = new LadeZaehldatenZeitreiheDTO();
        ProcessZaehldatenZeitreiheService.fillLadeZaehldatenZeitreiheDTO(options, ladeZaehldatenZeitreiheDTO1,
                ladeZaehldatumDTO);
        assertThat(ladeZaehldatenZeitreiheDTO1.getKfz().get(0), is(new BigDecimal(200)));
        assertThat(ladeZaehldatenZeitreiheDTO1.getSv().get(0), is(new BigDecimal(75)));
        assertThat(ladeZaehldatenZeitreiheDTO1.getGv().get(0), is(new BigDecimal(70)));
        assertThat(ladeZaehldatenZeitreiheDTO1.getFuss().get(0), is(45));
        assertThat(ladeZaehldatenZeitreiheDTO1.getRad().get(0), is(40));
        assertThat(ladeZaehldatenZeitreiheDTO1.getGesamt().get(0), is(new BigDecimal(285)));
        assertThat(ladeZaehldatenZeitreiheDTO1.getSvAnteilInProzent().get(0), is(BigDecimal.valueOf(37.5)));
        assertThat(ladeZaehldatenZeitreiheDTO1.getGvAnteilInProzent().get(0), is(BigDecimal.valueOf(35.0)));

        options.setKraftfahrzeugverkehr(false);
        options.setSchwerverkehr(false);
        options.setGueterverkehr(false);
        options.setFussverkehr(false);
        options.setRadverkehr(false);
        options.setSchwerverkehrsanteilProzent(false);

        final LadeZaehldatenZeitreiheDTO ladeZaehldatenZeitreiheDTO2 = new LadeZaehldatenZeitreiheDTO();
        ProcessZaehldatenZeitreiheService.fillLadeZaehldatenZeitreiheDTO(options, ladeZaehldatenZeitreiheDTO2,
                ladeZaehldatumDTO);
        assertThat(ladeZaehldatenZeitreiheDTO2.getKfz().size(), is(0));
        assertThat(ladeZaehldatenZeitreiheDTO2.getSv().size(), is(0));
        assertThat(ladeZaehldatenZeitreiheDTO2.getGv().size(), is(0));
        assertThat(ladeZaehldatenZeitreiheDTO2.getFuss().size(), is(0));
        assertThat(ladeZaehldatenZeitreiheDTO2.getRad().size(), is(0));
        assertThat(ladeZaehldatenZeitreiheDTO2.getGesamt().get(0), is(new BigDecimal(285)));
        assertThat(ladeZaehldatenZeitreiheDTO2.getSvAnteilInProzent().size(), is(0));
    }

    @Test
    public void calculateGesamt() {
        BigDecimal kfz = new BigDecimal(1000);
        Integer fussgaenger = 100;
        Integer fahrradfahrer = 300;

        assertThat(ProcessZaehldatenZeitreiheService.calculateGesamt(kfz, fussgaenger, fahrradfahrer), is(new BigDecimal(1400)));

        fussgaenger = null;
        fahrradfahrer = null;
        assertThat(ProcessZaehldatenZeitreiheService.calculateGesamt(kfz, fussgaenger, fahrradfahrer), is(new BigDecimal(1000)));
    }

    @Test
    public void checkBewegungsbeziehungenQU() {
        // setup
        Zaehlung zaehlung = new Zaehlung();
        zaehlung.setZaehlart(Zaehlart.QU.name());
        Querungsverkehr qv1 = new Querungsverkehr();
        qv1.setKnotenarm(1);
        qv1.setRichtung(Himmelsrichtung.N);
        Querungsverkehr qv2 = new Querungsverkehr();
        qv2.setKnotenarm(1);
        qv2.setRichtung(Himmelsrichtung.S);
        zaehlung.setQuerungsverkehr(List.of(qv1, qv2));

        // positive case
        Zaehlung matchingZaehlung = new Zaehlung();
        matchingZaehlung.setZaehlart(Zaehlart.QU.name());
        matchingZaehlung.setQuerungsverkehr(List.of(qv1, qv2));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(zaehlung, new OptionsDTO(), matchingZaehlung), is(true));

        // negative cases
        Zaehlung nonMatchingZaehlung1 = new Zaehlung();
        nonMatchingZaehlung1.setZaehlart(Zaehlart.QU.name());
        nonMatchingZaehlung1.setQuerungsverkehr(List.of(qv1));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(zaehlung, new OptionsDTO(), nonMatchingZaehlung1), is(false));

        Zaehlung nonMatchingZaehlung2 = new Zaehlung();
        nonMatchingZaehlung2.setZaehlart(Zaehlart.QU.name());
        Querungsverkehr qv3 = new Querungsverkehr();
        qv3.setKnotenarm(2);
        qv3.setRichtung(Himmelsrichtung.S);
        nonMatchingZaehlung2.setQuerungsverkehr(List.of(qv3, qv2));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(zaehlung, new OptionsDTO(), nonMatchingZaehlung2), is(false));

        Zaehlung nonMatchingZaehlung3 = new Zaehlung();
        nonMatchingZaehlung3.setZaehlart(Zaehlart.QU.name());
        nonMatchingZaehlung3.setQuerungsverkehr(List.of(qv1, qv3));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(zaehlung, new OptionsDTO(), nonMatchingZaehlung3), is(false));

        Zaehlung nonMatchingZaehlung4 = new Zaehlung();
        nonMatchingZaehlung4.setZaehlart(Zaehlart.QU.name());
        Querungsverkehr qv4 = new Querungsverkehr();
        qv4.setKnotenarm(1);
        qv4.setRichtung(Himmelsrichtung.W);
        nonMatchingZaehlung4.setQuerungsverkehr(List.of(qv2, qv4));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(zaehlung, new OptionsDTO(), nonMatchingZaehlung4), is(false));

    }

    @Test
    public void checkBewegungsbeziehungenFJS() {
        // setup
        Zaehlung zaehlung = new Zaehlung();
        zaehlung.setZaehlart(Zaehlart.FJS.name());
        Laengsverkehr l1 = new Laengsverkehr();
        l1.setKnotenarm(1);
        l1.setRichtung(Bewegungsrichtung.EIN);
        l1.setStrassenseite(Himmelsrichtung.N);
        Laengsverkehr l2 = new Laengsverkehr();
        l2.setKnotenarm(1);
        l2.setRichtung(Bewegungsrichtung.AUS);
        l2.setStrassenseite(Himmelsrichtung.S);
        zaehlung.setLaengsverkehr(List.of(l1, l2));

        // positive cases
        Zaehlung matchingZaehlung = new Zaehlung();
        matchingZaehlung.setZaehlart(Zaehlart.FJS.name());
        matchingZaehlung.setLaengsverkehr(List.of(l1, l2));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(zaehlung, new OptionsDTO(), matchingZaehlung), is(true));

        // negative cases
        Zaehlung nonMatchingZaehlung1 = new Zaehlung();
        nonMatchingZaehlung1.setZaehlart(Zaehlart.FJS.name());
        nonMatchingZaehlung1.setLaengsverkehr(List.of(l1));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(zaehlung, new OptionsDTO(), nonMatchingZaehlung1), is(false));

        Zaehlung nonMatchingZaehlung2 = new Zaehlung();
        nonMatchingZaehlung2.setZaehlart(Zaehlart.FJS.name());
        Laengsverkehr l3 = new Laengsverkehr();
        l3.setKnotenarm(2);
        l3.setRichtung(Bewegungsrichtung.AUS);
        l3.setStrassenseite(Himmelsrichtung.S);
        nonMatchingZaehlung2.setLaengsverkehr(List.of(l1, l3));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(zaehlung, new OptionsDTO(), nonMatchingZaehlung2), is(false));

        Zaehlung nonMatchingZaehlung3 = new Zaehlung();
        nonMatchingZaehlung3.setZaehlart(Zaehlart.FJS.name());
        Laengsverkehr l4 = new Laengsverkehr();
        l4.setKnotenarm(1);
        l4.setRichtung(Bewegungsrichtung.EIN);
        l4.setStrassenseite(Himmelsrichtung.S);
        nonMatchingZaehlung3.setLaengsverkehr(List.of(l2, l4));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(zaehlung, new OptionsDTO(), nonMatchingZaehlung3), is(false));

        Zaehlung nonMatchingZaehlung4 = new Zaehlung();
        nonMatchingZaehlung4.setZaehlart(Zaehlart.FJS.name());
        Laengsverkehr l5 = new Laengsverkehr();
        l5.setKnotenarm(1);
        l5.setRichtung(Bewegungsrichtung.EIN);
        l5.setStrassenseite(Himmelsrichtung.W);
        nonMatchingZaehlung4.setLaengsverkehr(List.of(l1, l5));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(zaehlung, new OptionsDTO(), nonMatchingZaehlung4), is(false));

    }

    @Test
    public void checkBewegungsbeziehungenQJS() {
        // setup
        Zaehlung zaehlung = new Zaehlung();
        zaehlung.setZaehlart(Zaehlart.QJS.name());
        Verkehrsbeziehung vb1 = new Verkehrsbeziehung();
        vb1.setVon(1);
        vb1.setNach(3);
        vb1.setStrassenseite(Himmelsrichtung.N);
        Verkehrsbeziehung vb2 = new Verkehrsbeziehung();
        vb2.setVon(3);
        vb2.setNach(1);
        vb2.setStrassenseite(Himmelsrichtung.N);
        zaehlung.setVerkehrsbeziehungen(List.of(vb1, vb2));

        // positive case
        Zaehlung matchingZaehlung = new Zaehlung();
        matchingZaehlung.setZaehlart(Zaehlart.QJS.name());
        matchingZaehlung.setVerkehrsbeziehungen(List.of(vb1, vb2));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(zaehlung, new OptionsDTO(), matchingZaehlung), is(true));

        // negative cases
        Zaehlung nonMatchingZaehlung1 = new Zaehlung();
        nonMatchingZaehlung1.setZaehlart(Zaehlart.QJS.name());
        nonMatchingZaehlung1.setVerkehrsbeziehungen(List.of(vb1));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(zaehlung, new OptionsDTO(), nonMatchingZaehlung1), is(false));

        Zaehlung nonMatchingZaehlung2 = new Zaehlung();
        nonMatchingZaehlung2.setZaehlart(Zaehlart.QJS.name());
        Verkehrsbeziehung vb3 = new Verkehrsbeziehung();
        vb3.setVon(1);
        vb3.setNach(1);
        vb3.setStrassenseite(Himmelsrichtung.N);
        nonMatchingZaehlung2.setVerkehrsbeziehungen(List.of(vb1, vb3));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(zaehlung, new OptionsDTO(), nonMatchingZaehlung2), is(false));

        Zaehlung nonMatchingZaehlung3 = new Zaehlung();
        nonMatchingZaehlung3.setZaehlart(Zaehlart.QJS.name());
        Verkehrsbeziehung vb4 = new Verkehrsbeziehung();
        vb4.setVon(3);
        vb4.setNach(2);
        vb4.setStrassenseite(Himmelsrichtung.N);
        nonMatchingZaehlung3.setVerkehrsbeziehungen(List.of(vb2, vb4));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(zaehlung, new OptionsDTO(), nonMatchingZaehlung3), is(false));

        Zaehlung nonMatchingZaehlung4 = new Zaehlung();
        nonMatchingZaehlung4.setZaehlart(Zaehlart.QJS.name());
        Verkehrsbeziehung vb5 = new Verkehrsbeziehung();
        vb5.setVon(3);
        vb5.setNach(1);
        vb5.setStrassenseite(Himmelsrichtung.S);
        nonMatchingZaehlung4.setVerkehrsbeziehungen(List.of(vb1, vb5));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(zaehlung, new OptionsDTO(), nonMatchingZaehlung4), is(false));

    }
}
