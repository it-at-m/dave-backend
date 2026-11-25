package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDto;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.Auswertung;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungIdDto;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDto;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatDto;
import de.muenchen.dave.services.messstelle.MessstelleService;
import de.muenchen.dave.services.messstelle.Zeitraum;
import de.muenchen.dave.util.messstelle.FahrtrichtungUtil;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipInputStream;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SpreadsheetServiceTest {

    private static final String ODS_MIMETYPE = "application/vnd.oasis.opendocument.spreadsheet";

    @Mock
    private MessstelleService messstelleService;

    private SpreadsheetService spreadsheetService;
    private List<SpreadsheetDocument> testDocuments;

    private Table createSheet() {
        try {
            final SpreadsheetDocument spreadsheetDocument = SpreadsheetDocument.newSpreadsheetDocument();
            testDocuments.add(spreadsheetDocument);
            final Table table = spreadsheetDocument.getSheetByIndex(0);
            table.removeRowsByIndex(0, table.getRowCount());
            return table;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Row firstRow(final Table table) {
        return table.getRowCount() == 0 ? table.appendRow() : table.getRowByIndex(0);
    }

    @BeforeEach
    void beforeEach() {
        spreadsheetService = new SpreadsheetService(messstelleService);
        testDocuments = new ArrayList<>();
        Mockito.reset(messstelleService);
    }

    @AfterEach
    void afterEach() {
        testDocuments.forEach(SpreadsheetDocument::close);
    }

    @Test
    void serializeSpreadsheetDocumentAsOds() throws Exception {
        final var spreadsheetDocument = SpreadsheetDocument.newSpreadsheetDocument();
        final Table sheet = spreadsheetDocument.getSheetByIndex(0);
        sheet.removeRowsByIndex(0, sheet.getRowCount());
        sheet.setTableName("Gesamt");
        sheet.getCellByPosition(0, 0).setStringValue("Jahr & Zeitraum");
        sheet.getCellByPosition(1, 0).setDoubleValue(2026.0);
        final byte[] spreadsheet = spreadsheetService.serializeSpreadsheetDocument(spreadsheetDocument);
        spreadsheetDocument.close();

        try (final var zip = new ZipInputStream(new ByteArrayInputStream(spreadsheet), StandardCharsets.UTF_8)) {
            Assertions.assertThat(zip.getNextEntry().getName()).isEqualTo("mimetype");
            Assertions.assertThat(new String(zip.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo(ODS_MIMETYPE);
            final List<String> entryNames = new ArrayList<>();
            var entry = zip.getNextEntry();
            while (entry != null) {
                zip.readAllBytes();
                entryNames.add(entry.getName());
                entry = zip.getNextEntry();
            }
            Assertions.assertThat(entryNames).contains("content.xml", "META-INF/manifest.xml");
        }

        final SpreadsheetDocument loadedDocument = SpreadsheetDocument.loadDocument(new ByteArrayInputStream(spreadsheet));
        Assertions.assertThat(loadedDocument.getSheetByIndex(0).getTableName()).isEqualTo("Gesamt");
        Assertions.assertThat(loadedDocument.getSheetByIndex(0).getCellByPosition(0, 0).getStringValue()).isEqualTo("Jahr & Zeitraum");
        Assertions.assertThat(loadedDocument.getSheetByIndex(0).getCellByPosition(1, 0).getDoubleValue()).isEqualTo(2026.0);
        loadedDocument.close();
    }

    @Test
    void createSpreadsheetForMessstellenAsOdsKeepsGesamtStructure(@TempDir final Path tempDir) throws Exception {
        final MessstelleAuswertungOptionsDto options = new MessstelleAuswertungOptionsDto();
        options.setTagesTyp(TagesTyp.MO_SO);
        options.setFahrzeuge(new FahrzeugOptionsDto());
        options.setZeitraum(List.of(AuswertungsZeitraum.JAHRE));

        final byte[] spreadsheet = spreadsheetService.createSpreadsheetForMessstellen(List.of(), options);
        final Path spreadsheetFile = tempDir.resolve("Gesamtauswertung.ods");
        Files.write(spreadsheetFile, spreadsheet);

        Assertions.assertThat(Files.size(spreadsheetFile)).isPositive();
        final SpreadsheetDocument spreadsheetDocument = SpreadsheetDocument.loadDocument(spreadsheetFile.toFile());
        final Table gesamtSheet = spreadsheetDocument.getSheetByIndex(0);

        Assertions.assertThat(gesamtSheet.getTableName()).isEqualTo("Gesamt");
        Assertions.assertThat(gesamtSheet.getRowCount()).isEqualTo(4);
        Assertions.assertThat(gesamtSheet.getCellByPosition(0, 3).getStringValue()).isEqualTo("Jahr");
        Assertions.assertThat(gesamtSheet.getCellByPosition(1, 3).getStringValue()).isEqualTo("MstId");
        Assertions.assertThat(gesamtSheet.getCellByPosition(2, 3).getStringValue()).isEqualTo("Anzahl relevante Tage");
        Assertions.assertThat(gesamtSheet.getCellByPosition(3, 3).getStringValue()).isEqualTo("Anzahl plausible Tage");
        Assertions.assertThat(gesamtSheet.getColumnByIndex(0).getWidth()).isBetween(69.9, 70.1);
        Assertions.assertThat(gesamtSheet.getColumnByIndex(1).getWidth()).isLessThan(60);
        spreadsheetDocument.close();
    }

    @Test
    void addMetaHeaderToRow() {
        final Table sheet = createSheet();

        Row row = firstRow(sheet);
        spreadsheetService.addMetaHeaderToRow(row, false);
        Assertions.assertThat(sheet.getRowCount()).isEqualTo(1);
        Assertions.assertThat(row.getCellCount()).isEqualTo(2);
        Cell cell = row.getCellByIndex(0);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("ausgewählter Wochentag");
        cell = row.getCellByIndex(1);
        Assertions.assertThat(cell.getStringValue())
                .isEqualTo("ausgewählter Messquerschnitt");

        spreadsheetService.addMetaHeaderToRow(row, true);
        Assertions.assertThat(sheet.getRowCount()).isEqualTo(1);
        Assertions.assertThat(row.getCellCount()).isEqualTo(3);
        cell = row.getCellByIndex(0);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("ausgewählter Wochentag");
        cell = row.getCellByIndex(1);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("Messstelle");
        cell = row.getCellByIndex(2);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("Messquerschnitt (Merkmale \"MQ-ID - Richtung - Standort MQ\")");
    }

    @Test
    void addMetaDataToRow() {
        final Table sheet = createSheet();

        final MessstelleAuswertungOptionsDto options = new MessstelleAuswertungOptionsDto();
        final MessstelleAuswertungIdDto messstelleAuswertungIdDTO1 = new MessstelleAuswertungIdDto();
        messstelleAuswertungIdDTO1.setMstId("123");
        messstelleAuswertungIdDTO1.setMqIds(Set.of("12301", "12302"));
        final MessstelleAuswertungIdDto messstelleAuswertungIdDTO2 = new MessstelleAuswertungIdDto();
        messstelleAuswertungIdDTO2.setMstId("456");
        messstelleAuswertungIdDTO2.setMqIds(Set.of("45601", "45602"));
        options.setTagesTyp(TagesTyp.MO_SO);
        options.setMessstelleAuswertungIds(Set.of(messstelleAuswertungIdDTO1, messstelleAuswertungIdDTO2));

        Row row = firstRow(sheet);
        spreadsheetService.addMetaDataToRow(row, options);

        Assertions.assertThat(sheet.getRowCount()).isEqualTo(1);
        Assertions.assertThat(row.getCellCount()).isEqualTo(2);
        Cell cell = row.getCellByIndex(0);
        Assertions.assertThat(cell.getStringValue()).isEqualTo(options.getTagesTyp().getBeschreibung());
        cell = row.getCellByIndex(1);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("alle Messquerschnitte");

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
        spreadsheetService.addMetaDataToRow(row, options);

        Assertions.assertThat(sheet.getRowCount()).isEqualTo(1);
        Assertions.assertThat(row.getCellCount()).isEqualTo(2);
        cell = row.getCellByIndex(0);
        Assertions.assertThat(cell.getStringValue()).isEqualTo(options.getTagesTyp().getBeschreibung());
        cell = row.getCellByIndex(1);

        final List<String> cellValue = new ArrayList<>();
        mockedMessstelle.getMessquerschnitte()
                .forEach(messquerschnitt -> cellValue.add(String.format("%s - %s - %s", messquerschnitt.getMqId(),
                        FahrtrichtungUtil.getLongTextOfFahrtrichtung(messquerschnitt.getFahrtrichtung()),
                        messquerschnitt.getStandort())));
        Assertions.assertThat(cell.getStringValue()).isEqualTo(String.join(", ", cellValue));
    }

    @Test
    void addMetaDataToMessquerschnittRow() {
        final Table sheet = createSheet();

        final MessstelleAuswertungOptionsDto options = new MessstelleAuswertungOptionsDto();
        options.setTagesTyp(TagesTyp.MO_SO);
        final String mstId = "123";
        final String mqId = "12301";

        Mockito.when(messstelleService.getOptionalOfMessquerschnittByMstId(mstId, mqId)).thenReturn(Optional.empty());
        Row row = firstRow(sheet);
        spreadsheetService.addMetaDataToMessquerschnittRow(row, options, mstId, mqId);

        Assertions.assertThat(sheet.getRowCount()).isEqualTo(1);
        Assertions.assertThat(row.getCellCount()).isEqualTo(3);
        Cell cell = row.getCellByIndex(0);
        Assertions.assertThat(cell.getStringValue()).isEqualTo(options.getTagesTyp().getBeschreibung());
        cell = row.getCellByIndex(1);
        Assertions.assertThat(cell.getStringValue()).isEqualTo(mstId);
        cell = row.getCellByIndex(2);
        Assertions.assertThat(cell.getStringValue()).isEqualTo(mqId);

        final Messquerschnitt messquerschnitt = new Messquerschnitt();
        messquerschnitt.setFahrtrichtung("W");
        messquerschnitt.setStandort("Standort MQ");
        messquerschnitt.setMqId(mqId);
        Mockito.when(messstelleService.getOptionalOfMessquerschnittByMstId(mstId, mqId)).thenReturn(Optional.of(messquerschnitt));
        spreadsheetService.addMetaDataToMessquerschnittRow(row, options, mstId, mqId);

        Assertions.assertThat(sheet.getRowCount()).isEqualTo(1);
        Assertions.assertThat(row.getCellCount()).isEqualTo(3);
        cell = row.getCellByIndex(0);
        Assertions.assertThat(cell.getStringValue()).isEqualTo(options.getTagesTyp().getBeschreibung());
        cell = row.getCellByIndex(1);
        Assertions.assertThat(cell.getStringValue()).isEqualTo(mstId);
        cell = row.getCellByIndex(2);
        Assertions.assertThat(cell.getStringValue()).isEqualTo(spreadsheetService.getFormattedStringForMessquerschnitt(messquerschnitt));
    }

    @Test
    void addDataHeaderToRow() {
        final Table sheet = createSheet();

        final MessstelleAuswertungOptionsDto options = new MessstelleAuswertungOptionsDto();
        options.setZeitraum(List.of(AuswertungsZeitraum.JAHRE));
        final FahrzeugOptionsDto fahrzeugOptions = new FahrzeugOptionsDto();
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
        Row row = firstRow(sheet);
        spreadsheetService.addDataHeaderToRow(row, options, false);

        Assertions.assertThat(row.getCellCount()).isEqualTo(10);
        Cell cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("Jahr");
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("PKW");
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("LFW");
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("BUS");
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("FUß");
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("KFZ");
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("GV");
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("GV%");
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("Anzahl relevante Tage");
        cell = row.getCellByIndex(cellIndex);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("Anzahl plausible Tage");

        cellIndex = 0;
        options.setZeitraum(List.of(AuswertungsZeitraum.MAERZ));
        spreadsheetService.addDataHeaderToRow(row, options, true);

        Assertions.assertThat(row.getCellCount()).isEqualTo(12);
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("Monat");
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("Jahr");
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("MstId");
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("PKW");
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("LFW");
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("BUS");
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("FUß");
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("KFZ");
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("GV");
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("GV%");
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("Anzahl relevante Tage");
        cell = row.getCellByIndex(cellIndex);
        Assertions.assertThat(cell.getStringValue()).isEqualTo("Anzahl plausible Tage");

    }

    @Test
    void addDataToRow() {
        final Table sheet = createSheet();
        final String mqId = "12301";

        final FahrzeugOptionsDto fahrzeugOptions = new FahrzeugOptionsDto();
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
        auswertung.setNumberOfRelevantKalendertage(42L);
        auswertung.setNumberOfUnauffaelligeTage(23L);
        auswertung.setDaten(daten);

        int cellIndex = 0;
        Row row = firstRow(sheet);
        spreadsheetService.addDataToRow(row, auswertung, fahrzeugOptions, false);

        Assertions.assertThat(row.getCellCount()).isEqualTo(11);
        Cell cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue())
                .isEqualTo(zeitraum.getAuswertungsZeitraum().getText());
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue())
                .isEqualTo(String.valueOf(zeitraum.getStart().getYear()));
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getDoubleValue()).isEqualTo(daten.getSummeAllePkw().doubleValue());
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getDoubleValue()).isEqualTo(daten.getAnzahlLfw().doubleValue());
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getDoubleValue()).isEqualTo(daten.getAnzahlBus().doubleValue());
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo(StringUtils.EMPTY);
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getDoubleValue()).isEqualTo(daten.getSummeKraftfahrzeugverkehr().doubleValue());
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getDoubleValue()).isEqualTo(daten.getSummeGueterverkehr().doubleValue());
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getDoubleValue()).isEqualTo(daten.getProzentGueterverkehr().doubleValue());
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getDoubleValue()).isEqualTo(auswertung.getNumberOfRelevantKalendertage().doubleValue());
        cell = row.getCellByIndex(cellIndex);
        Assertions.assertThat(cell.getDoubleValue()).isEqualTo(auswertung.getNumberOfUnauffaelligeTage().doubleValue());

        auswertung.getZeitraum().setAuswertungsZeitraum(AuswertungsZeitraum.JAHRE);
        cellIndex = 0;
        spreadsheetService.addDataToRow(row, auswertung, fahrzeugOptions, true);

        Assertions.assertThat(row.getCellCount()).isEqualTo(11);
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo(String.valueOf(zeitraum.getStart().getYear()));
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo(auswertung.getObjectId());
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getDoubleValue()).isEqualTo(daten.getSummeAllePkw().doubleValue());
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getDoubleValue()).isEqualTo(daten.getAnzahlLfw().doubleValue());
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getDoubleValue()).isEqualTo(daten.getAnzahlBus().doubleValue());
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getStringValue()).isEqualTo(StringUtils.EMPTY);
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getDoubleValue()).isEqualTo(daten.getSummeKraftfahrzeugverkehr().doubleValue());
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getDoubleValue()).isEqualTo(daten.getSummeGueterverkehr().doubleValue());
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getDoubleValue()).isEqualTo(daten.getProzentGueterverkehr().doubleValue());
        cell = row.getCellByIndex(cellIndex++);
        Assertions.assertThat(cell.getDoubleValue()).isEqualTo(auswertung.getNumberOfRelevantKalendertage().doubleValue());
        cell = row.getCellByIndex(cellIndex);
        Assertions.assertThat(cell.getDoubleValue()).isEqualTo(auswertung.getNumberOfUnauffaelligeTage().doubleValue());

    }

    @Test
    void addBigDecimalToCell() {
        final Table sheet = createSheet();
        final Row row = firstRow(sheet);
        final Cell cell = sheet.getCellByPosition(0, row.getRowIndex());
        final BigDecimal dataToAdd = BigDecimal.TEN;

        spreadsheetService.addBigDecimalToCell(cell, dataToAdd);
        Assertions.assertThat(cell.getDoubleValue()).isEqualTo(dataToAdd.doubleValue());

        spreadsheetService.addBigDecimalToCell(cell, null);
        Assertions.assertThat(cell.getStringValue()).isEqualTo(StringUtils.EMPTY);
    }

    @Test
    void addStringToCell() {
        final Table sheet = createSheet();
        final Row row = firstRow(sheet);
        final Cell cell = sheet.getCellByPosition(0, row.getRowIndex());
        final String dataToAdd = "String to add";

        spreadsheetService.addStringToCell(cell, dataToAdd);
        Assertions.assertThat(cell.getStringValue()).isEqualTo(dataToAdd);

        spreadsheetService.addStringToCell(cell, null);
        Assertions.assertThat(cell.getStringValue()).isEqualTo(StringUtils.EMPTY);
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
