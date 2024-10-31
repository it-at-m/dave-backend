/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungResponse;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDTO;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatDto;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatResponseDto;
import de.muenchen.dave.services.messstelle.MessstelleService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
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
public class ExcelService {

    private final MessstelleService messstelleService;

    private Sheet createSheet(final Workbook workbook, final String sheetName) {
        final Sheet sheet = workbook.createSheet(sheetName);
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 4000);
        return sheet;
    }

    private void createMetaHeader(final Sheet sheet) {
        final Row metaheader = sheet.createRow(0);
        Cell metaHeaderCell = metaheader.createCell(0);
        metaHeaderCell.setCellValue("ausgewählter Wochentag");
        metaHeaderCell = metaheader.createCell(1);
        metaHeaderCell.setCellValue("ausgewählter MQ (Merkmale \"MQ-ID - Richtung - Standort MQ\") bzw. \"Alle Messquerschnitte\"");
    }

    private void createMetaData(final Sheet sheet, final MessstelleAuswertungOptionsDTO options) {
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

    private void createData(final Sheet sheet, final CellStyle style, final List<AuswertungResponse> tagesaggregatResponseDtos,
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
            cell.setCellValue(entry.getMeanOfAggregatesForAllMqId().getMqId());
            cell.setCellStyle(style);

            int cellIndex = 2;
            if (fahrzeugOptions.isKraftfahrzeugverkehr()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(
                        StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOfAggregatesForAllMqId().getSummeKraftfahrzeugverkehr()), StringUtils.EMPTY));
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isSchwerverkehr()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(String.valueOf(entry.getMeanOfAggregatesForAllMqId().getSummeSchwerverkehr()));
                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOfAggregatesForAllMqId().getSummeSchwerverkehr()), StringUtils.EMPTY));
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isGueterverkehr()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOfAggregatesForAllMqId().getSummeGueterverkehr()), StringUtils.EMPTY));
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isSchwerverkehrsanteilProzent()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(
                        StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOfAggregatesForAllMqId().getProzentSchwerverkehr()), StringUtils.EMPTY));
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isGueterverkehrsanteilProzent()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(
                        StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOfAggregatesForAllMqId().getProzentGueterverkehr()), StringUtils.EMPTY));
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isRadverkehr()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOfAggregatesForAllMqId().getAnzahlRad()), StringUtils.EMPTY));
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
                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOfAggregatesForAllMqId().getAnzahlLkw()), StringUtils.EMPTY));
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isLieferwagen()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOfAggregatesForAllMqId().getAnzahlLfw()), StringUtils.EMPTY));
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isLastzuege()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOfAggregatesForAllMqId().getSummeLastzug()), StringUtils.EMPTY));
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isBusse()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOfAggregatesForAllMqId().getAnzahlBus()), StringUtils.EMPTY));
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isKraftraeder()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOfAggregatesForAllMqId().getAnzahlKrad()), StringUtils.EMPTY));
                cell.setCellStyle(style);
                cellIndex++;
            }
            if (fahrzeugOptions.isPersonenkraftwagen()) {
                cell = row.get().createCell(cellIndex);
                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getMeanOfAggregatesForAllMqId().getSummeAllePkw()), StringUtils.EMPTY));
                cell.setCellStyle(style);
            }

            rowIndex.getAndIncrement();
        });
    }

    private void createDataHeader(final Sheet sheet, final FahrzeugOptionsDTO fahrzeugOptions) {
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

    public byte[] createFile(final Map<Integer, List<AuswertungResponse>> auswertungen, final MessstelleAuswertungOptionsDTO options) throws IOException {
        //        if (CollectionUtils.isEmpty(auswertungen)) {
        //            return createFileTest();
        //        }
        final Workbook workbook = new XSSFWorkbook();

        final CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setWrapText(true);

        auswertungen.forEach((mstId, tagesaggregatResponseDtos) -> {

            final Sheet sheet = workbook.createSheet(String.format("Messstelle %s", mstId));

            createMetaHeader(sheet);
            createMetaData(sheet, options);
            createDataHeader(sheet, options.getFahrzeuge());
            createData(sheet, dataCellStyle, tagesaggregatResponseDtos, options.getFahrzeuge());

        });

        //        final Sheet sheet = workbook.createSheet(String.format("Messstelle %s", options.getMstIds().stream().findFirst().get()));
        //        sheet.setColumnWidth(0, 6000);
        //        sheet.setColumnWidth(1, 4000);
        //
        //        final Row metaheader = sheet.createRow(0);
        //        Cell metaHeaderCell = metaheader.createCell(0);
        //        metaHeaderCell.setCellValue("ausgewählter Wochentag");
        //        metaHeaderCell = metaheader.createCell(1);
        //        metaHeaderCell.setCellValue("ausgewählter MQ (Merkmale \"MQ-ID - Richtung - Standort MQ\") bzw. \"Alle Messquerschnitte\"");
        //
        //        final Row metaData = sheet.createRow(1);
        //        Cell metaDataCell = metaData.createCell(0);
        //        metaDataCell.setCellValue(options.getTagesTyp().getBeschreibung());
        //        metaDataCell = metaData.createCell(1);
        //
        //        if (CollectionUtils.isNotEmpty(options.getMqIds())) {
        //            metaDataCell.setCellValue("Alle Messquerschnitte");
        //        } else {
        //            // TODO MQ's laden wg Richtung und Standort
        //            metaDataCell.setCellValue(String.join(", ", options.getMqIds()));
        //        }
        //
        //        // Leere Zeile einfuegen
        //        sheet.createRow(2);

        //        final Row header = sheet.createRow(3);
        //
        //        Cell headerCell = header.createCell(0);
        //        headerCell.setCellValue("Zeitintervall");
        //
        //        headerCell = header.createCell(1);
        //        headerCell.setCellValue("MQ-ID");
        //
        //        final FahrzeugOptionsDTO fahrzeugOptions = options.getFahrzeuge();
        //        int headerCellIndex = 2;
        //        if (fahrzeugOptions.isKraftfahrzeugverkehr()) {
        //            headerCell = header.createCell(headerCellIndex);
        //            headerCell.setCellValue("KFZ");
        //            headerCellIndex++;
        //        }
        //        if (fahrzeugOptions.isSchwerverkehr()) {
        //            headerCell = header.createCell(headerCellIndex);
        //            headerCell.setCellValue("SV");
        //            headerCellIndex++;
        //        }
        //        if (fahrzeugOptions.isGueterverkehr()) {
        //            headerCell = header.createCell(headerCellIndex);
        //            headerCell.setCellValue("GV");
        //            headerCellIndex++;
        //        }
        //        if (fahrzeugOptions.isSchwerverkehrsanteilProzent()) {
        //            headerCell = header.createCell(headerCellIndex);
        //            headerCell.setCellValue("SV%");
        //            headerCellIndex++;
        //        }
        //        if (fahrzeugOptions.isGueterverkehrsanteilProzent()) {
        //            headerCell = header.createCell(headerCellIndex);
        //            headerCell.setCellValue("GV%");
        //            headerCellIndex++;
        //        }
        //        if (fahrzeugOptions.isRadverkehr()) {
        //            headerCell = header.createCell(headerCellIndex);
        //            headerCell.setCellValue("RAD");
        //            headerCellIndex++;
        //        }
        //        if (fahrzeugOptions.isFussverkehr()) {
        //            headerCell = header.createCell(headerCellIndex);
        //            headerCell.setCellValue("FUß");
        //            headerCellIndex++;
        //        }
        //        if (fahrzeugOptions.isLastkraftwagen()) {
        //            headerCell = header.createCell(headerCellIndex);
        //            headerCell.setCellValue("LKW");
        //            headerCellIndex++;
        //        }
        //        if (fahrzeugOptions.isLieferwagen()) {
        //            headerCell = header.createCell(headerCellIndex);
        //            headerCell.setCellValue("LFW");
        //            headerCellIndex++;
        //        }
        //        if (fahrzeugOptions.isLastzuege()) {
        //            headerCell = header.createCell(headerCellIndex);
        //            headerCell.setCellValue("LZ");
        //            headerCellIndex++;
        //        }
        //        if (fahrzeugOptions.isBusse()) {
        //            headerCell = header.createCell(headerCellIndex);
        //            headerCell.setCellValue("BUS");
        //            headerCellIndex++;
        //        }
        //        if (fahrzeugOptions.isKraftraeder()) {
        //            headerCell = header.createCell(headerCellIndex);
        //            headerCell.setCellValue("KRAD");
        //            headerCellIndex++;
        //        }
        //        if (fahrzeugOptions.isPersonenkraftwagen()) {
        //            headerCell = header.createCell(headerCellIndex);
        //            headerCell.setCellValue("PKW");
        //        }
        //
        //        CellStyle style = workbook.createCellStyle();
        //        style.setWrapText(true);
        //
        //        AtomicInteger rowIndex = new AtomicInteger(4);
        //        AtomicReference<Row> row = new AtomicReference<>();
        //        auswertung.forEach(entry -> {
        //            row.set(sheet.createRow(rowIndex.get()));
        //
        //            Cell cell = row.get().createCell(0);
        //
        //            if (AuswertungsZeitraum.JAHRE.equals(entry.getZeitraum().getAuswertungsZeitraum())) {
        //                cell.setCellValue(String.valueOf(entry.getZeitraum().getStart().getYear()));
        //            } else {
        //                cell.setCellValue(String.format("%s / %s", entry.getZeitraum().getAuswertungsZeitraum().getText(), entry.getZeitraum().getStart().getYear()));
        //            }
        //            cell.setCellStyle(style);
        //
        //            cell = row.get().createCell(1);
        //            cell.setCellValue(entry.getMqId());
        //            cell.setCellStyle(style);
        //
        //            int cellIndex = 2;
        //            if (fahrzeugOptions.isKraftfahrzeugverkehr()) {
        //                cell = row.get().createCell(cellIndex);
        //                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getSummeKraftfahrzeugverkehr()), StringUtils.EMPTY));
        //                cell.setCellStyle(style);
        //                cellIndex++;
        //            }
        //            if (fahrzeugOptions.isSchwerverkehr()) {
        //                cell = row.get().createCell(cellIndex);
        //                cell.setCellValue(String.valueOf(entry.getSummeSchwerverkehr()));
        //                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getSummeSchwerverkehr()), StringUtils.EMPTY));
        //                cell.setCellStyle(style);
        //                cellIndex++;
        //            }
        //            if (fahrzeugOptions.isGueterverkehr()) {
        //                cell = row.get().createCell(cellIndex);
        //                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getSummeGueterverkehr()), StringUtils.EMPTY));
        //                cell.setCellStyle(style);
        //                cellIndex++;
        //            }
        //            if (fahrzeugOptions.isSchwerverkehrsanteilProzent()) {
        //                cell = row.get().createCell(cellIndex);
        //                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getProzentSchwerverkehr()), StringUtils.EMPTY));
        //                cell.setCellStyle(style);
        //                cellIndex++;
        //            }
        //            if (fahrzeugOptions.isGueterverkehrsanteilProzent()) {
        //                cell = row.get().createCell(cellIndex);
        //                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getProzentGueterverkehr()), StringUtils.EMPTY));
        //                cell.setCellStyle(style);
        //                cellIndex++;
        //            }
        //            if (fahrzeugOptions.isRadverkehr()) {
        //                cell = row.get().createCell(cellIndex);
        //                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getAnzahlRad()), StringUtils.EMPTY));
        //                cell.setCellStyle(style);
        //                cellIndex++;
        //            }
        //            if (fahrzeugOptions.isFussverkehr()) {
        //                cell = row.get().createCell(cellIndex);
        //                cell.setCellValue(StringUtils.EMPTY);
        //                cell.setCellStyle(style);
        //                cellIndex++;
        //            }
        //            if (fahrzeugOptions.isLastkraftwagen()) {
        //                cell = row.get().createCell(cellIndex);
        //                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getAnzahlLkw()), StringUtils.EMPTY));
        //                cell.setCellStyle(style);
        //                cellIndex++;
        //            }
        //            if (fahrzeugOptions.isLieferwagen()) {
        //                cell = row.get().createCell(cellIndex);
        //                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getAnzahlLfw()), StringUtils.EMPTY));
        //                cell.setCellStyle(style);
        //                cellIndex++;
        //            }
        //            if (fahrzeugOptions.isLastzuege()) {
        //                cell = row.get().createCell(cellIndex);
        //                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getSummeLastzug()), StringUtils.EMPTY));
        //                cell.setCellStyle(style);
        //                cellIndex++;
        //            }
        //            if (fahrzeugOptions.isBusse()) {
        //                cell = row.get().createCell(cellIndex);
        //                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getAnzahlBus()), StringUtils.EMPTY));
        //                cell.setCellStyle(style);
        //                cellIndex++;
        //            }
        //            if (fahrzeugOptions.isKraftraeder()) {
        //                cell = row.get().createCell(cellIndex);
        //                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getAnzahlKrad()), StringUtils.EMPTY));
        //                cell.setCellStyle(style);
        //                cellIndex++;
        //            }
        //            if (fahrzeugOptions.isPersonenkraftwagen()) {
        //                cell = row.get().createCell(cellIndex);
        //                cell.setCellValue(StringUtils.defaultIfEmpty(String.valueOf(entry.getSummeAllePkw()), StringUtils.EMPTY));
        //                cell.setCellStyle(style);
        //            }
        //
        //            rowIndex.getAndIncrement();
        //        });

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        final byte[] workbookAsByteArray = baos.toByteArray();
        workbook.close();

        return workbookAsByteArray;
    }

    public byte[] createFileTest() throws IOException {
        final Workbook workbook = new XSSFWorkbook();

        final Sheet sheet = workbook.createSheet("Persons");
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 4000);

        final Row header = sheet.createRow(0);

        final CellStyle headerStyle = workbook.createCellStyle();
        final XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 16);
        font.setBold(true);
        headerStyle.setFont(font);

        Cell headerCell = header.createCell(0);
        headerCell.setCellValue("Name");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(1);
        headerCell.setCellValue("Age");
        headerCell.setCellStyle(headerStyle);

        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);

        Row row = sheet.createRow(2);
        Cell cell = row.createCell(0);
        cell.setCellValue("John Smith");
        cell.setCellStyle(style);

        cell = row.createCell(1);
        cell.setCellValue(20);
        cell.setCellStyle(style);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        final byte[] workbookAsByteArray = baos.toByteArray();
        workbook.close();

        return workbookAsByteArray;
    }
}
