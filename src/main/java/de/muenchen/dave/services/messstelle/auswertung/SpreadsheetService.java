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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
@Service
@Slf4j
public class SpreadsheetService {

    private final MessstelleService messstelleService;

    public byte[] createFile(final List<AuswertungProMessstelle> auswertungenProMessstelle, final MessstelleAuswertungOptionsDTO options)
            throws IOException {
        final var spreadsheetDocument = new XSSFWorkbook();

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

    private void addMetaHeaderToSheet(final Sheet sheet) {
        final Row metaheader = sheet.createRow(0);
        addDataToCell(metaheader.createCell(0, CellType.STRING), "ausgewählter Wochentag");
        addDataToCell(metaheader.createCell(1, CellType.STRING),
                "ausgewählter MQ (Merkmale \"MQ-ID - Richtung - Standort MQ\") bzw. \"Alle Messquerschnitte\"");
    }

    private void addMetaDataToSheet(final Sheet sheet, final MessstelleAuswertungOptionsDTO options) {
        final Row metaData = sheet.createRow(1);
        addDataToCell(metaData.createCell(0, CellType.STRING), options.getTagesTyp().getBeschreibung());

        if (CollectionUtils.isNotEmpty(options.getMessstelleAuswertungIds())) {
            if (options.getMessstelleAuswertungIds().size() > 1) {
                addDataToCell(metaData.createCell(1, CellType.STRING), "Alle Messquerschnitte");
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
                    addDataToCell(metaData.createCell(1, CellType.STRING), String.join(", ", cellValue));
                }
            }
        }
        // Leere Zeile einfuegen
        sheet.createRow(2);
    }

    private void addMetaHeaderToMessquerschnittSheet(final Sheet sheet) {
        final Row metaheader = sheet.createRow(0);
        addDataToCell(metaheader.createCell(0, CellType.STRING), "ausgewählter Wochentag");
        addDataToCell(metaheader.createCell(1, CellType.STRING), "Messstelle");
        addDataToCell(metaheader.createCell(2, CellType.STRING), "Messquerschnitt (Merkmale \"MQ-ID - Richtung - Standort MQ\")");
    }

    private void addMetaDataToMessquerschnittSheet(final Sheet sheet, final MessstelleAuswertungOptionsDTO options, final String mstId, final String mqId) {
        final Row metaData = sheet.createRow(1);
        addDataToCell(metaData.createCell(0, CellType.STRING), options.getTagesTyp().getBeschreibung());
        addDataToCell(metaData.createCell(1, CellType.STRING), mstId);
        addDataToCell(metaData.createCell(2, CellType.STRING), mqId);
        // Leere Zeile einfuegen
        sheet.createRow(2);
    }

    private void addDataHeaderToSheet(final Sheet sheet, final FahrzeugOptionsDTO fahrzeugOptions) {
        final Row header = sheet.createRow(3);

        int headerCellIndex = 0;
        addDataToCell(header.createCell(headerCellIndex, CellType.STRING), "Zeitintervall");
        headerCellIndex++;

        if (fahrzeugOptions.isKraftfahrzeugverkehr()) {
            addDataToCell(header.createCell(headerCellIndex, CellType.STRING), "KFZ");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isSchwerverkehr()) {
            addDataToCell(header.createCell(headerCellIndex, CellType.STRING), "SV");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isGueterverkehr()) {
            addDataToCell(header.createCell(headerCellIndex, CellType.STRING), "GV");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isSchwerverkehrsanteilProzent()) {
            addDataToCell(header.createCell(headerCellIndex, CellType.STRING), "SV%");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isGueterverkehrsanteilProzent()) {
            addDataToCell(header.createCell(headerCellIndex, CellType.STRING), "GV%");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isRadverkehr()) {
            addDataToCell(header.createCell(headerCellIndex, CellType.STRING), "RAD");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isFussverkehr()) {
            addDataToCell(header.createCell(headerCellIndex, CellType.STRING), "FUß");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isLastkraftwagen()) {
            addDataToCell(header.createCell(headerCellIndex, CellType.STRING), "LKW");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isLieferwagen()) {
            addDataToCell(header.createCell(headerCellIndex, CellType.STRING), "LFW");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isLastzuege()) {
            addDataToCell(header.createCell(headerCellIndex, CellType.STRING), "LZ");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isBusse()) {
            addDataToCell(header.createCell(headerCellIndex, CellType.STRING), "BUS");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isKraftraeder()) {
            addDataToCell(header.createCell(headerCellIndex, CellType.STRING), "KRAD");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isPersonenkraftwagen()) {
            addDataToCell(header.createCell(headerCellIndex, CellType.STRING), "PKW");
        }
    }

