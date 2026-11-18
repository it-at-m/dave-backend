package de.muenchen.dave.services.processzaehldaten;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenZeitreiheDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.enums.Bewegungsrichtung;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.relational.zaehlstellen.LaengsverkehrEntity;
import de.muenchen.dave.relational.zaehlstellen.QuerungsverkehrEntity;
import de.muenchen.dave.relational.zaehlstellen.VerkehrsbeziehungEntity;
import de.muenchen.dave.relational.zaehlstellen.ZaehlstelleEntity;
import de.muenchen.dave.relational.zaehlstellen.ZaehlungEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class ProcessZaehldatenZeitreiheTest {

    private static final LocalDate DATE1 = LocalDate.of(2011, 11, 4);
    private static final LocalDate DATE2 = LocalDate.of(2016, 8, 8);
    private static final LocalDate DATE3 = LocalDate.of(2020, 4, 3);
    private static final LocalDate DATE4 = LocalDate.of(2024, 1, 11);
    private static final LocalDate DATE5 = LocalDate.of(2024, 1, 11);
    private static final UUID ID1 = UUID.fromString("1234");
    private static final UUID ID2 = UUID.fromString("5678");
    private static final UUID ID3 = UUID.fromString("90ab");
    private static final UUID ID4 = UUID.fromString("42cd");
    private static final UUID ID5 = UUID.fromString("51fa");

    private static ZaehlstelleEntity getZaehlstelleWithZaehlungen() {

        ZaehlungEntity zaehlung1 = new ZaehlungEntity();
        zaehlung1.setId(ID1);
        zaehlung1.setDatum(DATE1);
        zaehlung1.setZaehlart(Zaehlart.N);

        ZaehlungEntity zaehlung2 = new ZaehlungEntity();
        zaehlung2.setId(ID2);
        zaehlung2.setDatum(DATE2);
        zaehlung2.setZaehlart(Zaehlart.N);

        ZaehlungEntity zaehlung3 = new ZaehlungEntity();
        zaehlung3.setId(ID3);
        zaehlung3.setDatum(DATE3);
        zaehlung3.setZaehlart(Zaehlart.R);

        ZaehlungEntity zaehlung4 = new ZaehlungEntity();
        zaehlung4.setId(ID4);
        zaehlung4.setDatum(DATE4);
        zaehlung4.setZaehlart(Zaehlart.N);

        ZaehlungEntity zaehlung5 = new ZaehlungEntity();
        zaehlung5.setId(ID5);
        zaehlung5.setDatum(DATE5);
        zaehlung5.setZaehlart(Zaehlart.N);

        List<ZaehlungEntity> zaehlungList = new ArrayList<>();
        zaehlungList.add(zaehlung1);
        zaehlungList.add(zaehlung2);
        zaehlungList.add(zaehlung3);
        zaehlungList.add(zaehlung4);
        zaehlungList.add(zaehlung5);

        ZaehlstelleEntity zaehlstelle = new ZaehlstelleEntity();
        zaehlstelle.setZaehlungen(zaehlungList);

        return zaehlstelle;
    }

    @Test
    public void calculateOldestDate() {
        ZaehlstelleEntity zaehlstelle = getZaehlstelleWithZaehlungen();
        OptionsDTO options = new OptionsDTO();

        assertThat(ProcessZaehldatenZeitreiheService.calculateOldestDate(zaehlstelle, ID4, options),
                is(DATE1));

        assertThat(ProcessZaehldatenZeitreiheService.calculateOldestDate(zaehlstelle, ID5, options),
                is(DATE1));

        options.setIdVergleichszaehlungZeitreihe(String.valueOf(ID2));
        assertThat(ProcessZaehldatenZeitreiheService.calculateOldestDate(zaehlstelle, ID4, options),
                is(DATE2));

        options.setIdVergleichszaehlungZeitreihe(null);
        assertThat(ProcessZaehldatenZeitreiheService.calculateOldestDate(zaehlstelle, ID2, options),
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

        final Set<String> kategorien = new HashSet(List.of(Fahrzeug.KFZ.getName(), Fahrzeug.FUSS.getName(), Fahrzeug.GV_P.getName(), Fahrzeug.SV_P.getName(), Fahrzeug.GV.getName(), Fahrzeug.RAD.getName(), Fahrzeug.SV.getName(),
                Fahrzeug.PKW.getName(), Fahrzeug.LKW.getName(), Fahrzeug.LZ.getName(), Fahrzeug.BUS.getName(), Fahrzeug.KRAD.getName()));

        final LadeZaehldatenZeitreiheDTO ladeZaehldatenZeitreiheDTO1 = new LadeZaehldatenZeitreiheDTO();
        ProcessZaehldatenZeitreiheService.fillLadeZaehldatenZeitreiheDTO(options, ladeZaehldatenZeitreiheDTO1,
                ladeZaehldatumDTO, kategorien);
        assertThat(ladeZaehldatenZeitreiheDTO1.getKfz().getFirst(), is(new BigDecimal(200)));
        assertThat(ladeZaehldatenZeitreiheDTO1.getSv().getFirst(), is(new BigDecimal(75)));
        assertThat(ladeZaehldatenZeitreiheDTO1.getGv().getFirst(), is(new BigDecimal(70)));
        assertThat(ladeZaehldatenZeitreiheDTO1.getFuss().getFirst(), is(45));
        assertThat(ladeZaehldatenZeitreiheDTO1.getRad().getFirst(), is(40));
        assertThat(ladeZaehldatenZeitreiheDTO1.getGesamt().getFirst(), is(new BigDecimal(240)));
        assertThat(ladeZaehldatenZeitreiheDTO1.getSvAnteilInProzent().getFirst(), is(BigDecimal.valueOf(37.5)));
        assertThat(ladeZaehldatenZeitreiheDTO1.getGvAnteilInProzent().getFirst(), is(BigDecimal.valueOf(35.0)));

        options.setKraftfahrzeugverkehr(false);
        options.setSchwerverkehr(false);
        options.setGueterverkehr(false);
        options.setFussverkehr(false);
        options.setRadverkehr(false);
        options.setSchwerverkehrsanteilProzent(false);

        final LadeZaehldatenZeitreiheDTO ladeZaehldatenZeitreiheDTO2 = new LadeZaehldatenZeitreiheDTO();
        ProcessZaehldatenZeitreiheService.fillLadeZaehldatenZeitreiheDTO(options, ladeZaehldatenZeitreiheDTO2,
                ladeZaehldatumDTO, kategorien);
        assertThat(ladeZaehldatenZeitreiheDTO2.getKfz().size(), is(0));
        assertThat(ladeZaehldatenZeitreiheDTO2.getSv().size(), is(0));
        assertThat(ladeZaehldatenZeitreiheDTO2.getGv().size(), is(0));
        assertThat(ladeZaehldatenZeitreiheDTO2.getFuss().size(), is(0));
        assertThat(ladeZaehldatenZeitreiheDTO2.getRad().size(), is(0));
        assertThat(ladeZaehldatenZeitreiheDTO2.getGesamt().getFirst(), is(new BigDecimal(240)));
        assertThat(ladeZaehldatenZeitreiheDTO2.getSvAnteilInProzent().size(), is(0));
    }

    @Test
    public void fillLadeZaehldatenZeitreiheDTO_missingCategoriesProducesNulls() {
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

        // Kategorien ohne RAD und ohne SV_P (Schwerverkehrsanteil in Prozent fehlt)
        final Set<String> kategorien = new HashSet<>( List.of(Fahrzeug.KFZ.getName(), Fahrzeug.FUSS.getName(), Fahrzeug.GV_P.getName(), Fahrzeug.GV.getName(),
                Fahrzeug.PKW.getName(), Fahrzeug.LKW.getName(), Fahrzeug.LZ.getName(), Fahrzeug.BUS.getName(), Fahrzeug.KRAD.getName()));

        final LadeZaehldatenZeitreiheDTO result = new LadeZaehldatenZeitreiheDTO();
        ProcessZaehldatenZeitreiheService.fillLadeZaehldatenZeitreiheDTO(options, result,
                ladeZaehldatumDTO, kategorien);

        assertThat(result.getRad().getFirst(), is((Integer) null));
        assertThat(result.getFuss().getFirst(), is(45));
        assertThat(result.getKfz().getFirst(), is(new BigDecimal(200)));
        assertThat(result.getGesamt().getFirst(), is(new BigDecimal(200)));
        assertThat(result.getSvAnteilInProzent().getFirst(), is((BigDecimal) null));
        assertThat(result.getGvAnteilInProzent().getFirst(), is(BigDecimal.valueOf(35.0)));
    }

    @Test
    public void fillLadeZaehldatenZeitreiheDTO_zeitreiheGesamtNullWhenNoKfzOrRad() {
        final OptionsDTO options = new OptionsDTO();

        options.setKraftfahrzeugverkehr(false);
        options.setSchwerverkehr(false);
        options.setGueterverkehr(false);
        options.setFussverkehr(false);
        options.setRadverkehr(true); // Anfrage für Rad vorhanden
        options.setSchwerverkehrsanteilProzent(false);
        options.setGueterverkehrsanteilProzent(false);
        options.setZeitreiheGesamt(true);

        final LadeZaehldatumDTO ladeZaehldatumDTO = new LadeZaehldatumDTO();
        ladeZaehldatumDTO.setPkw(100);
        ladeZaehldatumDTO.setFahrradfahrer(40);
        ladeZaehldatumDTO.setFussgaenger(10);
        ladeZaehldatumDTO.setPkwEinheiten(100);

        // Kategorien ohne KFZ und ohne RAD => Gesamt muss null sein
        final Set<String> kategorien = new HashSet<>(List.of(Fahrzeug.FUSS.getName(), Fahrzeug.GV_P.getName()));

        final LadeZaehldatenZeitreiheDTO result = new LadeZaehldatenZeitreiheDTO();
        ProcessZaehldatenZeitreiheService.fillLadeZaehldatenZeitreiheDTO(options, result,
                ladeZaehldatumDTO, kategorien);

        // Da weder KFZ noch RAD in kategorien vorhanden sind, muss Gesamt null sein
        assertThat(result.getGesamt().getFirst(), is((BigDecimal) null));
    }

    @Test
    public void fillLadeZaehldatenZeitreiheDTO_percentPresentButAbsoluteMissing() {
        final OptionsDTO options = new OptionsDTO();
        options.setKraftfahrzeugverkehr(false);
        options.setSchwerverkehr(false);
        options.setGueterverkehr(true);
        options.setFussverkehr(false);
        options.setRadverkehr(false);
        options.setSchwerverkehrsanteilProzent(false);
        options.setGueterverkehrsanteilProzent(true);
        options.setZeitreiheGesamt(false);

        final LadeZaehldatumDTO ladeZaehldatumDTO = new LadeZaehldatumDTO();
        ladeZaehldatumDTO.setPkw(100);
        ladeZaehldatumDTO.setFahrradfahrer(40);
        ladeZaehldatumDTO.setFussgaenger(10);
        ladeZaehldatumDTO.setPkwEinheiten(100);

        // GV (absolute) fehlt, GV_P (prozent) ist vorhanden
        final Set<String> kategorien = new HashSet<>(List.of(Fahrzeug.GV_P.getName()));

        final LadeZaehldatenZeitreiheDTO result = new LadeZaehldatenZeitreiheDTO();
        ProcessZaehldatenZeitreiheService.fillLadeZaehldatenZeitreiheDTO(options, result,
                ladeZaehldatumDTO, kategorien);

        // Absoluter Gueterverkehr soll null sein, Prozentwert aber vorhanden in form von 0.0
        assertThat(result.getGv().getFirst(), is((BigDecimal) null));
        assertThat(result.getGvAnteilInProzent().getFirst(), is(BigDecimal.valueOf(0.0)));
    }

    @Test
    public void calculateGesamt() {
        BigDecimal kfz = new BigDecimal(1000);
        Integer fahrradfahrer = 300;

        assertThat(ProcessZaehldatenZeitreiheService.calculateGesamt(kfz, fahrradfahrer), is(new BigDecimal(1300)));

        fahrradfahrer = null;
        assertThat(ProcessZaehldatenZeitreiheService.calculateGesamt(kfz, fahrradfahrer), is(new BigDecimal(1000)));

        kfz = null;
        fahrradfahrer = 300;
        assertThat(ProcessZaehldatenZeitreiheService.calculateGesamt(kfz, fahrradfahrer), is(new BigDecimal(300)));

        // wenn nichts davon gezählt wurde, wird null zurückgegeben
        fahrradfahrer = null;
        assertThat(ProcessZaehldatenZeitreiheService.calculateGesamt(kfz, fahrradfahrer), is(nullValue()));

        // Wenn beide 0 dann ist Summe 0
        kfz = BigDecimal.ZERO;

        fahrradfahrer = 0;
        assertThat(ProcessZaehldatenZeitreiheService.calculateGesamt(kfz, fahrradfahrer), is(BigDecimal.ZERO));

    }

    @Test
    public void checkBewegungsbeziehungenQU() {
        // setup
        ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setZaehlart(Zaehlart.QU);
        QuerungsverkehrEntity qv1 = new QuerungsverkehrEntity();
        qv1.setKnotenarm(1);
        qv1.setRichtung(Himmelsrichtung.N);
        QuerungsverkehrEntity qv2 = new QuerungsverkehrEntity();
        qv2.setKnotenarm(1);
        qv2.setRichtung(Himmelsrichtung.S);
        zaehlung.setQuerungsverkehre(List.of(qv1, qv2));

        // positive case
        ZaehlungEntity matchingZaehlung = new ZaehlungEntity();
        matchingZaehlung.setZaehlart(Zaehlart.QU);
        matchingZaehlung.setQuerungsverkehre(List.of(qv1, qv2));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(matchingZaehlung, new OptionsDTO(), zaehlung), is(true));

        // negative cases
        ZaehlungEntity nonMatchingZaehlung1 = new ZaehlungEntity();
        nonMatchingZaehlung1.setZaehlart(Zaehlart.QU);
        nonMatchingZaehlung1.setQuerungsverkehre(List.of(qv1));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(nonMatchingZaehlung1, new OptionsDTO(), zaehlung), is(false));

        ZaehlungEntity nonMatchingZaehlung2 = new ZaehlungEntity();
        nonMatchingZaehlung2.setZaehlart(Zaehlart.QU);
        QuerungsverkehrEntity qv3 = new QuerungsverkehrEntity();
        qv3.setKnotenarm(2);
        qv3.setRichtung(Himmelsrichtung.S);
        nonMatchingZaehlung2.setQuerungsverkehre(List.of(qv3, qv2));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(nonMatchingZaehlung2, new OptionsDTO(), zaehlung), is(false));

        ZaehlungEntity nonMatchingZaehlung3 = new ZaehlungEntity();
        nonMatchingZaehlung3.setZaehlart(Zaehlart.QU);
        nonMatchingZaehlung3.setQuerungsverkehre(List.of(qv1, qv3));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(nonMatchingZaehlung3, new OptionsDTO(), zaehlung), is(false));

        ZaehlungEntity nonMatchingZaehlung4 = new ZaehlungEntity();
        nonMatchingZaehlung4.setZaehlart(Zaehlart.QU);
        QuerungsverkehrEntity qv4 = new QuerungsverkehrEntity();
        qv4.setKnotenarm(1);
        qv4.setRichtung(Himmelsrichtung.W);
        nonMatchingZaehlung4.setQuerungsverkehre(List.of(qv2, qv4));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(nonMatchingZaehlung4, new OptionsDTO(), zaehlung), is(false));

    }

    @Test
    public void checkBewegungsbeziehungenFJS() {
        // setup
        ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setZaehlart(Zaehlart.FJS);
        LaengsverkehrEntity l1 = new LaengsverkehrEntity();
        l1.setKnotenarm(1);
        l1.setRichtung(Bewegungsrichtung.EIN);
        l1.setStrassenseite(Himmelsrichtung.N);
        LaengsverkehrEntity l2 = new LaengsverkehrEntity();
        l2.setKnotenarm(1);
        l2.setRichtung(Bewegungsrichtung.AUS);
        l2.setStrassenseite(Himmelsrichtung.S);
        zaehlung.setLaengsverkehre(List.of(l1, l2));

        // positive cases
        ZaehlungEntity matchingZaehlung = new ZaehlungEntity();
        matchingZaehlung.setZaehlart(Zaehlart.FJS);
        matchingZaehlung.setLaengsverkehre(List.of(l1, l2));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(matchingZaehlung, new OptionsDTO(), zaehlung), is(true));

        // negative cases
        ZaehlungEntity nonMatchingZaehlung1 = new ZaehlungEntity();
        nonMatchingZaehlung1.setZaehlart(Zaehlart.FJS);
        nonMatchingZaehlung1.setLaengsverkehre(List.of(l1));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(nonMatchingZaehlung1, new OptionsDTO(), zaehlung), is(false));

        ZaehlungEntity nonMatchingZaehlung2 = new ZaehlungEntity();
        nonMatchingZaehlung2.setZaehlart(Zaehlart.FJS);
        LaengsverkehrEntity l3 = new LaengsverkehrEntity();
        l3.setKnotenarm(2);
        l3.setRichtung(Bewegungsrichtung.AUS);
        l3.setStrassenseite(Himmelsrichtung.S);
        nonMatchingZaehlung2.setLaengsverkehre(List.of(l1, l3));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(nonMatchingZaehlung2, new OptionsDTO(), zaehlung), is(false));

        ZaehlungEntity nonMatchingZaehlung3 = new ZaehlungEntity();
        nonMatchingZaehlung3.setZaehlart(Zaehlart.FJS);
        LaengsverkehrEntity l4 = new LaengsverkehrEntity();
        l4.setKnotenarm(1);
        l4.setRichtung(Bewegungsrichtung.EIN);
        l4.setStrassenseite(Himmelsrichtung.S);
        nonMatchingZaehlung3.setLaengsverkehre(List.of(l2, l4));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(nonMatchingZaehlung3, new OptionsDTO(), zaehlung), is(false));

        ZaehlungEntity nonMatchingZaehlung4 = new ZaehlungEntity();
        nonMatchingZaehlung4.setZaehlart(Zaehlart.FJS);
        LaengsverkehrEntity l5 = new LaengsverkehrEntity();
        l5.setKnotenarm(1);
        l5.setRichtung(Bewegungsrichtung.EIN);
        l5.setStrassenseite(Himmelsrichtung.W);
        nonMatchingZaehlung4.setLaengsverkehre(List.of(l1, l5));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(nonMatchingZaehlung4, new OptionsDTO(), zaehlung), is(false));

    }

    @Test
    public void checkBewegungsbeziehungenQJS() {
        // setup
        ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setZaehlart(Zaehlart.QJS);
        VerkehrsbeziehungEntity vb1 = new VerkehrsbeziehungEntity();
        vb1.setVon(1);
        vb1.setNach(3);
        vb1.setStrassenseite(Himmelsrichtung.N);
        VerkehrsbeziehungEntity vb2 = new VerkehrsbeziehungEntity();
        vb2.setVon(3);
        vb2.setNach(1);
        vb2.setStrassenseite(Himmelsrichtung.N);
        zaehlung.setVerkehrsbeziehungen(List.of(vb1, vb2));

        // positive case
        ZaehlungEntity matchingZaehlung = new ZaehlungEntity();
        matchingZaehlung.setZaehlart(Zaehlart.QJS);
        matchingZaehlung.setVerkehrsbeziehungen(List.of(vb1, vb2));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(matchingZaehlung, new OptionsDTO(), zaehlung), is(true));

        // negative cases
        ZaehlungEntity nonMatchingZaehlung1 = new ZaehlungEntity();
        nonMatchingZaehlung1.setZaehlart(Zaehlart.QJS);
        nonMatchingZaehlung1.setVerkehrsbeziehungen(List.of(vb1));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(nonMatchingZaehlung1, new OptionsDTO(), zaehlung), is(false));

        ZaehlungEntity nonMatchingZaehlung2 = new ZaehlungEntity();
        nonMatchingZaehlung2.setZaehlart(Zaehlart.QJS);
        VerkehrsbeziehungEntity vb3 = new VerkehrsbeziehungEntity();
        vb3.setVon(1);
        vb3.setNach(1);
        vb3.setStrassenseite(Himmelsrichtung.N);
        nonMatchingZaehlung2.setVerkehrsbeziehungen(List.of(vb1, vb3));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(nonMatchingZaehlung2, new OptionsDTO(), zaehlung), is(false));

        ZaehlungEntity nonMatchingZaehlung3 = new ZaehlungEntity();
        nonMatchingZaehlung3.setZaehlart(Zaehlart.QJS);
        VerkehrsbeziehungEntity vb4 = new VerkehrsbeziehungEntity();
        vb4.setVon(3);
        vb4.setNach(2);
        vb4.setStrassenseite(Himmelsrichtung.N);
        nonMatchingZaehlung3.setVerkehrsbeziehungen(List.of(vb2, vb4));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(nonMatchingZaehlung3, new OptionsDTO(), zaehlung), is(false));

        ZaehlungEntity nonMatchingZaehlung4 = new ZaehlungEntity();
        nonMatchingZaehlung4.setZaehlart(Zaehlart.QJS);
        VerkehrsbeziehungEntity vb5 = new VerkehrsbeziehungEntity();
        vb5.setVon(3);
        vb5.setNach(1);
        vb5.setStrassenseite(Himmelsrichtung.S);
        nonMatchingZaehlung4.setVerkehrsbeziehungen(List.of(vb1, vb5));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(nonMatchingZaehlung4, new OptionsDTO(), zaehlung), is(false));
    }

    @Test
    public void checkBewegungsbeziehungenOtherThanQjsFjsQuWithFussverkehr() {
        // setup
        ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setZaehlart(Zaehlart.N);
        zaehlung.setKategorien(new HashSet<>(List.of(Fahrzeug.KFZ.getName(), Fahrzeug.RAD.getName(), Fahrzeug.FUSS.getName())));
        zaehlung.setKreisverkehr(false);
        VerkehrsbeziehungEntity vb1 = new VerkehrsbeziehungEntity();
        vb1.setVon(1);
        vb1.setNach(2);
        VerkehrsbeziehungEntity vb2 = new VerkehrsbeziehungEntity();
        vb2.setVon(2);
        vb2.setNach(1);
        zaehlung.setVerkehrsbeziehungen(List.of(vb1, vb2));

        OptionsDTO options = new OptionsDTO();
        options.setFussverkehr(true);

        // positive cases
        ZaehlungEntity matchingZaehlung = new ZaehlungEntity();
        matchingZaehlung.setZaehlart(Zaehlart.N);
        matchingZaehlung.setKategorien(new HashSet<>(List.of(Fahrzeug.KFZ.getName(), Fahrzeug.RAD.getName(), Fahrzeug.FUSS.getName())));
        matchingZaehlung.setKreisverkehr(false);
        matchingZaehlung.setVerkehrsbeziehungen(List.of(vb1, vb2));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(matchingZaehlung, options, zaehlung), is(true));

        ZaehlungEntity nonMatchingZaehlungWithoutFuss = new ZaehlungEntity();
        nonMatchingZaehlungWithoutFuss.setZaehlart(Zaehlart.N);
        nonMatchingZaehlungWithoutFuss.setKategorien(new HashSet<>(List.of(Fahrzeug.KFZ.getName(), Fahrzeug.RAD.getName())));
        nonMatchingZaehlungWithoutFuss.setKreisverkehr(false);
        nonMatchingZaehlungWithoutFuss.setVerkehrsbeziehungen(List.of(vb1));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(nonMatchingZaehlungWithoutFuss, options, zaehlung), is(true));

        // negative cases
        ZaehlungEntity nonMatchingZaehlung1 = new ZaehlungEntity();
        nonMatchingZaehlung1.setZaehlart(Zaehlart.N);
        nonMatchingZaehlung1.setKategorien(new HashSet<>(List.of(Fahrzeug.KFZ.getName(), Fahrzeug.RAD.getName(), Fahrzeug.FUSS.getName())));
        nonMatchingZaehlung1.setKreisverkehr(false);
        nonMatchingZaehlung1.setVerkehrsbeziehungen(List.of(vb1));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(nonMatchingZaehlung1, options, zaehlung), is(false));

        ZaehlungEntity nonMatchingZaehlung2 = new ZaehlungEntity();
        nonMatchingZaehlung2.setZaehlart(Zaehlart.N);
        nonMatchingZaehlung2.setKategorien(new HashSet<>(List.of(Fahrzeug.KFZ.getName(), Fahrzeug.RAD.getName(), Fahrzeug.FUSS.getName())));
        nonMatchingZaehlung2.setKreisverkehr(false);
        VerkehrsbeziehungEntity vb3 = new VerkehrsbeziehungEntity();
        vb3.setVon(1);
        vb3.setNach(1);
        nonMatchingZaehlung2.setVerkehrsbeziehungen(List.of(vb1, vb3));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(nonMatchingZaehlung2, options, zaehlung), is(false));

        ZaehlungEntity nonMatchingZaehlung3 = new ZaehlungEntity();
        nonMatchingZaehlung3.setZaehlart(Zaehlart.N);
        nonMatchingZaehlung3.setKategorien(new HashSet<>(List.of(Fahrzeug.KFZ.getName(), Fahrzeug.RAD.getName(), Fahrzeug.FUSS.getName())));
        nonMatchingZaehlung3.setKreisverkehr(false);
        VerkehrsbeziehungEntity vb4 = new VerkehrsbeziehungEntity();
        vb4.setVon(3);
        vb4.setNach(2);
        nonMatchingZaehlung3.setVerkehrsbeziehungen(List.of(vb2, vb4));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(nonMatchingZaehlung3, options, zaehlung), is(false));

        ZaehlungEntity nonMatchingZaehlung4 = new ZaehlungEntity();
        nonMatchingZaehlung4.setZaehlart(Zaehlart.N);
        nonMatchingZaehlung4.setKategorien(new HashSet<>(List.of(Fahrzeug.KFZ.getName(), Fahrzeug.RAD.getName(), Fahrzeug.FUSS.getName())));
        nonMatchingZaehlung4.setKreisverkehr(false);
        VerkehrsbeziehungEntity vb5 = new VerkehrsbeziehungEntity();
        vb5.setVon(3);
        vb5.setNach(1);
        nonMatchingZaehlung4.setVerkehrsbeziehungen(List.of(vb1, vb5));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(nonMatchingZaehlung4, options, zaehlung), is(false));
    }

    @Test
    public void checkBewegungsbeziehungenOtherThanQjsFjsQuWithoutFussverkehr() {
        // setup
        ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setZaehlart(Zaehlart.N);
        zaehlung.setKategorien(new HashSet<>(List.of(Fahrzeug.KFZ.getName(), Fahrzeug.RAD.getName())));
        zaehlung.setKreisverkehr(false);
        VerkehrsbeziehungEntity vb1 = new VerkehrsbeziehungEntity();
        vb1.setVon(1);
        vb1.setNach(2);
        VerkehrsbeziehungEntity vb2 = new VerkehrsbeziehungEntity();
        vb2.setVon(2);
        vb2.setNach(1);
        zaehlung.setVerkehrsbeziehungen(List.of(vb1, vb2));

        OptionsDTO options = new OptionsDTO();
        options.setFussverkehr(false);

        // positive cases
        ZaehlungEntity matchingZaehlung1 = new ZaehlungEntity();
        matchingZaehlung1.setZaehlart(Zaehlart.N);
        matchingZaehlung1.setKategorien(new HashSet<>(List.of(Fahrzeug.KFZ.getName(), Fahrzeug.RAD.getName())));
        matchingZaehlung1.setKreisverkehr(false);
        matchingZaehlung1.setVerkehrsbeziehungen(List.of(vb1, vb2));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(matchingZaehlung1, options, zaehlung), is(true));

        ZaehlungEntity matchingZaehlung2 = new ZaehlungEntity();
        matchingZaehlung2.setZaehlart(Zaehlart.N);
        matchingZaehlung2.setKategorien(new HashSet<>(List.of(Fahrzeug.KFZ.getName(), Fahrzeug.RAD.getName())));
        matchingZaehlung2.setKreisverkehr(false);
        matchingZaehlung2.setVerkehrsbeziehungen(List.of(vb1));
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(matchingZaehlung2, options, zaehlung), is(true));

        // negative case
        ZaehlungEntity nonMatchingZaehlung = new ZaehlungEntity();
        nonMatchingZaehlung.setZaehlart(Zaehlart.N);
        nonMatchingZaehlung.setKategorien(new HashSet<>(List.of(Fahrzeug.KFZ.getName(), Fahrzeug.RAD.getName())));
        nonMatchingZaehlung.setKreisverkehr(false);
        nonMatchingZaehlung.setVerkehrsbeziehungen(List.of());
        assertThat(ProcessZaehldatenZeitreiheService.checkBewegungsbeziehung(nonMatchingZaehlung, options, zaehlung), is(false));
    }
}
