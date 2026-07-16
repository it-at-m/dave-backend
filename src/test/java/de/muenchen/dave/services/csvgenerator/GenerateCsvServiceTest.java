package de.muenchen.dave.services.csvgenerator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.csv.CsvMetaObject;
import de.muenchen.dave.domain.csv.DatentabelleCsvZaehldatum;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.OptionsLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.OptionsQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.OptionsVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.elasticsearch.*;
import de.muenchen.dave.domain.enums.*;
import de.muenchen.dave.services.GenerateCsvService;
import de.muenchen.dave.spring.services.csvgenerator.GenerateCsvServiceSpringTest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class GenerateCsvServiceTest {

    final GenerateCsvService csvService = new GenerateCsvService(null, null, null);

    private static DatentabelleCsvZaehldatum getDatentabelleCsvZaehldatumTyp1() {
        final DatentabelleCsvZaehldatum table = new DatentabelleCsvZaehldatum();

        table.setStartUhrzeit("08:00");
        table.setEndeUhrzeit("08:15");
        table.setType("Stunde");
        table.setPkw(1);
        table.setLkw(2);
        table.setLastzuege(3);
        table.setBusse(4);
        table.setFahrradfahrer(5);
        table.setFussgaenger(6);
        table.setKraftraeder(7);
        table.setKfz(new BigDecimal(8));
        table.setSchwerverkehr(new BigDecimal(9));
        table.setGueterverkehr(new BigDecimal(10));
        table.setAnteilSchwerverkehrAnKfzProzent(new BigDecimal("1.1"));
        table.setAnteilGueterverkehrAnKfzProzent(new BigDecimal("1.2"));
        table.setPkwEinheiten(13);

        return table;
    }

    private static DatentabelleCsvZaehldatum getDatentabelleCsvZaehldatumTyp2() {
        final DatentabelleCsvZaehldatum table = new DatentabelleCsvZaehldatum();

        table.setStartUhrzeit("08:00");
        table.setEndeUhrzeit("08:15");
        table.setPkw(1);
        table.setLkw(2);
        table.setLastzuege(3);
        table.setBusse(4);
        table.setFahrradfahrer(null);
        table.setFussgaenger(6);
        table.setKraftraeder(7);
        table.setKfz(new BigDecimal(8));
        table.setSchwerverkehr(new BigDecimal(9));
        table.setGueterverkehr(new BigDecimal(10));
        table.setAnteilSchwerverkehrAnKfzProzent(new BigDecimal("1.1"));
        table.setAnteilGueterverkehrAnKfzProzent(new BigDecimal("1.2"));
        table.setPkwEinheiten(13);

        return table;
    }

    private static OptionsDTO getOptionsDTO() {
        OptionsDTO optionsDTO = new OptionsDTO();
        optionsDTO.setZaehldauer(Zaehldauer.DAUER_2_X_4_STUNDEN);
        optionsDTO.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL);
        optionsDTO.setZeitblock(Zeitblock.ZB_00_06);
        optionsDTO.setZeitauswahl(Zeitauswahl.TAGESWERT.getCapitalizedName());
        optionsDTO.setKraftfahrzeugverkehr(true);
        optionsDTO.setSchwerverkehr(true);
        optionsDTO.setGueterverkehr(true);
        optionsDTO.setRadverkehr(true);
        optionsDTO.setFussverkehr(true);
        optionsDTO.setSchwerverkehrsanteilProzent(true);
        optionsDTO.setGueterverkehrsanteilProzent(true);
        optionsDTO.setPkwEinheiten(true);
        optionsDTO.setPersonenkraftwagen(true);
        optionsDTO.setLastkraftwagen(true);
        optionsDTO.setLastzuege(true);
        optionsDTO.setBusse(true);
        optionsDTO.setKraftraeder(false);
        optionsDTO.setStundensumme(true);
        optionsDTO.setBlocksumme(true);
        optionsDTO.setTagessumme(true);
        optionsDTO.setSpitzenstunde(true);
        optionsDTO.setMittelwert(false);
        optionsDTO.setFahrzeugklassenStapeln(false);
        optionsDTO.setBeschriftung(false);
        optionsDTO.setDatentabelle(false);
        optionsDTO.setRounding(Rounding.R100);
        optionsDTO.setDifferenzdatenDarstellen(false);
        optionsDTO.setVergleichszaehlungsId(null);
        optionsDTO.setVonKnotenarm(null);
        optionsDTO.setNachKnotenarm(null);

        return optionsDTO;
    }

    @Test
    public void getData() {
        final String data1 = csvService.getData(GenerateCsvServiceTest.getOptionsDTO(), GenerateCsvServiceTest.getDatentabelleCsvZaehldatumTyp1());
        final String dataExpected1 = "08:00;08:15;Stunde;1;2;3;4;5;6;8;9;10;1.1%;1.2%;13;";
        assertThat(data1, is(dataExpected1));

        final String data2 = csvService.getData(GenerateCsvServiceTest.getOptionsDTO(), GenerateCsvServiceTest.getDatentabelleCsvZaehldatumTyp2());
        final String dataExpected2 = "08:00;08:15;;1;2;3;4;;6;8;9;10;1.1%;1.2%;13;";
        assertThat(data2, is(dataExpected2));
    }

    @Test
    public void getHeader() {
        final String header = csvService.getHeader(GenerateCsvServiceTest.getOptionsDTO());
        final String headerExpected = "von;bis;;Pkw;Lkw;Lz;Bus;Rad;Fuß;KFZ;SV;GV;SV%;GV%;PKW-Einheiten;";
        assertThat(header, is(headerExpected));
    }

    @Test
    public void getMetaData() {
        final String header = csvService.getHeader(GenerateCsvServiceTest.getOptionsDTO());
        Zaehlung zaehlung = GenerateCsvServiceSpringTest.getZaehlung();
        Zaehlstelle zaehlstelle = GenerateCsvServiceSpringTest.getZaehlstelle(zaehlung);
        CsvMetaObject metaObject = new CsvMetaObject();
        metaObject.setZaehlstelle(zaehlstelle);
        metaObject.setZaehlung(zaehlung);
        OptionsDTO options = GenerateCsvServiceTest.getOptionsDTO();

        // Zählart N; Alle Verkehrsbeziehungen
        String expected = "133301;N;04.11.2020;Von: Alle - Nach: Alle;;;;;;;;;;;;";
        assertThat(csvService.getMetaData(metaObject, header, options), is(expected));

        // Zählart N; Nur vonKnotenarm != null
        options.setVonKnotenarm(1);
        expected = "133301;N;04.11.2020;Von: 1 - Nach: Alle;;;;;;;;;;;;";
        assertThat(csvService.getMetaData(metaObject, header, options), is(expected));

        // Zählart N; Nur nachKnotenarm != null
        options.setVonKnotenarm(null);
        options.setNachKnotenarm(4);
        expected = "133301;N;04.11.2020;Von: Alle - Nach: 4;;;;;;;;;;;;";
        assertThat(csvService.getMetaData(metaObject, header, options), is(expected));

        // Zählart N; vonKnotenarm != null und nachKnotenarm != null
        options.setVonKnotenarm(1);
        expected = "133301;N;04.11.2020;Von: 1 - Nach: 4;;;;;;;;;;;;";
        assertThat(csvService.getMetaData(metaObject, header, options), is(expected));

        options.setVonKnotenarm(null);
        options.setNachKnotenarm(null);

        // Zählart QjS; Nicht alle Verkehrsbeziehungen
        zaehlung.setZaehlart(String.valueOf(Zaehlart.QJS));
        final Verkehrsbeziehung vb1 = new Verkehrsbeziehung();
        vb1.setVon(1);
        vb1.setNach(3);
        vb1.setStrassenseite(Himmelsrichtung.W);
        final Verkehrsbeziehung vb2 = new Verkehrsbeziehung();
        vb2.setVon(1);
        vb2.setNach(3);
        vb2.setStrassenseite(Himmelsrichtung.O);
        zaehlung.setVerkehrsbeziehungen(List.of(vb1, vb2));
        final OptionsVerkehrsbeziehungDTO ovb1 = new OptionsVerkehrsbeziehungDTO();
        ovb1.setVon(1);
        ovb1.setNach(3);
        ovb1.setStrassenseite(Himmelsrichtung.W);
        options.setChosenVerkehrsbeziehungen(List.of(ovb1));
        expected = "133301;QJS;04.11.2020;Teilauswahl;;;;;;;;;;;;";
        assertThat(csvService.getMetaData(metaObject, header, options), is(expected));

        // Zählart QjS; Alle Verkehrsbeziehungen
        final OptionsVerkehrsbeziehungDTO ovb2 = new OptionsVerkehrsbeziehungDTO();
        ovb2.setVon(1);
        ovb2.setNach(3);
        ovb2.setStrassenseite(Himmelsrichtung.O);
        options.setChosenVerkehrsbeziehungen(List.of(ovb1, ovb2));
        expected = "133301;QJS;04.11.2020;Alle;;;;;;;;;;;;";
        assertThat(csvService.getMetaData(metaObject, header, options), is(expected));

        // Zählart FjS; Nicht alle Verkehrsbeziehungen
        zaehlung.setZaehlart(String.valueOf(Zaehlart.FJS));
        final Laengsverkehr lv1 = new Laengsverkehr();
        lv1.setKnotenarm(1);
        lv1.setStrassenseite(Himmelsrichtung.W);
        lv1.setRichtung(Bewegungsrichtung.EIN);
        final Laengsverkehr lv2 = new Laengsverkehr();
        lv2.setKnotenarm(1);
        lv2.setStrassenseite(Himmelsrichtung.O);
        lv2.setRichtung(Bewegungsrichtung.EIN);
        zaehlung.setLaengsverkehr(List.of(lv1, lv2));
        final OptionsLaengsverkehrDTO olv1 = new OptionsLaengsverkehrDTO();
        olv1.setKnotenarm(1);
        olv1.setStrassenseite(Himmelsrichtung.W);
        olv1.setRichtung(Bewegungsrichtung.EIN);
        options.setChosenLaengsverkehre(List.of(olv1));
        expected = "133301;FJS;04.11.2020;Teilauswahl;;;;;;;;;;;;";
        assertThat(csvService.getMetaData(metaObject, header, options), is(expected));

        // Zählart FjS; Alle Verkehrsbeziehungen
        final OptionsLaengsverkehrDTO olv2 = new OptionsLaengsverkehrDTO();
        olv2.setKnotenarm(1);
        olv2.setStrassenseite(Himmelsrichtung.O);
        olv2.setRichtung(Bewegungsrichtung.EIN);
        options.setChosenLaengsverkehre(List.of(olv1, olv2));
        expected = "133301;FJS;04.11.2020;Alle;;;;;;;;;;;;";
        assertThat(csvService.getMetaData(metaObject, header, options), is(expected));

        // Zählart Qu; Nicht alle Verkehrsbeziehungen
        zaehlung.setZaehlart(String.valueOf(Zaehlart.QU));
        final Querungsverkehr qv1 = new Querungsverkehr();
        qv1.setKnotenarm(4);
        qv1.setRichtung(Himmelsrichtung.N);
        final Querungsverkehr qv2 = new Querungsverkehr();
        qv2.setKnotenarm(4);
        qv2.setRichtung(Himmelsrichtung.S);
        zaehlung.setQuerungsverkehr(List.of(qv1, qv2));
        final OptionsQuerungsverkehrDTO oqv1 = new OptionsQuerungsverkehrDTO();
        oqv1.setKnotenarm(4);
        oqv1.setRichtung(Himmelsrichtung.N);
        options.setChosenQuerungsverkehre(List.of(oqv1));
        expected = "133301;QU;04.11.2020;Teilauswahl;;;;;;;;;;;;";
        assertThat(csvService.getMetaData(metaObject, header, options), is(expected));

        // Zählart Qu; Alle Verkehrsbeziehungen
        final OptionsQuerungsverkehrDTO oqv2 = new OptionsQuerungsverkehrDTO();
        oqv2.setKnotenarm(4);
        oqv2.setRichtung(Himmelsrichtung.S);
        options.setChosenQuerungsverkehre(List.of(oqv1, oqv2));
        expected = "133301;QU;04.11.2020;Alle;;;;;;;;;;;;";
        assertThat(csvService.getMetaData(metaObject, header, options), is(expected));
    }
}
