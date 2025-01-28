package de.muenchen.dave.services.pdfgenerator;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenZeitreiheDTO;
import de.muenchen.dave.domain.dtos.laden.ZeitauswahlDTO;
import de.muenchen.dave.domain.elasticsearch.Knotenarm;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.mapper.ZeitreiheTableOptionsMapper;
import de.muenchen.dave.domain.pdf.components.ZusatzinformationenZeitreihePdfComponent;
import de.muenchen.dave.domain.pdf.helper.ZeitreiheTable;
import de.muenchen.dave.domain.pdf.helper.ZeitreiheTableRow;
import de.muenchen.dave.domain.pdf.templates.ZeitreihePdf;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import de.muenchen.dave.services.ZeitauswahlService;
import de.muenchen.dave.services.processzaehldaten.ProcessZaehldatenZeitreiheService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FillZeitreihePdfBeanService {

    public static final String DOCUMENT_TITLE_PREFIX = "Zeitreihenvergleich - Zählstelle ";
    public static final String ZUSATZINFORMATIONEN_ZEITREIHE_ZAEHLSTELLENKOMMENTAR = "Zählstellenkommentar:";
    public static final DateTimeFormatter ZUSATZINFORMATIONEN_ZEITREIHE_DATETIMEFORMATTER_MMMM_YYYY = DateTimeFormatter.ofPattern("MMMM yyyy:");
    private final ZaehlstelleIndexService indexService;
    private final ProcessZaehldatenZeitreiheService processZaehldatenZeitreiheService;
    private final ZeitreiheTableOptionsMapper zeitreiheTableOptionsMapper;
    private final ZeitauswahlService zeitauswahlService;

    public FillZeitreihePdfBeanService(final ZaehlstelleIndexService indexService,
            final ProcessZaehldatenZeitreiheService processZaehldatenZeitreiheService,
            final ZeitreiheTableOptionsMapper zeitreiheTableOptionsMapper,
            final ZeitauswahlService zeitauswahlService) {
        this.indexService = indexService;
        this.processZaehldatenZeitreiheService = processZaehldatenZeitreiheService;
        this.zeitreiheTableOptionsMapper = zeitreiheTableOptionsMapper;
        this.zeitauswahlService = zeitauswahlService;
    }

    /**
     * Hier werden die
     * {@link de.muenchen.dave.domain.pdf.components.ZaehlstelleninformationenZeitreihePdfComponent}
     * gesetzt. Es sollen die einzelnen
     * Straßennamen der Zählung angezeigt werden. Wenn ein Platz vorhanden ist, soll auch dieser
     * angezeigt werden. Der Platz wird nur angezeigt wenn die
     * Straßennamen nicht im Platznamen zu finden sind.
     *
     * @param zeitreihePdf ZeitreihePdf, die gefüllt werden soll
     * @param zaehlung Im Frontend ausgewählte Zählung
     */
    public static void fillZaehlstelleninformationenZeitreihe(final ZeitreihePdf zeitreihePdf, final Zaehlung zaehlung) {
        // Knotenarme nach Nummer sortieren und setzen
        final List<Knotenarm> knotenarmList = zaehlung.getKnotenarme()
                .stream()
                .sorted(Comparator.comparing(Knotenarm::getNummer))
                .collect(Collectors.toList());
        zeitreihePdf.getZaehlstelleninformationenZeitreihe().setKnotenarme(knotenarmList);

        // Prüfen, ob irgendein Knotenarm im Platznamen vorkommt
        boolean platzVorhanden = knotenarmList
                .stream()
                .anyMatch(knotenarm -> !zaehlung.getKreuzungsname().contains(knotenarm.getStrassenname()));

        // Wenn kein Knotenarm im Platz vorkommt => Platz setzen
        if (platzVorhanden) {
            zeitreihePdf.getZaehlstelleninformationenZeitreihe().setPlatzVorhanden(true);
            zeitreihePdf.getZaehlstelleninformationenZeitreihe().setPlatz(zaehlung.getKreuzungsname());
        }

    }

    /**
     * Befüllt eine ZeitreihePdf mit allen benötigten Informationen
     *
     * @param zeitreihePdf ZeitreihePdf, die befüllt werden soll
     * @param zaehlungId ID der aktuellen Zählung im Frontend
     * @param chartAsBase64Png Diagramm als PNG in Base64
     * @param schematischeUebersichtAsBase64Png schematische Uebersicht als Base64
     * @param options Optionen aus dem Frontend
     * @param department OU des Benutzers
     * @return Die befüllte ZeitreihePdf
     * @throws DataNotFoundException wenn keine Zaehlstelle/Zaehlung/Zaehldaten gefunden wurden
     */
    public ZeitreihePdf fillZeitreihePdf(final ZeitreihePdf zeitreihePdf, final String zaehlungId, final String chartAsBase64Png,
            final String schematischeUebersichtAsBase64Png, final OptionsDTO options, final String department) throws DataNotFoundException {
        final Zaehlstelle zaehlstelle = indexService.getZaehlstelleByZaehlungId(zaehlungId);
        final Zaehlung zaehlung = indexService.getZaehlung(zaehlungId);

        zeitreihePdf.setDocumentTitle(DOCUMENT_TITLE_PREFIX + zaehlstelle.getNummer());
        zeitreihePdf.setChart(chartAsBase64Png);
        zeitreihePdf.setSchematischeUebersichtNeeded(FillPdfBeanService.getSchematischeUebersichtNeeded(options));
        zeitreihePdf.setSchematischeUebersichtAsBase64Png(schematischeUebersichtAsBase64Png);

        final ZeitreiheTable zeitreiheTable = new ZeitreiheTable();
        zeitreiheTableOptionsMapper.options2zeitreiheTable(zeitreiheTable, options);
        zeitreiheTable.setZeitreiheTableRows(getZeitreiheTableDataForPdf(zaehlstelle.getId(), zaehlungId, options));
        zeitreihePdf.setZeitreiheTable(zeitreiheTable);

        FillPdfBeanService.fillPdfBeanWithData(zeitreihePdf, department);
        fillZusatzinformationenZeitreihe(zeitreihePdf, zaehlung, options, zaehlstelle);
        fillZaehlstelleninformationenZeitreihe(zeitreihePdf, zaehlung);
        zeitreihePdf.setChartTitle(FillPdfBeanService.createChartTitleFahrbeziehung(options, zaehlung));

        return zeitreihePdf;
    }

    /**
     * Lädt die ZeitreiheDaten und wandelt sie in eine Liste von ZeitreiheTableRow um.
     *
     * @param zaehlstelleId ID der aktuellen Zählstelle im Frontend
     * @param zaehlungId ID der aktuellen Zählung im Frontend
     * @param options Optionen aus dem Frontend
     * @return Liste von ZeitreiheTableRows
     * @throws DataNotFoundException wenn keine Daten gefunden wurden
     */
    public List<ZeitreiheTableRow> getZeitreiheTableDataForPdf(String zaehlstelleId, String zaehlungId, OptionsDTO options) throws DataNotFoundException {
        final LadeZaehldatenZeitreiheDTO ladeZaehldatenZeitreiheDTO = processZaehldatenZeitreiheService.getZeitreiheDTO(zaehlstelleId, zaehlungId, options);
        final List<ZeitreiheTableRow> zeitreiheTableRows = new ArrayList<>();
        for (int i = 0; i < ladeZaehldatenZeitreiheDTO.getDatum().size(); i++) {
            final ZeitreiheTableRow ztr = new ZeitreiheTableRow();
            ztr.setDatum(ladeZaehldatenZeitreiheDTO.getDatum().get(i));
            if (options.getKraftfahrzeugverkehr()) {
                ztr.setKfz(ladeZaehldatenZeitreiheDTO.getKfz().get(i));
            }
            if (options.getSchwerverkehr()) {
                ztr.setSv(ladeZaehldatenZeitreiheDTO.getSv().get(i));
            }
            if (options.getGueterverkehr()) {
                ztr.setGv(ladeZaehldatenZeitreiheDTO.getGv().get(i));
            }
            if (options.getFussverkehr()) {
                ztr.setFuss(ladeZaehldatenZeitreiheDTO.getFuss().get(i));
            }
            if (options.getRadverkehr()) {
                ztr.setRad(ladeZaehldatenZeitreiheDTO.getRad().get(i));
            }
            if (options.getZeitreiheGesamt()) {
                ztr.setGesamt(ladeZaehldatenZeitreiheDTO.getGesamt().get(i));
            }
            if (options.getSchwerverkehrsanteilProzent()) {
                ztr.setSvAnteilInProzent(ladeZaehldatenZeitreiheDTO.getSvAnteilInProzent().get(i));
            }
            if (options.getGueterverkehrsanteilProzent()) {
                ztr.setGvAnteilInProzent(ladeZaehldatenZeitreiheDTO.getGvAnteilInProzent().get(i));
            }
            zeitreiheTableRows.add(ztr);
        }
        return zeitreiheTableRows;
    }

    /**
     * Hier wird die @{@link ZusatzinformationenZeitreihePdfComponent} gefüllt. Es sollen der
     * Zählstellenkommentar und alle Kommtare zu allen ausgewählten
     * Zählungen erscheinen, insofern gesetzt.
     *
     * @param zeitreihePdf ZeitreihePdf, die gefüllt werden soll
     * @param zaehlung Aktuell im Frontend gesetzte Zählung
     * @param options Optionen aus dem Frontend
     * @param zaehlstelle Im Frontend gewählte Zählstelle
     */
    public void fillZusatzinformationenZeitreihe(final ZeitreihePdf zeitreihePdf, final Zaehlung zaehlung, final OptionsDTO options,
            final Zaehlstelle zaehlstelle) {
        final List<ZusatzinformationenZeitreihePdfComponent> zusatzinformationenZeitreihePdfComponentList = new ArrayList<>();

        if (StringUtils.isNotEmpty(zaehlstelle.getKommentar())) {
            ZusatzinformationenZeitreihePdfComponent zusatzinformationenZeitreihe = new ZusatzinformationenZeitreihePdfComponent();
            zusatzinformationenZeitreihe.setIdentifier(ZUSATZINFORMATIONEN_ZEITREIHE_ZAEHLSTELLENKOMMENTAR);
            zusatzinformationenZeitreihe.setComment(zaehlstelle.getKommentar());
            zusatzinformationenZeitreihePdfComponentList.add(zusatzinformationenZeitreihe);
        }

        final ZeitauswahlDTO zeitauswahlDTO = zeitauswahlService.determinePossibleZeitauswahl(zaehlung.getZaehldauer(), zaehlung.getId());

        processZaehldatenZeitreiheService.getFilteredAndSortedZaehlungenForZeitreihe(zaehlstelle, zaehlung, options, zeitauswahlDTO)
                .filter(zaehlungForComment -> StringUtils.isNotEmpty(zaehlungForComment.getKommentar()))
                .forEach(zaehlungForComment -> {
                    final ZusatzinformationenZeitreihePdfComponent zusatzinformationenZeitreihe = new ZusatzinformationenZeitreihePdfComponent();
                    zusatzinformationenZeitreihe.setIdentifier(zaehlungForComment.getDatum().format(ZUSATZINFORMATIONEN_ZEITREIHE_DATETIMEFORMATTER_MMMM_YYYY));
                    zusatzinformationenZeitreihe.setComment(zaehlungForComment.getKommentar());
                    zusatzinformationenZeitreihePdfComponentList.add(zusatzinformationenZeitreihe);
                });
        // Falls Zusatzinformationen vorhanden sind => true
        zeitreihePdf.setSindZusatzinformationenVorhanden(zusatzinformationenZeitreihePdfComponentList.size() > 0);

        zeitreihePdf.setZusatzinformationenZeitreihe(zusatzinformationenZeitreihePdfComponentList);
    }
}
