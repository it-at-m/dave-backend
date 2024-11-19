package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.Auswertung;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungProMessstelle;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungIdDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDTO;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.services.messstelle.MessstelleService;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
@Service
@Slf4j
public class SpreadsheetService {

    private final MessstelleService messstelleService;

    /**
     * Erzeugt eine Datei als byte[], welches die ausgewerteten Daten beinhaltet
     *
     * @param auswertungenProMessstelle ausgewerteten Daten
     * @param options zur Auswertung verwendete Optionen
     * @return das File als byte[]
     * @throws IOException kann bei der Erstellung des byte[] geworfen werden. Behandlung erfolgt im
     *             Controller.
     */
    public byte[] createFile(final List<AuswertungProMessstelle> auswertungenProMessstelle, final MessstelleAuswertungOptionsDTO options)
            throws IOException {
        final var spreadsheetDocument = new XSSFWorkbook();

        auswertungenProMessstelle.sort(Comparator.comparing(AuswertungProMessstelle::getMstId));

        // Füge Daten zum Document hinzu.
        ListUtils.emptyIfNull(auswertungenProMessstelle).forEach(auswertungProMessstelle -> {

            // Sheet Messstelle
            final Sheet mstSheet = spreadsheetDocument.createSheet(String.format("Messstelle %s", auswertungProMessstelle.getMstId()));

            addMetaHeaderToSheet(mstSheet);
            addMetaDataToSheet(mstSheet, options);
            addDataHeaderToSheet(mstSheet, options.getFahrzeuge());
            addDataToSheet(
                    mstSheet,
                    ListUtils.emptyIfNull(auswertungProMessstelle.getAuswertungenProZeitraum()),
                    options.getFahrzeuge());

            auswertungProMessstelle.getAuswertungenProMq().forEach((mqId, auswertungenProMessquerschnitt) -> {
                final Sheet mqSheet = spreadsheetDocument.createSheet(String.format("Messquerschnitt %s", mqId));

                addMetaHeaderToMessquerschnittSheet(mqSheet);
                addMetaDataToMessquerschnittSheet(mqSheet, options, auswertungProMessstelle.getMstId(), mqId);
                addDataHeaderToSheet(mqSheet, options.getFahrzeuge());
                addDataToSheet(
                        mqSheet,
                        ListUtils.emptyIfNull(auswertungenProMessquerschnitt),
                        options.getFahrzeuge());

                mqSheet.autoSizeColumn(0);
                mqSheet.autoSizeColumn(1);
            });
            mstSheet.autoSizeColumn(0);
        });

        return serializeSpreadsheetDocument(spreadsheetDocument);
    }

    /**
     * Legt im Sheet eine Zeile für die Header-Metainformationen an und schreibt in
     * ersten beiden Zellen der Zeile die Headerwerte.
     *
     * @param sheet aktuelles Sheet einer Messstelle in der Auswertungsdatei
     */
    private void addMetaHeaderToSheet(final Sheet sheet) {
        final Row metaheader = sheet.createRow(0);
        addStringToCell(metaheader.createCell(0, CellType.STRING), "ausgewählter Wochentag");
        addStringToCell(metaheader.createCell(1, CellType.STRING),
                "ausgewählter MQ (Merkmale \"MQ-ID - Richtung - Standort MQ\") bzw. \"Alle Messquerschnitte\"");
    }

    /**
     * Legt im Sheet eine Zeile für die Daten der Metainformationen an und schreibt in
     * ersten beiden Zellen der Zeile die Daten.
     *
     * @param sheet aktuelles Sheet einer Messstelle in der Auswertungsdatei
     * @param options Verwendetet Optionen bei der Auswertung
     */
    protected void addMetaDataToSheet(final Sheet sheet, final MessstelleAuswertungOptionsDTO options) {
        final Row metaData = sheet.createRow(1);
        addStringToCell(metaData.createCell(0, CellType.STRING), options.getTagesTyp().getBeschreibung());

        if (CollectionUtils.isNotEmpty(options.getMessstelleAuswertungIds())) {
            if (options.getMessstelleAuswertungIds().size() > 1) {
                addStringToCell(metaData.createCell(1, CellType.STRING), "Alle Messquerschnitte");
            } else {
                final Optional<MessstelleAuswertungIdDTO> first = options.getMessstelleAuswertungIds().stream().findFirst();
                if (first.isPresent()) {
                    final MessstelleAuswertungIdDTO messstelleAuswertungIdDTO = first.get();
                    final Messstelle messstelleByMstId = messstelleService.getMessstelleByMstId(messstelleAuswertungIdDTO.getMstId());
                    final List<String> cellValue = new ArrayList<>();
                    messstelleByMstId.getMessquerschnitte().forEach(messquerschnitt -> {
                        if (messstelleAuswertungIdDTO.getMqIds().contains(messquerschnitt.getMqId())) {
                            cellValue.add(String.format("%s - %s - %s", messquerschnitt.getMqId(), messquerschnitt.getFahrtrichtung(),
                                    messquerschnitt.getStandort()));
                        }
                    });
                    addStringToCell(metaData.createCell(1, CellType.STRING), String.join(", ", cellValue));
                }
            }
        }
        // Leere Zeile einfuegen
        sheet.createRow(2);
    }

