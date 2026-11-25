package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.Auswertung;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungMessstelle;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungIdDto;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDto;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.services.messstelle.MessstelleService;
import de.muenchen.dave.util.messstelle.FahrtrichtungUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class SpreadsheetService {

    private static final double FIRST_COLUMN_WIDTH = 70;

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
    public byte[] createSpreadsheetForMessstellen(final List<AuswertungMessstelle> auswertungenProMessstelle, final MessstelleAuswertungOptionsDto options)
            throws IOException {
        try (SpreadsheetDocument spreadsheetDocument = createSpreadsheetDocument()) {

            // Sheet Gesamt
            final Table gesamtSheet = spreadsheetDocument.getSheetByIndex(0);
            gesamtSheet.setTableName("Gesamt");

            // Template dient danach als Vorlage für die Messstellen- und Messquerschnitt-Sheets,
            // damit diese dieselbe Tabellenstruktur und Formatierung erhalten.
            final Table sheetTemplate = spreadsheetDocument.appendSheet(gesamtSheet, "Template");

            addMetaHeaderToRow(getRowAtIndex(gesamtSheet, 0), false);
            addMetaDataToRow(getRowAtIndex(gesamtSheet, 1), options);
            getRowAtIndex(gesamtSheet, 2);
            addDataHeaderToRow(getRowAtIndex(gesamtSheet, 3), options, true);

            // Füge Daten zum Dokument hinzu.
            int gesamtRowIndex = 4;
            for (final AuswertungMessstelle auswertungMessstelle : ListUtils.emptyIfNull(auswertungenProMessstelle)) {
                // Sheet Messstelle
                final Table mstSheet = spreadsheetDocument.appendSheet(sheetTemplate, String.format("Messstelle %s", auswertungMessstelle.getMstId()));
                addMetaHeaderToRow(getRowAtIndex(mstSheet, 0), false);
                addMetaDataToRow(getRowAtIndex(mstSheet, 1), options);
                getRowAtIndex(mstSheet, 2);
                addDataHeaderToRow(getRowAtIndex(mstSheet, 3), options, false);
                final List<Auswertung> auswertungen = ListUtils.emptyIfNull(auswertungMessstelle.getAuswertungenProZeitraum());
                addDataToSheet(
                        mstSheet,
                        auswertungen,
                        options.getFahrzeuge());
                setFirstColumnWidth(mstSheet);
                gesamtRowIndex = addDataToGesamtSheet(
                        gesamtSheet,
                        auswertungen,
                        options.getFahrzeuge(),
                        gesamtRowIndex);

                auswertungMessstelle.getAuswertungenProMq().forEach((mqId, auswertungenProMessquerschnitt) -> {
                    final Table mqSheet = spreadsheetDocument.appendSheet(sheetTemplate, String.format("Messquerschnitt %s", mqId));

                    addMetaHeaderToRow(getRowAtIndex(mqSheet, 0), true);
                    addMetaDataToMessquerschnittRow(getRowAtIndex(mqSheet, 1), options, auswertungMessstelle.getMstId(), mqId);
                    getRowAtIndex(mqSheet, 2);
                    addDataHeaderToRow(getRowAtIndex(mqSheet, 3), options, false);
                    addDataToSheet(
                            mqSheet,
                            ListUtils.emptyIfNull(auswertungenProMessquerschnitt),
                            options.getFahrzeuge());
                    setFirstColumnWidth(mqSheet);

                });
            }
            setFirstColumnWidth(gesamtSheet);
            // Das Sheet an Index 1 ist das Hilfs-Template.
            // Danach folgen erst die eigentlichen Daten-Sheets.
            spreadsheetDocument.removeSheet(1);
            return serializeSpreadsheetDocument(spreadsheetDocument);
        }
    }

    /**
     * Schreibt die Headerwerte in die Zeile.
     *
     * @param row aktuelle Zeile
     * @param isMessquerschnitt Flag, ob es sich um einen Messquerschnitt handelt
     */
    protected void addMetaHeaderToRow(final Row row, final boolean isMessquerschnitt) {
        int cellIndex = 0;
        addStringToCell(getCell(row, cellIndex++), "ausgewählter Wochentag");
        if (isMessquerschnitt) {
            addStringToCell(getCell(row, cellIndex++), "Messstelle");
            addStringToCell(getCell(row, cellIndex), "Messquerschnitt (Merkmale \"MQ-ID - Richtung - Standort MQ\")");
        } else {
            addStringToCell(getCell(row, cellIndex), "ausgewählter Messquerschnitt");
        }
    }

    /**
     * Schreibt die Meta-Daten in die Zeile.
     *
     * @param row aktuelle Zeile
     * @param options Verwendetet Optionen bei der Auswertung
     */
    protected void addMetaDataToRow(final Row row, final MessstelleAuswertungOptionsDto options) {
        int cellIndex = 0;
        addStringToCell(getCell(row, cellIndex++), options.getTagesTyp().getBeschreibung());

        if (CollectionUtils.isNotEmpty(options.getMessstelleAuswertungIds())) {
            String cellValue = "alle Messquerschnitte";
            if (options.getMessstelleAuswertungIds().size() == 1) {
                final Optional<MessstelleAuswertungIdDto> first = options.getMessstelleAuswertungIds().stream().findFirst();
                final MessstelleAuswertungIdDto messstelleAuswertungIdDTO = first.get();
                final Messstelle messstelleByMstId = messstelleService.getMessstelleByMstId(messstelleAuswertungIdDTO.getMstId());
                final List<String> formattedMessquerschnitte = new ArrayList<>();
                messstelleByMstId.getMessquerschnitte().forEach(messquerschnitt -> {
                    if (messstelleAuswertungIdDTO.getMqIds().contains(messquerschnitt.getMqId())) {
                        formattedMessquerschnitte.add(getFormattedStringForMessquerschnitt(messquerschnitt));
                    }
                });
                cellValue = String.join(", ", formattedMessquerschnitte);
            }
            addStringToCell(getCell(row, cellIndex), cellValue);
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
    protected void addMetaDataToMessquerschnittRow(final Row row, final MessstelleAuswertungOptionsDto options, final String mstId, final String mqId) {
        int cellIndex = 0;
        addStringToCell(getCell(row, cellIndex++), options.getTagesTyp().getBeschreibung());
        addStringToCell(getCell(row, cellIndex++), mstId);
        final Optional<Messquerschnitt> optionalMessquerschnitt = messstelleService.getOptionalOfMessquerschnittByMstId(mstId, mqId);
        String cellValue = mqId;
        if (optionalMessquerschnitt.isPresent()) {
            cellValue = getFormattedStringForMessquerschnitt(optionalMessquerschnitt.get());
        }
        addStringToCell(getCell(row, cellIndex), cellValue);
    }

    /**
     * Legt pro gewählte Fahrzeugoption eine Zelle mit dem Header in der Zeile an.
     *
     * @param row aktuelle Zeile
     * @param options bei der Auswertung gewählte Optionen
     * @param isGesamtSheet Flag falls es das GesamtSheet ist
     */
    protected void addDataHeaderToRow(final Row row, final MessstelleAuswertungOptionsDto options, final boolean isGesamtSheet) {
        int headerCellIndex = 0;
        final FahrzeugOptionsDTO fahrzeugOptions = options.getFahrzeuge();
        final List<AuswertungsZeitraum> zeitraum = options.getZeitraum();
        if (CollectionUtils.isNotEmpty(zeitraum)) {
            if (AuswertungsZeitraum.isHalbjahr(zeitraum.getFirst())) {
                addStringToCell(getCell(row, headerCellIndex++), "Halbjahr");
            } else if (AuswertungsZeitraum.isQuartal(zeitraum.getFirst())) {
                addStringToCell(getCell(row, headerCellIndex++), "Quartal");
            } else if (!AuswertungsZeitraum.isJahr(zeitraum.getFirst())) {
                addStringToCell(getCell(row, headerCellIndex++), "Monat");
            }
        }
        addStringToCell(getCell(row, headerCellIndex++), "Jahr");
        if (isGesamtSheet) {
            addStringToCell(getCell(row, headerCellIndex++), "MstId");
        }

        if (fahrzeugOptions.isPersonenkraftwagen()) {
            addStringToCell(getCell(row, headerCellIndex++), "PKW");
        }
        if (fahrzeugOptions.isLieferwagen()) {
            addStringToCell(getCell(row, headerCellIndex++), "LFW");
        }
        if (fahrzeugOptions.isLastkraftwagen()) {
            addStringToCell(getCell(row, headerCellIndex++), "LKW");
        }
        if (fahrzeugOptions.isLastzuege()) {
            addStringToCell(getCell(row, headerCellIndex++), "LZ");
        }
        if (fahrzeugOptions.isBusse()) {
            addStringToCell(getCell(row, headerCellIndex++), "BUS");
        }
        if (fahrzeugOptions.isKraftraeder()) {
            addStringToCell(getCell(row, headerCellIndex++), "KRAD");
        }
        if (fahrzeugOptions.isRadverkehr()) {
            addStringToCell(getCell(row, headerCellIndex++), "RAD");
        }
        if (fahrzeugOptions.isFussverkehr()) {
            addStringToCell(getCell(row, headerCellIndex++), "FUß");
        }

        if (fahrzeugOptions.isKraftfahrzeugverkehr()) {
            addStringToCell(getCell(row, headerCellIndex++), "KFZ");
        }
        if (fahrzeugOptions.isSchwerverkehr()) {
            addStringToCell(getCell(row, headerCellIndex++), "SV");
        }
        if (fahrzeugOptions.isSchwerverkehrsanteilProzent()) {
            addStringToCell(getCell(row, headerCellIndex++), "SV%");
        }
        if (fahrzeugOptions.isGueterverkehr()) {
            addStringToCell(getCell(row, headerCellIndex++), "GV");
        }
        if (fahrzeugOptions.isGueterverkehrsanteilProzent()) {
            addStringToCell(getCell(row, headerCellIndex++), "GV%");
        }
        addStringToCell(getCell(row, headerCellIndex++), "Anzahl relevante Tage");
        addStringToCell(getCell(row, headerCellIndex), "Anzahl plausible Tage");
    }

    /**
     * Legt im Arbeitsblatt der Gesamtansicht für jeden Auswertungszeitraum eine Zeile an und
     * lässt diese mit den Daten befüllen.
     *
     * @param sheet aktuelles Arbeitsblatt
     * @param auswertungen Liste mit den Daten pro Zeitraum
     * @param fahrzeugOptions bei der Auswertung gewählte Fahrzeugoptionen
     */
    private int addDataToGesamtSheet(
            final Table sheet,
            final List<Auswertung> auswertungen,
            final FahrzeugOptionsDTO fahrzeugOptions,
            final int startRowIndex) {
        for (int rowIndex = 0; rowIndex < auswertungen.size(); rowIndex++) {
            addDataToRow(getRowAtIndex(sheet, startRowIndex + rowIndex), auswertungen.get(rowIndex), fahrzeugOptions, true);
        }
        return startRowIndex + auswertungen.size();
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
            final Table sheet,
            final List<Auswertung> auswertungen,
            final FahrzeugOptionsDTO fahrzeugOptions) {
        for (int rowIndex = 0; rowIndex < auswertungen.size(); rowIndex++) {
            addDataToRow(getRowAtIndex(sheet, 4 + rowIndex), auswertungen.get(rowIndex), fahrzeugOptions, false);
        }
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
            addStringToCell(getCell(row, cellIndex++), auswertung.getZeitraum().getAuswertungsZeitraum().getText());
        }
        addStringToCell(getCell(row, cellIndex++), String.valueOf(auswertung.getZeitraum().getStart().getYear()));

        if (isGesamtSheet) {
            addStringToCell(getCell(row, cellIndex++), auswertung.getObjectId());
        }
        if (fahrzeugOptions.isPersonenkraftwagen()) {
            addBigDecimalToCell(getCell(row, cellIndex++), auswertung.getDaten().getSummeAllePkw());
        }
        if (fahrzeugOptions.isLieferwagen()) {
            addBigDecimalToCell(getCell(row, cellIndex++), auswertung.getDaten().getAnzahlLfw());
        }
        if (fahrzeugOptions.isLastkraftwagen()) {
            addBigDecimalToCell(getCell(row, cellIndex++), auswertung.getDaten().getAnzahlLkw());
        }
        if (fahrzeugOptions.isLastzuege()) {
            addBigDecimalToCell(getCell(row, cellIndex++), auswertung.getDaten().getSummeLastzug());
        }
        if (fahrzeugOptions.isBusse()) {
            addBigDecimalToCell(getCell(row, cellIndex++), auswertung.getDaten().getAnzahlBus());
        }
        if (fahrzeugOptions.isKraftraeder()) {
            addBigDecimalToCell(getCell(row, cellIndex++), auswertung.getDaten().getAnzahlKrad());
        }
        if (fahrzeugOptions.isRadverkehr()) {
            addBigDecimalToCell(getCell(row, cellIndex++), auswertung.getDaten().getAnzahlRad());
        }
        if (fahrzeugOptions.isFussverkehr()) {
            // Wird aktuell noch nicht erfasst
            addStringToCell(getCell(row, cellIndex++), StringUtils.EMPTY);
        }
        if (fahrzeugOptions.isKraftfahrzeugverkehr()) {
            addBigDecimalToCell(getCell(row, cellIndex++), auswertung.getDaten().getSummeKraftfahrzeugverkehr());
        }
        if (fahrzeugOptions.isSchwerverkehr()) {
            addBigDecimalToCell(getCell(row, cellIndex++), auswertung.getDaten().getSummeSchwerverkehr());
        }
        if (fahrzeugOptions.isSchwerverkehrsanteilProzent()) {
            addBigDecimalToCell(getCell(row, cellIndex++), auswertung.getDaten().getProzentSchwerverkehr());
        }
        if (fahrzeugOptions.isGueterverkehr()) {
            addBigDecimalToCell(getCell(row, cellIndex++), auswertung.getDaten().getSummeGueterverkehr());
        }
        if (fahrzeugOptions.isGueterverkehrsanteilProzent()) {
            addBigDecimalToCell(getCell(row, cellIndex++), auswertung.getDaten().getProzentGueterverkehr());
        }

        final var numberOfRelevantKalendertage = Objects.isNull(auswertung.getNumberOfRelevantKalendertage())
                ? null
                : BigDecimal.valueOf(auswertung.getNumberOfRelevantKalendertage());
        addBigDecimalToCell(getCell(row, cellIndex++), numberOfRelevantKalendertage);

        final var numberOfUnauffaelligeTage = Objects.isNull(auswertung.getNumberOfUnauffaelligeTage())
                ? null
                : BigDecimal.valueOf(auswertung.getNumberOfUnauffaelligeTage());
        addBigDecimalToCell(getCell(row, cellIndex), numberOfUnauffaelligeTage);
    }

    protected void addBigDecimalToCell(final Cell cell, final BigDecimal data) {
        if (ObjectUtils.isNotEmpty(data)) {
            cell.setDoubleValue(data.doubleValue());
        } else {
            addStringToCell(cell, StringUtils.EMPTY);
        }
    }

    protected void addStringToCell(final Cell cell, final String data) {
        if (ObjectUtils.isNotEmpty(data)) {
            cell.setStringValue(data);
        } else {
            cell.setStringValue(StringUtils.EMPTY);
        }
    }

    private Cell getCell(final Row row, final int cellIndex) {
        return row.getTable().getCellByPosition(cellIndex, row.getRowIndex());
    }

    private Row getRowAtIndex(final Table table, final int rowIndex) {
        while (table.getRowCount() <= rowIndex) {
            table.appendRow();
        }
        return table.getRowByIndex(rowIndex);
    }

    private void setFirstColumnWidth(final Table table) {
        final var firstColumn = table.getColumnByIndex(0);
        // Die Spalten können dieselbe automatische Formatvorlage verwenden; erst eine eigene Vorlage
        // stellt sicher, dass setWidth nicht zugleich die Breite der übrigen Spalten ändert.
        firstColumn.getOdfElement().getOrCreateUnqiueAutomaticStyle();
        firstColumn.setWidth(FIRST_COLUMN_WIDTH);
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
     * Erzeugt ein leeres ODS-Dokument.
     *
     * @return ODS-Dokument
     * @throws IOException wenn das Dokument nicht angelegt werden kann
     */
    private SpreadsheetDocument createSpreadsheetDocument() throws IOException {
        try {
            return SpreadsheetDocument.newSpreadsheetDocument();
        } catch (final Exception e) {
            throw new IOException("ODS-Dokument konnte nicht angelegt werden.", e);
        }
    }

    /**
     * Serialisiert das Tabellenkalkulationsdokument in ein Byte-Array.
     *
     * @param spreadsheetDocument zu serialisierendes ODS-Dokument
     * @return serialisiertes Dokument als Byte-Array
     * @throws IOException wenn das Dokument nicht serialisiert werden kann
     */
    byte[] serializeSpreadsheetDocument(final SpreadsheetDocument spreadsheetDocument) throws IOException {
        try (final var output = new ByteArrayOutputStream()) {
            spreadsheetDocument.save(output);
            return output.toByteArray();
        } catch (final Exception e) {
            throw new IOException("ODS-Dokument konnte nicht serialisiert werden.", e);
        }
    }

}
