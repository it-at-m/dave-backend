package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.Auswertung;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungIdDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDTO;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatDto;
import de.muenchen.dave.services.messstelle.MessstelleService;
import de.muenchen.dave.services.messstelle.Zeitraum;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import de.muenchen.dave.util.messstelle.FahrtrichtungUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SpreadsheetServiceTest {

    @Mock
    private MessstelleService messstelleService;

    private SpreadsheetService spreadsheetService;

    @BeforeEach
    void beforeEach() {
        spreadsheetService = new SpreadsheetService(messstelleService);
        Mockito.reset(messstelleService);
    }

    @Test
    void addMetaHeaderToRow() {
        final var spreadsheetDocument = new XSSFWorkbook();
        final Sheet sheet = spreadsheetDocument.createSheet("Test");

        spreadsheetService.addMetaHeaderToRow(sheet.createRow(0), false);
        Assertions.assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(1);
        Row row = sheet.getRow(0);
        Assertions.assertThat(row.getPhysicalNumberOfCells()).isEqualTo(2);
        Cell cell = row.getCell(0);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("ausgewählter Wochentag");
        cell = row.getCell(1);
        Assertions.assertThat(cell.getStringCellValue())
                .isEqualTo("ausgewählter Messquerschnitt");

        spreadsheetService.addMetaHeaderToRow(sheet.createRow(0), true);
        Assertions.assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(1);
        row = sheet.getRow(0);
        Assertions.assertThat(row.getPhysicalNumberOfCells()).isEqualTo(3);
        cell = row.getCell(0);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("ausgewählter Wochentag");
        cell = row.getCell(1);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("Messstelle");
        cell = row.getCell(2);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("Messquerschnitt (Merkmale \"MQ-ID - Richtung - Standort MQ\")");
    }

    @Test
    void addMetaDataToRow() {
        final var spreadsheetDocument = new XSSFWorkbook();
        final Sheet sheet = spreadsheetDocument.createSheet("Test");

        final MessstelleAuswertungOptionsDTO options = new MessstelleAuswertungOptionsDTO();
        final MessstelleAuswertungIdDTO messstelleAuswertungIdDTO1 = new MessstelleAuswertungIdDTO();
        messstelleAuswertungIdDTO1.setMstId("123");
        messstelleAuswertungIdDTO1.setMqIds(Set.of("12301", "12302"));
        final MessstelleAuswertungIdDTO messstelleAuswertungIdDTO2 = new MessstelleAuswertungIdDTO();
        messstelleAuswertungIdDTO2.setMstId("456");
        messstelleAuswertungIdDTO2.setMqIds(Set.of("45601", "45602"));
        options.setTagesTyp(TagesTyp.MO_SO);
        options.setMessstelleAuswertungIds(Set.of(messstelleAuswertungIdDTO1, messstelleAuswertungIdDTO2));

        spreadsheetService.addMetaDataToRow(sheet.createRow(0), options);

        Assertions.assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(1);
        Row row = sheet.getRow(0);
        Assertions.assertThat(row.getPhysicalNumberOfCells()).isEqualTo(2);
        Cell cell = row.getCell(0);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo(options.getTagesTyp().getBeschreibung());
        cell = row.getCell(1);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("alle Messquerschnitte");

        final Messstelle mockedMessstelle = new Messstelle();
        mockedMessstelle.setMstId("123");
        mockedMessstelle.setMessquerschnitte(new ArrayList<>());
        final Messquerschnitt messquerschnitt1 = new Messquerschnitt();
        messquerschnitt1.setMqId("12301");
        messquerschnitt1.setFahrtrichtung("W");
        messquerschnitt1.setStandort("Standort MQ1");
        final Messquerschnitt messquerschnitt2 = new Messquerschnitt();
        messquerschnitt2.setMqId("12302");
        messquerschnitt2.setFahrtrichtung("O");
        messquerschnitt2.setStandort("Standort MQ2");
        mockedMessstelle.getMessquerschnitte().add(messquerschnitt1);
        mockedMessstelle.getMessquerschnitte().add(messquerschnitt2);

        Mockito.when(messstelleService.getMessstelleByMstId("123")).thenReturn(mockedMessstelle);

        options.setMessstelleAuswertungIds(Set.of(messstelleAuswertungIdDTO1));
        spreadsheetService.addMetaDataToRow(sheet.createRow(0), options);

        Assertions.assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(1);
        row = sheet.getRow(0);
        Assertions.assertThat(row.getPhysicalNumberOfCells()).isEqualTo(2);
        cell = row.getCell(0);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo(options.getTagesTyp().getBeschreibung());
        cell = row.getCell(1);

        final List<String> cellValue = new ArrayList<>();
        mockedMessstelle.getMessquerschnitte()
                .forEach(messquerschnitt -> cellValue.add(String.format("%s - %s - %s", messquerschnitt.getMqId(), FahrtrichtungUtil.getLongTextOfFahrtrichtung(messquerschnitt.getFahrtrichtung()),
                        messquerschnitt.getStandort())));
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo(String.join(", ", cellValue));
    }

    @Test
    void addMetaDataToMessquerschnittRow() {
        final var spreadsheetDocument = new XSSFWorkbook();
        final Sheet sheet = spreadsheetDocument.createSheet("Test");

        final MessstelleAuswertungOptionsDTO options = new MessstelleAuswertungOptionsDTO();
        options.setTagesTyp(TagesTyp.MO_SO);
        final String mstId = "123";
        final String mqId = "12301";

        Mockito.when(messstelleService.getOptionalOfMessquerschnittByMstId(mstId, mqId)).thenReturn(Optional.empty());
        spreadsheetService.addMetaDataToMessquerschnittRow(sheet.createRow(0), options, mstId, mqId);

        Assertions.assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(1);
        Row row = sheet.getRow(0);
        Assertions.assertThat(row.getPhysicalNumberOfCells()).isEqualTo(3);
        Cell cell = row.getCell(0);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo(options.getTagesTyp().getBeschreibung());
        cell = row.getCell(1);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo(mstId);
        cell = row.getCell(2);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo(mqId);

        final Messquerschnitt messquerschnitt = new Messquerschnitt();
        messquerschnitt.setFahrtrichtung("W");
        messquerschnitt.setStandort("Standort MQ");
        messquerschnitt.setMqId(mqId);
        Mockito.when(messstelleService.getOptionalOfMessquerschnittByMstId(mstId, mqId)).thenReturn(Optional.of(messquerschnitt));
        spreadsheetService.addMetaDataToMessquerschnittRow(sheet.createRow(0), options, mstId, mqId);

        Assertions.assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(1);
        row = sheet.getRow(0);
        Assertions.assertThat(row.getPhysicalNumberOfCells()).isEqualTo(3);
        cell = row.getCell(0);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo(options.getTagesTyp().getBeschreibung());
        cell = row.getCell(1);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo(mstId);
        cell = row.getCell(2);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo(spreadsheetService.getFormattedStringForMessquerschnitt(messquerschnitt));
    }

    @Test
    void addDataHeaderToRow() {
        final var spreadsheetDocument = new XSSFWorkbook();
        final Sheet sheet = spreadsheetDocument.createSheet("Test");

        final MessstelleAuswertungOptionsDTO options = new MessstelleAuswertungOptionsDTO();
        options.setZeitraum(List.of(AuswertungsZeitraum.JAHRE));
        final FahrzeugOptionsDTO fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setGueterverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(true);
        fahrzeugOptions.setRadverkehr(false);
        fahrzeugOptions.setFussverkehr(true);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLieferwagen(true);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(true);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(true);
        options.setFahrzeuge(fahrzeugOptions);

        int cellIndex = 0;
        Row row = sheet.createRow(0);
        spreadsheetService.addDataHeaderToRow(row, options, false);

        Assertions.assertThat(row.getPhysicalNumberOfCells()).isEqualTo(8);
        Cell cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("Jahr");
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("KFZ");
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("GV");
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("GV%");
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("FUß");
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("LFW");
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("BUS");
        cell = row.getCell(cellIndex);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("PKW");

        row = sheet.createRow(0);
        cellIndex = 0;
        options.setZeitraum(List.of(AuswertungsZeitraum.MAERZ));
        spreadsheetService.addDataHeaderToRow(row, options, true);

        Assertions.assertThat(row.getPhysicalNumberOfCells()).isEqualTo(10);
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("Monat");
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("Jahr");
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("MstId");
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("KFZ");
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("GV");
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("GV%");
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("FUß");
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("LFW");
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("BUS");
        cell = row.getCell(cellIndex);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo("PKW");

    }

    @Test
    void addDataToRow() {
        final var spreadsheetDocument = new XSSFWorkbook();
        final Sheet sheet = spreadsheetDocument.createSheet("Test");
        final String mqId = "12301";

        final FahrzeugOptionsDTO fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setGueterverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(true);
        fahrzeugOptions.setRadverkehr(false);
        fahrzeugOptions.setFussverkehr(true);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLieferwagen(true);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(true);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(true);

        final Zeitraum zeitraum = new Zeitraum(YearMonth.of(2024, 1), YearMonth.of(2024, 3), AuswertungsZeitraum.QUARTAL_1);

        final TagesaggregatDto daten = new TagesaggregatDto();
        daten.setMqId(Integer.valueOf(mqId));
        daten.setAnzahlLfw(BigDecimal.valueOf(2));
        daten.setAnzahlKrad(BigDecimal.valueOf(3));
        daten.setAnzahlLkw(BigDecimal.valueOf(4));
        daten.setAnzahlBus(BigDecimal.valueOf(7));
        daten.setAnzahlRad(BigDecimal.valueOf(9));
        daten.setSummeAllePkw(BigDecimal.valueOf(10));
        daten.setSummeLastzug(BigDecimal.valueOf(11));
        daten.setSummeGueterverkehr(BigDecimal.valueOf(12));
        daten.setSummeSchwerverkehr(BigDecimal.valueOf(13));
        daten.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(14));
        daten.setProzentSchwerverkehr(BigDecimal.valueOf(15));
        daten.setProzentGueterverkehr(BigDecimal.valueOf(16));
        daten.setIncludedMeasuringDays(17L);

        final Auswertung auswertung = new Auswertung();
        auswertung.setObjectId(mqId);
        auswertung.setZeitraum(zeitraum);
        auswertung.setDaten(daten);

        int cellIndex = 0;
        Row row = sheet.createRow(0);
        spreadsheetService.addDataToRow(row, auswertung, fahrzeugOptions, false);

        Assertions.assertThat(row.getPhysicalNumberOfCells()).isEqualTo(9);
        Cell cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue())
                .isEqualTo(zeitraum.getAuswertungsZeitraum().getText());
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue())
                .isEqualTo(String.valueOf(zeitraum.getStart().getYear()));
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getNumericCellValue()).isEqualTo(daten.getSummeKraftfahrzeugverkehr().doubleValue());
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getNumericCellValue()).isEqualTo(daten.getSummeGueterverkehr().doubleValue());
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getNumericCellValue()).isEqualTo(daten.getProzentGueterverkehr().doubleValue());
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo(StringUtils.EMPTY);
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getNumericCellValue()).isEqualTo(daten.getAnzahlLfw().doubleValue());
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getNumericCellValue()).isEqualTo(daten.getAnzahlBus().doubleValue());
        cell = row.getCell(cellIndex);
        Assertions.assertThat(cell.getNumericCellValue()).isEqualTo(daten.getSummeAllePkw().doubleValue());

        auswertung.getZeitraum().setAuswertungsZeitraum(AuswertungsZeitraum.JAHRE);
        row = sheet.createRow(0);
        cellIndex = 0;
        spreadsheetService.addDataToRow(row, auswertung, fahrzeugOptions, true);

        Assertions.assertThat(row.getPhysicalNumberOfCells()).isEqualTo(9);
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(zeitraum.getStart().getYear()));
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo(auswertung.getObjectId());
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getNumericCellValue()).isEqualTo(daten.getSummeKraftfahrzeugverkehr().doubleValue());
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getNumericCellValue()).isEqualTo(daten.getSummeGueterverkehr().doubleValue());
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getNumericCellValue()).isEqualTo(daten.getProzentGueterverkehr().doubleValue());
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo(StringUtils.EMPTY);
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getNumericCellValue()).isEqualTo(daten.getAnzahlLfw().doubleValue());
        cell = row.getCell(cellIndex++);
        Assertions.assertThat(cell.getNumericCellValue()).isEqualTo(daten.getAnzahlBus().doubleValue());
        cell = row.getCell(cellIndex);
        Assertions.assertThat(cell.getNumericCellValue()).isEqualTo(daten.getSummeAllePkw().doubleValue());

    }

    @Test
    void addBigDecimalToCell() {
        final var spreadsheetDocument = new XSSFWorkbook();
        final Sheet sheet = spreadsheetDocument.createSheet("Test");
        final Cell cell = sheet.createRow(0).createCell(0);
        final BigDecimal dataToAdd = BigDecimal.TEN;

        spreadsheetService.addBigDecimalToCell(cell, dataToAdd);
        Assertions.assertThat(cell.getNumericCellValue()).isEqualTo(dataToAdd.doubleValue());

        spreadsheetService.addBigDecimalToCell(cell, null);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo(StringUtils.EMPTY);
    }

    @Test
    void addStringToCell() {
        final var spreadsheetDocument = new XSSFWorkbook();
        final Sheet sheet = spreadsheetDocument.createSheet("Test");
        final Cell cell = sheet.createRow(0).createCell(0);
        final String dataToAdd = "String to add";

        spreadsheetService.addStringToCell(cell, dataToAdd);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo(dataToAdd);

        spreadsheetService.addStringToCell(cell, null);
        Assertions.assertThat(cell.getStringCellValue()).isEqualTo(StringUtils.EMPTY);
    }

    @Test
    void getFormattedStringForMessquerschnitt() {
        final Messquerschnitt messquerschnitt = new Messquerschnitt();
        messquerschnitt.setMqId("12301");
        messquerschnitt.setFahrtrichtung("W");
        messquerschnitt.setStandort("Standort MQ");

        final String expected = "12301 - West - Standort MQ";

        Assertions.assertThat(spreadsheetService.getFormattedStringForMessquerschnitt(messquerschnitt)).isEqualTo(expected);
    }
}