    /**
     * Legt im Sheet eine Zeile für die Header-Metainformationen an und schreibt in
     * ersten beiden Zellen der Zeile die Headerwerte.
     *
     * @param sheet aktuelles Sheet eines Messquerschnitts
     */
    private void addMetaHeaderToMessquerschnittSheet(final Sheet sheet) {
        final Row metaheader = sheet.createRow(0);
        addStringToCell(metaheader.createCell(0, CellType.STRING), "ausgewählter Wochentag");
        addStringToCell(metaheader.createCell(1, CellType.STRING), "Messstelle");
        addStringToCell(metaheader.createCell(2, CellType.STRING), "Messquerschnitt (Merkmale \"MQ-ID - Richtung - Standort MQ\")");
    }

    /**
     * Legt im Sheet eine Zeile für die Daten der Metainformationen an und schreibt in
     * ersten beiden Zellen der Zeile die Daten.
     *
     * @param sheet aktuelles Sheet eines Messquerschnitts in der Auswertungsdatei
     * @param options Verwendetet Optionen bei der Auswertung
     * @param mstId ausgewertete Messstelle
     * @param mqId ausgewerteter Messquerschnitt
     */
    private void addMetaDataToMessquerschnittSheet(final Sheet sheet, final MessstelleAuswertungOptionsDTO options, final String mstId, final String mqId) {
        final Row metaData = sheet.createRow(1);
        addStringToCell(metaData.createCell(0, CellType.STRING), options.getTagesTyp().getBeschreibung());
        addStringToCell(metaData.createCell(1, CellType.STRING), mstId);
        addStringToCell(metaData.createCell(2, CellType.STRING), mqId);
        // Leere Zeile einfuegen
        sheet.createRow(2);
    }

    /**
     * Legt im Sheet eine Zeile für die Header-Informationen an und legt pro
     * gewählte Fahrzeugoption eine Zelle mit dem Header in der Reihe an.
     *
     * @param sheet aktuelles Sheet
     * @param fahrzeugOptions bei der Auswertung gewählte Fahrzeugoptionen
     */
    private void addDataHeaderToSheet(final Sheet sheet, final FahrzeugOptionsDTO fahrzeugOptions) {
        final Row header = sheet.createRow(3);

        int headerCellIndex = 0;
        addStringToCell(header.createCell(headerCellIndex, CellType.STRING), "Zeitintervall");
        headerCellIndex++;

        if (fahrzeugOptions.isKraftfahrzeugverkehr()) {
            addStringToCell(header.createCell(headerCellIndex, CellType.STRING), "KFZ");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isSchwerverkehr()) {
            addStringToCell(header.createCell(headerCellIndex, CellType.STRING), "SV");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isGueterverkehr()) {
            addStringToCell(header.createCell(headerCellIndex, CellType.STRING), "GV");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isSchwerverkehrsanteilProzent()) {
            addStringToCell(header.createCell(headerCellIndex, CellType.STRING), "SV%");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isGueterverkehrsanteilProzent()) {
            addStringToCell(header.createCell(headerCellIndex, CellType.STRING), "GV%");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isRadverkehr()) {
            addStringToCell(header.createCell(headerCellIndex, CellType.STRING), "RAD");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isFussverkehr()) {
            addStringToCell(header.createCell(headerCellIndex, CellType.STRING), "FUß");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isLastkraftwagen()) {
            addStringToCell(header.createCell(headerCellIndex, CellType.STRING), "LKW");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isLieferwagen()) {
            addStringToCell(header.createCell(headerCellIndex, CellType.STRING), "LFW");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isLastzuege()) {
            addStringToCell(header.createCell(headerCellIndex, CellType.STRING), "LZ");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isBusse()) {
            addStringToCell(header.createCell(headerCellIndex, CellType.STRING), "BUS");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isKraftraeder()) {
            addStringToCell(header.createCell(headerCellIndex, CellType.STRING), "KRAD");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isPersonenkraftwagen()) {
            addStringToCell(header.createCell(headerCellIndex, CellType.STRING), "PKW");
        }
    }

