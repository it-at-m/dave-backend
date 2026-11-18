package de.muenchen.dave.services.auswertung;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.VerkehrsbeziehungVisumDTO;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitauswahl;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.relational.zaehlstellen.VerkehrsbeziehungEntity;
import de.muenchen.dave.relational.zaehlstellen.ZaehlungEntity;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class AuswertungVisumServiceTest {

    @Test
    public void testIsZaehldatumRelevant() {
        var zaehlung = new ZaehlungEntity();
        zaehlung.setJahr("2020");
        zaehlung.setMonat("Juli");
        assertThat(AuswertungVisumService.isZaehldatumRelevant(zaehlung, "2020", "Juli"), is(true));

        zaehlung = new ZaehlungEntity();
        zaehlung.setJahr("2020");
        zaehlung.setMonat("Juli");
        assertThat(AuswertungVisumService.isZaehldatumRelevant(zaehlung, "2020", "November"), is(false));

        zaehlung = new ZaehlungEntity();
        zaehlung.setJahr("2020");
        zaehlung.setMonat("Juli");
        assertThat(AuswertungVisumService.isZaehldatumRelevant(zaehlung, null, null), is(false));
    }

    @Test
    public void testIsZaehlartRelevant() {
        var zaehlung = new ZaehlungEntity();
        zaehlung.setZaehlart(Zaehlart.QJS);
        assertThat(AuswertungVisumService.isZaehlartRelevant(zaehlung), is(false));

        zaehlung = new ZaehlungEntity();
        zaehlung.setZaehlart(Zaehlart.FJS);
        assertThat(AuswertungVisumService.isZaehlartRelevant(zaehlung), is(false));

        zaehlung = new ZaehlungEntity();
        zaehlung.setZaehlart(Zaehlart.QU);
        assertThat(AuswertungVisumService.isZaehlartRelevant(zaehlung), is(false));

        zaehlung = new ZaehlungEntity();
        zaehlung.setZaehlart(Zaehlart.N);
        assertThat(AuswertungVisumService.isZaehlartRelevant(zaehlung), is(true));
    }

    @Test
    public void testGetVerkehrsbeziehungVisum() {
        // Kreuzung
        var verkehrsbeziehung = new VerkehrsbeziehungEntity();
        verkehrsbeziehung.setIsKreuzung(true);
        verkehrsbeziehung.setVon(98);
        verkehrsbeziehung.setNach(99);

        var firstExpected = new VerkehrsbeziehungVisumDTO();
        firstExpected.setVon(98);
        firstExpected.setNach(null);
        var secondExpected = new VerkehrsbeziehungVisumDTO();
        secondExpected.setVon(null);
        secondExpected.setNach(99);
        List<VerkehrsbeziehungVisumDTO> expectedList = Arrays.asList(firstExpected, secondExpected);

        var zaehlung = new ZaehlungEntity();
        zaehlung.setKreisverkehr(false);

        assertThat(AuswertungVisumService.getVerkehrsbeziehungVisum(verkehrsbeziehung, zaehlung), is(expectedList));

        // Kreisverkehr Hinein
        verkehrsbeziehung = new VerkehrsbeziehungEntity();
        verkehrsbeziehung.setIsKreuzung(false);
        verkehrsbeziehung.setKnotenarm(55);
        verkehrsbeziehung.setHinein(true);
        verkehrsbeziehung.setHeraus(false);
        verkehrsbeziehung.setVorbei(false);

        firstExpected = new VerkehrsbeziehungVisumDTO();
        firstExpected.setVon(55);
        firstExpected.setNach(null);
        secondExpected = new VerkehrsbeziehungVisumDTO();
        secondExpected.setVon(null);
        secondExpected.setNach(55);
        expectedList = Arrays.asList(firstExpected, secondExpected);

        assertThat(AuswertungVisumService.getVerkehrsbeziehungVisum(verkehrsbeziehung, zaehlung), is(expectedList));

        // Kreisverkehr Heraus
        verkehrsbeziehung = new VerkehrsbeziehungEntity();
        verkehrsbeziehung.setIsKreuzung(false);
        verkehrsbeziehung.setKnotenarm(56);
        verkehrsbeziehung.setHinein(false);
        verkehrsbeziehung.setHeraus(true);
        verkehrsbeziehung.setVorbei(false);

        firstExpected = new VerkehrsbeziehungVisumDTO();
        firstExpected.setVon(56);
        firstExpected.setNach(null);
        secondExpected = new VerkehrsbeziehungVisumDTO();
        secondExpected.setVon(null);
        secondExpected.setNach(56);
        expectedList = Arrays.asList(firstExpected, secondExpected);

        assertThat(AuswertungVisumService.getVerkehrsbeziehungVisum(verkehrsbeziehung, zaehlung), is(expectedList));

        // Kreisverkehr Vorbei
        verkehrsbeziehung = new VerkehrsbeziehungEntity();
        verkehrsbeziehung.setIsKreuzung(false);
        verkehrsbeziehung.setKnotenarm(57);
        verkehrsbeziehung.setHinein(false);
        verkehrsbeziehung.setHeraus(false);
        verkehrsbeziehung.setVorbei(true);

        firstExpected = new VerkehrsbeziehungVisumDTO();
        firstExpected.setVon(57);
        firstExpected.setNach(null);
        secondExpected = new VerkehrsbeziehungVisumDTO();
        secondExpected.setVon(null);
        secondExpected.setNach(57);
        expectedList = Arrays.asList(firstExpected, secondExpected);

        assertThat(AuswertungVisumService.getVerkehrsbeziehungVisum(verkehrsbeziehung, zaehlung), is(expectedList));
    }

    @Test
    public void testCreateOptions() {
        final var zaehlung = new ZaehlungEntity();
        zaehlung.setKategorien(new HashSet<String>(Arrays.asList(Fahrzeug.KFZ.getName(), Fahrzeug.RAD.getName(), Fahrzeug.FUSS.getName())));
        zaehlung.setZaehldauer(Zaehldauer.DAUER_2_X_4_STUNDEN);

        final var verkehrsbeziehungVisum = new VerkehrsbeziehungVisumDTO();
        verkehrsbeziehungVisum.setVon(98);
        verkehrsbeziehungVisum.setNach(99);

        final var expected = new OptionsDTO();
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
        expected.setVonKnotenarm(98);
        expected.setNachKnotenarm(99);
        expected.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL);

        assertThat(AuswertungVisumService.createOptions(verkehrsbeziehungVisum, zaehlung), is(expected));
    }

}
