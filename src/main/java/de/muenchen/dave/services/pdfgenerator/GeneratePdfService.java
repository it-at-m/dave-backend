package de.muenchen.dave.services.pdfgenerator;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.domain.pdf.MustacheBean;
import de.muenchen.dave.domain.pdf.templates.DatentabellePdf;
import de.muenchen.dave.domain.pdf.templates.DiagrammPdf;
import de.muenchen.dave.domain.pdf.templates.GangliniePdf;
import de.muenchen.dave.domain.pdf.templates.PdfBean;
import de.muenchen.dave.domain.pdf.templates.ZeitreihePdf;
import de.muenchen.dave.domain.pdf.templates.messstelle.BelastungsplanPdf;
import de.muenchen.dave.exceptions.DataNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GeneratePdfService {

    // Mustache Files
    // Templates
    private static final String PDF_TEMPLATES_BELASTUNGSPLAN_MUSTACHE = "/pdf/templates/belastungsplan.mustache";
    private static final String PDF_TEMPLATES_GANGLINIE_MUSTACHE = "/pdf/templates/ganglinie.mustache";
    private static final String PDF_TEMPLATES_DATENTABELLE_MUSTACHE = "/pdf/templates/datentabelle.mustache";
    private static final String PDF_TEMPLATES_ZEITREIHE_MUSTACHE = "/pdf/templates/zeitreihe.mustache";
    private static final String PDF_TEMPLATES_BELASTUNGSPLAN_MESSSTELLE_MUSTACHE = "/pdf/templates/messstelle/belastungsplan.mustache";
    private static final String PDF_TEMPLATES_GANGLINIE_MESSSTELLE_MUSTACHE = "/pdf/templates/messstelle/ganglinie.mustache";
    private static final String PDF_TEMPLATES_DATENTABELLE_MESSSTELLE_MUSTACHE = "/pdf/templates/messstelle/datentabelle.mustache";

    // Parts
    private static final String PDF_TEMPLATES_PARTS_LOGO_MUSTACHE = "/pdf/templates/parts/logo.mustache";
    private static final String PDF_TEMPLATES_PARTS_GLOBAL_CSS_MUSTACHE = "/pdf/templates/parts/global-css.mustache";
    private static final String PDF_TEMPLATES_PARTS_FOOTER_MUSTACHE = "/pdf/templates/parts/footer.mustache";
    private static final String PDF_TEMPLATES_PARTS_ZAEHLSTELLENINFORMATIONEN_MUSTACHE = "/pdf/templates/parts/zaehlstelleninformationen.mustache";
    private static final String PDF_TEMPLATES_PARTS_ZUSATZINFORMATIONEN_MUSTACHE = "/pdf/templates/parts/zusatzinformationen.mustache";
    private static final String PDF_TEMPLATES_PARTS_SCHEMATISCHE_UEBERSICHT = "/pdf/templates/parts/schematische-uebersicht.mustache";
    private static final String PDF_TEMPLATES_PARTS_MESSSTELLENINFORMATIONEN_MUSTACHE = "/pdf/templates/messstelle/parts/messstelleninformationen.mustache";

    // Ganglinie
    private static final String PDF_TEMPLATES_PARTS_GANGLINIE_TABLE_MUSTACHE = "/pdf/templates/parts/ganglinie-table.mustache";
    private static final String PDF_TEMPLATES_PARTS_GANGLINIE_CSS_MUSTACHE = "/pdf/templates/parts/ganglinie-css.mustache";

    // Datentabelle
    private static final String PDF_TEMPLATES_PARTS_DATENTABELLE_ZAEHLSTELLE_TABLE_MUSTACHE = "/pdf/templates/parts/datentabelle-table.mustache";
    private static final String PDF_TEMPLATES_PARTS_DATENTABELLE_CSS_MUSTACHE = "/pdf/templates/parts/datentabelle-css.mustache";

    // Zeitreihe
    private static final String PDF_TEMPLATES_PARTS_ZAEHLSTELLENINFORMATIONEN_ZEITREIHE_MUSTACHE = "/pdf/templates/parts/zeitreihe/zaehlstelleninformationen-zeitreihe.mustache";
    private static final String PDF_TEMPLATES_PARTS_ZUSATZINFORMATIONEN_ZEITREIHE_MUSTACHE = "/pdf/templates/parts/zeitreihe/zusatzinformationen-zeitreihe.mustache";
    private static final String PDF_TEMPLATES_PARTS_ZEITREIHE_TABLES_MUSTACHE = "/pdf/templates/parts/zeitreihe/zeitreihe-tables.mustache";
    private static final String PDF_TEMPLATES_PARTS_ZEITREIHE_CSS_MUSTACHE = "/pdf/templates/parts/zeitreihe/zeitreihe-css.mustache";

    private static final String TEMPLATE_CODE = "chart";
    private static final String FILE_SUFFIX_TTF = ".ttf";
    private static final float PDF_VERSION_1_4 = 1.4f;
    private static final float PDF_VERSION_1_5 = 1.5f;

    // Font
    private static final String FONT_FAMILY_ROBOTO = "Roboto";

    @Value("classpath:pdf/fonts/roboto/Roboto-Thin.ttf")
    Resource robotoThin;
    @Value("classpath:pdf/fonts/roboto/Roboto-Thin.ttf")
    Resource robotoLight;
    @Value("classpath:pdf/fonts/roboto/Roboto-Regular.ttf")
    Resource robotoRegular;
    @Value("classpath:pdf/fonts/roboto/Roboto-Medium.ttf")
    Resource robotoMedium;
    @Value("classpath:pdf/fonts/roboto/Roboto-Bold.ttf")
    Resource robotoBold;
    @Value("classpath:pdf/fonts/roboto/Roboto-Black.ttf")
    Resource robotoBlack;
    FillPdfBeanService fillPdfBeanService;
    FillZeitreihePdfBeanService fillZeitreihePdfBeanService;

    // Templates
    private Mustache belastungsplan;
    private Mustache belastungsplan_messstelle;
    private Mustache ganglinie;
    private Mustache ganglinie_messstelle;
    private Mustache datentabelle;
    private Mustache datentabelle_messstelle;
    private Mustache zeitreihe;

    // Parts
    private Mustache zaehlstelleninformationen;
    private Mustache messstelleninformationen;
    private Mustache zusatzinformationen;
    private Mustache globalCss;
    private Mustache header;
    private Mustache footer;
    private Mustache schematischeUebersicht;
    // Ganglinie
    private Mustache ganglinieTable;
    private Mustache ganglinieCss;
    // Datentabelle
    private Mustache datentabelleTable;
    private Mustache datentabelleCss;
    // Zeitreihe
    private Mustache zaehlstelleninformationenZeitreihe;
    private Mustache zusatzinformationenZeitreihe;
    private Mustache zeitreiheTables;
    private Mustache zeitreiheCss;

    public GeneratePdfService(final FillPdfBeanService fillPdfBeanService, final FillZeitreihePdfBeanService fillZeitreihePdfBeanService) {
        this.fillPdfBeanService = fillPdfBeanService;
        this.fillZeitreihePdfBeanService = fillZeitreihePdfBeanService;

    }

    /**
     * Befüllt die Bean mit MustacheParts und erstellt anschließend einen HTML-String für einen
     * Belastungsplan
     *
     * @param bean Bean mit Daten um das Template zu befüllen.
     * @return Belastungsplan als HTML-String
     */
    public String createBelastungsplanHTML(final DiagrammPdf bean) {
        fillPdfBeanMustacheParts(bean);
        bean.setZaehlstelleninformationenMustachePart(getHtml(this.zaehlstelleninformationen, bean));
        bean.setZusatzinformationenMustachePart(getHtml(this.zusatzinformationen, bean));

        return getHtml(this.belastungsplan, bean);
    }

    public String createBelastungsplanHTML(final BelastungsplanPdf bean) {
        fillPdfBeanMustacheParts(bean);
        bean.setMessstelleninformationenMustachePart(getHtml(this.messstelleninformationen, bean));

        return getHtml(this.belastungsplan_messstelle, bean);
    }

    /**
     * Befüllt die Bean mit MustacheParts und erstellt anschließend einen HTML-String für eine Ganglinie
     *
     * @param bean Bean mit Daten um das Template zu befüllen.
     * @return Ganglinie als HTML-String
     */
    public String createGanglinieHTML(final GangliniePdf bean) {
        fillPdfBeanMustacheParts(bean);
        bean.setZaehlstelleninformationenMustachePart(getHtml(this.zaehlstelleninformationen, bean));
        bean.setZusatzinformationenMustachePart(getHtml(this.zusatzinformationen, bean));
        bean.setGanglinieCssMustachePart(getHtml(this.ganglinieCss, bean));
        bean.setSchematischeUebersichtMustachePart(getHtml(this.schematischeUebersicht, bean));

        fillGanglinieTable(bean);

        return getHtml(this.ganglinie, bean);
    }

    public String createGanglinieHTML(final de.muenchen.dave.domain.pdf.templates.messstelle.GangliniePdf bean) {
        fillPdfBeanMustacheParts(bean);
        bean.setMessstelleninformationenMustachePart(getHtml(this.messstelleninformationen, bean));
        bean.setGanglinieCssMustachePart(getHtml(this.ganglinieCss, bean));
        bean.setSchematischeUebersichtMustachePart(getHtml(this.schematischeUebersicht, bean));

        bean.setGanglinieTablesMustachePart(getHtml(this.ganglinieTable, bean));

        return getHtml(this.ganglinie_messstelle, bean);
    }

    /**
     * Befüllt die Bean mit MustacheParts und erstellt anschließend einen HTML-String für die
     * Datentabelle
     *
     * @param bean Bean mit Daten um das Template zu befüllen.
     * @return Datentabelle als HTML-String
     */
    public String createDatentabelleHTML(final DatentabellePdf bean) {
        fillPdfBeanMustacheParts(bean);
        bean.setZaehlstelleninformationenMustachePart(getHtml(this.zaehlstelleninformationen, bean));
        bean.setZusatzinformationenMustachePart(getHtml(this.zusatzinformationen, bean));

        bean.setDatentabelleCssMustachePart(getHtml(this.datentabelleCss, bean));
        bean.setDatentabelleTableMustachePart(getHtml(this.datentabelleTable, bean));
        bean.setSchematischeUebersichtMustachePart(getHtml(this.schematischeUebersicht, bean));

        return getHtml(this.datentabelle, bean);
    }

    public String createDatentabelleHTML(final de.muenchen.dave.domain.pdf.templates.messstelle.DatentabellePdf bean) {
        fillPdfBeanMustacheParts(bean);
        bean.setMessstelleninformationenMustachePart(getHtml(this.messstelleninformationen, bean));
        bean.setDatentabelleCssMustachePart(getHtml(this.datentabelleCss, bean));
        bean.setDatentabelleTableMustachePart(getHtml(this.datentabelleTable, bean));
        bean.setSchematischeUebersichtMustachePart(getHtml(this.schematischeUebersicht, bean));

        return getHtml(this.datentabelle_messstelle, bean);
    }

    public String createZeitreiheHtml(final ZeitreihePdf bean) {
        fillPdfBeanMustacheParts(bean);

        bean.setZaehlstelleninformationenZeitreiheMustachePart(getHtml(this.zaehlstelleninformationenZeitreihe, bean));
        bean.setZusatzinformationenZeitreiheMustachePart(getHtml(this.zusatzinformationenZeitreihe, bean));
        bean.setZeitreiheTableMustachePart(getHtml(this.zeitreiheTables, bean));
        bean.setZeitreiheCssMustachePart(getHtml(this.zeitreiheCss, bean));
        bean.setSchematischeUebersichtMustachePart(getHtml(this.schematischeUebersicht, bean));

        return getHtml(this.zeitreihe, bean);
    }

    /**
     * Hier wird das entsprechende Mustache-Table-Template ausgesucht - bei 2_X_4 Stunden sind die
     * Zellen größer als bei 24
     *
     * @param bean GangliniePdf
     * @return Die befüllte mit dem MustachePart befüllte Bean
     */
    GangliniePdf fillGanglinieTable(final GangliniePdf bean) {
        bean.setGanglinieTablesMustachePart(getHtml(this.ganglinieTable, bean));

        return bean;
    }

    /**
     * Befüllt eine PdfBean mit footer, header und css
     *
     * @param bean PdfBean
     * @return Befüllte Bean
     */
    public PdfBean fillPdfBeanMustacheParts(final PdfBean bean) {
        bean.setGlobalCssMustachePart(getHtml(this.globalCss, bean));
        bean.setLogoMustachePart(getHtml(this.header, bean));
        bean.setFooterMustachePart(getHtml(this.footer, bean));
        return bean;
    }

    /**
     * Steckt eine Bean in ein Mustache Template und gibt den daraus resultierenden HTML-String zurück
     *
     * @param mustache Mustache Template das befüllt werden soll
     * @param bean Bean mit Daten für das Template
     * @return HTML als String
     */
    public String getHtml(final Mustache mustache, final MustacheBean bean) {
        final StringWriter writer = new StringWriter();
        mustache.execute(writer, bean);
        return writer.toString();
    }

    /**
     * Erzeugt ein PDF
     *
     * @param html PDF-Inhalt
     * @return Pdf as byte[]
     * @throws IOException wenn beim Erzeugen des PDF was schief geht
     */
    public byte[] createPdf(final String html) throws IOException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        // fonts laden
        final File thinFont = this.getFileFromResource(this.robotoThin);
        final File lightFont = this.getFileFromResource(this.robotoLight);
        final File regularFont = this.getFileFromResource(this.robotoRegular);
        final File mediumFont = this.getFileFromResource(this.robotoMedium);
        final File boldFont = this.getFileFromResource(this.robotoBold);
        final File blackFont = this.getFileFromResource(this.robotoBlack);

        final PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();

        // PDF/A
        PdfRendererBuilder.PdfAConformance conform = PdfRendererBuilder.PdfAConformance.PDFA_1_A;
        builder.usePdfVersion(conform.getPart() == 1 ? PDF_VERSION_1_4 : PDF_VERSION_1_5);
        builder.usePdfAConformance(conform);

        // fonts einbinden
        builder.useFont(regularFont, FONT_FAMILY_ROBOTO);
        builder.useFont(thinFont, FONT_FAMILY_ROBOTO, 100, BaseRendererBuilder.FontStyle.NORMAL, true);
        builder.useFont(lightFont, FONT_FAMILY_ROBOTO, 300, BaseRendererBuilder.FontStyle.NORMAL, true);
        builder.useFont(mediumFont, FONT_FAMILY_ROBOTO, 500, BaseRendererBuilder.FontStyle.NORMAL, true);
        builder.useFont(boldFont, FONT_FAMILY_ROBOTO, 700, BaseRendererBuilder.FontStyle.NORMAL, true);
        builder.useFont(blackFont, FONT_FAMILY_ROBOTO, 900, BaseRendererBuilder.FontStyle.NORMAL, true);

        // sonstige konfiguration
        builder.withHtmlContent(html, "");
        builder.toStream(os);
        builder.useSVGDrawer(new BatikSVGDrawer());
        log.info("creating pdf");
        builder.run();

        return os.toByteArray();

    }

    /**
     * Alle Mustache Templates werden hier bei Programmstart initialisiert und kompiliert
     */
    @PostConstruct
    void init() {
        log.info("initialisiere die Templates");
        MustacheFactory mf = new DefaultMustacheFactory();

        this.belastungsplan = compileMustache(PDF_TEMPLATES_BELASTUNGSPLAN_MUSTACHE, mf);
        this.ganglinie = compileMustache(PDF_TEMPLATES_GANGLINIE_MUSTACHE, mf);
        this.datentabelle = compileMustache(PDF_TEMPLATES_DATENTABELLE_MUSTACHE, mf);
        this.zeitreihe = compileMustache(PDF_TEMPLATES_ZEITREIHE_MUSTACHE, mf);
        this.belastungsplan_messstelle = compileMustache(PDF_TEMPLATES_BELASTUNGSPLAN_MESSSTELLE_MUSTACHE, mf);
        this.ganglinie_messstelle = compileMustache(PDF_TEMPLATES_GANGLINIE_MESSSTELLE_MUSTACHE, mf);
        this.datentabelle_messstelle = compileMustache(PDF_TEMPLATES_DATENTABELLE_MESSSTELLE_MUSTACHE, mf);

        this.header = compileMustache(PDF_TEMPLATES_PARTS_LOGO_MUSTACHE, mf);
        this.globalCss = compileMustache(PDF_TEMPLATES_PARTS_GLOBAL_CSS_MUSTACHE, mf);
        this.footer = compileMustache(PDF_TEMPLATES_PARTS_FOOTER_MUSTACHE, mf);
        this.zaehlstelleninformationen = compileMustache(PDF_TEMPLATES_PARTS_ZAEHLSTELLENINFORMATIONEN_MUSTACHE, mf);
        this.zusatzinformationen = compileMustache(PDF_TEMPLATES_PARTS_ZUSATZINFORMATIONEN_MUSTACHE, mf);
        this.schematischeUebersicht = compileMustache(PDF_TEMPLATES_PARTS_SCHEMATISCHE_UEBERSICHT, mf);
        this.ganglinieTable = compileMustache(PDF_TEMPLATES_PARTS_GANGLINIE_TABLE_MUSTACHE, mf);
        this.ganglinieCss = compileMustache(PDF_TEMPLATES_PARTS_GANGLINIE_CSS_MUSTACHE, mf);
        this.datentabelleTable = compileMustache(PDF_TEMPLATES_PARTS_DATENTABELLE_ZAEHLSTELLE_TABLE_MUSTACHE, mf);
        this.datentabelleCss = compileMustache(PDF_TEMPLATES_PARTS_DATENTABELLE_CSS_MUSTACHE, mf);
        this.messstelleninformationen = compileMustache(PDF_TEMPLATES_PARTS_MESSSTELLENINFORMATIONEN_MUSTACHE, mf);

        this.zaehlstelleninformationenZeitreihe = compileMustache(PDF_TEMPLATES_PARTS_ZAEHLSTELLENINFORMATIONEN_ZEITREIHE_MUSTACHE, mf);
        this.zusatzinformationenZeitreihe = compileMustache(PDF_TEMPLATES_PARTS_ZUSATZINFORMATIONEN_ZEITREIHE_MUSTACHE, mf);
        this.zeitreiheTables = compileMustache(PDF_TEMPLATES_PARTS_ZEITREIHE_TABLES_MUSTACHE, mf);
        this.zeitreiheCss = compileMustache(PDF_TEMPLATES_PARTS_ZEITREIHE_CSS_MUSTACHE, mf);
    }

    /**
     * Kompiliert die Templates
     *
     * @param path Classpath Pfad zu den Templates
     * @param mf MustacheFactory
     * @return Kompiliertes Template
     */
    Mustache compileMustache(final String path, final MustacheFactory mf) {
        final InputStream stream = this.getClass().getResourceAsStream(path);
        final InputStreamReader streamReader = new InputStreamReader(stream);
        return mf.compile(streamReader, TEMPLATE_CODE);
    }

    /**
     * Das ist notwendig, um die Schriftdateien aus dem Jar laden zu können.
     *
     * @param resource
     * @return
     * @throws IOException
     */
    File getFileFromResource(final Resource resource) throws IOException {
        final File tempFile = File.createTempFile(resource.getFilename(), FILE_SUFFIX_TTF);
        FileUtils.copyInputStreamToFile(resource.getInputStream(), tempFile);
        return tempFile;
    }

    /**
     * Generiert eine Belastungsplan-PDF einer bestimmten Zählung mit den im Frontend gewählten Optionen
     * und gibt diesen zurück.
     *
     * @param zaehlungId ID der im Frontend gewählten Zählung
     * @param options Die im Frontend gewählten Optionen
     * @param chartAsBase64Png Bilddatei des Graph als Base64-PNG
     * @param department Organisationseinheit des Benutzers
     * @return Belastungsplan-PDF als byte[]
     * @throws IOException beim Erzeugen des PDF
     * @throws DataNotFoundException wenn keine Daten geladen werden konnten
     */
    public byte[] generateBelastungsplanPdf(final String zaehlungId, final OptionsDTO options, final String chartAsBase64Png, final String department)
            throws IOException, DataNotFoundException {
        final DiagrammPdf diagrammPdf = new DiagrammPdf();
        fillPdfBeanService.fillBelastungsplanPdf(diagrammPdf, zaehlungId, options, chartAsBase64Png, department);
        final String html = createBelastungsplanHTML(diagrammPdf);

        return createPdf(html);
    }

    public byte[] generateBelastungsplanPdf(final String messstelleId, final MessstelleOptionsDTO options, final String chartAsBase64Png,
            final String department)
            throws IOException, DataNotFoundException {
        final BelastungsplanPdf belastungsplanPdf = new BelastungsplanPdf();
        fillPdfBeanService.fillBelastungsplanPdf(belastungsplanPdf, messstelleId, options, chartAsBase64Png, department);
        final String html = createBelastungsplanHTML(belastungsplanPdf);

        return createPdf(html);
    }

    /**
     * Generiert eine Ganglinie-PDF einer bestimmten Zählung mit den im Frontend gewählten Optionen und
     * gibt diese zurück.
     *
     * @param zaehlungId ID der im Frontend gewählten Zählung
     * @param options Die im Frontend gewählten Optionen
     * @param chartAsBase64Png Bilddatei des Graph als Base64-PNG
     * @param schematischeUebersichtAsBase64Png Schematische Übersicht als Base64-PNG
     * @param department Organisationseinheit des Benutzers
     * @return Ganglinie-PDF als byte[]
     * @throws IOException beim Erzeugen des PDF
     * @throws DataNotFoundException wenn keine Daten geladen werden konnten
     */
    public byte[] generateGangliniePdf(final String zaehlungId, final OptionsDTO options, final String chartAsBase64Png,
            final String schematischeUebersichtAsBase64Png, final String department) throws IOException, DataNotFoundException {
        final GangliniePdf gangliniePdf = new GangliniePdf();
        fillPdfBeanService.fillGangliniePdf(gangliniePdf, zaehlungId, options, chartAsBase64Png, schematischeUebersichtAsBase64Png, department);
        final String html = createGanglinieHTML(gangliniePdf);

        return createPdf(html);
    }

    public byte[] generateGangliniePdf(final String messstelleId, final MessstelleOptionsDTO options, final String chartAsBase64Png,
            final String schematischeUebersichtAsBase64Png, final String department) throws IOException, DataNotFoundException {
        final de.muenchen.dave.domain.pdf.templates.messstelle.GangliniePdf gangliniePdf = new de.muenchen.dave.domain.pdf.templates.messstelle.GangliniePdf();
        fillPdfBeanService.fillGangliniePdf(gangliniePdf, messstelleId, options, chartAsBase64Png, schematischeUebersichtAsBase64Png, department);
        final String html = createGanglinieHTML(gangliniePdf);

        return createPdf(html);
    }

    /**
     * Generiert eine Zeitreihen-PDF einer bestimmten Zählung bis zu der in den Frontendoptionen
     * gewählten Zählung und gibt diese zurück.
     *
     * @param zaehlungId ID der im Frontend gewählten Zählung
     * @param options Die im Frontend gewählten Optionen
     * @param chartAsBase64Png Bilddatei des Graph als Base64-PNG
     * @param schematischeUebersichtAsBase64Png schematische Uebersicht als Base64-PNG
     * @param department Organisationseinheit des Benutzers
     * @return Zeitreihe-PDF als byte[]
     * @throws IOException beim Erzeugen des PDF
     * @throws DataNotFoundException wenn keine Daten geladen werden konnten
     */
    public byte[] generateZeitreihePdf(final String zaehlungId, final OptionsDTO options, final String chartAsBase64Png,
            final String schematischeUebersichtAsBase64Png, final String department) throws IOException, DataNotFoundException {
        final ZeitreihePdf zeitreihePdf = new ZeitreihePdf();
        fillZeitreihePdfBeanService.fillZeitreihePdf(zeitreihePdf, zaehlungId, chartAsBase64Png, schematischeUebersichtAsBase64Png, options, department);
        final String html = createZeitreiheHtml(zeitreihePdf);

        return createPdf(html);
    }

    /**
     * Generiert eine Datentabelle bzw. Listenausgabe-PDF einer bestimmten Zählung mit den im Frontend
     * gewählten Optionen und gibt diese zurück.
     *
     * @param zaehlungId ID der im Frontend gewählten Zählung
     * @param options Die im Frontend gewählten Optionen
     * @param schematischeUebersichtAsBase64Png schematische Uebersicht als Base64-PNG
     * @param department Organisationseinheit des Benutzers
     * @return Datentabelle-PDF aus byte[]
     * @throws IOException beim Erzeugen des PDF
     * @throws DataNotFoundException wenn keine Daten geladen werden konnten
     */
    public byte[] generateDatentabellePdf(final String zaehlungId, final OptionsDTO options, final String schematischeUebersichtAsBase64Png,
            final String department) throws IOException, DataNotFoundException {
        final DatentabellePdf datentabellePdf = new DatentabellePdf();
        fillPdfBeanService.fillDatentabellePdf(datentabellePdf, zaehlungId, options, schematischeUebersichtAsBase64Png, department);
        final String html = createDatentabelleHTML(datentabellePdf);

        return createPdf(html);
    }

    public byte[] generateDatentabellePdf(final String messstelleId, final MessstelleOptionsDTO options, final String schematischeUebersichtAsBase64Png,
            final String department) throws IOException, DataNotFoundException {
        final de.muenchen.dave.domain.pdf.templates.messstelle.DatentabellePdf datentabellePdf = new de.muenchen.dave.domain.pdf.templates.messstelle.DatentabellePdf();
        fillPdfBeanService.fillDatentabellePdf(datentabellePdf, messstelleId, options, schematischeUebersichtAsBase64Png, department);
        final String html = createDatentabelleHTML(datentabellePdf);

        return createPdf(html);
    }
}