    private void addDataToSheet(
            final Sheet sheet,
            final List<Auswertung> auswertung,
            final FahrzeugOptionsDTO fahrzeugOptions) {
        final AtomicInteger rowIndex = new AtomicInteger(4);
        final AtomicReference<Row> row = new AtomicReference<>();
        auswertung.forEach(entry -> {
            row.set(sheet.createRow(rowIndex.get()));

            int cellIndex = 0;
            if (AuswertungsZeitraum.JAHRE.equals(entry.getZeitraum().getAuswertungsZeitraum())) {
                addDataToCell(row.get().createCell(cellIndex, CellType.STRING), String.valueOf(entry.getZeitraum().getStart().getYear()));
            } else {
                addDataToCell(row.get().createCell(cellIndex, CellType.STRING),
                        String.format("%s / %s", entry.getZeitraum().getAuswertungsZeitraum().getText(), entry.getZeitraum().getStart().getYear()));
            }
            cellIndex++;

            if (fahrzeugOptions.isKraftfahrzeugverkehr()) {
                addDataToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getSummeKraftfahrzeugverkehr());
                cellIndex++;
            }
            if (fahrzeugOptions.isSchwerverkehr()) {
                addDataToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getSummeSchwerverkehr());
                cellIndex++;
            }
            if (fahrzeugOptions.isGueterverkehr()) {
                addDataToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getSummeGueterverkehr());
                cellIndex++;
            }
            if (fahrzeugOptions.isSchwerverkehrsanteilProzent()) {
                addDataToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getProzentSchwerverkehr());
                cellIndex++;
            }
            if (fahrzeugOptions.isGueterverkehrsanteilProzent()) {
                addDataToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getProzentGueterverkehr());
                cellIndex++;
            }
            if (fahrzeugOptions.isRadverkehr()) {
                addDataToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getAnzahlRad());
                cellIndex++;
            }
            if (fahrzeugOptions.isFussverkehr()) {
                // Wird aktuell noch nicht erfasst
                addDataToCell(row.get().createCell(cellIndex, CellType.NUMERIC), StringUtils.EMPTY);
                cellIndex++;
            }
            if (fahrzeugOptions.isLastkraftwagen()) {
                addDataToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getAnzahlLkw());
                cellIndex++;
            }
            if (fahrzeugOptions.isLieferwagen()) {
                addDataToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getAnzahlLfw());
                cellIndex++;
            }
            if (fahrzeugOptions.isLastzuege()) {
                addDataToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getSummeLastzug());
                cellIndex++;
            }
            if (fahrzeugOptions.isBusse()) {
                addDataToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getAnzahlBus());
                cellIndex++;
            }
            if (fahrzeugOptions.isKraftraeder()) {
                addDataToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getAnzahlKrad());
                cellIndex++;
            }
            if (fahrzeugOptions.isPersonenkraftwagen()) {
                addDataToCell(row.get().createCell(cellIndex, CellType.NUMERIC), entry.getDaten().getSummeAllePkw());
            }
            rowIndex.getAndIncrement();
        });
    }

    protected void addDataToCell(final Cell cell, final BigDecimal data) {
        if (ObjectUtils.isNotEmpty(data)) {
            cell.setCellValue(data.doubleValue());
        } else {
            addDataToCell(cell, StringUtils.EMPTY);
        }
    }

    protected void addDataToCell(final Cell cell, final String data) {
        if (ObjectUtils.isNotEmpty(data)) {
            cell.setCellValue(data);
        } else {
            cell.setCellValue(StringUtils.EMPTY);
        }
    }

    protected byte[] serializeSpreadsheetDocument(final Workbook spreadsheetDocument) throws IOException {
        try (final var baos = new ByteArrayOutputStream()) {
            spreadsheetDocument.write(baos);
            return baos.toByteArray();
        } catch (final IOException exception) {
            // TODO Fehlerhandling einbauen
            throw exception;
        }
    }
}
