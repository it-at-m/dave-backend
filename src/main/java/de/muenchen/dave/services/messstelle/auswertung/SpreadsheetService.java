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
import de.muenchen.dave.util.messstelle.FahrtrichtungUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@RequiredArgsConstructor
@Service
@Slf4j
public class SpreadsheetService {

    private static final String ODS_SETTINGS_FILE = "settings.xml";
    private static final String ODS_MANIFEST_FILE = "META-INF/manifest.xml";
    private static final String ODS_MANIFEST_NAMESPACE = "urn:oasis:names:tc:opendocument:xmlns:manifest:1.0";

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
        try (SpreadsheetDocument spreadsheetDocument = createSpreadsheetDocument()) {

            // Sheet Gesamt
            final Table gesamtSheet = spreadsheetDocument.getSheetByIndex(0);
            clearRows(gesamtSheet);
            gesamtSheet.setTableName("Gesamt");
            addMetaHeaderToRow(getNextRow(gesamtSheet), false);
            addMetaDataToRow(getNextRow(gesamtSheet), options);
            addEmptyRowToSheet(gesamtSheet);
            addDataHeaderToRow(getNextRow(gesamtSheet), options, true);

            // Füge Daten zum Dokument hinzu.
            ListUtils.emptyIfNull(auswertungenProMessstelle).forEach(auswertungMessstelle -> {
                // Sheet Messstelle
                final Table mstSheet = spreadsheetDocument.appendSheet(String.format("Messstelle %s", auswertungMessstelle.getMstId()));
                clearRows(mstSheet);
                addMetaHeaderToRow(getNextRow(mstSheet), false);
                addMetaDataToRow(getNextRow(mstSheet), options);
                addEmptyRowToSheet(mstSheet);
                addDataHeaderToRow(getNextRow(mstSheet), options, false);
                final List<Auswertung> auswertungen = ListUtils.emptyIfNull(auswertungMessstelle.getAuswertungenProZeitraum());
                addDataToSheet(
                        mstSheet,
                        auswertungen,
                        options.getFahrzeuge());
                addDataToGesamtSheet(
                        gesamtSheet,
                        auswertungen,
                        options.getFahrzeuge());

                auswertungMessstelle.getAuswertungenProMq().forEach((mqId, auswertungenProMessquerschnitt) -> {
                    final Table mqSheet = spreadsheetDocument.appendSheet(String.format("Messquerschnitt %s", mqId));
                    clearRows(mqSheet);

                    addMetaHeaderToRow(getNextRow(mqSheet), true);
                    addMetaDataToMessquerschnittRow(getNextRow(mqSheet), options, auswertungMessstelle.getMstId(), mqId);
                    addEmptyRowToSheet(mqSheet);
                    addDataHeaderToRow(getNextRow(mqSheet), options, false);
                    addDataToSheet(
                            mqSheet,
                            ListUtils.emptyIfNull(auswertungenProMessquerschnitt),
                            options.getFahrzeuge());

                });
            });

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
    protected void addMetaDataToRow(final Row row, final MessstelleAuswertungOptionsDTO options) {
        int cellIndex = 0;
        addStringToCell(getCell(row, cellIndex++), options.getTagesTyp().getBeschreibung());

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
    protected void addMetaDataToMessquerschnittRow(final Row row, final MessstelleAuswertungOptionsDTO options, final String mstId, final String mqId) {
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
    protected void addDataHeaderToRow(final Row row, final MessstelleAuswertungOptionsDTO options, final boolean isGesamtSheet) {
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
    private void addDataToGesamtSheet(
            final Table sheet,
            final List<Auswertung> auswertungen,
            final FahrzeugOptionsDTO fahrzeugOptions) {
        auswertungen.forEach(auswertung -> addDataToRow(getNextRow(sheet), auswertung, fahrzeugOptions, true));
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
        auswertungen.forEach(auswertung -> addDataToRow(getNextRow(sheet), auswertung, fahrzeugOptions, false));
    }

    /**
     * Erzeugt eine Leerzeile im Arbeitsblatt.
     *
     * @param sheet aktuelles Arbeitsblatt
     */
    private void addEmptyRowToSheet(final Table sheet) {
        getNextRow(sheet);
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

    private Row getNextRow(final Table table) {
        if (table.getRowCount() == 0) {
            return table.appendRow();
        }
        final Row firstRow = table.getRowByIndex(0);
        if (table.getRowCount() == 1 && firstRow.getCellCount() == 0) {
            return firstRow;
        }
        return table.appendRow();
    }

    private void clearRows(final Table table) {
        if (table.getRowCount() > 0) {
            table.removeRowsByIndex(0, table.getRowCount());
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

    byte[] serializeSpreadsheetDocument(final SpreadsheetDocument spreadsheetDocument) throws IOException {
        try (final var output = new ByteArrayOutputStream()) {
            spreadsheetDocument.save(output);
            return removeUnusedSettingsFile(output.toByteArray());
        } catch (final Exception e) {
            throw new IOException("ODS-Dokument konnte nicht serialisiert werden.", e);
        }
    }

    private byte[] removeUnusedSettingsFile(final byte[] spreadsheet) throws IOException {
        try (final var zipInput = new ZipInputStream(new ByteArrayInputStream(spreadsheet), StandardCharsets.UTF_8);
                final var output = new ByteArrayOutputStream();
                final var zipOutput = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zipInput.getNextEntry()) != null) {
                byte[] content = zipInput.readAllBytes();
                if (ODS_SETTINGS_FILE.equals(entry.getName())) {
                    continue;
                }
                if (ODS_MANIFEST_FILE.equals(entry.getName())) {
                    content = removeSettingsEntryFromManifest(content);
                }

                final var outputEntry = new ZipEntry(entry.getName());
                if ("mimetype".equals(entry.getName())) {
                    outputEntry.setMethod(ZipEntry.STORED);
                    outputEntry.setSize(content.length);
                    outputEntry.setCompressedSize(content.length);
                    final var crc = new CRC32();
                    crc.update(content);
                    outputEntry.setCrc(crc.getValue());
                }
                zipOutput.putNextEntry(outputEntry);
                zipOutput.write(content);
                zipOutput.closeEntry();
            }
            zipOutput.finish();
            return output.toByteArray();
        }
    }

    private byte[] removeSettingsEntryFromManifest(final byte[] manifest) throws IOException {
        try (final var input = new ByteArrayInputStream(manifest); final var output = new ByteArrayOutputStream()) {
            final var documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            final var document = documentBuilderFactory.newDocumentBuilder().parse(input);
            final NodeList fileEntries = document.getElementsByTagNameNS(ODS_MANIFEST_NAMESPACE, "file-entry");
            for (int index = fileEntries.getLength() - 1; index >= 0; index--) {
                final Element fileEntry = (Element) fileEntries.item(index);
                if (ODS_SETTINGS_FILE.equals(fileEntry.getAttributeNS(ODS_MANIFEST_NAMESPACE, "full-path"))) {
                    fileEntry.getParentNode().removeChild(fileEntry);
                }
            }
            final var transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            transformer.transform(new DOMSource(document), new StreamResult(output));
            return output.toByteArray();
        } catch (final Exception e) {
            throw new IOException("ODS-Manifest konnte nicht angepasst werden.", e);
        }
    }
}
