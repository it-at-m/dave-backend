/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.messstelle;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import de.muenchen.dave.domain.dtos.laden.messwerte.BelastungsplanMessquerschnitteDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.LadeBelastungsplanMessquerschnittDataDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessquerschnittDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessstelleInfoDTO;
import de.muenchen.dave.geodateneai.gen.model.TotalSumPerMessquerschnitt;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BelastungsplanServiceTest {
    @Mock
    MessstelleService messstelleService;
    private BelastungsplanService belastungsplanService;

    @BeforeEach
    void setup() {
        belastungsplanService = new BelastungsplanService(messstelleService);
    }

    @Test
    void ladeBelastungsplan() {
        // setup
        List<TotalSumPerMessquerschnitt> totalSumOfAllMessquerschnitte = new ArrayList<>();
        TotalSumPerMessquerschnitt totalSumPerMessquerschnitt1 = new TotalSumPerMessquerschnitt();
        totalSumPerMessquerschnitt1.setSumGv(50);
        totalSumPerMessquerschnitt1.setSumKfz(200);
        totalSumPerMessquerschnitt1.setSumRad(20);
        totalSumPerMessquerschnitt1.setSumSv(30);
        totalSumPerMessquerschnitt1.setMqId("1");

        TotalSumPerMessquerschnitt totalSumPerMessquerschnitt2 = new TotalSumPerMessquerschnitt();
        totalSumPerMessquerschnitt2.setSumGv(30);
        totalSumPerMessquerschnitt2.setSumKfz(100);
        totalSumPerMessquerschnitt2.setSumRad(10);
        totalSumPerMessquerschnitt2.setSumSv(50);
        totalSumPerMessquerschnitt2.setMqId("2");
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
        expected.setStrassenname("Hauptstraße 1");
        expected.setStadtbezirkNummer(1);
        expected.setMstId("13");

        doReturn(getMessstelle()).when(messstelleService).readMessstelleInfo(anyString());
        //result
        var result = belastungsplanService.ladeBelastungsplan(totalSumOfAllMessquerschnitte, "123");

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

    ReadMessstelleInfoDTO getMessstelle() {
        ReadMessstelleInfoDTO readMessstelleInfoDTO = new ReadMessstelleInfoDTO();
        readMessstelleInfoDTO.setId("123");
        readMessstelleInfoDTO.setMstId("13");
        readMessstelleInfoDTO.setStandort("Hauptstraße 1");
        readMessstelleInfoDTO.setStadtbezirk("Mitte");
        readMessstelleInfoDTO.setStadtbezirkNummer(1);
        readMessstelleInfoDTO.setFahrzeugKlassen("PKW, LKW");
        readMessstelleInfoDTO.setDetektierteVerkehrsarten("PKW, LKW, Motorrad");
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
}
