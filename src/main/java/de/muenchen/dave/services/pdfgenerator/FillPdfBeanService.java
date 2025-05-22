package de.muenchen.dave.services.pdfgenerator;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenTableDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBaseDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBigDecimalDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryIntegerDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.LadeProcessedMesswerteDTO;
import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungIdDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDTO;
import de.muenchen.dave.domain.elasticsearch.Knotenarm;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.enums.Zeitauswahl;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.domain.mapper.DatentabellePdfZaehldatumMapper;
import de.muenchen.dave.domain.mapper.DiagrammPdfOptionsMapper;
import de.muenchen.dave.domain.pdf.components.MessstelleninformationenPdfComponent;
import de.muenchen.dave.domain.pdf.components.ZaehlstelleninformationenPdfComponent;
import de.muenchen.dave.domain.pdf.components.ZusatzinformationenPdfComponent;
import de.muenchen.dave.domain.pdf.helper.DatentabellePdfZaehldaten;
import de.muenchen.dave.domain.pdf.helper.GanglinieTable;
import de.muenchen.dave.domain.pdf.helper.GanglinieTableColumn;
import de.muenchen.dave.domain.pdf.helper.GesamtauswertungTable;
import de.muenchen.dave.domain.pdf.helper.GesamtauswertungTableColumn;
import de.muenchen.dave.domain.pdf.helper.GesamtauswertungTableHeader;
import de.muenchen.dave.domain.pdf.helper.GesamtauswertungTableRow;
import de.muenchen.dave.domain.pdf.templates.BasicPdf;
import de.muenchen.dave.domain.pdf.templates.DatentabellePdf;
import de.muenchen.dave.domain.pdf.templates.DiagrammPdf;
import de.muenchen.dave.domain.pdf.templates.GangliniePdf;
import de.muenchen.dave.domain.pdf.templates.PdfBean;
import de.muenchen.dave.domain.pdf.templates.messstelle.BasicMessstellePdf;
import de.muenchen.dave.domain.pdf.templates.messstelle.BelastungsplanMessstellePdf;
import de.muenchen.dave.domain.pdf.templates.messstelle.DatentabelleMessstellePdf;
import de.muenchen.dave.domain.pdf.templates.messstelle.GanglinieMessstellePdf;
import de.muenchen.dave.domain.pdf.templates.messstelle.GesamtauswertungMessstellePdf;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.services.messstelle.MessstelleService;
import de.muenchen.dave.services.messstelle.MesswerteService;
import de.muenchen.dave.util.DomainValues;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FillPdfBeanService {

    public static final DateTimeFormatter DDMMYYYY = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final String DEPARTMENT_FOOTER_NO_AUTHORITY = "no-authority";
    public static final String CHART_TITLE_GESAMTE_ZAEHLSTELLE = "Gesamte Zählstelle (Zulauf)";
    public static final String CHART_TITLE_GESAMTE_MESSSTELLE = "Gesamte Messstelle";
    public static final String CHART_TITLE_MEHRERE_MESSSTELLE = "Ausgewählte Messstellen";
    public static final String CHART_TITLE_VON = "von";
    public static final String CHART_TITLE_NACH = "nach";
    public static final String CHART_TITLE_OPEN_PARENTHESIS = "(";
    public static final String CHART_TITLE_CLOSE_PARENTHESIS = ")";
    public static final String KEINE_DATEN_VORHANDEN = "Keine Daten vorhanden";
    private static final String BELASTUNGSPLAN_TITLE_ZAEHLSTELLE = "Belastungsplan - Zählstelle ";
    private static final String BELASTUNGSPLAN_TITLE_MESSSTELLE = "Belastungsplan - Messstelle ";
    private static final String GANGLINIE_TITLE_ZAEHLSTELLE = "Ganglinie - Zählstelle ";
    private static final String GANGLINIE_TITLE_MESSSTELLE = "Ganglinie - Messstelle ";
    private static final String GESAMTAUSWERTUNG_TITLE_MESSSTELLE = "Zeitreihe - Messstelle ";
    private static final String GESAMTAUSWERTUNG_TITLE_MEHRERE_MESSSTELLE = "Zeitreihe - Messstellen";
    private static final String DATENTABELLE_TITLE_ZAEHLSTELLE = "Listenausgabe - Zählstelle ";
    private static final String DATENTABELLE_TITLE_MESSSTELLE = "Listenausgabe - Messstelle ";
    private static final String CHART_TITLE_BLOCK = "Block";
    private static final String CHART_TITLE_UHR = "Uhr";
    private static final String UHRZEIT_23_24 = "23 - 24";
    private static final String UHRZEIT_24_STRING = "24";
    private static final String MINUS = "-";

    private static final int UHRZEIT_HOURS_23 = 23;
    private static final int UHRZEIT_MINUTES_59 = 59;
    private static final int MAX_ELEMENTS_IN_GANGLINIE_TABLE = 13;
    protected static final int MAX_ELEMENTS_IN_GESAMTAUSWERTUNG_TABLE = 12;
    private static final String CELL_WIDTH_20_MM = "20mm";
    private static final String CELL_WIDTH_UNITS = "mm";

    private final ZaehlstelleIndexService indexService;
    private final LadeZaehldatenService ladeZaehldatenService;
    private final DiagrammPdfOptionsMapper diagrammPdfOptionsMapper;
    private final DatentabellePdfZaehldatumMapper datentabellePdfZaehldatumMapper;

    private final MessstelleService messstelleService;
    private final MesswerteService messwerteService;

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
     * Templates gebraucht werden. MustacheParts werden hier
     * noch nicht befüllt.
     *
     * @param basicPdf Die Bean, die befüllt werden soll.
     * @param zaehlung Die im Frontend ausgewählte Zählung.
     * @param kreuzungsname ZaehlstelleHeaderDTO, da hier Kreuzungsname vorhanden.
     * @param zaehlstelle Zählstelle aus dem Frontend.
     * @param department Organisationseinheit des Benutzers
     * @return Befüllte BasicPdf Bean.
     */
    protected static BasicPdf fillBasicPdf(final BasicPdf basicPdf, final Zaehlung zaehlung, final String kreuzungsname, final Zaehlstelle zaehlstelle,
            final String department) {
        fillPdfBeanWithData(basicPdf, department);
        basicPdf.setZusatzinformationen(fillZusatzinformationen(basicPdf.getZusatzinformationen(), zaehlstelle, zaehlung));
        basicPdf.setZaehlstelleninformationen(fillZaehlstelleninformationen(basicPdf.getZaehlstelleninformationen(), kreuzungsname, zaehlung));

        return basicPdf;
    }

    protected static BasicMessstellePdf fillBasicPdf(final BasicMessstellePdf basicPdf,
            final Messstelle messstelle, final String department, final MessstelleOptionsDTO optionsDTO, final String tagesTyp) {
        fillPdfBeanWithData(basicPdf, department);
        basicPdf.setMessstelleninformationen(fillMessstelleninformationen(basicPdf.getMessstelleninformationen(), messstelle, optionsDTO, tagesTyp));

        return basicPdf;
    }

    /**
     * Befuellt den Standardteil eines PDF-Files über Messstellen mit den Daten
     *
     * @param basicPdf Standard-Teil eines Messstellen-PDFs
     * @param messstelle Ausgewertete Messstelle
     * @param department Abteilung des anfragenden Nutzers
     * @param optionsDTO Ausgewählte Optionen der Auswertung
     * @param isSingleMessstelle Flag, ob nur eine Messstelle ausgewertet wurde
     * @return Standardteil des PDFs
     */
    protected static BasicMessstellePdf fillBasicPdfGesamtauswertung(
            final BasicMessstellePdf basicPdf,
            final Messstelle messstelle,
            final String department,
            final MessstelleAuswertungOptionsDTO optionsDTO,
            final boolean isSingleMessstelle) {
        fillPdfBeanWithData(basicPdf, department);
        basicPdf.setMessstelleninformationen(
                fillMessstelleninformationenGesamtauswertung(basicPdf.getMessstelleninformationen(), messstelle, optionsDTO, isSingleMessstelle));

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
    protected static ZaehlstelleninformationenPdfComponent fillZaehlstelleninformationen(final ZaehlstelleninformationenPdfComponent zaehlstelleninformationen,
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

    protected static MessstelleninformationenPdfComponent fillMessstelleninformationen(final MessstelleninformationenPdfComponent messstelleninformationen,
            final Messstelle messstelle, final MessstelleOptionsDTO optionsDTO, final String tagesTyp) {
        messstelleninformationen.setStandortNeeded(true);
        messstelleninformationen.setStandort(StringUtils.defaultIfEmpty(messstelle.getStandort(), KEINE_DATEN_VORHANDEN));
        messstelleninformationen.setSelectedFahrzeuge(StringUtils.defaultIfEmpty(messstelle.getDetektierteVerkehrsarten(), KEINE_DATEN_VORHANDEN));
        if (!optionsDTO.getZeitraum().getFirst().isEqual(optionsDTO.getZeitraum().getLast())) {
            optionsDTO.getZeitraum().sort(LocalDate::compareTo);
            messstelleninformationen.setMesszeitraum(
                    String.format("%s - %s", optionsDTO.getZeitraum().get(0).format(DDMMYYYY), optionsDTO.getZeitraum().get(1).format(DDMMYYYY)));
            messstelleninformationen.setWochentagNeeded(true);
            messstelleninformationen
                    .setWochentag(StringUtils.defaultIfEmpty(tagesTyp, KEINE_DATEN_VORHANDEN));
        } else {
            messstelleninformationen.setMesszeitraum(optionsDTO.getZeitraum().getFirst().format(DDMMYYYY));
            messstelleninformationen.setWochentagNeeded(false);
        }
        messstelleninformationen.setKommentarNeeded(StringUtils.isNotEmpty(messstelle.getKommentar()));
        messstelleninformationen.setKommentar(messstelle.getKommentar());
        return messstelleninformationen;
    }

    /**
     * Befuellt die Messstelleinformationen mit den ausgewerteten Daten.
     *
     * @param messstelleninformationen PDF-Komponente der Messstelleninformationen
     * @param messstelle ausewertet Messstelle
     * @param optionsDTO Ausgewählte Option der Auswertung
     * @param isSingleMessstelle flag, ob nur eine Messstelle auswertet wurde
     * @return
     */
    protected static MessstelleninformationenPdfComponent fillMessstelleninformationenGesamtauswertung(
            final MessstelleninformationenPdfComponent messstelleninformationen,
            final Messstelle messstelle,
            final MessstelleAuswertungOptionsDTO optionsDTO,
            final boolean isSingleMessstelle) {
        messstelleninformationen.setStandortNeeded(isSingleMessstelle);
        messstelleninformationen.setKommentarNeeded(isSingleMessstelle);
        if (isSingleMessstelle) {
            messstelleninformationen.setStandort(StringUtils.defaultIfEmpty(messstelle.getStandort(), KEINE_DATEN_VORHANDEN));
            messstelleninformationen.setKommentar(messstelle.getKommentar());
        }
        messstelleninformationen
                .setSelectedFahrzeuge(StringUtils.defaultIfEmpty(getSelectedFahrzeugeAsText(optionsDTO.getFahrzeuge()), KEINE_DATEN_VORHANDEN));
        messstelleninformationen
                .setMesszeitraum(ListUtils.emptyIfNull(optionsDTO.getJahre()).stream().sorted().map(String::valueOf).collect(Collectors.joining(", ")));
        messstelleninformationen.setZeitintervallNeeded(true);
        messstelleninformationen.setZeitintervall(optionsDTO.getZeitraum().stream().map(AuswertungsZeitraum::getLongText).collect(Collectors.joining(", ")));
        messstelleninformationen.setWochentagNeeded(true);
        messstelleninformationen
                .setWochentag(StringUtils.defaultIfEmpty(optionsDTO.getTagesTyp().getBeschreibung(), KEINE_DATEN_VORHANDEN));
        return messstelleninformationen;
    }

    /**
     * Wandelt die Fahrzeugoptions in Text um
     *
     * @param options aktuelle Einstellungen
     * @return ausgewaehlte Verkehrsarten als String
     */
    protected static String getSelectedFahrzeugeAsText(final FahrzeugOptionsDTO options) {
        final List<String> selectedFahrzeuge = new ArrayList<>();
        // Fahrzeugtypen
        if (options.isPersonenkraftwagen()) {
            selectedFahrzeuge.add("Pkw");
        }
        if (options.isLastkraftwagen()) {
            selectedFahrzeuge.add("Lkw");
        }
        if (options.isLastzuege()) {
            selectedFahrzeuge.add("Lz");
        }
        if (options.isLieferwagen()) {
            selectedFahrzeuge.add("Lfw");
        }
        if (options.isBusse()) {
            selectedFahrzeuge.add("Bus");
        }
        if (options.isKraftraeder()) {
            selectedFahrzeuge.add("Krad");
        }
        if (options.isRadverkehr()) {
            selectedFahrzeuge.add("Rad");
        }
        if (options.isFussverkehr()) {
            selectedFahrzeuge.add("Fuß");
        }
        // Fahrzeugklassen
        if (options.isKraftfahrzeugverkehr()) {
            selectedFahrzeuge.add("KFZ");
        }
        if (options.isSchwerverkehr()) {
            selectedFahrzeuge.add("SV");
        }
        if (options.isSchwerverkehrsanteilProzent()) {
            selectedFahrzeuge.add("SV%");
        }
        if (options.isGueterverkehr()) {
            selectedFahrzeuge.add("GV");
        }
        if (options.isGueterverkehrsanteilProzent()) {
            selectedFahrzeuge.add("GV%");
        }
        return String.join(", ", selectedFahrzeuge);
    }

    /**
     * Befüllt die übergebene ZusatzinformationenPdfComponent und gibt diese zurück
     *
     * @param zusatzinformationen
     * @param zaehlstelle
     * @param zaehlung
     * @return
     */
    protected static ZusatzinformationenPdfComponent fillZusatzinformationen(final ZusatzinformationenPdfComponent zusatzinformationen,
            final Zaehlstelle zaehlstelle,
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
     * Fahrbeziehung ausgewählt wurde (VonKnotenarm oder / und
     * NachKnotenarm) und ggf. die Straßennamen und Knotenarmnummern gesetzt. Wenn nichts ausgewählt:
     * "Gesamte Zählstelle" VonKnotenarm ausgewählt: "von
     * [straßenname] ([knotenarmnummer]) " NachKnotenarm ausgewählt: "nach [straßenname]
     * ([knotenarmnummer])" Beides ausgewählt: "von [straßenname]
     * ([knotenarmnummer]) nach [straßenname] ([knotenarmnummer])"
     *
     * @param options Optionen aus dem Frontend
     * @param zaehlung Die im Frontend gewählte Zählung
     * @return chartTitle als String
     */
    static String createChartTitleFahrbeziehung(final OptionsDTO options, final Zaehlung zaehlung) {
        final StringBuilder chartTitle = new StringBuilder();
        if (options.getVonKnotenarm() == null && options.getNachKnotenarm() == null) {
            chartTitle.append(CHART_TITLE_GESAMTE_ZAEHLSTELLE);
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

    protected static String createChartTitle(final MessstelleOptionsDTO options, final Messstelle messstelle) {
        final StringBuilder chartTitle = new StringBuilder();
        if (options.getMessquerschnittIds().size() == messstelle.getMessquerschnitte().size()) {
            chartTitle.append(CHART_TITLE_GESAMTE_MESSSTELLE);
        } else {
            messstelle.getMessquerschnitte().stream().filter(messquerschnitt -> options.getMessquerschnittIds().contains(messquerschnitt.getMqId()))
                    .forEach(messquerschnitt -> {
                        chartTitle.append(messquerschnitt.getMqId());
                        chartTitle.append(StringUtils.SPACE);
                        chartTitle.append("-");
                        chartTitle.append(StringUtils.SPACE);
                        chartTitle.append(StringUtils.defaultIfEmpty(messquerschnitt.getStandort(), KEINE_DATEN_VORHANDEN));
                        chartTitle.append(StringUtils.SPACE);
                    });
        }
        return chartTitle.toString().trim();
    }

    protected static String createChartTitle(final MessstelleAuswertungOptionsDTO options, final Messstelle messstelle) {
        final StringBuilder chartTitle = new StringBuilder();
        if (options.getMessstelleAuswertungIds().size() > 1) {
            chartTitle.append(CHART_TITLE_MEHRERE_MESSSTELLE);
        } else {
            CollectionUtils.emptyIfNull(options.getMessstelleAuswertungIds()).stream().findFirst()
                    .ifPresent(messstelleAuswertungIdDTO -> {
                        if (messstelle.getMessquerschnitte().size() == messstelleAuswertungIdDTO.getMqIds().size()) {
                            chartTitle.append(CHART_TITLE_GESAMTE_MESSSTELLE);
                        } else {
                            messstelle.getMessquerschnitte().stream()
                                    .filter(messquerschnitt -> messstelleAuswertungIdDTO.getMqIds().contains(messquerschnitt.getMqId()))
                                    .forEach(messquerschnitt -> {
                                        chartTitle.append(messquerschnitt.getMqId());
                                        chartTitle.append(StringUtils.SPACE);
                                        chartTitle.append("-");
                                        chartTitle.append(StringUtils.SPACE);
                                        chartTitle.append(StringUtils.defaultIfEmpty(messquerschnitt.getFahrtrichtung(), KEINE_DATEN_VORHANDEN));
                                        chartTitle.append(StringUtils.SPACE);
                                        chartTitle.append("-");
                                        chartTitle.append(StringUtils.SPACE);
                                        chartTitle.append(StringUtils.defaultIfEmpty(messquerschnitt.getStandort(), KEINE_DATEN_VORHANDEN));
                                        chartTitle.append(StringUtils.SPACE);
                                    });
                        }
                    });
        }
        return chartTitle.toString().trim();
    }

    /**
     * Konvertiert den übergebenen Parameter (bisher Integer oder BigDecimal) in einen String.
     *
     * @param zaehldata Wert der in einen String umgewandelt werden soll.
     * @return Der übergebene Wert als String. Wenn der übergebene Wert 'null' beträgt wird ein leerer
     *         String zurückgegeben.
     */
    protected static String convertZaehldata(final Number zaehldata) {
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
     * Diese Methode gibt den jeweiligen, in den options gewählten Zeitblock zurück in der Form: "0 - 6
     * Uhr" "15 - 19 Uhr" "0 - 24 Uhr"
     *
     * @param optionsDTO Options aus dem Frontend
     */
    protected static String getTimeblockForChartTitle(final OptionsDTO optionsDTO) {
        return getTimeblockForChartTitle(optionsDTO.getZeitblock());
    }

    protected static String getTimeblockForChartTitle(final MessstelleOptionsDTO optionsDTO) {
        return getTimeblockForChartTitle(optionsDTO.getZeitblock());
    }

    protected static String getTimeblockForChartTitle(final Zeitblock zeitblock) {
        final StringBuilder timeblock = new StringBuilder();
        timeblock.append(zeitblock.getStart().getHour());
        timeblock.append(StringUtils.SPACE);
        timeblock.append(MINUS);
        timeblock.append(StringUtils.SPACE);
        // Bei 23 Uhr muss unterschieden werden zwischen 23:00 (=> 23 Uhr) und 23:59.999999 (24 Uhr)
        if (zeitblock.getEnd().getHour() == UHRZEIT_HOURS_23
                && zeitblock.getEnd().getMinute() == UHRZEIT_MINUTES_59) {
            timeblock.append(UHRZEIT_24_STRING);
        } else {
            timeblock.append(zeitblock.getEnd().getHour());
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
    protected static String calculateCellWidth(final Integer tdCount) {
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
     * Erstellt den ChartTitle für das Diagramm eines Belastungsplanes. Bei Tageswert wird nur Tageswert
     * angezeigt. Bei Block und Stunde wird noch die
     * ausgewählte Zeit angezeigt. Bei Spitzenstunde wir zuerst der ausgewählte Zeitblock angezeigt,
     * dann in Klammern die berechnete Spitzenstunde.
     * <p>
     * Beispiele: __________________________________________________________________________
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

    public String createChartTitleZeitauswahl(final MessstelleOptionsDTO optionsDTO, final List<LadeMesswerteDTO> zaehldaten) {
        final StringBuilder chartTitle = new StringBuilder();
        if (StringUtils.equals(optionsDTO.getZeitauswahl(), Zeitauswahl.TAGESWERT.getCapitalizedName())) {
            if (optionsDTO.getZeitraum().size() > 1) {
                chartTitle.append("Durchschnittlicher");
                chartTitle.append(StringUtils.SPACE);
            }
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
                        final Optional<LadeMesswerteDTO> optSpitzenStundeBlock = zaehldaten.stream()
                                .filter(ladeZaehldatumDTO -> StringUtils.containsAny(ladeZaehldatumDTO.getType(),
                                        LadeZaehldatenService.SPITZENSTUNDE_BLOCK,
                                        LadeZaehldatenService.SPITZENSTUNDE_TAG))
                                .findFirst();
                        if (optSpitzenStundeBlock.isPresent()) {
                            final LadeMesswerteDTO spitzenStundeBlock = optSpitzenStundeBlock.get();

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
     * Templates gebraucht werden. MustacheParts werden hier noch
     * nicht befüllt.
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

        diagrammPdf.setDocumentTitle(BELASTUNGSPLAN_TITLE_ZAEHLSTELLE + zaehlstelle.getNummer() + this.getCorrectZaehlartString(zaehlung));
        if (zaehlung.getKreisverkehr()) {
            diagrammPdf.setBelastungsplanKreisverkehr(chartAsBase64Png);
        } else {
            diagrammPdf.setChart(chartAsBase64Png);
        }

        diagrammPdf.setChartTitle(this.createChartTitleZeitauswahl(zaehlungId, options));

        return diagrammPdf;
    }

    public BelastungsplanMessstellePdf fillBelastungsplanPdf(final BelastungsplanMessstellePdf belastungsplanPdf, final String messstelleId,
            final MessstelleOptionsDTO options, final String chartAsBase64Png, final String department) {
        final Messstelle messstelle = this.messstelleService.getMessstelle(messstelleId);
        final LadeProcessedMesswerteDTO ladeProcessedMesswerteDTO = this.messwerteService.ladeMesswerte(messstelle.getId(), options);
        fillBasicPdf(belastungsplanPdf, messstelle, department, options, ladeProcessedMesswerteDTO.getTagesTyp().getBeschreibung());

        belastungsplanPdf.setDocumentTitle(BELASTUNGSPLAN_TITLE_MESSSTELLE + messstelle.getMstId());
        belastungsplanPdf.setChart(chartAsBase64Png);

        belastungsplanPdf.setChartTitle(this.createChartTitleZeitauswahl(options, ladeProcessedMesswerteDTO.getZaehldatenTable().getZaehldaten()));

        return belastungsplanPdf;
    }

    /**
     * Diese Methode befüllt eine GangliniePdf Bean mit allen relevanten Daten, die später in den
     * Templates gebraucht werden. MustacheParts werden hier noch
     * nicht befüllt.
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

        gangliniePdf.setDocumentTitle(GANGLINIE_TITLE_ZAEHLSTELLE + zaehlstelle.getNummer() + this.getCorrectZaehlartString(zaehlung));
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
        this.diagrammPdfOptionsMapper.options2gangliniePdf(gangliniePdf, options);

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
        if (CollectionUtils.isNotEmpty(gtcList)) {
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

    public GanglinieMessstellePdf fillGangliniePdf(
            final GanglinieMessstellePdf gangliniePdf, final String messstelleId,
            final MessstelleOptionsDTO options, final String chartAsBase64Png,
            final String schematischeUebersichtAsBase64Png, final String department) {
        final Messstelle messstelle = this.messstelleService.getMessstelle(messstelleId);
        final LadeProcessedMesswerteDTO ladeProcessedMesswerteDTO = messwerteService.ladeMesswerte(messstelleId, options);

        fillBasicPdf(gangliniePdf, messstelle, department, options, ladeProcessedMesswerteDTO.getTagesTyp().getBeschreibung());

        gangliniePdf.setDocumentTitle(GANGLINIE_TITLE_MESSSTELLE + messstelle.getMstId());
        gangliniePdf.setChart(chartAsBase64Png);
        gangliniePdf.setChartTitle(createChartTitle(options, messstelle));
        gangliniePdf.setSchematischeUebersichtNeeded(messstelle.getMessquerschnitte().size() > options.getMessquerschnittIds().size());
        gangliniePdf.setSchematischeUebersichtAsBase64Png(schematischeUebersichtAsBase64Png);

        final List<LadeMesswerteDTO> messwerte = ladeProcessedMesswerteDTO.getZaehldatenTable().getZaehldaten();
        final List<GanglinieTable> gtList = new ArrayList<>();

        // Initialisierung vor erstem Schleifendurchlauf
        List<GanglinieTableColumn> gtcList = new ArrayList<>();
        GanglinieTable gt = new GanglinieTable();

        // Ausgewählte Fahrzeugklassen / -kategorien werden gemapped, damit nur diese im PDF angezeigt werden.
        this.diagrammPdfOptionsMapper.options2gangliniePdf(gangliniePdf, options.getFahrzeuge());

        for (final LadeMesswerteDTO messwert : messwerte) {
            // Type ist 'null', wenn normales Intervall. Es sind im Gangliniendiagramm nur Stunden-, Tageswerte gewuenscht
            if (StringUtils.equals(messwert.getType(), LadeZaehldatenService.STUNDE)
                    || StringUtils.equals(messwert.getType(), LadeZaehldatenService.GESAMT)
                    || StringUtils.equals(messwert.getType(), LadeZaehldatenService.TAGESWERT)
                    //                    Blocksumme nur anzeigen, wenn im Frontend ein Block ausgewählt wurde und es keine andere Gesamtsumme gibt.
                    || (StringUtils.equals(messwert.getType(), LadeZaehldatenService.BLOCK)
                            && options.getZeitblock().getTypeZeitintervall() == TypeZeitintervall.BLOCK)) {

                final GanglinieTableColumn gtc = new GanglinieTableColumn();

                // Uhrzeit setzen
                final StringBuilder sbUhrzeit = new StringBuilder();
                if (StringUtils.equals(messwert.getType(), LadeZaehldatenService.GESAMT)
                        || StringUtils.equals(messwert.getType(), LadeZaehldatenService.TAGESWERT)
                        || StringUtils.equals(messwert.getType(), LadeZaehldatenService.BLOCK)) {
                    sbUhrzeit.append(messwert.getType());
                } else if (messwert.getStartUhrzeit().getHour() == 23 && messwert.getEndeUhrzeit().getHour() == 23) {
                    sbUhrzeit.append(UHRZEIT_23_24);
                } else {
                    sbUhrzeit.append(messwert.getStartUhrzeit().getHour());
                    sbUhrzeit.append(StringUtils.SPACE);
                    sbUhrzeit.append(MINUS);
                    sbUhrzeit.append(StringUtils.SPACE);
                    sbUhrzeit.append(messwert.getEndeUhrzeit().getHour());
                }
                gtc.setUhrzeit(sbUhrzeit.toString());

                gtc.setKfz(convertZaehldata(messwert.getKfz()));
                gtc.setSv(convertZaehldata(messwert.getSchwerverkehr()));
                gtc.setSvAnteil(convertZaehldata(messwert.getAnteilSchwerverkehrAnKfzProzent()));
                gtc.setGv(convertZaehldata(messwert.getGueterverkehr()));
                gtc.setGvAnteil(convertZaehldata(messwert.getAnteilGueterverkehrAnKfzProzent()));
                gtc.setPkw(convertZaehldata(messwert.getPkw()));
                gtc.setLkw(convertZaehldata(messwert.getLkw()));
                gtc.setLastzuege(convertZaehldata(messwert.getLastzuege()));
                gtc.setBusse(convertZaehldata(messwert.getBusse()));
                gtc.setKraftraeder(convertZaehldata(messwert.getKraftraeder()));
                gtc.setFahrradfahrer(convertZaehldata(messwert.getFahrradfahrer()));
                gtc.setFussgaenger(convertZaehldata(messwert.getFussgaenger()));
                gtc.setLfw(convertZaehldata(messwert.getLfw()));

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
        if (CollectionUtils.isNotEmpty(gtcList)) {
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
     * Befuellt das PDf-Objekt der Auswertung mit den ausgewerteten Daten.
     *
     * @param gesamtauswertungPdf zu füllendes PDF-Objekt
     * @param options ausgewählte Optionen der Auswertung
     * @param auswertung zugrunde liegende Messwerte der Auswertung
     * @param chartAsBase64Png Grafik der Auswertung
     * @param department Abteilung des anfragenden Nutzers
     * @return gefüllte PDF-Objekt
     */
    public GesamtauswertungMessstellePdf fillGesamtauswertungPdf(
            final GesamtauswertungMessstellePdf gesamtauswertungPdf,
            final MessstelleAuswertungOptionsDTO options,
            final LadeZaehldatenSteplineDTO auswertung,
            final String chartAsBase64Png,
            final String department) {

        final Collection<MessstelleAuswertungIdDTO> messstelleAuswertungIdDTOS = CollectionUtils.emptyIfNull(options.getMessstelleAuswertungIds());
        final Optional<MessstelleAuswertungIdDTO> messstelleAuswertungIdDTO = messstelleAuswertungIdDTOS.stream()
                .findFirst();
        if (messstelleAuswertungIdDTO.isPresent()) {
            final Messstelle messstelle = this.messstelleService.getMessstelleByMstId(messstelleAuswertungIdDTO.get().getMstId());
            fillBasicPdfGesamtauswertung(gesamtauswertungPdf, messstelle, department, options, messstelleAuswertungIdDTOS.size() == 1);

            final boolean hasMultipleMessstellen = messstelleAuswertungIdDTOS.size() > 1;
            if (hasMultipleMessstellen) {
                gesamtauswertungPdf.setDocumentTitle(GESAMTAUSWERTUNG_TITLE_MEHRERE_MESSSTELLE);
            } else {
                gesamtauswertungPdf.setDocumentTitle(GESAMTAUSWERTUNG_TITLE_MESSSTELLE + messstelle.getMstId());
            }
            gesamtauswertungPdf.setChartTitle(createChartTitle(options, messstelle));
            gesamtauswertungPdf.setChart(chartAsBase64Png);

            // Ausgewählte Fahrzeugklassen / -kategorien werden gemapped, damit nur diese im PDF angezeigt werden.
            this.diagrammPdfOptionsMapper.options2gesamtauswertungPdf(gesamtauswertungPdf, options.getFahrzeuge());

            final List<String> header = ListUtils.emptyIfNull(auswertung.getXAxisDataFirstChart());
            final List<StepLineSeriesEntryBaseDTO> seriesEntries = ListUtils.emptyIfNull(auswertung.getSeriesEntriesFirstChart());
            final List<String> legend = ListUtils.emptyIfNull(auswertung.getLegend());

            final List<GesamtauswertungTableRow> gesamtauswertungTableRows = getGesamtauswertungTableRows(seriesEntries,
                    legend, hasMultipleMessstellen, header);
            final Map<Integer, List<GesamtauswertungTableRow>> rowsPerTable = splitTableRowsIfNecessary(gesamtauswertungTableRows);

            final List<GesamtauswertungTable> gtList = getGesamtauswertungTables(header, rowsPerTable, hasMultipleMessstellen);

            // Wenn mehrere Tabellen vonnöten => große Zählung, Zellenbreite auf Minimum verkleinern
            // Hier sollte später dynamisch berechnet werden wie viele Tabellen benötigt werden und die Elemente gleichmäßig in diese verteilen.
            if (gtList.size() > 1) {
                gesamtauswertungPdf.setTableCellWidth("11.5mm");
                // Wenn nur eine Tabelle => Zellenbreite anpassen
            } else {
                final Integer gtSize = gtList.getFirst().getGesamtauswertungTableRows().getFirst().getGesamtauswertungTableColumns().size();
                gesamtauswertungPdf.setTableCellWidth(calculateCellWidth(gtSize));
            }
            gesamtauswertungPdf.setGesamtauswertungTables(gtList);
        }
        return gesamtauswertungPdf;
    }

    /**
     * Erzeugt eine Liste an Tabellen mit den entsprechenden Zeilen.
     *
     * @param header Header
     * @param rowsPerTable Zeilen pro Tabelle
     * @return Liste an anzuzeigenden Tabellen
     */
    protected static List<GesamtauswertungTable> getGesamtauswertungTables(
            final List<String> header,
            final Map<Integer, List<GesamtauswertungTableRow>> rowsPerTable,
            final boolean hasMultipleMessstellen) {
        final List<GesamtauswertungTableHeader> tableHeader = header.stream().map(s -> {
            final GesamtauswertungTableHeader gesamtauswertungTableHeader = new GesamtauswertungTableHeader();
            gesamtauswertungTableHeader.setHeader(s);
            return gesamtauswertungTableHeader;
        }).toList();

        final var firstColumnHeader = new GesamtauswertungTableHeader();
        firstColumnHeader.setHeader(hasMultipleMessstellen ? "Mst-ID" : StringUtils.EMPTY);

        final List<List<GesamtauswertungTableHeader>> partitionTableHeaders = ListUtils.partition(tableHeader, MAX_ELEMENTS_IN_GESAMTAUSWERTUNG_TABLE);
        return IntStream.range(0, partitionTableHeaders.size()).mapToObj(index -> {
            final List<GesamtauswertungTableHeader> gesamtauswertungTableHeaders = new ArrayList<>(partitionTableHeaders.get(index));
            gesamtauswertungTableHeaders.addFirst(firstColumnHeader);
            final GesamtauswertungTable gesamtauswertungTable = new GesamtauswertungTable();
            gesamtauswertungTable.setGesamtauswertungTableHeaders(gesamtauswertungTableHeaders);
            gesamtauswertungTable.setGesamtauswertungTableRows(rowsPerTable.get(index));
            return gesamtauswertungTable;
        }).toList();
    }

    /**
     * Erzeugt aus den ausgewerteten Daten einzelne Zeile zur Darstellung als Tabelle.
     * Sind in einer Reihe zu viele Einträge, so wird diese auf mehrere Tabellen aufgeteilt.
     *
     * @param seriesEntries ausgewertete Daten
     * @param legend Legende der Daten
     * @param hasMultipleMessstellen Flag, ob mehrere Messstellen ausgewertet wurden
     * @param header Headerzeile der Tabellen
     * @return Map<Nummer der Tabelle, Zeilen pro Tabelle>
     */
    protected static List<GesamtauswertungTableRow> getGesamtauswertungTableRows(
            final List<StepLineSeriesEntryBaseDTO> seriesEntries,
            final List<String> legend,
            final boolean hasMultipleMessstellen,
            final List<String> header) {

        return IntStream.range(0, seriesEntries.size())
                .mapToObj(index -> {
                    final GesamtauswertungTableRow row = new GesamtauswertungTableRow();
                    row.setLegend(legend.get(index));
                    if (hasMultipleMessstellen) {
                        row.setCssColorBox(String.format("mst-%s", index));
                    } else {
                        row.setCssColorBox(row.getLegend().toLowerCase());
                    }
                    if (row.getCssColorBox().contains("%")) {
                        row.setCssColorBox(row.getCssColorBox().replace(" %", "-anteil"));
                    }
                    final StepLineSeriesEntryBaseDTO baseDto = seriesEntries.get(index);
                    if (baseDto.getClass() == StepLineSeriesEntryIntegerDTO.class) {
                        final StepLineSeriesEntryIntegerDTO integerDto = (StepLineSeriesEntryIntegerDTO) baseDto;
                        row.setGesamtauswertungTableColumns(
                                integerDto.getYAxisData().stream().map(integer -> new GesamtauswertungTableColumn(convertZaehldata(integer))).toList());
                    } else if (baseDto.getClass() == StepLineSeriesEntryBigDecimalDTO.class) {
                        final StepLineSeriesEntryBigDecimalDTO bigdecimalDto = (StepLineSeriesEntryBigDecimalDTO) baseDto;
                        row.setGesamtauswertungTableColumns(bigdecimalDto.getYAxisData().stream()
                                .map(bigDecimal -> new GesamtauswertungTableColumn(convertZaehldata(bigDecimal))).toList());
                    } else {
                        row.setGesamtauswertungTableColumns(header.stream().map((headerValue) -> new GesamtauswertungTableColumn("???")).toList());
                    }
                    return row;
                }).toList();
    }

    /**
     * Wenn eine Row zu lang für die Tabelle ist, wird diese auf mehrere Tabellen vereteilt.
     *
     * @param gesamtauswertungTableRows Liste der Zeilen
     * @return Map<Nummer der Tabelle, Zeilen pro Tabelle>
     */
    protected static Map<Integer, List<GesamtauswertungTableRow>> splitTableRowsIfNecessary(final List<GesamtauswertungTableRow> gesamtauswertungTableRows) {
        final Map<Integer, List<GesamtauswertungTableRow>> rowsPerTable = new HashMap<>();
        gesamtauswertungTableRows.forEach(gesamtauswertungTableRow -> {
            final AtomicInteger tableIndex = new AtomicInteger(0);
            final List<List<GesamtauswertungTableColumn>> partition = ListUtils.partition(gesamtauswertungTableRow.getGesamtauswertungTableColumns(),
                    MAX_ELEMENTS_IN_GESAMTAUSWERTUNG_TABLE);
            partition.forEach(gesamtauswertungTableColumns -> {
                if (!rowsPerTable.containsKey(tableIndex.get())) {
                    rowsPerTable.put(tableIndex.get(), new ArrayList<>());
                }
                final GesamtauswertungTableRow row = new GesamtauswertungTableRow();
                row.setCssColorBox(gesamtauswertungTableRow.getCssColorBox());
                row.setLegend(gesamtauswertungTableRow.getLegend());
                row.setGesamtauswertungTableColumns(gesamtauswertungTableColumns);
                rowsPerTable.get(tableIndex.getAndIncrement()).add(row);
            });
        });
        return rowsPerTable;
    }

    /**
     * Diese Methode befüllt eine DatentabellePdf Bean mit allen relevanten Daten, die später in den
     * Templates gebraucht werden. MustacheParts werden hier noch
     * nicht befüllt.
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

        datentabellePdf.setDocumentTitle(DATENTABELLE_TITLE_ZAEHLSTELLE + zaehlstelle.getNummer() + this.getCorrectZaehlartString(zaehlung));

        datentabellePdf.setTableTitle(createChartTitleFahrbeziehung(options, zaehlung));

        datentabellePdf.setSchematischeUebersichtNeeded(getSchematischeUebersichtNeeded(options));
        datentabellePdf.setSchematischeUebersichtAsBase64Png(schematischeUebersichtAsBase64Png);

        final DatentabellePdfZaehldaten datentabellePdfZaehldaten = this.getDatentabellePdfZaehldaten(options, zaehlungId);
        datentabellePdf.setDatentabelleZaehldaten(datentabellePdfZaehldaten);

        return datentabellePdf;
    }

    public DatentabelleMessstellePdf fillDatentabellePdf(
            final DatentabelleMessstellePdf datentabellePdf,
            final String messstelleId,
            final MessstelleOptionsDTO options,
            final String schematischeUebersichtAsBase64Png,
            final String department) {
        final Messstelle messstelle = this.messstelleService.getMessstelle(messstelleId);
        final LadeProcessedMesswerteDTO ladeProcessedMesswerteDTO = messwerteService.ladeMesswerte(messstelleId, options);

        fillBasicPdf(datentabellePdf, messstelle, department, options, ladeProcessedMesswerteDTO.getTagesTyp().getBeschreibung());

        datentabellePdf.setDocumentTitle(DATENTABELLE_TITLE_MESSSTELLE + messstelle.getMstId());

        datentabellePdf.setTableTitle(createChartTitle(options, messstelle));

        datentabellePdf.setSchematischeUebersichtNeeded(messstelle.getMessquerschnitte().size() > options.getMessquerschnittIds().size());
        datentabellePdf.setSchematischeUebersichtAsBase64Png(schematischeUebersichtAsBase64Png);

        final DatentabellePdfZaehldaten datentabellePdfMessstelle = this.getDatentabellePdfZaehldaten(options, ladeProcessedMesswerteDTO.getZaehldatenTable()
                .getZaehldaten());
        datentabellePdf.setDatentabelleZaehldaten(datentabellePdfMessstelle);

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

    public DatentabellePdfZaehldaten getDatentabellePdfZaehldaten(final MessstelleOptionsDTO options, final String messstelleId) {
        final LadeProcessedMesswerteDTO ladeProcessedMesswerteDTO = messwerteService.ladeMesswerte(messstelleId, options);
        return getDatentabellePdfZaehldaten(options, ladeProcessedMesswerteDTO.getZaehldatenTable().getZaehldaten());
    }

    public DatentabellePdfZaehldaten getDatentabellePdfZaehldaten(final MessstelleOptionsDTO options, final List<LadeMesswerteDTO> ladeMesswerteDTOS) {
        // Bei Tageswert soll keine Uhrzeit angezeigt werden
        ladeMesswerteDTOS.stream()
                .filter(ladeMesswerteDTO -> StringUtils.equalsIgnoreCase(ladeMesswerteDTO.getType(), LadeZaehldatenService.TAGESWERT))
                .forEach(ladeMesswerteDTO -> {
                    ladeMesswerteDTO.setEndeUhrzeit(null);
                    ladeMesswerteDTO.setStartUhrzeit(null);
                });

        final FahrzeugOptionsDTO fahrzeugOptions = options.getFahrzeuge();
        final DatentabellePdfZaehldaten datentabellePdfZaehldaten = this.datentabellePdfZaehldatumMapper
                .fahrzeugOptionsToDatentabellePdfZaehldaten(fahrzeugOptions);

        datentabellePdfZaehldaten.setActiveTabsFahrzeugtypen(this.calcActiveTabsFahrzeugtypen(fahrzeugOptions));
        datentabellePdfZaehldaten.setActiveTabsFahrzeugklassen(this.calcActiveTabsFahrzeugklassen(fahrzeugOptions));
        datentabellePdfZaehldaten.setActiveTabsAnteile(this.calcActiveTabsAnteile(fahrzeugOptions));

        datentabellePdfZaehldaten.setZaehldatenList(this.datentabellePdfZaehldatumMapper.ladeMesswerteDTOList2beanList(ladeMesswerteDTOS));
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

    private int calcActiveTabsFahrzeugtypen(final FahrzeugOptionsDTO optionsDTO) {
        int activeTabsFahrzeugtypen = 0;
        if (optionsDTO.isPersonenkraftwagen()) {
            activeTabsFahrzeugtypen++;
        }
        if (optionsDTO.isLastkraftwagen()) {
            activeTabsFahrzeugtypen++;
        }
        if (optionsDTO.isLastzuege()) {
            activeTabsFahrzeugtypen++;
        }
        if (optionsDTO.isLieferwagen()) {
            activeTabsFahrzeugtypen++;
        }
        if (optionsDTO.isBusse()) {
            activeTabsFahrzeugtypen++;
        }
        if (optionsDTO.isKraftraeder()) {
            activeTabsFahrzeugtypen++;
        }
        if (optionsDTO.isRadverkehr()) {
            activeTabsFahrzeugtypen++;
        }
        if (optionsDTO.isFussverkehr()) {
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

    private int calcActiveTabsFahrzeugklassen(final FahrzeugOptionsDTO optionsDTO) {
        int activeTabsFahrzeugklasse = 0;
        if (optionsDTO.isKraftfahrzeugverkehr()) {
            activeTabsFahrzeugklasse++;
        }
        if (optionsDTO.isSchwerverkehr()) {
            activeTabsFahrzeugklasse++;
        }
        if (optionsDTO.isGueterverkehr()) {
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

    private int calcActiveTabsAnteile(final FahrzeugOptionsDTO optionsDTO) {
        int activeTabsAnteile = 0;
        if (optionsDTO.isSchwerverkehrsanteilProzent()) {
            activeTabsAnteile++;
        }
        if (optionsDTO.isGueterverkehrsanteilProzent()) {
            activeTabsAnteile++;
        }
        return activeTabsAnteile;
    }

    /**
     * @param zaehlung als {@link Zaehlung}
     * @return die {@link Zaehlart} der {@link Zaehlung} als String oder {@link StringUtils#EMPTY} falls
     *         {@link Zaehlart#N}.
     */
    public String getCorrectZaehlartString(final Zaehlung zaehlung) {
        return StringUtils.equals(zaehlung.getZaehlart(), Zaehlart.N.toString())
                ? StringUtils.EMPTY
                : zaehlung.getZaehlart();
    }
}
