package de.muenchen.dave.services.csvgenerator;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessquerschnittDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessstelleInfoDTO;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zeitauswahl;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.services.messstelle.GenerateCsvMstService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class GenerateCsvMstServiceTest {

    final GenerateCsvMstService csvService = new GenerateCsvMstService(null, null);

    private static LadeMesswerteDTO getLadeMesswerteDTO() {
        final LadeMesswerteDTO table = new LadeMesswerteDTO();

        table.setStartUhrzeit(LocalTime.of(8, 0));
        table.setEndeUhrzeit(LocalTime.of(8, 15));
        table.setType("Stunde");
        table.setPkw(1);
        table.setLkw(2);
        table.setLastzuege(3);
        table.setLfw(4);
        table.setBusse(5);
        table.setFahrradfahrer(6);
        table.setFussgaenger(7);
        table.setKraftraeder(8);
        table.setKfz(9);
        table.setSchwerverkehr(10);
        table.setGueterverkehr(11);
        table.setAnteilSchwerverkehrAnKfzProzent(1.2);
        table.setAnteilGueterverkehrAnKfzProzent(1.3);

        return table;
    }

    private static LadeMesswerteDTO getLadeMesswerteDTOTyp2() {
        final LadeMesswerteDTO table = new LadeMesswerteDTO();

        table.setStartUhrzeit(LocalTime.of(8, 0));
        table.setEndeUhrzeit(LocalTime.of(8, 15));
        table.setPkw(1);
        table.setLkw(2);
        table.setLastzuege(3);
        table.setLfw(4);
        table.setBusse(5);
        table.setFahrradfahrer(null);
        table.setFussgaenger(7);
        table.setKraftraeder(8);
        table.setKfz(9);
        table.setSchwerverkehr(10);
        table.setGueterverkehr(11);
        table.setAnteilSchwerverkehrAnKfzProzent(1.2);
        table.setAnteilGueterverkehrAnKfzProzent(1.3);

        return table;
    }

    private static MessstelleOptionsDTO getOptionsDTO() {
        MessstelleOptionsDTO optionsDTO = new MessstelleOptionsDTO();
        optionsDTO.setZeitraum(List.of(LocalDate.of(2024, 3, 22)));
        optionsDTO.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL);
        optionsDTO.setZeitblock(Zeitblock.ZB_00_06);
        optionsDTO.setZeitauswahl(Zeitauswahl.TAGESWERT.getCapitalizedName());
        final FahrzeugOptionsDTO fahrzeugOptionsDTO = new FahrzeugOptionsDTO();
        fahrzeugOptionsDTO.setKraftfahrzeugverkehr(true);
        fahrzeugOptionsDTO.setSchwerverkehr(true);
        fahrzeugOptionsDTO.setGueterverkehr(true);
        fahrzeugOptionsDTO.setRadverkehr(true);
        fahrzeugOptionsDTO.setFussverkehr(true);
        fahrzeugOptionsDTO.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptionsDTO.setGueterverkehrsanteilProzent(true);
        fahrzeugOptionsDTO.setLieferwagen(true);
        fahrzeugOptionsDTO.setPersonenkraftwagen(true);
        fahrzeugOptionsDTO.setLastkraftwagen(true);
        fahrzeugOptionsDTO.setLastzuege(true);
        fahrzeugOptionsDTO.setBusse(true);
        fahrzeugOptionsDTO.setKraftraeder(false);
        optionsDTO.setFahrzeuge(fahrzeugOptionsDTO);
        optionsDTO.setStundensumme(true);
        optionsDTO.setBlocksumme(true);
        optionsDTO.setTagessumme(true);
        optionsDTO.setSpitzenstunde(true);
        optionsDTO.setWerteHundertRunden(true);
        optionsDTO.setMessquerschnittIds(Set.of("0"));
        return optionsDTO;
    }

    @Test
    void getData() {
        final String data1 = csvService.getData(GenerateCsvMstServiceTest.getOptionsDTO().getFahrzeuge(), GenerateCsvMstServiceTest.getLadeMesswerteDTO());
        final String dataExpected1 = "08:00;08:15;Stunde;1;2;3;4;5;6;7;9;10;11;1.2%;1.3%;";
        assertThat(data1, is(dataExpected1));

        final String data2 = csvService.getData(GenerateCsvMstServiceTest.getOptionsDTO().getFahrzeuge(), GenerateCsvMstServiceTest.getLadeMesswerteDTOTyp2());
        final String dataExpected2 = "08:00;08:15;;1;2;3;4;5;;7;9;10;11;1.2%;1.3%;";
        assertThat(data2, is(dataExpected2));
    }

    @Test
    void getHeader() {
        final String header = csvService.getHeader(GenerateCsvMstServiceTest.getOptionsDTO().getFahrzeuge());
        final String headerExpected = "von;bis;;Pkw;Lkw;Lz;Lfw;Bus;Rad;Fuß;KFZ;SV;GV;SV%;GV%;";
        assertThat(header, is(headerExpected));
    }

    @Test
    void getMetaHeader() {
        final String header = csvService.getMetaHeader(csvService.getHeader(GenerateCsvMstServiceTest.getOptionsDTO().getFahrzeuge()), false);
        final String headerExpected = "ID Messstelle;Detektierte Fahrzeuge;ausgewählter Messzeitraum / Einzeltag;ausgewählter Wochentag;ausgewählte MQ;;;;;;;;;;;";
        assertThat(header, is(headerExpected));
    }

    @Test
    void getMetaData() {
        final ReadMessstelleInfoDTO messstelleInfoDTO = new ReadMessstelleInfoDTO();
        messstelleInfoDTO.setMstId("123");
        messstelleInfoDTO.setDetektierteVerkehrsarten("KFZ");
        ReadMessquerschnittDTO mq = new ReadMessquerschnittDTO();
        mq.setMqId("0");

        messstelleInfoDTO.setMessquerschnitte(List.of(mq));

        MessstelleOptionsDTO optionsDTO = GenerateCsvMstServiceTest.getOptionsDTO();
        final String header = csvService.getMetaData(messstelleInfoDTO, csvService.getHeader(optionsDTO.getFahrzeuge()), optionsDTO, false);
        final List<String> expectedData = new ArrayList<>();
        expectedData.add(messstelleInfoDTO.getMstId());
        expectedData.add(messstelleInfoDTO.getDetektierteVerkehrsarten());
        expectedData.add(optionsDTO.getZeitraum().get(0).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        expectedData.add("");
        expectedData.add("Alle Messquerschnitte");
        expectedData.add(";;;;;;;;;;");
        assertThat(header, is(StringUtils.join(expectedData, ";")));
    }
}
