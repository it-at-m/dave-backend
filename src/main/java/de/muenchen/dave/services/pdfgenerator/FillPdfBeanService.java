package de.muenchen.dave.services.pdfgenerator;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenTableDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.Knotenarm;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.enums.Zeitauswahl;
import de.muenchen.dave.domain.mapper.DatentabellePdfZaehldatumMapper;
import de.muenchen.dave.domain.mapper.GangliniePdfOptionsMapper;
import de.muenchen.dave.domain.pdf.components.ZaehlstelleninformationenPdfComponent;
import de.muenchen.dave.domain.pdf.components.ZusatzinformationenPdfComponent;
import de.muenchen.dave.domain.pdf.helper.DatentabellePdfZaehldaten;
import de.muenchen.dave.domain.pdf.helper.GanglinieTable;
import de.muenchen.dave.domain.pdf.helper.GanglinieTableColumn;
import de.muenchen.dave.domain.pdf.templates.BasicPdf;
import de.muenchen.dave.domain.pdf.templates.DatentabellePdf;
import de.muenchen.dave.domain.pdf.templates.DiagrammPdf;
import de.muenchen.dave.domain.pdf.templates.GangliniePdf;
import de.muenchen.dave.domain.pdf.templates.PdfBean;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.IndexService;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.util.DomainValues;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class FillPdfBeanService {

    public static final DateTimeFormatter DDMMYYYY = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final String DEPARTMENT_FOOTER_NO_AUTHORITY = "no-authority";
    public static final String CHART_TITLE_GESAMTE_ZÄHLSTELLE = "Gesamte Zählstelle (Zulauf)";
    public static final String CHART_TITLE_VON = "von";
    public static final String CHART_TITLE_NACH = "nach";
    public static final String CHART_TITLE_OPEN_PARENTHESIS = "(";
    public static final String CHART_TITLE_CLOSE_PARENTHESIS = ")";
    private static final String BELASTUNGSPLAN_TITLE = "Belastungsplan - Zählstelle ";
    private static final String GANGLINIE_TITLE = "Ganglinie - Zählstelle ";
    private static final String DATENTABELLE_TITLE = "Listenausgabe - Zählstelle ";
    private static final String KEINE_DATEN_VORHANDEN = "Keine Daten vorhanden";
    private static final String CHART_TITLE_BLOCK = "Block";
    private static final String CHART_TITLE_UHR = "Uhr";
    private static final String UHRZEIT_23_24 = "23 - 24";
    private static final String UHRZEIT_24_STRING = "24";
    private static final String MINUS = "-";

    private static final int UHRZEIT_HOURS_23 = 23;
    private static final int UHRZEIT_MINUTES_59 = 59;
    private static final int MAX_ELEMENTS_IN_GANGLINIE_TABLE = 13;
    private static final String CELL_WIDTH_20_MM = "20mm";
    private static final String CELL_WIDTH_UNITS = "mm";

    private final IndexService indexService;
    private final LadeZaehldatenService ladeZaehldatenService;
    private final GangliniePdfOptionsMapper gangliniePdfOptionsMapper;
    private final DatentabellePdfZaehldatumMapper datentabellePdfZaehldatumMapper;

    public FillPdfBeanService(final IndexService indexService,
            final GangliniePdfOptionsMapper gangliniePdfOptionsMapper,
            final DatentabellePdfZaehldatumMapper datentabellePdfZaehldatumMapper,
            final LadeZaehldatenService ladeZaehldatenService) {
        this.indexService = indexService;
        this.ladeZaehldatenService = ladeZaehldatenService;
        this.gangliniePdfOptionsMapper = gangliniePdfOptionsMapper;
        this.datentabellePdfZaehldatumMapper = datentabellePdfZaehldatumMapper;
    }

    /**
     * Befüllt die PDF-Bean mit den Footer Daten
     *
     * @param pdfBean PDF-Bean die befüllt werden soll
     * @param department Organisationseinheit des Benutzers
     * @return {@link PdfBean}
     */
    public static PdfBean fillPdfBeanWithData(final PdfBean pdfBean, final String department) {
        if (StringUtils.equals(department, DEPARTMENT_FOOTER_NO_AUTHORITY)) {
            pdfBean.setFooterOrganisationseinheit(StringUtils.EMPTY);
        } else {
            pdfBean.setFooterOrganisationseinheit(department);
        }
        pdfBean.setFooterDate(LocalDate.now().format(DDMMYYYY));
        return pdfBean;
    }

    /**
     * Diese Methode befüllt eine BasicPdf Bean mit allen relevanten Daten, die später in den Mustache
     * Templates gebraucht werden. MustacheParts werden hier noch nicht befüllt.
     *
     * @param basicPdf Die Bean, die befüllt werden soll.
     * @param zaehlung Die im Frontend ausgewählte Zählung.
     * @param kreuzungsname ZaehlstelleHeaderDTO, da hier Kreuzungsname vorhanden.
     * @param zaehlstelle Zählstelle aus dem Frontend.
     * @param department Organisationseinheit des Benutzers
     * @return Befüllte BasicPdf Bean.
     */
    static BasicPdf fillBasicPdf(final BasicPdf basicPdf, final Zaehlung zaehlung, final String kreuzungsname, final Zaehlstelle zaehlstelle,
            final String department) {
        fillPdfBeanWithData(basicPdf, department);
        basicPdf.setZusatzinformationen(fillZusatzinformationen(basicPdf.getZusatzinformationen(), zaehlstelle, zaehlung));
        basicPdf.setZaehlstelleninformationen(fillZaehlstelleninformationen(basicPdf.getZaehlstelleninformationen(), kreuzungsname, zaehlung));

        return basicPdf;

    }

    /**
     * Befüllt die übergebene ZaehlstelleninformationenPdfComponent und gibt diese zurück
     *
     * @param zaehlstelleninformationen
     * @param kreuzungsname
     * @param zaehlung
     * @return
     */
    static ZaehlstelleninformationenPdfComponent fillZaehlstelleninformationen(final ZaehlstelleninformationenPdfComponent zaehlstelleninformationen,
            final String kreuzungsname, final Zaehlung zaehlung) {
        zaehlstelleninformationen.setProjektname(StringUtils.defaultIfEmpty(zaehlung.getProjektName(), KEINE_DATEN_VORHANDEN));
        zaehlstelleninformationen.setZaehldatum(zaehlung.getDatum().format(DDMMYYYY));
        zaehlstelleninformationen.setZaehldauer(DomainValues.getZaehldauerValue(zaehlung.getZaehldauer()));
        zaehlstelleninformationen.setKreuzungsname(kreuzungsname);
        zaehlstelleninformationen.setWetter(DomainValues.getWetterValue(zaehlung.getWetter()));
        zaehlstelleninformationen.setZaehlsituation(StringUtils.defaultIfEmpty(zaehlung.getZaehlsituation(), KEINE_DATEN_VORHANDEN));
        zaehlstelleninformationen.setZaehlsituationErweitert(StringUtils.defaultIfEmpty(zaehlung.getZaehlsituationErweitert(), KEINE_DATEN_VORHANDEN));
        return zaehlstelleninformationen;
    }

    /**
     * Befüllt die übergebene ZusatzinformationenPdfComponent und gibt diese zurück
     *
     * @param zusatzinformationen
     * @param zaehlstelle
     * @param zaehlung
     * @return
     */
    static ZusatzinformationenPdfComponent fillZusatzinformationen(final ZusatzinformationenPdfComponent zusatzinformationen, final Zaehlstelle zaehlstelle,
            final Zaehlung zaehlung) {
        zusatzinformationen.setIstKommentarVorhandenZaehlstelle(StringUtils.isNotEmpty(zaehlstelle.getKommentar()));
        zusatzinformationen.setIstKommentarVorhandenZaehlung(StringUtils.isNotEmpty(zaehlung.getKommentar()));
        zusatzinformationen
                .setIstKommentarVorhanden(zusatzinformationen.isIstKommentarVorhandenZaehlstelle() || zusatzinformationen.isIstKommentarVorhandenZaehlung());
        zusatzinformationen.setKommentarZaehlung(StringUtils.defaultIfEmpty(zaehlung.getKommentar(), StringUtils.EMPTY));
        zusatzinformationen.setKommentarZaehlstelle(StringUtils.defaultIfEmpty(zaehlstelle.getKommentar(), StringUtils.EMPTY));

        return zusatzinformationen;
    }

    /**
     * Erstellt und setzt den Titel des Diagramms. Hier wird überprüft ob in den Optionen eine bestimmte
     * Fahrbeziehung
     * ausgewählt wurde (VonKnotenarm oder / und NachKnotenarm) und ggf. die Straßennamen und
     * Knotenarmnummern gesetzt.
     * Wenn nichts ausgewählt: "Gesamte Zählstelle"
     * VonKnotenarm ausgewählt: "von [straßenname] ([knotenarmnummer]) "
     * NachKnotenarm ausgewählt: "nach [straßenname] ([knotenarmnummer])"
     * Beides ausgewählt: "von [straßenname] ([knotenarmnummer]) nach [straßenname] ([knotenarmnummer])"
     *
     * @param options Optionen aus dem Frontend
     * @param zaehlung Die im Frontend gewählte Zählung
     * @return chartTitle als String
     */
    static String createChartTitleFahrbeziehung(final OptionsDTO options, final Zaehlung zaehlung) {
        final StringBuilder chartTitle = new StringBuilder();
        if (options.getVonKnotenarm() == null && options.getNachKnotenarm() == null) {
            chartTitle.append(CHART_TITLE_GESAMTE_ZÄHLSTELLE);
        }
        if (options.getVonKnotenarm() != null) {
            for (final Knotenarm knotenarm : zaehlung.getKnotenarme()) {
                if (knotenarm.getNummer() == (options.getVonKnotenarm())) {
                    if (!zaehlung.getKreisverkehr()) {
                        chartTitle.append(CHART_TITLE_VON);
                        chartTitle.append(StringUtils.SPACE);
                    }
                    chartTitle.append(knotenarm.getStrassenname());
                    chartTitle.append(StringUtils.SPACE);
                    chartTitle.append(CHART_TITLE_OPEN_PARENTHESIS);
                    chartTitle.append(knotenarm.getNummer());
                    chartTitle.append(CHART_TITLE_CLOSE_PARENTHESIS);
                    chartTitle.append(StringUtils.SPACE);
                }
            }
        }
        if (options.getNachKnotenarm() != null) {
            for (final Knotenarm knotenarm : zaehlung.getKnotenarme()) {
                if (knotenarm.getNummer() == (options.getNachKnotenarm())) {
                    chartTitle.append(CHART_TITLE_NACH);
                    chartTitle.append(StringUtils.SPACE);
                    chartTitle.append(knotenarm.getStrassenname());
                    chartTitle.append(StringUtils.SPACE);
                    chartTitle.append(CHART_TITLE_OPEN_PARENTHESIS);
                    chartTitle.append(knotenarm.getNummer());
                    chartTitle.append(CHART_TITLE_CLOSE_PARENTHESIS);
                }
            }
        }
        return chartTitle.toString();
    }

    /**
     * Konvertiert den übergebenen Parameter (bisher Integer oder BigDecimal) in einen String.
     *
     * @param zaehldata Wert der in einen String umgewandelt werden soll.
     * @return Der übergebene Wert als String. Wenn der übergebene Wert 'null' beträgt wird ein leerer
     *         String zurückgegeben.
     */
    static String convertZaehldata(final Number zaehldata) {
        if (zaehldata == null) {
            return StringUtils.EMPTY;
        } else {
            return String.valueOf(zaehldata);
        }

    }

    static boolean getSchematischeUebersichtNeeded(final OptionsDTO optionsDTO) {
        return optionsDTO.getVonKnotenarm() != null || optionsDTO.getNachKnotenarm() != null;
    }

    /**
     * Diese Methode gibt den jeweiligen, in den options gewählten Zeitblock zurück in der Form:
     * "0 - 6 Uhr"
     * "15 - 19 Uhr"
     * "0 - 24 Uhr"
     *
     * @param optionsDTO Options aus dem Frontend
     */
    static String getTimeblockForChartTitle(final OptionsDTO optionsDTO) {
        final StringBuilder timeblock = new StringBuilder();
        timeblock.append(optionsDTO.getZeitblock().getStart().getHour());
        timeblock.append(StringUtils.SPACE);
        timeblock.append(MINUS);
        timeblock.append(StringUtils.SPACE);
        // Bei 23 Uhr muss unterschieden werden zwischen 23:00 (=> 23 Uhr) und 23:59.999999 (24 Uhr)
        if (optionsDTO.getZeitblock().getEnd().getHour() == UHRZEIT_HOURS_23
                && optionsDTO.getZeitblock().getEnd().getMinute() == UHRZEIT_MINUTES_59) {
            timeblock.append(UHRZEIT_24_STRING);
        } else {
            timeblock.append(optionsDTO.getZeitblock().getEnd().getHour());
        }

        timeblock.append(StringUtils.SPACE);
        timeblock.append(CHART_TITLE_UHR);
        return timeblock.toString();
    }

    /**
     * Berechnet die Zellengröße für die Tabellen der Ganglinie und Zeitreihe
     *
     * @param tdCount Anzahl der Zellen
     * @return Zellengröße als String in mm
     */
    public static String calculateCellWidth(final Integer tdCount) {
        if (tdCount < 7) {
            // Bei wenig Spalten => Maximale Zellenbreite setzen
            return CELL_WIDTH_20_MM;
        } else {
            // Sonst dynamisch aufteilen
            final Integer cellWidth = 160 / (tdCount + 1);
            return (cellWidth + CELL_WIDTH_UNITS);
        }
    }

    /**
     * Erstellt den ChartTitle für das Diagramm eines Belastungsplanes.
     * Bei Tageswert wird nur Tageswert angezeigt. Bei Block und Stunde wird noch die ausgewählte Zeit
     * angezeigt.
     * Bei Spitzenstunde wir zuerst der ausgewählte Zeitblock angezeigt, dann in Klammern die berechnete
     * Spitzenstunde.
     * <p>
     * Beispiele:
     * __________________________________________________________________________
     * | Zeitauswahl | Überschrift im Diagramm |
     * |########################################################################|
     * | Tageswert | Tageswert |
     * | Block | Block 0 - 6 Uhr |
     * | Stunde | Stunde 2 - 3 Uhr |
     * | Spitzenstunde | Spitzenstunde 07:30 - 08:30 Uhr (Block 6 - 10 Uhr) |
     * ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
     *
     * @param zaehlungId ID der Zaehlung
     * @param optionsDTO als {@link OptionsDTO}
     * @return Chart-Title as String
     * @throws DataNotFoundException wenn keine Zaehldaten gefunden wurden
     */
    public String createChartTitleZeitauswahl(final String zaehlungId, final OptionsDTO optionsDTO) throws DataNotFoundException {
        final StringBuilder chartTitle = new StringBuilder();
        if (StringUtils.equals(optionsDTO.getZeitauswahl(), Zeitauswahl.TAGESWERT.getCapitalizedName())) {
            chartTitle.append(Zeitauswahl.TAGESWERT.getCapitalizedName());
        } else if (StringUtils.equals(optionsDTO.getZeitauswahl(), Zeitauswahl.BLOCK.getCapitalizedName())
                || StringUtils.equals(optionsDTO.getZeitauswahl(), Zeitauswahl.STUNDE.getCapitalizedName())) {
                    chartTitle.append(optionsDTO.getZeitauswahl());
                    chartTitle.append(StringUtils.SPACE);

                    chartTitle.append(getTimeblockForChartTitle(optionsDTO));

                } else
            if (StringUtils.equalsAny(optionsDTO.getZeitauswahl(),
                    Zeitauswahl.SPITZENSTUNDE_KFZ.getCapitalizedName(),
                    Zeitauswahl.SPITZENSTUNDE_RAD.getCapitalizedName(),
                    Zeitauswahl.SPITZENSTUNDE_FUSS.getCapitalizedName())) {

                        chartTitle.append(optionsDTO.getZeitauswahl());
                        chartTitle.append(StringUtils.SPACE);

                        // Wenn Spitzenstunde ausgewählt soll die berechnete Spitzenstunde ebenfalls in der Überschrift erscheinen
                        final LadeZaehldatenTableDTO ladeZaehldatenTableDTO = this.ladeZaehldatenService.ladeZaehldaten(UUID.fromString(zaehlungId),
                                optionsDTO);
                        final List<LadeZaehldatumDTO> ladeZaehldatumDTOs = ladeZaehldatenTableDTO.getZaehldaten();

                        final Optional<LadeZaehldatumDTO> optSpitzenStundeBlock = ladeZaehldatumDTOs.stream()
                                .filter(ladeZaehldatumDTO -> StringUtils.containsAny(ladeZaehldatumDTO.getType(),
                                        LadeZaehldatenService.SPITZENSTUNDE_BLOCK,
                                        LadeZaehldatenService.SPITZENSTUNDE_TAG))
                                .findFirst();
                        if (optSpitzenStundeBlock.isPresent()) {
                            final LadeZaehldatumDTO spitzenStundeBlock = optSpitzenStundeBlock.get();

                            chartTitle.append(spitzenStundeBlock.getStartUhrzeit());
                            chartTitle.append(StringUtils.SPACE);
                            chartTitle.append(MINUS);
                            chartTitle.append(StringUtils.SPACE);
                            chartTitle.append(spitzenStundeBlock.getEndeUhrzeit());
                            chartTitle.append(StringUtils.SPACE);
                            chartTitle.append(CHART_TITLE_UHR);
                            chartTitle.append(StringUtils.SPACE);
                        }

                        chartTitle.append("(");
                        chartTitle.append(CHART_TITLE_BLOCK);
                        chartTitle.append(StringUtils.SPACE);
                        chartTitle.append(getTimeblockForChartTitle(optionsDTO));
                        chartTitle.append(")");
                    }
        return chartTitle.toString();
    }

    /**
     * Diese Methode befüllt eine DiagrammPdf Bean mit allen relevanten Daten, die später in den
     * Templates gebraucht werden. MustacheParts werden hier noch nicht befüllt.
     *
     * @param diagrammPdf DiagrammPdf bean die befüllt werden soll.
     * @param zaehlungId Die ID der im Frontend ausgewählten Zählung.
     * @param options Die im Frontend ausgewählten Optionen.
     * @param chartAsBase64Png Der Graph aus dem Frontend als Base64-PNG
     * @param department Organisationseinheit des Benutzers
     * @return {@link DiagrammPdf}
     * @throws DataNotFoundException wenn keine Zaehlstelle oder Zaehlung gefunden wurde
     */
    public DiagrammPdf fillBelastungsplanPdf(final DiagrammPdf diagrammPdf, final String zaehlungId,
            final OptionsDTO options, final String chartAsBase64Png, final String department) throws DataNotFoundException {
        final Zaehlstelle zaehlstelle = this.indexService.getZaehlstelleByZaehlungId(zaehlungId);
        final Zaehlung zaehlung = this.indexService.getZaehlung(zaehlungId);

        fillBasicPdf(diagrammPdf, zaehlung, zaehlung.getKreuzungsname(), zaehlstelle, department);

        diagrammPdf.setDocumentTitle(BELASTUNGSPLAN_TITLE + zaehlstelle.getNummer() + this.getCorrectZaehlartString(zaehlung));
        if (zaehlung.getKreisverkehr()) {
            diagrammPdf.setBelastungsplanKreisverkehr(chartAsBase64Png);
        } else {
            diagrammPdf.setChart(chartAsBase64Png);
        }

        diagrammPdf.setChartTitle(this.createChartTitleZeitauswahl(zaehlungId, options));

        return diagrammPdf;
    }

    /**
     * Diese Methode befüllt eine GangliniePdf Bean mit allen relevanten Daten, die später in den
     * Templates gebraucht werden. MustacheParts werden hier noch nicht befüllt.
     *
     * @param gangliniePdf GangliniePdf bean die befüllt werden soll.
     * @param zaehlungId Die ID der im Frontend ausgewählten Zählung.
     * @param options Die im Frontend ausgewählten Optionen.
     * @param chartAsBase64Png Der Graph aus dem Frontend als Base64-PNG
     * @param schematischeUebersichtAsBase64Png Die schematische Uebersicht als Base64-PNG
     * @param department Organisationseinheit des Benutzers
     * @return Die befüllte Bean als {@link GangliniePdf}
     * @throws DataNotFoundException wenn keine Zaehlstelle oder Zaehlung gefunden wurde
     */
    public GangliniePdf fillGangliniePdf(final GangliniePdf gangliniePdf, final String zaehlungId,
            final OptionsDTO options, final String chartAsBase64Png,
            final String schematischeUebersichtAsBase64Png, final String department) throws DataNotFoundException {
        final Zaehlstelle zaehlstelle = this.indexService.getZaehlstelleByZaehlungId(zaehlungId);
        final Zaehlung zaehlung = this.indexService.getZaehlung(zaehlungId);

        fillBasicPdf(gangliniePdf, zaehlung, zaehlung.getKreuzungsname(), zaehlstelle, department);

        gangliniePdf.setDocumentTitle(GANGLINIE_TITLE + zaehlstelle.getNummer() + this.getCorrectZaehlartString(zaehlung));
        gangliniePdf.setChart(chartAsBase64Png);
        gangliniePdf.setChartTitle(createChartTitleFahrbeziehung(options, zaehlung));
        gangliniePdf.setSchematischeUebersichtNeeded(getSchematischeUebersichtNeeded(options));
        gangliniePdf.setSchematischeUebersichtAsBase64Png(schematischeUebersichtAsBase64Png);

        final LadeZaehldatenTableDTO ladeZaehldatenTableDTO = this.ladeZaehldatenService.ladeZaehldaten(UUID.fromString(zaehlungId), options);
        final List<LadeZaehldatumDTO> ladeZaehldatumDTOS = ladeZaehldatenTableDTO.getZaehldaten();
        final List<GanglinieTable> gtList = new ArrayList<>();

        // Initialisierung vor erstem Schleifendurchlauf
        List<GanglinieTableColumn> gtcList = new ArrayList<>();
        GanglinieTable gt = new GanglinieTable();

        // Ausgewählte Fahrzeugklassen / -kategorien werden gemapped, damit nur diese im PDF angezeigt werden.
        this.gangliniePdfOptionsMapper.options2gangliniePdf(gangliniePdf, options);

        for (final LadeZaehldatumDTO lzDto : ladeZaehldatumDTOS) {
            // Type ist 'null', wenn normales Intervall. Es sind im Gangliniendiagramm nur Stunden-, Tageswerte gewuenscht
            if (StringUtils.equals(lzDto.getType(), LadeZaehldatenService.STUNDE)
                    || StringUtils.equals(lzDto.getType(), LadeZaehldatenService.GESAMT)
                    || StringUtils.equals(lzDto.getType(), LadeZaehldatenService.TAGESWERT)
                    //                    Blocksumme nur anzeigen, wenn im Frontend ein Block ausgewählt wurde und es keine andere Gesamtsumme gibt.
                    || (StringUtils.equals(lzDto.getType(), LadeZaehldatenService.BLOCK)
                            && options.getZeitblock().getTypeZeitintervall() == TypeZeitintervall.BLOCK)) {

                final GanglinieTableColumn gtc = new GanglinieTableColumn();

                // Uhrzeit setzen
                final StringBuilder sbUhrzeit = new StringBuilder();
                if (StringUtils.equals(lzDto.getType(), LadeZaehldatenService.GESAMT)
                        || StringUtils.equals(lzDto.getType(), LadeZaehldatenService.TAGESWERT)
                        || StringUtils.equals(lzDto.getType(), LadeZaehldatenService.BLOCK)) {
                    sbUhrzeit.append(lzDto.getType());
                } else if (lzDto.getStartUhrzeit().getHour() == 23 && lzDto.getEndeUhrzeit().getHour() == 23) {
                    sbUhrzeit.append(UHRZEIT_23_24);
                } else {
                    sbUhrzeit.append(lzDto.getStartUhrzeit().getHour());
                    sbUhrzeit.append(StringUtils.SPACE);
                    sbUhrzeit.append(MINUS);
                    sbUhrzeit.append(StringUtils.SPACE);
                    sbUhrzeit.append(lzDto.getEndeUhrzeit().getHour());
                }
                gtc.setUhrzeit(sbUhrzeit.toString());

                gtc.setKfz(convertZaehldata(lzDto.getKfz()));
                gtc.setSv(convertZaehldata(lzDto.getSchwerverkehr()));
                gtc.setSvAnteil(convertZaehldata(lzDto.getAnteilSchwerverkehrAnKfzProzent()));
                gtc.setGv(convertZaehldata(lzDto.getGueterverkehr()));
                gtc.setGvAnteil(convertZaehldata(lzDto.getAnteilGueterverkehrAnKfzProzent()));
                gtc.setPkw(convertZaehldata(lzDto.getPkw()));
                gtc.setLkw(convertZaehldata(lzDto.getLkw()));
                gtc.setLastzuege(convertZaehldata(lzDto.getLastzuege()));
                gtc.setBusse(convertZaehldata(lzDto.getBusse()));
                gtc.setKraftraeder(convertZaehldata(lzDto.getKraftraeder()));
                gtc.setFahrradfahrer(convertZaehldata(lzDto.getFahrradfahrer()));
                gtc.setFussgaenger(convertZaehldata(lzDto.getFussgaenger()));
                gtc.setPkwEinheiten(convertZaehldata(lzDto.getPkwEinheiten()));

                gtcList.add(gtc);

                // Wenn maximale Spaltenanzahl überschritten => Tabelle speichern und neue Tabelle erstellen
                if (gtcList.size() == MAX_ELEMENTS_IN_GANGLINIE_TABLE) {
                    gt.setGanglinieTableColumns(gtcList);
                    gtList.add(gt);
                    gt = new GanglinieTable();
                    gtcList = new ArrayList<>();
                }
            }
        }
        if (gtcList.size() != 0) {
            gt.setGanglinieTableColumns(gtcList);
            gtList.add(gt);
        }

        // Wenn mehrere Tabellen vonnöten => große Zählung, Zellenbreite auf Minimum verkleinern
        // Hier sollte später dynamisch berechnet werden wie viele Tabellen benötigt werden und die Elemente gleichmäßig in diese verteilen.
        if (gtList.size() > 1) {
            gangliniePdf.setTableCellWidth("11.5mm");
            // Wenn nur eine Tabelle => Zellenbreite anpassen
        } else {
            final Integer gtSize = gt.getGanglinieTableColumns().size();

            gangliniePdf.setTableCellWidth(calculateCellWidth(gtSize));
        }

        gangliniePdf.setGanglinieTables(gtList);

        return gangliniePdf;
    }

    /**
     * Diese Methode befüllt eine DatentabellePdf Bean mit allen relevanten Daten, die später in den
     * Templates gebraucht werden. MustacheParts werden hier noch nicht befüllt.
     *
     * @param datentabellePdf Die zu befüllende Bean
     * @param zaehlungId ID der im Frontend ausgewählten Zählung
     * @param options Die im Frontend ausgewählten Optionen.
     * @param schematischeUebersichtAsBase64Png Die schematische Uebersicht als Base64-PNG
     * @param department Organisationseinheit des Benutzers
     * @return die befüllte Datentabelle als {@link DatentabellePdf}
     * @throws DataNotFoundException wenn keine Zaehlstelle/Zaehlung gefunden wurde
     */
    public DatentabellePdf fillDatentabellePdf(final DatentabellePdf datentabellePdf, final String zaehlungId,
            final OptionsDTO options, final String schematischeUebersichtAsBase64Png,
            final String department) throws DataNotFoundException {
        final Zaehlstelle zaehlstelle = this.indexService.getZaehlstelleByZaehlungId(zaehlungId);
        final Zaehlung zaehlung = this.indexService.getZaehlung(zaehlungId);

        fillBasicPdf(datentabellePdf, zaehlung, zaehlung.getKreuzungsname(), zaehlstelle, department);

        datentabellePdf.setDocumentTitle(DATENTABELLE_TITLE + zaehlstelle.getNummer() + this.getCorrectZaehlartString(zaehlung));

        datentabellePdf.setTableTitle(createChartTitleFahrbeziehung(options, zaehlung));

        datentabellePdf.setSchematischeUebersichtNeeded(getSchematischeUebersichtNeeded(options));
        datentabellePdf.setSchematischeUebersichtAsBase64Png(schematischeUebersichtAsBase64Png);

        final DatentabellePdfZaehldaten datentabellePdfZaehldaten = this.getDatentabellePdfZaehldaten(options, zaehlungId);
        datentabellePdf.setDatentabelleZaehldaten(datentabellePdfZaehldaten);

        return datentabellePdf;
    }

    /**
     * Diese Methode befüllt ein Objekt der Klasse {@link DatentabellePdfZaehldaten} und gibt dieses
     * zurück.
     *
     * @param options Die im Frontend ausgewählten Optionen.
     * @param zaehlungId ID der im Frontend ausgewählten Zählung
     * @return Befülltes Objekt vom Typ {@link DatentabellePdfZaehldaten}.
     * @throws DataNotFoundException wenn keine Zaehldaten gefunden wurden
     */
    public DatentabellePdfZaehldaten getDatentabellePdfZaehldaten(final OptionsDTO options, final String zaehlungId) throws DataNotFoundException {
        final LadeZaehldatenTableDTO ladeZaehldatenTableDTO = this.ladeZaehldatenService.ladeZaehldaten(UUID.fromString(zaehlungId), options);
        final List<LadeZaehldatumDTO> ladeZaehldatumDTOS = ladeZaehldatenTableDTO.getZaehldaten();

        // Bei Tageswert soll keine Uhrzeit angezeigt werden
        ladeZaehldatumDTOS.stream()
                .filter(ladeZaehldatumDTO -> StringUtils.equalsIgnoreCase(ladeZaehldatumDTO.getType(), LadeZaehldatenService.TAGESWERT))
                .forEach(ladeZaehldatumDTO -> {
                    ladeZaehldatumDTO.setEndeUhrzeit(null);
                    ladeZaehldatumDTO.setStartUhrzeit(null);
                });

        final DatentabellePdfZaehldaten datentabellePdfZaehldaten = new DatentabellePdfZaehldaten();

        datentabellePdfZaehldaten.setShowKraftfahrzeugverkehr(options.getKraftfahrzeugverkehr());
        datentabellePdfZaehldaten.setShowSchwerverkehr(options.getSchwerverkehr());
        datentabellePdfZaehldaten.setShowGueterverkehr(options.getGueterverkehr());
        datentabellePdfZaehldaten.setShowRadverkehr(options.getRadverkehr());
        datentabellePdfZaehldaten.setShowFussverkehr(options.getFussverkehr());
        datentabellePdfZaehldaten.setShowSchwerverkehrsanteilProzent(options.getSchwerverkehrsanteilProzent());
        datentabellePdfZaehldaten.setShowGueterverkehrsanteilProzent(options.getGueterverkehrsanteilProzent());
        datentabellePdfZaehldaten.setShowPkwEinheiten(options.getPkwEinheiten());
        datentabellePdfZaehldaten.setShowPersonenkraftwagen(options.getPersonenkraftwagen());
        datentabellePdfZaehldaten.setShowLastkraftwagen(options.getLastkraftwagen());
        datentabellePdfZaehldaten.setShowLastzuege(options.getLastzuege());
        datentabellePdfZaehldaten.setShowBusse(options.getBusse());
        datentabellePdfZaehldaten.setShowKraftraeder(options.getKraftraeder());

        datentabellePdfZaehldaten.setActiveTabsFahrzeugtypen(this.calcActiveTabsFahrzeugtypen(options));
        datentabellePdfZaehldaten.setActiveTabsFahrzeugklassen(this.calcActiveTabsFahrzeugklassen(options));
        datentabellePdfZaehldaten.setActiveTabsAnteile(this.calcActiveTabsAnteile(options));

        datentabellePdfZaehldaten.setZaehldatenList(this.datentabellePdfZaehldatumMapper.ladeZaehldatumDTOList2beanList(ladeZaehldatumDTOS));
        return datentabellePdfZaehldaten;
    }

    private int calcActiveTabsFahrzeugtypen(final OptionsDTO optionsDTO) {
        int activeTabsFahrzeugtypen = 0;
        if (optionsDTO.getPersonenkraftwagen()) {
            activeTabsFahrzeugtypen++;
        }
        if (optionsDTO.getLastkraftwagen()) {
            activeTabsFahrzeugtypen++;
        }
        if (optionsDTO.getLastzuege()) {
            activeTabsFahrzeugtypen++;
        }
        if (optionsDTO.getBusse()) {
            activeTabsFahrzeugtypen++;
        }
        if (optionsDTO.getKraftraeder()) {
            activeTabsFahrzeugtypen++;
        }
        if (optionsDTO.getRadverkehr()) {
            activeTabsFahrzeugtypen++;
        }
        if (optionsDTO.getFussverkehr()) {
            activeTabsFahrzeugtypen++;
        }
        return activeTabsFahrzeugtypen;
    }

    private int calcActiveTabsFahrzeugklassen(final OptionsDTO optionsDTO) {
        int activeTabsFahrzeugklasse = 0;
        if (optionsDTO.getKraftfahrzeugverkehr()) {
            activeTabsFahrzeugklasse++;
        }
        if (optionsDTO.getSchwerverkehr()) {
            activeTabsFahrzeugklasse++;
        }
        if (optionsDTO.getGueterverkehr()) {
            activeTabsFahrzeugklasse++;
        }
        return activeTabsFahrzeugklasse;
    }

    private int calcActiveTabsAnteile(final OptionsDTO optionsDTO) {
        int activeTabsAnteile = 0;
        if (optionsDTO.getSchwerverkehrsanteilProzent()) {
            activeTabsAnteile++;
        }
        if (optionsDTO.getGueterverkehrsanteilProzent()) {
            activeTabsAnteile++;
        }
        return activeTabsAnteile;
    }

    /**
     * @param zaehlung als {@link Zaehlung}
     * @return die {@link Zaehlart} der {@link Zaehlung} als String
     *         oder {@link StringUtils#EMPTY} falls {@link Zaehlart#N}.
     */
    public String getCorrectZaehlartString(final Zaehlung zaehlung) {
        return StringUtils.equals(zaehlung.getZaehlart(), Zaehlart.N.toString())
                ? StringUtils.EMPTY
                : zaehlung.getZaehlart();
    }
}
