/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungMessquerschnitte;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDTO;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@AllArgsConstructor
@Service
@Slf4j
public class SpreadsheetService {

    public byte[] createFile(final Map<Integer, List<AuswertungMessquerschnitte>> auswertungenByMstId, final MessstelleAuswertungOptionsDTO options)
            throws IOException {
        final var spreadsheetDocument = new XSSFWorkbook();

        final var dataCellStyle = spreadsheetDocument.createCellStyle();
        dataCellStyle.setWrapText(true);

        // Füge Daten zum Document hinzu.
        MapUtils.emptyIfNull(auswertungenByMstId).forEach((mstId, tagesaggregatResponseDtos) -> {

            final Sheet sheet = spreadsheetDocument.createSheet(String.format("Messstelle %s", mstId));

            addMetaHeaderToSheet(sheet);
            addMetaDataToSheet(sheet, options);
            addDataHeaderToSheet(sheet, options.getFahrzeuge());
            addDataToSheet(
                    sheet,
                    dataCellStyle,
                    ListUtils.emptyIfNull(tagesaggregatResponseDtos),
                    options.getFahrzeuge());

        });

        return serializeSpreadsheetDocument(spreadsheetDocument);
    }

    private void addMetaHeaderToSheet(final Sheet sheet) {
        final Row metaheader = sheet.createRow(0);
        Cell metaHeaderCell = metaheader.createCell(0);
        metaHeaderCell.setCellValue("ausgewählter Wochentag");
        metaHeaderCell = metaheader.createCell(1);
        metaHeaderCell.setCellValue("ausgewählter MQ (Merkmale \"MQ-ID - Richtung - Standort MQ\") bzw. \"Alle Messquerschnitte\"");
    }

    private void addMetaDataToSheet(final Sheet sheet, final MessstelleAuswertungOptionsDTO options) {
        final Row metaData = sheet.createRow(1);
        Cell metaDataCell = metaData.createCell(0);
        metaDataCell.setCellValue(options.getTagesTyp().getBeschreibung());
        metaDataCell = metaData.createCell(1);

        if (CollectionUtils.isNotEmpty(options.getMqIds())) {
            metaDataCell.setCellValue("Alle Messquerschnitte");
        } else {
            // TODO MQ's laden wg Richtung und Standort
            metaDataCell.setCellValue(String.join(", ", options.getMqIds()));
        }

        // Leere Zeile einfuegen
        sheet.createRow(2);
    }

