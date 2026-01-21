package de.muenchen.dave.services.auswertung;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.FahrbeziehungVisumDTO;
import de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitauswahl;
import de.muenchen.dave.domain.enums.Zeitblock;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AuswertungVisumServiceTest {

    @Test
    public void isZaehlungRelevant() {
        var zaehlung = new Zaehlung();
        zaehlung.setJahr("2020");
        zaehlung.setMonat("Juli");
        assertThat(AuswertungVisumService.isZaehlungRelevant(zaehlung, "2020", "Juli"), is(true));

        zaehlung = new Zaehlung();
        zaehlung.setJahr("2020");
        zaehlung.setMonat("Juli");
        assertThat(AuswertungVisumService.isZaehlungRelevant(zaehlung, "2020", "November"), is(false));

        zaehlung = new Zaehlung();
        zaehlung.setJahr("2020");
        zaehlung.setMonat("Juli");
        assertThat(AuswertungVisumService.isZaehlungRelevant(zaehlung, null, null), is(false));
    }

    @Test
    public void getFahrbeziehungenVisum() {
        // Kreuzung
        var fahrbeziehung = new Verkehrsbeziehung();
        fahrbeziehung.setIsKreuzung(true);
        fahrbeziehung.setVon(98);
        fahrbeziehung.setNach(99);

        var firstExpected = new FahrbeziehungVisumDTO();
        firstExpected.setVon(98);
        firstExpected.setNach(null);
        var secondExpected = new FahrbeziehungVisumDTO();
        secondExpected.setVon(null);
        secondExpected.setNach(99);
        List<FahrbeziehungVisumDTO> expectedList = Arrays.asList(firstExpected, secondExpected);

        var zaehlung = new Zaehlung();
        zaehlung.setKreisverkehr(false);

        assertThat(AuswertungVisumService.getFahrbeziehungenVisum(fahrbeziehung, zaehlung), is(expectedList));

        // Kreisverkehr Hinein
        fahrbeziehung = new Verkehrsbeziehung();
        fahrbeziehung.setIsKreuzung(false);
        fahrbeziehung.setKnotenarm(55);
        fahrbeziehung.setHinein(true);
        fahrbeziehung.setHeraus(false);
        fahrbeziehung.setVorbei(false);

        firstExpected = new FahrbeziehungVisumDTO();
        firstExpected.setVon(55);
        firstExpected.setNach(null);
        secondExpected = new FahrbeziehungVisumDTO();
        secondExpected.setVon(null);
        secondExpected.setNach(55);
        expectedList = Arrays.asList(firstExpected, secondExpected);

        assertThat(AuswertungVisumService.getFahrbeziehungenVisum(fahrbeziehung, zaehlung), is(expectedList));

        // Kreisverkehr Heraus
        fahrbeziehung = new Verkehrsbeziehung();
        fahrbeziehung.setIsKreuzung(false);
        fahrbeziehung.setKnotenarm(56);
        fahrbeziehung.setHinein(false);
        fahrbeziehung.setHeraus(true);
        fahrbeziehung.setVorbei(false);

        firstExpected = new FahrbeziehungVisumDTO();
        firstExpected.setVon(56);
        firstExpected.setNach(null);
        secondExpected = new FahrbeziehungVisumDTO();
        secondExpected.setVon(null);
        secondExpected.setNach(56);
        expectedList = Arrays.asList(firstExpected, secondExpected);

        assertThat(AuswertungVisumService.getFahrbeziehungenVisum(fahrbeziehung, zaehlung), is(expectedList));

        // Kreisverkehr Vorbei
        fahrbeziehung = new Verkehrsbeziehung();
        fahrbeziehung.setIsKreuzung(false);
        fahrbeziehung.setKnotenarm(57);
        fahrbeziehung.setHinein(false);
        fahrbeziehung.setHeraus(false);
        fahrbeziehung.setVorbei(true);

        firstExpected = new FahrbeziehungVisumDTO();
        firstExpected.setVon(57);
        firstExpected.setNach(null);
        secondExpected = new FahrbeziehungVisumDTO();
        secondExpected.setVon(null);
        secondExpected.setNach(57);
        expectedList = Arrays.asList(firstExpected, secondExpected);

        assertThat(AuswertungVisumService.getFahrbeziehungenVisum(fahrbeziehung, zaehlung), is(expectedList));
    }

    @Test
    public void createOptions() {
        final var zaehlung = new Zaehlung();
        zaehlung.setKategorien(Arrays.asList(Fahrzeug.KFZ, Fahrzeug.RAD, Fahrzeug.FUSS));
        zaehlung.setZaehldauer("DAUER_2_X_4_STUNDEN");

        final var fahrbeziehungVisum = new FahrbeziehungVisumDTO();
        fahrbeziehungVisum.setVon(98);
        fahrbeziehungVisum.setNach(99);

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

        assertThat(AuswertungVisumService.createOptions(fahrbeziehungVisum, zaehlung), is(expected));
    }

}
