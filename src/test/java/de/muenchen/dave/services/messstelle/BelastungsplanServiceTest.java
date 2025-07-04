package de.muenchen.dave.services.messstelle;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import de.muenchen.dave.domain.dtos.laden.messwerte.BelastungsplanMessquerschnitteDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.LadeBelastungsplanMessquerschnittDataDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessquerschnittDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessstelleInfoDTO;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.Verkehrsart;
import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BelastungsplanServiceTest {
    @Mock
    MessstelleService messstelleService;

    @Mock
    SpitzenstundeService spitzenstundeService;

    private BelastungsplanService belastungsplanService;

    @BeforeEach
    void setup() {
        belastungsplanService = new BelastungsplanService(messstelleService, new RoundingService(), spitzenstundeService);
    }

    @Test
    void ladeBelastungsplan() {
        // setup
        List<IntervalDto> totalSumOfAllMessquerschnitte = new ArrayList<>();
        IntervalDto totalSumPerMessquerschnitt1 = new IntervalDto();
        totalSumPerMessquerschnitt1.setSummeGueterverkehr(BigDecimal.valueOf(50));
        totalSumPerMessquerschnitt1.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(200));
        totalSumPerMessquerschnitt1.setAnzahlRad(BigDecimal.valueOf(20));
        totalSumPerMessquerschnitt1.setSummeSchwerverkehr(BigDecimal.valueOf(30));
        totalSumPerMessquerschnitt1.setMqId(1);

        IntervalDto totalSumPerMessquerschnitt2 = new IntervalDto();
        totalSumPerMessquerschnitt2.setSummeGueterverkehr(BigDecimal.valueOf(30));
        totalSumPerMessquerschnitt2.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(100));
        totalSumPerMessquerschnitt2.setAnzahlRad(BigDecimal.valueOf(10));
        totalSumPerMessquerschnitt2.setSummeSchwerverkehr(BigDecimal.valueOf(50));
        totalSumPerMessquerschnitt2.setMqId(2);
        totalSumOfAllMessquerschnitte.add(totalSumPerMessquerschnitt1);
        totalSumOfAllMessquerschnitte.add(totalSumPerMessquerschnitt2);

        // expected
        LadeBelastungsplanMessquerschnittDataDTO ladeBelastungsplanMessquerschnittDataDTO1 = new LadeBelastungsplanMessquerschnittDataDTO();
        ladeBelastungsplanMessquerschnittDataDTO1.setSumGv(50);
        ladeBelastungsplanMessquerschnittDataDTO1.setSumKfz(200);
        ladeBelastungsplanMessquerschnittDataDTO1.setSumRad(20);
        ladeBelastungsplanMessquerschnittDataDTO1.setSumSv(30);
        ladeBelastungsplanMessquerschnittDataDTO1.setMqId("1");
        ladeBelastungsplanMessquerschnittDataDTO1.setPercentSv(new BigDecimal("15.0"));
        ladeBelastungsplanMessquerschnittDataDTO1.setPercentGV(new BigDecimal("25.0"));
        ladeBelastungsplanMessquerschnittDataDTO1.setDirection("O");

        LadeBelastungsplanMessquerschnittDataDTO ladeBelastungsplanMessquerschnittDataDTO2 = new LadeBelastungsplanMessquerschnittDataDTO();
        ladeBelastungsplanMessquerschnittDataDTO2.setSumGv(30);
        ladeBelastungsplanMessquerschnittDataDTO2.setSumKfz(100);
        ladeBelastungsplanMessquerschnittDataDTO2.setSumRad(10);
        ladeBelastungsplanMessquerschnittDataDTO2.setSumSv(50);
        ladeBelastungsplanMessquerschnittDataDTO2.setMqId("2");
        ladeBelastungsplanMessquerschnittDataDTO2.setPercentSv(new BigDecimal("50.0"));
        ladeBelastungsplanMessquerschnittDataDTO2.setPercentGV(new BigDecimal("30.0"));
        ladeBelastungsplanMessquerschnittDataDTO2.setDirection("W");

        List<LadeBelastungsplanMessquerschnittDataDTO> ladeBelastungsplanMessquerschnittDataDTOS = new ArrayList<>();
        ladeBelastungsplanMessquerschnittDataDTOS.add(ladeBelastungsplanMessquerschnittDataDTO1);
        ladeBelastungsplanMessquerschnittDataDTOS.add(ladeBelastungsplanMessquerschnittDataDTO2);

        BelastungsplanMessquerschnitteDTO expected = new BelastungsplanMessquerschnitteDTO();
        expected.setLadeBelastungsplanMessquerschnittDataDTOList(ladeBelastungsplanMessquerschnittDataDTOS);
        expected.setTotalGv(80);
        expected.setTotalKfz(300);
        expected.setTotalSv(80);
        expected.setTotalRad(30);
        expected.setTotalPercentSv(new BigDecimal("17.4"));
        expected.setTotalPercentGv(new BigDecimal("17.4"));
        expected.setStrassenname("Musterstraße");
        expected.setStadtbezirkNummer(1);
        expected.setMstId("13");
        expected.setEndeUhrzeitSpitzenstunde(null);
        expected.setStartUhrzeitSpitzenstunde(null);

        doReturn(getMessstelle()).when(messstelleService).readMessstelleInfo(anyString());
        //result
        final MessstelleOptionsDTO options = new MessstelleOptionsDTO();
        options.setMessquerschnittIds(Set.of("1", "2"));
        final IntervalDto interval = new IntervalDto();
        var result = belastungsplanService.ladeBelastungsplan(List.of(interval), totalSumOfAllMessquerschnitte, "123", options);

        Assertions.assertThat(result)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void ladeBelastungsplanWithSpitzenstunde() {
        // setup
        List<IntervalDto> totalSumOfAllMessquerschnitte = new ArrayList<>();
        IntervalDto totalSumPerMessquerschnitt1 = new IntervalDto();
        totalSumPerMessquerschnitt1.setSummeGueterverkehr(BigDecimal.valueOf(50));
        totalSumPerMessquerschnitt1.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(200));
        totalSumPerMessquerschnitt1.setAnzahlRad(BigDecimal.valueOf(20));
        totalSumPerMessquerschnitt1.setSummeSchwerverkehr(BigDecimal.valueOf(30));
        totalSumPerMessquerschnitt1.setMqId(1);

        IntervalDto totalSumPerMessquerschnitt2 = new IntervalDto();
        totalSumPerMessquerschnitt2.setSummeGueterverkehr((BigDecimal.valueOf(30)));
        totalSumPerMessquerschnitt2.setSummeKraftfahrzeugverkehr((BigDecimal.valueOf(100)));
        totalSumPerMessquerschnitt2.setAnzahlRad((BigDecimal.valueOf(10)));
        totalSumPerMessquerschnitt2.setSummeSchwerverkehr((BigDecimal.valueOf(50)));
        totalSumPerMessquerschnitt2.setMqId(2);
        totalSumOfAllMessquerschnitte.add(totalSumPerMessquerschnitt1);
        totalSumOfAllMessquerschnitte.add(totalSumPerMessquerschnitt2);

        // expected
        LadeBelastungsplanMessquerschnittDataDTO ladeBelastungsplanMessquerschnittDataDTO1 = new LadeBelastungsplanMessquerschnittDataDTO();
        ladeBelastungsplanMessquerschnittDataDTO1.setSumGv(50);
        ladeBelastungsplanMessquerschnittDataDTO1.setSumKfz(200);
        ladeBelastungsplanMessquerschnittDataDTO1.setSumRad(20);
        ladeBelastungsplanMessquerschnittDataDTO1.setSumSv(30);
        ladeBelastungsplanMessquerschnittDataDTO1.setMqId("1");
        ladeBelastungsplanMessquerschnittDataDTO1.setPercentSv(new BigDecimal("15.0"));
        ladeBelastungsplanMessquerschnittDataDTO1.setPercentGV(new BigDecimal("25.0"));
        ladeBelastungsplanMessquerschnittDataDTO1.setDirection("O");

        LadeBelastungsplanMessquerschnittDataDTO ladeBelastungsplanMessquerschnittDataDTO2 = new LadeBelastungsplanMessquerschnittDataDTO();
        ladeBelastungsplanMessquerschnittDataDTO2.setSumGv(30);
        ladeBelastungsplanMessquerschnittDataDTO2.setSumKfz(100);
        ladeBelastungsplanMessquerschnittDataDTO2.setSumRad(10);
        ladeBelastungsplanMessquerschnittDataDTO2.setSumSv(50);
        ladeBelastungsplanMessquerschnittDataDTO2.setMqId("2");
        ladeBelastungsplanMessquerschnittDataDTO2.setPercentSv(new BigDecimal("50.0"));
        ladeBelastungsplanMessquerschnittDataDTO2.setPercentGV(new BigDecimal("30.0"));
        ladeBelastungsplanMessquerschnittDataDTO2.setDirection("W");

        List<LadeBelastungsplanMessquerschnittDataDTO> ladeBelastungsplanMessquerschnittDataDTOS = new ArrayList<>();
        ladeBelastungsplanMessquerschnittDataDTOS.add(ladeBelastungsplanMessquerschnittDataDTO1);
        ladeBelastungsplanMessquerschnittDataDTOS.add(ladeBelastungsplanMessquerschnittDataDTO2);

        BelastungsplanMessquerschnitteDTO expected = new BelastungsplanMessquerschnitteDTO();
        expected.setLadeBelastungsplanMessquerschnittDataDTOList(ladeBelastungsplanMessquerschnittDataDTOS);
        expected.setTotalGv(80);
        expected.setTotalKfz(300);
        expected.setTotalSv(80);
        expected.setTotalRad(30);
        expected.setTotalPercentSv(new BigDecimal("17.4"));
        expected.setTotalPercentGv(new BigDecimal("17.4"));
        expected.setStrassenname("Musterstraße");
        expected.setStadtbezirkNummer(1);
        expected.setMstId("13");
        expected.setEndeUhrzeitSpitzenstunde(LocalTime.parse("15:00"));
        expected.setStartUhrzeitSpitzenstunde(LocalTime.parse("14:00"));

        doReturn(getMessstelle()).when(messstelleService).readMessstelleInfo(anyString());

        LadeMesswerteDTO spitzenStunde = new LadeMesswerteDTO();
        spitzenStunde.setSortingIndex(5);
        spitzenStunde.setType("type");
        spitzenStunde.setStartUhrzeit(LocalTime.parse("14:00"));
        spitzenStunde.setEndeUhrzeit(LocalTime.parse("15:00"));
        spitzenStunde.setPkw(500);
        spitzenStunde.setLkw(50);
        spitzenStunde.setLfw(20);
        spitzenStunde.setLastzuege(10);
        spitzenStunde.setBusse(5);
        spitzenStunde.setKraftraeder(50);
        spitzenStunde.setFahrradfahrer(20);
        spitzenStunde.setFussgaenger(10);
        spitzenStunde.setKfz(665);
        spitzenStunde.setSchwerverkehr(80);
        spitzenStunde.setGueterverkehr(30);
        spitzenStunde.setAnteilSchwerverkehrAnKfzProzent(12.0);
        spitzenStunde.setAnteilGueterverkehrAnKfzProzent(4.5);
        Mockito.when(spitzenstundeService.calculateSpitzenstundeAndAddBlockSpecificDataToResult(
                any(),
                anyList(),
                anyBoolean(),
                any()))
                .thenReturn(spitzenStunde);
        //result
        final MessstelleOptionsDTO options = new MessstelleOptionsDTO();
        options.setMessquerschnittIds(Set.of("1"));

        final IntervalDto interval = new IntervalDto();
        var result = belastungsplanService.ladeBelastungsplan(List.of(interval), totalSumOfAllMessquerschnitte, "123", options);

        Assertions.assertThat(result)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void calcPercentage() {
        var result = belastungsplanService.calcPercentage(25, 100);
        var expected = new BigDecimal("25.0");
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @Test
    void getDirection() {
        var result = belastungsplanService.getDirection(getMessstelle(), "1");
        var expected = "O";
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @Test
    void roundNumberToHundredIfNeeded() {
        final var options = new MessstelleOptionsDTO();

        options.setWerteHundertRunden(true);
        var result = belastungsplanService.roundNumberToHundredIfNeeded(null, options);
        Assertions.assertThat(result).isNull();

        options.setWerteHundertRunden(true);
        result = belastungsplanService.roundNumberToHundredIfNeeded(0, options);
        Assertions.assertThat(result).isNotNull().isEqualTo(0);

        options.setWerteHundertRunden(true);
        result = belastungsplanService.roundNumberToHundredIfNeeded(1, options);
        Assertions.assertThat(result).isNotNull().isEqualTo(0);

        options.setWerteHundertRunden(true);
        result = belastungsplanService.roundNumberToHundredIfNeeded(49, options);
        Assertions.assertThat(result).isNotNull().isEqualTo(0);

        options.setWerteHundertRunden(true);
        result = belastungsplanService.roundNumberToHundredIfNeeded(50, options);
        Assertions.assertThat(result).isNotNull().isEqualTo(100);

        options.setWerteHundertRunden(true);
        result = belastungsplanService.roundNumberToHundredIfNeeded(99, options);
        Assertions.assertThat(result).isNotNull().isEqualTo(100);

        options.setWerteHundertRunden(true);
        result = belastungsplanService.roundNumberToHundredIfNeeded(100, options);
        Assertions.assertThat(result).isNotNull().isEqualTo(100);

        options.setWerteHundertRunden(true);
        result = belastungsplanService.roundNumberToHundredIfNeeded(101, options);
        Assertions.assertThat(result).isNotNull().isEqualTo(100);

        options.setWerteHundertRunden(false);
        result = belastungsplanService.roundNumberToHundredIfNeeded(null, options);
        Assertions.assertThat(result).isNull();

        options.setWerteHundertRunden(false);
        result = belastungsplanService.roundNumberToHundredIfNeeded(101, options);
        Assertions.assertThat(result).isNotNull().isEqualTo(101);
    }

    ReadMessstelleInfoDTO getMessstelle() {
        ReadMessstelleInfoDTO readMessstelleInfoDTO = new ReadMessstelleInfoDTO();
        readMessstelleInfoDTO.setId("123");
        readMessstelleInfoDTO.setMstId("13");
        readMessstelleInfoDTO.setStandort("Hauptstraße 1");
        readMessstelleInfoDTO.setStadtbezirk("Mitte");
        readMessstelleInfoDTO.setStadtbezirkNummer(1);
        readMessstelleInfoDTO.setFahrzeugklasse(Fahrzeugklasse.ZWEI_PLUS_EINS);
        readMessstelleInfoDTO.setDetektierteVerkehrsart(Verkehrsart.KFZ);
        readMessstelleInfoDTO.setHersteller("Messung GmbH");
        readMessstelleInfoDTO.setLongitude(10.12345);
        readMessstelleInfoDTO.setLatitude(50.67890);
        readMessstelleInfoDTO.setDatumLetztePlausibleMessung(LocalDate.now());
        readMessstelleInfoDTO.setRealisierungsdatum(LocalDate.of(2022, 1, 1));
        readMessstelleInfoDTO.setAbbaudatum(LocalDate.of(2023, 12, 31));
        readMessstelleInfoDTO.setKommentar("Messstelle an belebter Hauptstraße");

        ReadMessquerschnittDTO readMessquerschnittDTO1 = new ReadMessquerschnittDTO();
        readMessquerschnittDTO1.setId("1001");
        readMessquerschnittDTO1.setMqId("1");
        readMessquerschnittDTO1.setLongitude(9.1234);
        readMessquerschnittDTO1.setLatitude(48.5678);
        readMessquerschnittDTO1.setStrassenname("Musterstraße");
        readMessquerschnittDTO1.setLageMessquerschnitt("außerorts");
        readMessquerschnittDTO1.setFahrtrichtung("O");
        readMessquerschnittDTO1.setAnzahlFahrspuren(3);
        readMessquerschnittDTO1.setAnzahlDetektoren(6);
        readMessquerschnittDTO1.setStandort("Autobahnauffahrt");

        ReadMessquerschnittDTO readMessquerschnittDTO2 = new ReadMessquerschnittDTO();
        readMessquerschnittDTO2.setId("112312");
        readMessquerschnittDTO2.setMqId("2");
        readMessquerschnittDTO2.setLongitude(10.5678);
        readMessquerschnittDTO2.setLatitude(49.8765);
        readMessquerschnittDTO2.setStrassenname("Teststraße");
        readMessquerschnittDTO2.setLageMessquerschnitt("innerorts");
        readMessquerschnittDTO2.setFahrtrichtung("W");
        readMessquerschnittDTO2.setAnzahlFahrspuren(2);
        readMessquerschnittDTO2.setAnzahlDetektoren(4);
        readMessquerschnittDTO2.setStandort("Kreuzung");

        List<ReadMessquerschnittDTO> listMessquerschnitte = new ArrayList<>();
        listMessquerschnitte.add(readMessquerschnittDTO1);
        listMessquerschnitte.add(readMessquerschnittDTO2);
        readMessstelleInfoDTO.setMessquerschnitte(listMessquerschnitte);

        return readMessstelleInfoDTO;
    }

    @Test
    void isDirectionNorthOrSouth() {
        ReadMessstelleInfoDTO messstelle = getMessstelle();
        messstelle.getMessquerschnitte().getFirst().setFahrtrichtung("O");
        messstelle.getMessquerschnitte().getLast().setFahrtrichtung("W");
        Assertions.assertThat(belastungsplanService.isDirectionNorthOrSouth(messstelle)).isFalse();
        messstelle.getMessquerschnitte().getFirst().setFahrtrichtung("w");
        messstelle.getMessquerschnitte().getLast().setFahrtrichtung("O");
        Assertions.assertThat(belastungsplanService.isDirectionNorthOrSouth(messstelle)).isFalse();

        messstelle.getMessquerschnitte().getFirst().setFahrtrichtung("N");
        messstelle.getMessquerschnitte().getLast().setFahrtrichtung("S");
        Assertions.assertThat(belastungsplanService.isDirectionNorthOrSouth(messstelle)).isTrue();
        messstelle.getMessquerschnitte().getFirst().setFahrtrichtung("s");
        messstelle.getMessquerschnitte().getLast().setFahrtrichtung("n");
        Assertions.assertThat(belastungsplanService.isDirectionNorthOrSouth(messstelle)).isTrue();

        messstelle.setMessquerschnitte(null);
        Assertions.assertThat(belastungsplanService.isDirectionNorthOrSouth(messstelle)).isFalse();
    }
}