    private void addDataToSheet(
            final Sheet sheet,
            final CellStyle style,
            final List<AuswertungMessquerschnitte> tagesaggregatResponseDtos,
            final FahrzeugOptionsDTO fahrzeugOptions) {
        AtomicInteger rowIndex = new AtomicInteger(4);
        AtomicReference<Row> row = new AtomicReference<>();
        tagesaggregatResponseDtos.forEach(entry -> {
            row.set(sheet.createRow(rowIndex.get()));

            Cell cell = row.get().createCell(0);

            if (AuswertungsZeitraum.JAHRE.equals(entry.getZeitraum().getAuswertungsZeitraum())) {
                cell.setCellValue(String.valueOf(entry.getZeitraum().getStart().getYear()));
            } else {
                cell.setCellValue(String.format("%s / %s", entry.getZeitraum().getAuswertungsZeitraum().getText(), entry.getZeitraum().getStart().getYear()));
            }
            cell.setCellStyle(style);

            cell = row.get().createCell(1);
            cell.setCellValue(entry.getMeanOverAllAggregatesOfAllMqId().getMqId());
            cell.setCellStyle(style);

            int cellIndex = 2;
            if (fahrzeugOptions.isKraftfahrzeugverkehr()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(
                        StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOverAllAggregatesOfAllMqId().getSummeKraftfahrzeugverkehr()), StringUtils.EMPTY));
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isSchwerverkehr()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(String.valueOf(entry.getMeanOverAllAggregatesOfAllMqId().getSummeSchwerverkehr()));
                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOverAllAggregatesOfAllMqId().getSummeSchwerverkehr()), StringUtils.EMPTY));
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isGueterverkehr()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOverAllAggregatesOfAllMqId().getSummeGueterverkehr()), StringUtils.EMPTY));
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isSchwerverkehrsanteilProzent()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(
                        StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOverAllAggregatesOfAllMqId().getProzentSchwerverkehr()), StringUtils.EMPTY));
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isGueterverkehrsanteilProzent()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(
                        StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOverAllAggregatesOfAllMqId().getProzentGueterverkehr()), StringUtils.EMPTY));
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isRadverkehr()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOverAllAggregatesOfAllMqId().getAnzahlRad()), StringUtils.EMPTY));
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isFussverkehr()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(StringUtils.EMPTY);
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isLastkraftwagen()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOverAllAggregatesOfAllMqId().getAnzahlLkw()), StringUtils.EMPTY));
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isLieferwagen()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOverAllAggregatesOfAllMqId().getAnzahlLfw()), StringUtils.EMPTY));
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isLastzuege()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOverAllAggregatesOfAllMqId().getSummeLastzug()), StringUtils.EMPTY));
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isBusse()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOverAllAggregatesOfAllMqId().getAnzahlBus()), StringUtils.EMPTY));
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isKraftraeder()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOverAllAggregatesOfAllMqId().getAnzahlKrad()), StringUtils.EMPTY));
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isPersonenkraftwagen()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOverAllAggregatesOfAllMqId().getSummeAllePkw()), StringUtils.EMPTY));
                cell.setCellStyle(style);
            }

            rowIndex.getAndIncrement();
        });
    }

    private void addDataHeaderToSheet(final Sheet sheet, final FahrzeugOptionsDTO fahrzeugOptions) {
        final Row header = sheet.createRow(3);

        Cell headerCell = header.createCell(0);
        headerCell.setCellValue("Zeitintervall");

        headerCell = header.createCell(1);
        headerCell.setCellValue("MQ-ID");

        int headerCellIndex = 2;
        if (fahrzeugOptions.isKraftfahrzeugverkehr()) {
            headerCell = header.createCell(headerCellIndex);
            headerCell.setCellValue("KFZ");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isSchwerverkehr()) {
            headerCell = header.createCell(headerCellIndex);
            headerCell.setCellValue("SV");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isGueterverkehr()) {
            headerCell = header.createCell(headerCellIndex);
            headerCell.setCellValue("GV");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isSchwerverkehrsanteilProzent()) {
            headerCell = header.createCell(headerCellIndex);
            headerCell.setCellValue("SV%");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isGueterverkehrsanteilProzent()) {
            headerCell = header.createCell(headerCellIndex);
            headerCell.setCellValue("GV%");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isRadverkehr()) {
            headerCell = header.createCell(headerCellIndex);
            headerCell.setCellValue("RAD");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isFussverkehr()) {
            headerCell = header.createCell(headerCellIndex);
            headerCell.setCellValue("FUß");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isLastkraftwagen()) {
            headerCell = header.createCell(headerCellIndex);
            headerCell.setCellValue("LKW");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isLieferwagen()) {
            headerCell = header.createCell(headerCellIndex);
            headerCell.setCellValue("LFW");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isLastzuege()) {
            headerCell = header.createCell(headerCellIndex);
            headerCell.setCellValue("LZ");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isBusse()) {
            headerCell = header.createCell(headerCellIndex);
            headerCell.setCellValue("BUS");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isKraftraeder()) {
            headerCell = header.createCell(headerCellIndex);
            headerCell.setCellValue("KRAD");
            headerCellIndex++;
        }
        if (fahrzeugOptions.isPersonenkraftwagen()) {
            headerCell = header.createCell(headerCellIndex);
            headerCell.setCellValue("PKW");
        }
    }

    protected byte[] serializeSpreadsheetDocument(final Workbook spreadsheetDocument) throws IOException {
        try (final var baos = new ByteArrayOutputStream()) {
            spreadsheetDocument.write(baos);
            return baos.toByteArray();
        } catch (final IOException exception) {
            throw exception;
        }
    }
}
