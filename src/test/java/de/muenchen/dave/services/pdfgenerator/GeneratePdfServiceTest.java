package de.muenchen.dave.services.pdfgenerator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import de.muenchen.dave.domain.pdf.assets.TextAsset;
import de.muenchen.dave.domain.pdf.components.ZaehlstelleninformationenPdfComponent;
import de.muenchen.dave.domain.pdf.components.ZusatzinformationenPdfComponent;
import de.muenchen.dave.domain.pdf.helper.DatentabellePdfZaehldaten;
import de.muenchen.dave.domain.pdf.helper.DatentabellePdfZaehldatum;
import de.muenchen.dave.domain.pdf.helper.GanglinieTable;
import de.muenchen.dave.domain.pdf.helper.GanglinieTableColumn;
import de.muenchen.dave.domain.pdf.templates.DatentabellePdf;
import de.muenchen.dave.domain.pdf.templates.DiagrammPdf;
import de.muenchen.dave.domain.pdf.templates.GangliniePdf;
import de.muenchen.dave.domain.pdf.templates.PdfBean;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class GeneratePdfServiceTest {

    private final GeneratePdfService generatePdfService = new GeneratePdfService(null, null, null);

    @BeforeEach
    public void init() {
        generatePdfService.init();
    }

    private void fillZaehlstelleninformationenPdfComponent(ZaehlstelleninformationenPdfComponent zpc) {
        zpc.setWetter("Sonnig");
        zpc.setZaehlsituation("Situation unklar");
        zpc.setProjektname("Projekt X");
        zpc.setZaehldatum("24.11.2017");
        zpc.setZaehldauer("24-Stunden");
        zpc.setKreuzungsname("Goetheplatz");
    }

    private void fillZusatzinformationenPdfComponent(ZusatzinformationenPdfComponent zusatzinformationenPdfComponent) {
        zusatzinformationenPdfComponent.setKommentarZaehlstelle("Kommentar der Zähldaten");
        zusatzinformationenPdfComponent.setKommentarZaehlung("Kommentar des Zähldatums");
    }

    private void fillDiagrammPdf(DiagrammPdf diagrammPdf) {
        ZaehlstelleninformationenPdfComponent zaehlstelleninformationenPdfComponent = new ZaehlstelleninformationenPdfComponent();
        fillZaehlstelleninformationenPdfComponent(zaehlstelleninformationenPdfComponent);
        diagrammPdf.setZaehlstelleninformationen(zaehlstelleninformationenPdfComponent);

        ZusatzinformationenPdfComponent zusatzinformationenPdfComponent = new ZusatzinformationenPdfComponent();
        fillZusatzinformationenPdfComponent(zusatzinformationenPdfComponent);
        diagrammPdf.setZusatzinformationen(zusatzinformationenPdfComponent);

        diagrammPdf.setDocumentTitle("Dokumenttitel");
        diagrammPdf.setChart("Base64PNG");
        diagrammPdf.setChartTitle("Überschrift für den Graph");
        diagrammPdf.setFooterDate("06.11.2020");
        diagrammPdf.setFooterOrganisationseinheit("MOR 1/2/3");
    }

    private void fillGangliniePdf(GangliniePdf gangliniePdf) {
        gangliniePdf.setKreuzungsgeometrie("Platzhalter");
        List<GanglinieTable> ganglinieTables = new ArrayList<>();
        List<GanglinieTableColumn> ganglinieTableColumns = new ArrayList<>();
        gangliniePdf.setTableCellWidth("13mm");
        gangliniePdf.setKraftraeder(true);

        gangliniePdf.setKraftfahrzeugverkehr(true);
        gangliniePdf.setSchwerverkehr(true);
        gangliniePdf.setRadverkehr(true);
        gangliniePdf.setFussverkehr(true);
        gangliniePdf.setSchwerverkehrsanteilProzent(true);
        gangliniePdf.setPkwEinheiten(true);
        gangliniePdf.setPersonenkraftwagen(true);
        gangliniePdf.setLastzuege(true);
        gangliniePdf.setBusse(true);
        gangliniePdf.setKraftraeder(true);

        gangliniePdf.setGueterverkehr(false);
        gangliniePdf.setGueterverkehrsanteilProzent(false);
        gangliniePdf.setLastkraftwagen(false);

        for (int i = 0; i < 10; i++) {
            GanglinieTableColumn gtc = new GanglinieTableColumn();
            gtc.setPkw("Pkw" + i);
            gtc.setLastzuege("Lastzuege" + i);
            gtc.setBusse("Busse" + i);
            gtc.setKraftraeder("Kraftraeder" + i);
            gtc.setFahrradfahrer("Fahrradfahrer" + i);
            gtc.setFussgaenger("Fussgaenger" + i);
            gtc.setPkwEinheiten("PkwEinheiten" + i);
            gtc.setKfz("Kfz" + i);
            gtc.setSv("Sv" + i);
            gtc.setSvAnteil("SvAnteil" + i);
            gtc.setUhrzeit("Uhrzeit" + i);
            ganglinieTableColumns.add(gtc);
        }
        GanglinieTable gt = new GanglinieTable();
        gt.setGanglinieTableColumns(ganglinieTableColumns);
        ganglinieTables.add(gt);

        gangliniePdf.setGanglinieTables(ganglinieTables);
    }

    private void fillDatentabellePdfZaehldaten(DatentabellePdfZaehldaten datentabellePdfZaehldaten) {
        List<DatentabellePdfZaehldatum> datentabellePdfZaehldata = new ArrayList<>();

        for (int i = 0; i <= 10; i++) {
            DatentabellePdfZaehldatum dpz = new DatentabellePdfZaehldatum();
            dpz.setType("Type" + i);
            dpz.setStartUhrzeit("06:00");
            dpz.setEndeUhrzeit("08:00");
            dpz.setPkw(i);
            dpz.setLkw(i + 3);
            dpz.setLastzuege(i + 5);
            dpz.setBusse(i + i);
            dpz.setKraftraeder(i * i);
            dpz.setFahrradfahrer(i + 10);
            dpz.setFussgaenger(i + 100);
            dpz.setPkwEinheiten(i + 25);
            dpz.setGesamt(new BigDecimal("3500"));
            dpz.setKfz(new BigDecimal("2000"));
            dpz.setSchwerverkehr(new BigDecimal("1000"));
            dpz.setGueterverkehr(new BigDecimal("500"));
            dpz.setAnteilSchwerverkehrAnKfzProzent(new BigDecimal("25"));
            dpz.setAnteilGueterverkehrAnKfzProzent(new BigDecimal("10"));
            datentabellePdfZaehldata.add(dpz);
        }

        datentabellePdfZaehldaten.setZaehldatenList(datentabellePdfZaehldata);
    }

    private void fillDatentabellePdf(DatentabellePdf datentabellePdf) {
        datentabellePdf.setTableTitle("Gesamte Zählung");

        datentabellePdf.setDocumentTitle("Dokumententitel");
        datentabellePdf.setFooterDate("02.12.2020");
        datentabellePdf.setFooterOrganisationseinheit("MOR 1/2/3");

        ZaehlstelleninformationenPdfComponent zaehlstelleninformationenPdfComponent = new ZaehlstelleninformationenPdfComponent();
        ZusatzinformationenPdfComponent zusatzinformationenPdfComponent = new ZusatzinformationenPdfComponent();
        DatentabellePdfZaehldaten datentabellePdfZaehldaten = new DatentabellePdfZaehldaten();

        fillZaehlstelleninformationenPdfComponent(zaehlstelleninformationenPdfComponent);
        fillZusatzinformationenPdfComponent(zusatzinformationenPdfComponent);
        fillDatentabellePdfZaehldaten(datentabellePdfZaehldaten);

        datentabellePdf.setZaehlstelleninformationen(zaehlstelleninformationenPdfComponent);
        datentabellePdf.setZusatzinformationen(zusatzinformationenPdfComponent);
        datentabellePdf.setDatentabelleZaehldaten(datentabellePdfZaehldaten);
    }

    @Test
    public void getHtml() {
        MustacheFactory mf = new DefaultMustacheFactory();
        final InputStream stream = this.getClass().getResourceAsStream("/pdf/templates/test.mustache");
        final InputStreamReader streamReader = new InputStreamReader(stream);
        Mustache mustache = mf.compile(streamReader, "chart");

        PdfBean pdfBean = new PdfBean();
        pdfBean.setFooterDate("14.12.2020");
        pdfBean.setFooterOrganisationseinheit("<TestOU>");
        pdfBean.setGlobalCssMustachePart("<style></style>");
        pdfBean.setLogoMustachePart("<header>Der Header</header>");
        pdfBean.setFooterMustachePart("<footer>Der Footer</footer>");

        final String html = generatePdfService.getHtml(mustache, pdfBean);

        final String expected;

        expected = String.format(
                "<html>%n<head>%n  <style></style>%n</head>%n<body>%nNur ein Test-Template.%n<header>Der Header</header>%n%n<footer>Der Footer</footer>%n%n14.12.2020%n&lt;TestOU&gt;%n</body>%n</html>");

        assertThat(html, is(expected));
    }

    @Test
    public void testSanitizeAllowedHtml() {
        // Harmloser HTML-Input bleibt erhalten
        String allowedHtml = "<p>Hello <strong>World</strong>. Visit <a href=\"https://example.com\">Link</a> or <a href=\"mailto:foo@example.com\">Email</a></p>";
        TextAsset assetWithAllowedHtml = new TextAsset();
        assetWithAllowedHtml.setText(allowedHtml);
        generatePdfService.sanitizeAllowedHtml(assetWithAllowedHtml);
        String expected = "<p>Hello <strong>World</strong>. Visit <a href=\"https://example.com\">Link</a> or <a href=\"mailto:foo@example.com\">Email</a></p>";
        assertThat(assetWithAllowedHtml.getText(), is(expected));

        // Schädlicher / nicht erlaubter HTML-Input wird entfernt
        String notAllowedHtml1 = "<p>Click <a href=\"javascript:alert('XSS')\">here</a></p><script>alert('x')</script>";
        TextAsset assetWithNotAllowedHtml1 = new TextAsset();
        assetWithNotAllowedHtml1.setText(notAllowedHtml1);
        generatePdfService.sanitizeAllowedHtml(assetWithNotAllowedHtml1);
        String expected1 = "<p>Click <a>here</a></p>"; // href mit nicht erlaubtem uri scheme sowie script tags werden entfernt
        assertThat(assetWithNotAllowedHtml1.getText(), is(expected1));

        String notAllowedHtml2 = "<p onclick=\"doEvil()\" style=\"color:red\" class=\"foo\">Hi</p>";
        TextAsset assetWithNotAllowedHtml2 = new TextAsset();
        assetWithNotAllowedHtml2.setText(notAllowedHtml2);
        generatePdfService.sanitizeAllowedHtml(assetWithNotAllowedHtml2);
        String expected2 = "<p>Hi</p>"; // onclick wird entfernt
        assertThat(assetWithNotAllowedHtml2.getText(), is(expected2));

        String notAllowedHtml3 = "Before<img src=\"https://example.com/pic.png\" alt=\"pic\">After";
        TextAsset assetWithNotAllowedHtml3 = new TextAsset();
        assetWithNotAllowedHtml3.setText(notAllowedHtml3);
        generatePdfService.sanitizeAllowedHtml(assetWithNotAllowedHtml3);
        String expected3 = "BeforeAfter"; // img wird entfernt
        assertThat(assetWithNotAllowedHtml3.getText(), is(expected3));

        String notAllowedHtml4 = "<a href=\"/local/path\">Local</a>";
        TextAsset assetWithNotAllowedHtml4 = new TextAsset();
        assetWithNotAllowedHtml4.setText(notAllowedHtml4);
        generatePdfService.sanitizeAllowedHtml(assetWithNotAllowedHtml4);
        String expected4 = "<a>Local</a>"; // relative URL wird entfernt
        assertThat(assetWithNotAllowedHtml4.getText(), is(expected4));
    }
}
