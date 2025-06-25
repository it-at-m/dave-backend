package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.Auswertung;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungMessstelle;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungIdDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDTO;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.services.messstelle.MessstelleService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import de.muenchen.dave.util.messstelle.FahrtrichtungUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class SpreadsheetService {

    private final MessstelleService messstelleService;

    /**
     * Erzeugt eine Tabellenkalkulationsdatei als byte[], welches die ausgewerteten Daten beinhaltet.
     *
     * @param auswertungenProMessstelle ausgewerteten Daten. Die Sortierung des Attributs und der darin
     *            enthaltenen Unterattribute
     *            bildet sich ebenfalls in der erstellen Datei ab.
     * @param options zur Auswertung verwendete Optionen
     * @return das File als byte[]
     * @throws IOException kann bei der Erstellung des byte[] geworfen werden. Behandlung erfolgt im
     *             Controller.
     */
    public byte[] createSpreadsheetForMessstellen(final List<AuswertungMessstelle> auswertungenProMessstelle, final MessstelleAuswertungOptionsDTO options)
            throws IOException {
        final var spreadsheetDocument = new XSSFWorkbook();

        // Sheet Gesamt
        final Sheet gesamtSheet = spreadsheetDocument.createSheet("Gesamt");
        int gesamtRowIndex = 0;
        addMetaHeaderToRow(gesamtSheet.createRow(gesamtRowIndex++), false);
        addMetaDataToRow(gesamtSheet.createRow(gesamtRowIndex++), options);
        addEmptyRowToSheetAtIndex(gesamtSheet, gesamtRowIndex++);
        addDataHeaderToRow(gesamtSheet.createRow(gesamtRowIndex), options, true);

        final AtomicReference<Integer> rowOffset = new AtomicReference<>(0);
        // Füge Daten zum Dokument hinzu.
        ListUtils.emptyIfNull(auswertungenProMessstelle).forEach(auswertungMessstelle -> {
            int mstRowIndex = 0;
            // Sheet Messstelle
            final Sheet mstSheet = spreadsheetDocument.createSheet(String.format("Messstelle %s", auswertungMessstelle.getMstId()));
            addMetaHeaderToRow(mstSheet.createRow(mstRowIndex++), false);
            addMetaDataToRow(mstSheet.createRow(mstRowIndex++), options);
            addEmptyRowToSheetAtIndex(mstSheet, mstRowIndex++);
            addDataHeaderToRow(mstSheet.createRow(mstRowIndex), options, false);
            final List<Auswertung> auswertungen = ListUtils.emptyIfNull(auswertungMessstelle.getAuswertungenProZeitraum());
            addDataToSheet(
                    mstSheet,
                    auswertungen,
                    options.getFahrzeuge());
            addDataToGesamtSheet(
                    gesamtSheet,
                    auswertungen,
                    options.getFahrzeuge(),
                    rowOffset.getAndUpdate(v -> v + auswertungen.size()));

            auswertungMessstelle.getAuswertungenProMq().forEach((mqId, auswertungenProMessquerschnitt) -> {
                final Sheet mqSheet = spreadsheetDocument.createSheet(String.format("Messquerschnitt %s", mqId));
                int mqRowIndex = 0;

                addMetaHeaderToRow(mqSheet.createRow(mqRowIndex++), true);
                addMetaDataToMessquerschnittRow(mqSheet.createRow(mqRowIndex++), options, auswertungMessstelle.getMstId(), mqId);
                addEmptyRowToSheetAtIndex(mqSheet, mqRowIndex++);
                addDataHeaderToRow(mqSheet.createRow(mqRowIndex), options, false);
                addDataToSheet(
                        mqSheet,
                        ListUtils.emptyIfNull(auswertungenProMessquerschnitt),
                        options.getFahrzeuge());

                mqSheet.autoSizeColumn(0);
                mqSheet.autoSizeColumn(1);
            });
            mstSheet.autoSizeColumn(0);
        });
        gesamtSheet.autoSizeColumn(0);

        return serializeSpreadsheetDocument(spreadsheetDocument);
    }

    /**
     * Schreibt die Headerwerte in die Zeile.
     *
     * @param row aktuelle Zeile
     * @param isMessquerschnitt Flag, ob es sich um einen Messquerschnitt handelt
     */
    protected void addMetaHeaderToRow(final Row row, final boolean isMessquerschnitt) {
        int cellIndex = 0;
        addStringToCell(row.createCell(cellIndex++, CellType.STRING), "ausgewählter Wochentag");
        if (isMessquerschnitt) {
            addStringToCell(row.createCell(cellIndex++, CellType.STRING), "Messstelle");
            addStringToCell(row.createCell(cellIndex, CellType.STRING), "Messquerschnitt (Merkmale \"MQ-ID - Richtung - Standort MQ\")");
        } else {
            addStringToCell(row.createCell(cellIndex, CellType.STRING),
                    "ausgewählter Messquerschnitt");
        }
    }

    /**
     * Schreibt die Meta-Daten in die Zeile.
     *
     * @param row aktuelle Zeile
     * @param options Verwendetet Optionen bei der Auswertung
     */
    protected void addMetaDataToRow(final Row row, final MessstelleAuswertungOptionsDTO options) {
        int cellIndex = 0;
        addStringToCell(row.createCell(cellIndex++, CellType.STRING), options.getTagesTyp().getBeschreibung());

        if (CollectionUtils.isNotEmpty(options.getMessstelleAuswertungIds())) {
            String cellValue = "alle Messquerschnitte";
            if (options.getMessstelleAuswertungIds().size() == 1) {
                final Optional<MessstelleAuswertungIdDTO> first = options.getMessstelleAuswertungIds().stream().findFirst();
                final MessstelleAuswertungIdDTO messstelleAuswertungIdDTO = first.get();
                final Messstelle messstelleByMstId = messstelleService.getMessstelleByMstId(messstelleAuswertungIdDTO.getMstId());
                final List<String> formattedMessquerschnitte = new ArrayList<>();
                messstelleByMstId.getMessquerschnitte().forEach(messquerschnitt -> {
                    if (messstelleAuswertungIdDTO.getMqIds().contains(messquerschnitt.getMqId())) {
                        formattedMessquerschnitte.add(getFormattedStringForMessquerschnitt(messquerschnitt));
                    }
                });
                cellValue = String.join(", ", formattedMessquerschnitte);
            }
            addStringToCell(row.createCell(cellIndex, CellType.STRING), cellValue);
        }
    }

    /**
     * Schreibt die Meta-Daten eines Messquerschnitts in die Zeile.
     *
     * @param row aktuelle Zeile eines Messquerschnitts
     * @param options Verwendetet Optionen bei der Auswertung
     * @param mstId ausgewertete Messstelle
     * @param mqId ausgewerteter Messquerschnitt
     */
    protected void addMetaDataToMessquerschnittRow(final Row row, final MessstelleAuswertungOptionsDTO options, final String mstId, final String mqId) {
        int cellIndex = 0;
        addStringToCell(row.createCell(cellIndex++, CellType.STRING), options.getTagesTyp().getBeschreibung());
        addStringToCell(row.createCell(cellIndex++, CellType.STRING), mstId);
        final Optional<Messquerschnitt> optionalMessquerschnitt = messstelleService.getOptionalOfMessquerschnittByMstId(mstId, mqId);
        String cellValue = mqId;
        if (optionalMessquerschnitt.isPresent()) {
            cellValue = getFormattedStringForMessquerschnitt(optionalMessquerschnitt.get());
        }
        addStringToCell(row.createCell(cellIndex, CellType.STRING), cellValue);
    }

    /**
     * Legt pro gewählte Fahrzeugoption eine Zelle mit dem Header in der Zeile an.
     *
     * @param row aktuelle Zeile
     * @param options bei der Auswertung gewählte Optionen
     * @param isGesamtSheet Flag falls es das GesamtSheet ist
     */
    protected void addDataHeaderToRow(final Row row, final MessstelleAuswertungOptionsDTO options, final boolean isGesamtSheet) {
        int headerCellIndex = 0;
        final FahrzeugOptionsDTO fahrzeugOptions = options.getFahrzeuge();
        final List<AuswertungsZeitraum> zeitraum = options.getZeitraum();
        if (CollectionUtils.isNotEmpty(zeitraum)) {
            if (AuswertungsZeitraum.isHalbjahr(zeitraum.getFirst())) {
                addStringToCell(row.createCell(headerCellIndex++, CellType.STRING), "Halbjahr");
            } else if (AuswertungsZeitraum.isQuartal(zeitraum.getFirst())) {
                addStringToCell(row.createCell(headerCellIndex++, CellType.STRING), "Quartal");
            } else if (!AuswertungsZeitraum.isJahr(zeitraum.getFirst())) {
                addStringToCell(row.createCell(headerCellIndex++, CellType.STRING), "Monat");
            }
        }
        addStringToCell(row.createCell(headerCellIndex++, CellType.STRING), "Jahr");
        if (isGesamtSheet) {
            addStringToCell(row.createCell(headerCellIndex++, CellType.STRING), "MstId");
        }
        if (fahrzeugOptions.isKraftfahrzeugverkehr()) {
            addStringToCell(row.createCell(headerCellIndex++, CellType.STRING), "KFZ");
        }
        if (fahrzeugOptions.isSchwerverkehr()) {
            addStringToCell(row.createCell(headerCellIndex++, CellType.STRING), "SV");
        }
        if (fahrzeugOptions.isGueterverkehr()) {
            addStringToCell(row.createCell(headerCellIndex++, CellType.STRING), "GV");
        }
        if (fahrzeugOptions.isSchwerverkehrsanteilProzent()) {
            addStringToCell(row.createCell(headerCellIndex++, CellType.STRING), "SV%");
        }
        if (fahrzeugOptions.isGueterverkehrsanteilProzent()) {
            addStringToCell(row.createCell(headerCellIndex++, CellType.STRING), "GV%");
        }
        if (fahrzeugOptions.isRadverkehr()) {
            addStringToCell(row.createCell(headerCellIndex++, CellType.STRING), "RAD");
        }
        if (fahrzeugOptions.isFussverkehr()) {
            addStringToCell(row.createCell(headerCellIndex++, CellType.STRING), "FUß");
        }
        if (fahrzeugOptions.isLastkraftwagen()) {
            addStringToCell(row.createCell(headerCellIndex++, CellType.STRING), "LKW");
        }
        if (fahrzeugOptions.isLieferwagen()) {
            addStringToCell(row.createCell(headerCellIndex++, CellType.STRING), "LFW");
        }
        if (fahrzeugOptions.isLastzuege()) {
            addStringToCell(row.createCell(headerCellIndex++, CellType.STRING), "LZ");
        }
        if (fahrzeugOptions.isBusse()) {
            addStringToCell(row.createCell(headerCellIndex++, CellType.STRING), "BUS");
        }
        if (fahrzeugOptions.isKraftraeder()) {
            addStringToCell(row.createCell(headerCellIndex++, CellType.STRING), "KRAD");
        }
        if (fahrzeugOptions.isPersonenkraftwagen()) {
            addStringToCell(row.createCell(headerCellIndex, CellType.STRING), "PKW");
        }
    }

    /**
     * Legt im Arbeitsblatt der Gesamtansicht für jeden Auswertungszeitraum eine Zeile an und
     * lässt diese mit den Daten befüllen.
     *
     * @param sheet aktuelles Arbeitsblatt
     * @param auswertungen Liste mit den Daten pro Zeitraum
     * @param fahrzeugOptions bei der Auswertung gewählte Fahrzeugoptionen
     * @param rowOffset Offset für den Zeilenindex
     */
    private void addDataToGesamtSheet(
            final Sheet sheet,
            final List<Auswertung> auswertungen,
            final FahrzeugOptionsDTO fahrzeugOptions,
            final Integer rowOffset) {
        final AtomicInteger rowIndex = new AtomicInteger(4 + rowOffset);
        auswertungen.forEach(auswertung -> addDataToRow(sheet.createRow(rowIndex.getAndIncrement()), auswertung, fahrzeugOptions, true));
    }

    /**
     * Legt im Arbeitsblatt für jeden Auswertungszeitraum eine Zeile an und
     * lässt diese mit den Daten befüllen.
     *
     * @param sheet aktuelles Arbeitsblatt
     * @param auswertungen Liste mit den Daten pro Zeitraum
     * @param fahrzeugOptions bei der Auswertung gewählte Fahrzeugoptionen
     */
    private void addDataToSheet(
            final Sheet sheet,
            final List<Auswertung> auswertungen,
            final FahrzeugOptionsDTO fahrzeugOptions) {
        final AtomicInteger rowIndex = new AtomicInteger(4);
        auswertungen.forEach(auswertung -> addDataToRow(sheet.createRow(rowIndex.getAndIncrement()), auswertung, fahrzeugOptions, false));
    }

    /**
     * Erzeugt am Index eine Leerzeile im Arbeitsblatt.
     *
     * @param sheet aktuelles Arbeitsblatt
     * @param index Index der Leerzeile
     */
    private void addEmptyRowToSheetAtIndex(final Sheet sheet, final int index) {
        sheet.createRow(index);
    }

    /**
     * Legt pro gewählte Fahrzeugoption eine Zelle mit den Daten in dieser Reihe an.
     *
     * @param row aktuelle Zeile
     * @param auswertung Liste mit den Daten pro Zeitraum
     * @param fahrzeugOptions bei der Auswertung gewählte Fahrzeugoptionen
     * @param isGesamtSheet Flag ob es sich um das Sheet der Gesamtansicht handelt
     */
    protected void addDataToRow(
            final Row row,
            final Auswertung auswertung,
            final FahrzeugOptionsDTO fahrzeugOptions,
            final boolean isGesamtSheet) {

        int cellIndex = 0;
        if (!AuswertungsZeitraum.JAHRE.equals(auswertung.getZeitraum().getAuswertungsZeitraum())) {
            addStringToCell(row.createCell(cellIndex++, CellType.STRING), auswertung.getZeitraum().getAuswertungsZeitraum().getText());
        }
        addStringToCell(row.createCell(cellIndex++, CellType.STRING), String.valueOf(auswertung.getZeitraum().getStart().getYear()));

        if (isGesamtSheet) {
            addStringToCell(row.createCell(cellIndex++, CellType.STRING), auswertung.getObjectId());
        }
        if (fahrzeugOptions.isKraftfahrzeugverkehr()) {
            addBigDecimalToCell(row.createCell(cellIndex++, CellType.NUMERIC), auswertung.getDaten().getSummeKraftfahrzeugverkehr());
        }
        if (fahrzeugOptions.isSchwerverkehr()) {
            addBigDecimalToCell(row.createCell(cellIndex++, CellType.NUMERIC), auswertung.getDaten().getSummeSchwerverkehr());
        }
        if (fahrzeugOptions.isGueterverkehr()) {
            addBigDecimalToCell(row.createCell(cellIndex++, CellType.NUMERIC), auswertung.getDaten().getSummeGueterverkehr());
        }
        if (fahrzeugOptions.isSchwerverkehrsanteilProzent()) {
            addBigDecimalToCell(row.createCell(cellIndex++, CellType.NUMERIC), auswertung.getDaten().getProzentSchwerverkehr());
        }
        if (fahrzeugOptions.isGueterverkehrsanteilProzent()) {
            addBigDecimalToCell(row.createCell(cellIndex++, CellType.NUMERIC), auswertung.getDaten().getProzentGueterverkehr());
        }
        if (fahrzeugOptions.isRadverkehr()) {
            addBigDecimalToCell(row.createCell(cellIndex++, CellType.NUMERIC), auswertung.getDaten().getAnzahlRad());
        }
        if (fahrzeugOptions.isFussverkehr()) {
            // Wird aktuell noch nicht erfasst
            addStringToCell(row.createCell(cellIndex++, CellType.NUMERIC), StringUtils.EMPTY);
        }
        if (fahrzeugOptions.isLastkraftwagen()) {
            addBigDecimalToCell(row.createCell(cellIndex++, CellType.NUMERIC), auswertung.getDaten().getAnzahlLkw());
        }
        if (fahrzeugOptions.isLieferwagen()) {
            addBigDecimalToCell(row.createCell(cellIndex++, CellType.NUMERIC), auswertung.getDaten().getAnzahlLfw());
        }
        if (fahrzeugOptions.isLastzuege()) {
            addBigDecimalToCell(row.createCell(cellIndex++, CellType.NUMERIC), auswertung.getDaten().getSummeLastzug());
        }
        if (fahrzeugOptions.isBusse()) {
            addBigDecimalToCell(row.createCell(cellIndex++, CellType.NUMERIC), auswertung.getDaten().getAnzahlBus());
        }
        if (fahrzeugOptions.isKraftraeder()) {
            addBigDecimalToCell(row.createCell(cellIndex++, CellType.NUMERIC), auswertung.getDaten().getAnzahlKrad());
        }
        if (fahrzeugOptions.isPersonenkraftwagen()) {
            addBigDecimalToCell(row.createCell(cellIndex, CellType.NUMERIC), auswertung.getDaten().getSummeAllePkw());
        }
    }

    protected void addBigDecimalToCell(final Cell cell, final BigDecimal data) {
        if (ObjectUtils.isNotEmpty(data)) {
            cell.setCellValue(data.doubleValue());
        } else {
            addStringToCell(cell, StringUtils.EMPTY);
        }
    }

    protected void addStringToCell(final Cell cell, final String data) {
        if (ObjectUtils.isNotEmpty(data)) {
            cell.setCellValue(data);
        } else {
            cell.setCellValue(StringUtils.EMPTY);
        }
    }

    /**
     * Liefert einen formatierten String für einen Messquerschnitt.
     *
     * @param messquerschnitt zu formatierender Messquerschnitt
     * @return Messquerschnitt als String
     */
    protected String getFormattedStringForMessquerschnitt(final Messquerschnitt messquerschnitt) {
        return String.format("%s - %s - %s", messquerschnitt.getMqId(), FahrtrichtungUtil.getLongTextOfFahrtrichtung(messquerschnitt.getFahrtrichtung()),
                messquerschnitt.getStandort());
    }

    /**
     * Erzeugt aus dem WorkBook ein byte[]
     *
     * @param spreadsheetDocument Workbook zum Serialisieren
     * @return Workbook als byte[]
     * @throws IOException kann beim Erstellen geworfen werden. Behandlung erfolgt im Controller.
     */
    private byte[] serializeSpreadsheetDocument(final Workbook spreadsheetDocument) throws IOException {
        try (final var baos = new ByteArrayOutputStream()) {
            spreadsheetDocument.write(baos);
            return baos.toByteArray();
        }
    }
}
