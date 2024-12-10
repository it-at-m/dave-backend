package de.muenchen.dave.services.pdfgenerator;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBaseDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBigDecimalDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryIntegerDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungIdDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDTO;
import de.muenchen.dave.domain.elasticsearch.MessquerschnittRandomFactory;
import de.muenchen.dave.domain.elasticsearch.MessstelleRandomFactory;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.domain.pdf.components.MessstelleninformationenPdfComponent;
import de.muenchen.dave.domain.pdf.components.ZaehlstelleninformationenPdfComponent;
import de.muenchen.dave.domain.pdf.components.ZusatzinformationenPdfComponent;
import de.muenchen.dave.domain.pdf.helper.GesamtauswertungTable;
import de.muenchen.dave.domain.pdf.helper.GesamtauswertungTableColumn;
import de.muenchen.dave.domain.pdf.helper.GesamtauswertungTableHeader;
import de.muenchen.dave.domain.pdf.helper.GesamtauswertungTableRow;
import de.muenchen.dave.domain.pdf.templates.BasicPdf;
import de.muenchen.dave.domain.pdf.templates.messstelle.BasicMessstellePdf;
import de.muenchen.dave.spring.services.pdfgenerator.FillPdfBeanServiceSpringTest;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class FillPdfBeanServiceTest {

    @Test
    public void fillBasicPdf() {
        final BasicPdf basicPdf = new BasicPdf();
        final Zaehlung zaehlung = FillPdfBeanServiceSpringTest.getZaehlung();
        final Zaehlstelle zaehlstelle = FillPdfBeanServiceSpringTest.getZaehlstelle(zaehlung);
        final String kreuzungsname = "Kreuzungsname";
        final String department = "TestOU";

        FillPdfBeanService.fillBasicPdf(basicPdf, zaehlung, kreuzungsname, zaehlstelle, department);

        assertThat(basicPdf.getFooterOrganisationseinheit(), is("TestOU"));
        assertThat(basicPdf.getFooterDate(), is(LocalDate.now().format(FillPdfBeanService.DDMMYYYY)));
    }

    @Test
    public void fillZaehlstelleninformationen() {
        final ZaehlstelleninformationenPdfComponent zaehlstelleninformationen = new ZaehlstelleninformationenPdfComponent();
        final String kreuzungsname = "Kreuzungsname";
        final Zaehlung zaehlung = FillPdfBeanServiceSpringTest.getZaehlung();

        FillPdfBeanService.fillZaehlstelleninformationen(zaehlstelleninformationen, kreuzungsname, zaehlung);
        assertThat(zaehlstelleninformationen.getProjektname(), is("VZ Testinger"));
        assertThat(zaehlstelleninformationen.getZaehldatum(), is("04.11.2020"));
        assertThat(zaehlstelleninformationen.getZaehldauer(), is("Kurzzeiterhebung (6 bis 10 Uhr; 15 bis 19 Uhr)"));
        assertThat(zaehlstelleninformationen.getKreuzungsname(), is("Kreuzungsname"));
        assertThat(zaehlstelleninformationen.getWetter(), is("Regnerisch (dauerhaft)"));
        assertThat(zaehlstelleninformationen.getZaehlsituation(), is("Situation normal"));
        assertThat(zaehlstelleninformationen.getZaehlsituationErweitert(), is("Alles in bester Ordnung"));
    }

    @Test
    public void fillZusatzinformationen() {
        final ZusatzinformationenPdfComponent zusatzinformationenPdfComponent = new ZusatzinformationenPdfComponent();
        final Zaehlung zaehlung = FillPdfBeanServiceSpringTest.getZaehlung();
        final Zaehlstelle zaehlstelle = FillPdfBeanServiceSpringTest.getZaehlstelle(zaehlung);

        FillPdfBeanService.fillZusatzinformationen(zusatzinformationenPdfComponent, zaehlstelle, zaehlung);
        assertThat(zusatzinformationenPdfComponent.isIstKommentarVorhanden(), is(false));
        assertThat(zusatzinformationenPdfComponent.isIstKommentarVorhandenZaehlstelle(), is(false));
        assertThat(zusatzinformationenPdfComponent.isIstKommentarVorhandenZaehlung(), is(false));
        assertThat(zusatzinformationenPdfComponent.getKommentarZaehlung(), is(""));
        assertThat(zusatzinformationenPdfComponent.getKommentarZaehlstelle(), is(""));

        zaehlung.setKommentar("Zaehlung Kommentar");
        zaehlstelle.setKommentar("Zaehlstellenkommentar");
        FillPdfBeanService.fillZusatzinformationen(zusatzinformationenPdfComponent, zaehlstelle, zaehlung);
        assertThat(zusatzinformationenPdfComponent.isIstKommentarVorhanden(), is(true));
        assertThat(zusatzinformationenPdfComponent.isIstKommentarVorhandenZaehlstelle(), is(true));
        assertThat(zusatzinformationenPdfComponent.isIstKommentarVorhandenZaehlung(), is(true));
        assertThat(zusatzinformationenPdfComponent.getKommentarZaehlung(), is("Zaehlung Kommentar"));
        assertThat(zusatzinformationenPdfComponent.getKommentarZaehlstelle(), is("Zaehlstellenkommentar"));

    }

    @Test
    public void createChartTitleGanglinieDatentabelle() {
        final OptionsDTO optionsDTO = FillPdfBeanServiceSpringTest.getChosenOptionsDTO();
        final Zaehlung zaehlung = FillPdfBeanServiceSpringTest.getZaehlung();

        assertThat(FillPdfBeanService.createChartTitleFahrbeziehung(optionsDTO, zaehlung), is("Gesamte Zählstelle (Zulauf)"));

        optionsDTO.setVonKnotenarm(1);
        assertThat(FillPdfBeanService.createChartTitleFahrbeziehung(optionsDTO, zaehlung), is("von Cosimastr. (1) "));

        optionsDTO.setNachKnotenarm(4);
        assertThat(FillPdfBeanService.createChartTitleFahrbeziehung(optionsDTO, zaehlung), is("von Cosimastr. (1) nach Wahnfriedallee (4)"));

        optionsDTO.setVonKnotenarm(null);
        assertThat(FillPdfBeanService.createChartTitleFahrbeziehung(optionsDTO, zaehlung), is("nach Wahnfriedallee (4)"));

    }

    @Test
    public void convertZaehldata() {
        assertThat(FillPdfBeanService.convertZaehldata(new BigDecimal(102)), is("102"));
        assertThat(FillPdfBeanService.convertZaehldata(Integer.valueOf(123)), is("123"));
        assertThat(FillPdfBeanService.convertZaehldata(null), is(""));
    }

    @Test
    void getTimeblockForChartTitle() {
        final OptionsDTO optionsDTO = FillPdfBeanServiceSpringTest.getChosenOptionsDTO();
        assertThat(FillPdfBeanService.getTimeblockForChartTitle(optionsDTO), is("0 - 6 Uhr"));
        optionsDTO.setZeitblock(Zeitblock.ZB_10_15);
        assertThat(FillPdfBeanService.getTimeblockForChartTitle(optionsDTO), is("10 - 15 Uhr"));
        optionsDTO.setZeitblock(Zeitblock.ZB_10_11);
        assertThat(FillPdfBeanService.getTimeblockForChartTitle(optionsDTO), is("10 - 11 Uhr"));
        optionsDTO.setZeitblock(Zeitblock.ZB_00_24);
        assertThat(FillPdfBeanService.getTimeblockForChartTitle(optionsDTO), is("0 - 24 Uhr"));
    }

    // Messstelle
    @Test
    void fillBasicPdf_Messstelle() {
        final var basicPdf = new BasicMessstellePdf();
        final Messstelle messstelle = MessstelleRandomFactory.getMessstelle();
        final String department = "TestOU";
        final String tagesTyp = TagesTyp.SAMSTAG.name();
        final MessstelleOptionsDTO optionsDTO = new MessstelleOptionsDTO();
        optionsDTO.setZeitraum(List.of(LocalDate.now()));

        FillPdfBeanService.fillBasicPdf(basicPdf, messstelle, department, optionsDTO, tagesTyp);

        assertThat(basicPdf.getFooterOrganisationseinheit(), is("TestOU"));
        assertThat(basicPdf.getFooterDate(), is(LocalDate.now().format(FillPdfBeanService.DDMMYYYY)));
    }

    @Test
    void fillMessstelleninformationen() {
        final DateTimeFormatter DDMMYYYY = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        MessstelleninformationenPdfComponent informationen = new MessstelleninformationenPdfComponent();
        final Messstelle messstelle = MessstelleRandomFactory.getMessstelle();
        final String tagesTyp = TagesTyp.SAMSTAG.name();
        final MessstelleOptionsDTO optionsDTO = new MessstelleOptionsDTO();
        final ArrayList<LocalDate> localDates = new ArrayList<>();
        localDates.add(LocalDate.now());
        localDates.add(LocalDate.now());
        optionsDTO.setZeitraum(localDates);

        FillPdfBeanService.fillMessstelleninformationen(informationen, messstelle, optionsDTO, tagesTyp);
        assertThat(informationen.getStandort(), is(messstelle.getStandort()));
        assertThat(informationen.getDetektierteFahrzeuge(), is(messstelle.getDetektierteVerkehrsarten()));
        assertThat(informationen.getMesszeitraum(),
                is(String.format("%s - %s", optionsDTO.getZeitraum().getFirst().format(DDMMYYYY), optionsDTO.getZeitraum().getFirst().format(DDMMYYYY))));
        assertThat(informationen.getWochentag(), is(tagesTyp));
        assertThat(informationen.isWochentagNeeded(), is(true));
        assertThat(informationen.getKommentar(), is(messstelle.getKommentar()));

        informationen = new MessstelleninformationenPdfComponent();
        optionsDTO.setZeitraum(List.of(LocalDate.now()));
        FillPdfBeanService.fillMessstelleninformationen(informationen, messstelle, optionsDTO, tagesTyp);
        assertThat(informationen.getStandort(), is(messstelle.getStandort()));
        assertThat(informationen.getDetektierteFahrzeuge(), is(messstelle.getDetektierteVerkehrsarten()));
        assertThat(informationen.getMesszeitraum(), is(optionsDTO.getZeitraum().getFirst().format(DDMMYYYY)));
        assertThat(informationen.isWochentagNeeded(), is(false));
        assertThat(informationen.getWochentag(), is(nullValue()));
        assertThat(informationen.getKommentar(), is(messstelle.getKommentar()));

    }

    @Test
    void createChartTitle() {
        final Messstelle messstelle = MessstelleRandomFactory.getMessstelle();
        final MessstelleOptionsDTO optionsDTO = new MessstelleOptionsDTO();
        final Set<String> mqIds = new HashSet<>();
        messstelle.getMessquerschnitte().forEach(messquerschnitt -> mqIds.add(messquerschnitt.getMqId()));
        optionsDTO.setMessquerschnittIds(mqIds);

        assertThat(FillPdfBeanService.createChartTitle(optionsDTO, messstelle), is(FillPdfBeanService.CHART_TITLE_GESAMTE_MESSSTELLE));

        final Messquerschnitt messquerschnitt = messstelle.getMessquerschnitte().getFirst();
        optionsDTO.setMessquerschnittIds(Set.of(messquerschnitt.getMqId()));
        String expectedChartTitle = messquerschnitt.getMqId() +
                StringUtils.SPACE +
                "-" +
                StringUtils.SPACE +
                StringUtils.defaultIfEmpty(messquerschnitt.getStandort(), FillPdfBeanService.KEINE_DATEN_VORHANDEN) +
                StringUtils.SPACE;
        assertThat(FillPdfBeanService.createChartTitle(optionsDTO, messstelle), is(expectedChartTitle.trim()));
    }

    @Test
    void getTimeblockForChartTitle_Messstelle() {
        final MessstelleOptionsDTO optionsDTO = new MessstelleOptionsDTO();
        optionsDTO.setZeitblock(Zeitblock.ZB_00_06);
        assertThat(FillPdfBeanService.getTimeblockForChartTitle(optionsDTO), is("0 - 6 Uhr"));
        optionsDTO.setZeitblock(Zeitblock.ZB_10_15);
        assertThat(FillPdfBeanService.getTimeblockForChartTitle(optionsDTO), is("10 - 15 Uhr"));
        optionsDTO.setZeitblock(Zeitblock.ZB_10_11);
        assertThat(FillPdfBeanService.getTimeblockForChartTitle(optionsDTO), is("10 - 11 Uhr"));
        optionsDTO.setZeitblock(Zeitblock.ZB_00_24);
        assertThat(FillPdfBeanService.getTimeblockForChartTitle(optionsDTO), is("0 - 24 Uhr"));
    }

    @Test
    void fillMessstelleninformationenGesamtauswertung() {
        MessstelleninformationenPdfComponent informationen = new MessstelleninformationenPdfComponent();
        final Messstelle messstelle = MessstelleRandomFactory.getMessstelle();
        final MessstelleAuswertungOptionsDTO optionsDTO = new MessstelleAuswertungOptionsDTO();
        optionsDTO.setJahre(List.of(2006, 2007, 2008));
        optionsDTO.setTagesTyp(TagesTyp.SAMSTAG);
        optionsDTO.setZeitraum(List.of(AuswertungsZeitraum.JANUAR, AuswertungsZeitraum.FEBRUAR, AuswertungsZeitraum.MAERZ));
        boolean isSingleMessstelle = false;

        FillPdfBeanService.fillMessstelleninformationenGesamtauswertung(informationen, messstelle, optionsDTO, isSingleMessstelle);
        assertThat(informationen.isStandortNeeded(), is(isSingleMessstelle));
        assertThat(informationen.getStandort(), is(nullValue()));
        assertThat(informationen.isKommentarNeeded(), is(isSingleMessstelle));
        assertThat(informationen.getKommentar(), is(nullValue()));
        assertThat(informationen.getDetektierteFahrzeuge(), is(messstelle.getDetektierteVerkehrsarten()));
        assertThat(informationen.getMesszeitraum(),
                is(optionsDTO.getJahre().stream().map(String::valueOf).collect(Collectors.joining(", "))));
        assertThat(informationen.isZeitintervallNeeded(), is(true));
        assertThat(informationen.getZeitintervall(),
                is(optionsDTO.getZeitraum().stream().map(AuswertungsZeitraum::getLongText).collect(Collectors.joining(", "))));
        assertThat(informationen.isWochentagNeeded(), is(true));
        assertThat(informationen.getWochentag(), is(optionsDTO.getTagesTyp().getBeschreibung()));

        informationen = new MessstelleninformationenPdfComponent();
        isSingleMessstelle = true;
        FillPdfBeanService.fillMessstelleninformationenGesamtauswertung(informationen, messstelle, optionsDTO, isSingleMessstelle);
        assertThat(informationen.isStandortNeeded(), is(isSingleMessstelle));
        assertThat(informationen.getStandort(), is(messstelle.getStandort()));
        assertThat(informationen.isKommentarNeeded(), is(isSingleMessstelle));
        assertThat(informationen.getKommentar(), is(messstelle.getKommentar()));
        assertThat(informationen.getDetektierteFahrzeuge(), is(messstelle.getDetektierteVerkehrsarten()));
        assertThat(informationen.getMesszeitraum(),
                is(optionsDTO.getJahre().stream().map(String::valueOf).collect(Collectors.joining(", "))));
        assertThat(informationen.isZeitintervallNeeded(), is(true));
        assertThat(informationen.getZeitintervall(),
                is(optionsDTO.getZeitraum().stream().map(AuswertungsZeitraum::getLongText).collect(Collectors.joining(", "))));
        assertThat(informationen.isWochentagNeeded(), is(true));
        assertThat(informationen.getWochentag(), is(optionsDTO.getTagesTyp().getBeschreibung()));
    }

    @Test
    void createChartTitleGesamtauswertung() {
        final MessstelleAuswertungOptionsDTO options = new MessstelleAuswertungOptionsDTO();
        final MessstelleAuswertungIdDTO messstelleAuswertungIdDTO = new MessstelleAuswertungIdDTO();
        messstelleAuswertungIdDTO.setMstId("123");
        options.setMessstelleAuswertungIds(Set.of(messstelleAuswertungIdDTO, new MessstelleAuswertungIdDTO()));
        assertThat(FillPdfBeanService.createChartTitle(options, new Messstelle()), is(FillPdfBeanService.CHART_TITLE_MEHRERE_MESSSTELLE));

        final Messstelle messstelle = new Messstelle();
        messstelle.setMessquerschnitte(new ArrayList<>());
        messstelleAuswertungIdDTO.setMqIds(new HashSet<>());
        options.setMessstelleAuswertungIds(Set.of(messstelleAuswertungIdDTO));
        assertThat(FillPdfBeanService.createChartTitle(options, messstelle), is(FillPdfBeanService.CHART_TITLE_GESAMTE_MESSSTELLE));

        final Messquerschnitt messquerschnitt = MessquerschnittRandomFactory.getMessquerschnitt();
        final Messquerschnitt messquerschnitt1 = MessquerschnittRandomFactory.getMessquerschnitt();
        messstelle.setMessquerschnitte(List.of(messquerschnitt, messquerschnitt1));
        final Set<String> mqIds = new HashSet<>();
        mqIds.add(messquerschnitt1.getMqId());
        messstelleAuswertungIdDTO.setMstId(messstelle.getMstId());
        messstelleAuswertungIdDTO.setMqIds(mqIds);
        options.setMessstelleAuswertungIds(Set.of(messstelleAuswertungIdDTO));

        final String expected = String.format("%s - %s - %s", messquerschnitt1.getMqId(), messquerschnitt1.getFahrtrichtung(), messquerschnitt1.getStandort());

        assertThat(FillPdfBeanService.createChartTitle(options, messstelle), is(expected));

    }

    @Test
    void getGesamtauswertungTableRows() {
        final List<StepLineSeriesEntryBaseDTO> seriesEntries = new ArrayList<>();
        final StepLineSeriesEntryIntegerDTO entryInt = new StepLineSeriesEntryIntegerDTO();
        entryInt.setYAxisData(List.of(123, 456));
        final StepLineSeriesEntryBigDecimalDTO entryDec = new StepLineSeriesEntryBigDecimalDTO();
        entryDec.setYAxisData(List.of(BigDecimal.ONE, BigDecimal.TEN));
        seriesEntries.add(entryInt);
        seriesEntries.add(entryDec);
        final List<String> legend = new ArrayList<>();
        legend.add("Legend 1");
        legend.add("Legend 2");
        final List<String> header = new ArrayList<>();
        header.add("header 1");
        header.add("header 2");
        final boolean hasMultipleMessstellen = true;

        final List<GesamtauswertungTableRow> expected = new ArrayList<>();
        final GesamtauswertungTableRow row1 = new GesamtauswertungTableRow();
        row1.setLegend(legend.getFirst());
        row1.setCssColorBox("default");
        row1.setGesamtauswertungTableColumns(List.of(new GesamtauswertungTableColumn(String.valueOf(entryInt.getYAxisData().getFirst())),
                new GesamtauswertungTableColumn(String.valueOf(entryInt.getYAxisData().getLast()))));
        final GesamtauswertungTableRow row2 = new GesamtauswertungTableRow();
        row2.setLegend(legend.getLast());
        row2.setGesamtauswertungTableColumns(List.of(new GesamtauswertungTableColumn(String.valueOf(entryDec.getYAxisData().getFirst())),
                new GesamtauswertungTableColumn(String.valueOf(entryDec.getYAxisData().getLast()))));
        row2.setCssColorBox("default");
        expected.add(row1);
        expected.add(row2);
        assertThat(FillPdfBeanService.getGesamtauswertungTableRows(seriesEntries, legend, hasMultipleMessstellen, header), is(expected));
    }

    @Test
    void splitTableRowsIfNecessary() {
        final List<GesamtauswertungTableRow> gesamtauswertungTableRows = new ArrayList<>();
        final GesamtauswertungTableRow row = new GesamtauswertungTableRow();
        row.setLegend("Legend");
        row.setCssColorBox("default");
        final ArrayList<GesamtauswertungTableColumn> gesamtauswertungTableColumns = new ArrayList<>();
        for (int index = 0; index < FillPdfBeanService.MAX_ELEMENTS_IN_GESAMTAUSWERTUNG_TABLE; index++) {
            gesamtauswertungTableColumns.add(new GesamtauswertungTableColumn(String.valueOf(index)));
        }
        row.setGesamtauswertungTableColumns(gesamtauswertungTableColumns);
        gesamtauswertungTableRows.add(row);

        final Map<Integer, List<GesamtauswertungTableRow>> expected = new HashMap<>();
        expected.put(0, gesamtauswertungTableRows);

        assertThat(FillPdfBeanService.splitTableRowsIfNecessary(gesamtauswertungTableRows), is(expected));

        // Test 2
        final List<GesamtauswertungTableRow> gesamtauswertungTableRowsTest2 = new ArrayList<>();
        final var rowTest2 = new GesamtauswertungTableRow();
        rowTest2.setLegend("Legend");
        rowTest2.setCssColorBox("default");
        final ArrayList<GesamtauswertungTableColumn> gesamtauswertungTableColumnsTest2 = new ArrayList<>();
        for (int index = 0; index < FillPdfBeanService.MAX_ELEMENTS_IN_GESAMTAUSWERTUNG_TABLE + 3; index++) {
            gesamtauswertungTableColumnsTest2.add(new GesamtauswertungTableColumn(String.valueOf(index)));
        }
        rowTest2.setGesamtauswertungTableColumns(gesamtauswertungTableColumnsTest2);
        gesamtauswertungTableRowsTest2.add(rowTest2);

        final Map<Integer, List<GesamtauswertungTableRow>> expectedTest2 = new HashMap<>();

        final List<List<GesamtauswertungTableColumn>> partition = ListUtils.partition(gesamtauswertungTableColumnsTest2,
                FillPdfBeanService.MAX_ELEMENTS_IN_GESAMTAUSWERTUNG_TABLE);
        final GesamtauswertungTableRow row1 = new GesamtauswertungTableRow();
        row1.setGesamtauswertungTableColumns(partition.getFirst());
        row1.setLegend("Legend");
        row1.setCssColorBox("default");
        final GesamtauswertungTableRow row2 = new GesamtauswertungTableRow();
        row2.setGesamtauswertungTableColumns(partition.getLast());
        row2.setLegend("Legend");
        row2.setCssColorBox("default");
        expectedTest2.put(0, List.of(row1));
        expectedTest2.put(1, List.of(row2));

        assertThat(FillPdfBeanService.splitTableRowsIfNecessary(gesamtauswertungTableRowsTest2), is(expectedTest2));

    }

    @Test
    void getGesamtauswertungTables() {
        final List<GesamtauswertungTableHeader> headerExpected = new ArrayList<>();
        final List<String> header = new ArrayList<>();
        final Map<Integer, List<GesamtauswertungTableRow>> rowsPerTable = new HashMap<>();
        final List<GesamtauswertungTableRow> gesamtauswertungTableRows = new ArrayList<>();
        final GesamtauswertungTableRow row = new GesamtauswertungTableRow();
        row.setLegend("Legend");
        row.setCssColorBox("default");
        final ArrayList<GesamtauswertungTableColumn> gesamtauswertungTableColumns = new ArrayList<>();
        for (int index = 0; index < FillPdfBeanService.MAX_ELEMENTS_IN_GESAMTAUSWERTUNG_TABLE; index++) {
            gesamtauswertungTableColumns.add(new GesamtauswertungTableColumn(String.valueOf(index)));
            final GesamtauswertungTableHeader gesamtauswertungTableHeader = new GesamtauswertungTableHeader();
            gesamtauswertungTableHeader.setHeader(String.valueOf(index));
            headerExpected.add(gesamtauswertungTableHeader);
            header.add(gesamtauswertungTableHeader.getHeader());
        }
        row.setGesamtauswertungTableColumns(gesamtauswertungTableColumns);
        gesamtauswertungTableRows.add(row);
        rowsPerTable.put(0, gesamtauswertungTableRows);

        final List<GesamtauswertungTable> expected = new ArrayList<>();
        final GesamtauswertungTable gesamtauswertungTable = new GesamtauswertungTable();
        gesamtauswertungTable.setGesamtauswertungTableHeaders(headerExpected);
        gesamtauswertungTable.setGesamtauswertungTableRows(gesamtauswertungTableRows);
        expected.add(gesamtauswertungTable);
        assertThat(FillPdfBeanService.getGesamtauswertungTables(header, rowsPerTable), is(expected));
    }

    @Test
    void getGesamtauswertungTablesWithSplitting() {
        final List<GesamtauswertungTableHeader> headerExpected = new ArrayList<>();
        final List<String> header = new ArrayList<>();
        final List<GesamtauswertungTableRow> gesamtauswertungTableRows = new ArrayList<>();
        final GesamtauswertungTableRow row = new GesamtauswertungTableRow();
        row.setLegend("Legend");
        row.setCssColorBox("default");
        final ArrayList<GesamtauswertungTableColumn> gesamtauswertungTableColumns = new ArrayList<>();
        for (int index = 0; index < FillPdfBeanService.MAX_ELEMENTS_IN_GESAMTAUSWERTUNG_TABLE + 3; index++) {
            gesamtauswertungTableColumns.add(new GesamtauswertungTableColumn(String.valueOf(index)));
            final GesamtauswertungTableHeader gesamtauswertungTableHeader = new GesamtauswertungTableHeader();
            gesamtauswertungTableHeader.setHeader(String.valueOf(index));
            headerExpected.add(gesamtauswertungTableHeader);
            header.add(gesamtauswertungTableHeader.getHeader());
        }
        row.setGesamtauswertungTableColumns(gesamtauswertungTableColumns);
        gesamtauswertungTableRows.add(row);

        final Map<Integer, List<GesamtauswertungTableRow>> rowsPerTable = FillPdfBeanService.splitTableRowsIfNecessary(gesamtauswertungTableRows);
        final List<List<GesamtauswertungTableHeader>> partition = ListUtils.partition(headerExpected,
                FillPdfBeanService.MAX_ELEMENTS_IN_GESAMTAUSWERTUNG_TABLE);
        final List<GesamtauswertungTable> expected = new ArrayList<>();
        rowsPerTable.forEach((integer, gesamtauswertungTableRow) -> {
            final GesamtauswertungTable gesamtauswertungTable = new GesamtauswertungTable();
            gesamtauswertungTable.setGesamtauswertungTableHeaders(partition.get(integer));
            gesamtauswertungTable.setGesamtauswertungTableRows(gesamtauswertungTableRow);
            expected.add(gesamtauswertungTable);
        });
        assertThat(FillPdfBeanService.getGesamtauswertungTables(header, rowsPerTable), is(expected));
    }
}