    /**
     * Legt im Sheet pro Zeitraum eine Zeile für die Daten an und legt pro
     * gewählte Fahrzeugoption eine Zelle mit den Daten in dieser Reihe an.
     *
     * @param sheet aktuelles Sheet
     * @param auswertung Liste mit den Daten pro Zeitraum
     * @param fahrzeugOptions bei der Auswertung gewählte Fahrzeugoptionen
     */
    private void addDataToSheet(
            final Sheet sheet,
            final List<Auswertung> auswertung,
            final FahrzeugOptionsDTO fahrzeugOptions) {
        final AtomicInteger rowIndex = new AtomicInteger(4);
        final AtomicReference<Row> row = new AtomicReference<>();

        auswertung.sort(Comparator.comparing(o -> o.getZeitraum().getStart()));

        auswertung.forEach(entry -> {
            row.set(sheet.createRow(rowIndex.get()));

            int cellIndex = 0;
            if (AuswertungsZeitraum.JAHRE.equals(entry.getZeitraum().getAuswertungsZeitraum())) {
                addStringToCell(row.get().createCell(cellIndex, CellType.STRING), String.valueOf(entry.getZeitraum().getStart().getYear()));
            } else {
                addStringToCell(row.get().createCell(cellIndex, CellType.STRING),
                        String.format("%s / %s", entry.getZeitraum().getAuswertungsZeitraum().getText(), entry.getZeitraum().getStart().getYear()));
            }
            cellIndex++;

            if (fahrzeugOptions.isKraftfahrzeugverkehr()) {
                addBigDecimalToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getSummeKraftfahrzeugverkehr());
                cellIndex++;
            }
            if (fahrzeugOptions.isSchwerverkehr()) {
                addBigDecimalToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getSummeSchwerverkehr());
                cellIndex++;
            }
            if (fahrzeugOptions.isGueterverkehr()) {
                addBigDecimalToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getSummeGueterverkehr());
                cellIndex++;
            }
            if (fahrzeugOptions.isSchwerverkehrsanteilProzent()) {
                addBigDecimalToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getProzentSchwerverkehr());
                cellIndex++;
            }
            if (fahrzeugOptions.isGueterverkehrsanteilProzent()) {
                addBigDecimalToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getProzentGueterverkehr());
                cellIndex++;
            }
            if (fahrzeugOptions.isRadverkehr()) {
                addBigDecimalToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getAnzahlRad());
                cellIndex++;
            }
            if (fahrzeugOptions.isFussverkehr()) {
                // Wird aktuell noch nicht erfasst
                addStringToCell(row.get().createCell(cellIndex, CellType.NUMERIC), StringUtils.EMPTY);
                cellIndex++;
            }
            if (fahrzeugOptions.isLastkraftwagen()) {
                addBigDecimalToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getAnzahlLkw());
                cellIndex++;
            }
            if (fahrzeugOptions.isLieferwagen()) {
                addBigDecimalToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getAnzahlLfw());
                cellIndex++;
            }
            if (fahrzeugOptions.isLastzuege()) {
                addBigDecimalToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getSummeLastzug());
                cellIndex++;
            }
            if (fahrzeugOptions.isBusse()) {
                addBigDecimalToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getAnzahlBus());
                cellIndex++;
            }
            if (fahrzeugOptions.isKraftraeder()) {
                addBigDecimalToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getAnzahlKrad());
                cellIndex++;
            }
            if (fahrzeugOptions.isPersonenkraftwagen()) {
                addBigDecimalToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getSummeAllePkw());
            }
            rowIndex.getAndIncrement();
        });
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
     * Erzeugt aus dem WorkBook ein byte[]
     *
     * @param spreadsheetDocument Workbook zum Serialisieren
     * @return Workbook als byte[]
     * @throws IOException kann beim Erstellen geworfen werden. Behandlung erfolgt im Controller.
     */
    protected byte[] serializeSpreadsheetDocument(final Workbook spreadsheetDocument) throws IOException {
        try (final var baos = new ByteArrayOutputStream()) {
            spreadsheetDocument.write(baos);
            return baos.toByteArray();
        }
    }
}
