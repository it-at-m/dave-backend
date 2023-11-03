package de.muenchen.dave.services.csvgenerator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.csv.DatentabelleCsvZaehldatum;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitauswahl;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.services.GenerateCsvService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class GenerateCsvServiceTest {

    final GenerateCsvService csvService = new GenerateCsvService(null, null, null);

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
        optionsDTO.setWerteHundertRunden(true);
        optionsDTO.setDifferenzdatenDarstellen(false);
        optionsDTO.setVergleichszaehlungsId(null);
        optionsDTO.setVonKnotenarm(null);
        optionsDTO.setNachKnotenarm(null);

        return optionsDTO;
    }
}
