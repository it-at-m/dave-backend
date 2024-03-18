package de.muenchen.dave.services.pdfgenerator;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.AssetType;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.mapper.LadeZaehldatumMapper;
import de.muenchen.dave.domain.pdf.assets.BaseAsset;
import de.muenchen.dave.domain.pdf.assets.DatatableAsset;
import de.muenchen.dave.domain.pdf.assets.LogoAsset;
import de.muenchen.dave.domain.pdf.assets.MessstelleDatatableAsset;
import de.muenchen.dave.domain.pdf.assets.ZaehlungskenngroessenAsset;
import de.muenchen.dave.domain.pdf.helper.DatentabellePdfZaehldaten;
import de.muenchen.dave.domain.pdf.helper.ZaehlungskenngroessenData;
import de.muenchen.dave.domain.pdf.templates.ReportPdf;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import de.muenchen.dave.services.processzaehldaten.ProcessZaehldatenService;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ReportService {

    private static final String PDF_TEMPLATES_REPORT_PARTS_REPORT_TEXT_ASSET = "/pdf/templates/parts/report/text-asset.mustache";
    private static final String PDF_TEMPLATES_REPORT_PARTS_REPORT_IMAGE_ASSET = "/pdf/templates/parts/report/image-asset.mustache";
    private static final String PDF_TEMPLATES_REPORT_PARTS_REPORT_HEADING1_ASSET = "/pdf/templates/parts/report/heading1-asset.mustache";
    private static final String PDF_TEMPLATES_REPORT_PARTS_REPORT_HEADING2_ASSET = "/pdf/templates/parts/report/heading2-asset.mustache";
    private static final String PDF_TEMPLATES_REPORT_PARTS_REPORT_HEADING3_ASSET = "/pdf/templates/parts/report/heading3-asset.mustache";
    private static final String PDF_TEMPLATES_REPORT_PARTS_REPORT_HEADING4_ASSET = "/pdf/templates/parts/report/heading4-asset.mustache";
    private static final String PDF_TEMPLATES_REPORT_PARTS_REPORT_HEADING5_ASSET = "/pdf/templates/parts/report/heading5-asset.mustache";
    private static final String PDF_TEMPLATES_REPORT_PARTS_REPORT_PAGEBREAK_ASSET = "/pdf/templates/parts/report/pagebreak-asset.mustache";
    private static final String PDF_TEMPLATES_REPORT_PARTS_REPORT_NEWLINE_ASSET = "/pdf/templates/parts/report/newline-asset.mustache";
    private static final String PDF_TEMPLATES_REPORT_PARTS_REPORT_LOGO_ASSET = "/pdf/templates/parts/report/logo-asset.mustache";
    private static final String PDF_TEMPLATES_REPORT_PARTS_REPORT_DATENTABELLE_TABLE = "/pdf/templates/parts/report/datatable-report-table.mustache";
    private static final String PDF_TEMPLATES_REPORT_PARTS_REPORT_ZAEHLUNGSKENNGROESSEN = "/pdf/templates/parts/report/zaehlungskenngroessen-asset.mustache";
    private static final String PDF_TEMPLATES_REPORT = "/pdf/templates/report.mustache";
    // CSS
    private static final String PDF_TEMPLATES_REPORT_PARTS_DATENTABELLE_CSS_CUSTOM = "/pdf/templates/parts/report/datatable-custom-css.mustache";
    private static final String PDF_TEMPLATES_REPORT_PARTS_DATENTABELLE_CSS_FIXED = "/pdf/templates/parts/report/datatable-fixed-css.mustache";
    private final GeneratePdfService generatePdfService;
    private final FillPdfBeanService fillPdfBeanService;
    private final ProcessZaehldatenService processZaehldatenService;
    private final ZaehlstelleIndexService indexService;
    private final LadeZaehldatumMapper ladeZaehldatumMapper;
    private Mustache textAssetMustache;
    private Mustache imageAssetMustache;
    private Mustache heading1AssetMustache;
    private Mustache heading2AssetMustache;
    private Mustache heading3AssetMustache;
    private Mustache heading4AssetMustache;
    private Mustache heading5AssetMustache;
    private Mustache pagebreakAssetMustache;
    private Mustache newlineAssetMustache;
    private Mustache logoAssetMustache;
    private Mustache dataTableMustache;
    private Mustache zaehlungskenngroessenMustache;
    private Mustache reportMustache;
    // CSS
    private Mustache dataTableCssMustacheCustom;
    private Mustache dataTableCssMustacheFixed;

    public ReportService(final GeneratePdfService generatePdfService,
            final FillPdfBeanService fillPdfBeanService,
            final ProcessZaehldatenService processZaehldatenService,
            final ZaehlstelleIndexService indexService,
            final LadeZaehldatumMapper ladeZaehldatumMapper) {
        this.fillPdfBeanService = fillPdfBeanService;
        this.generatePdfService = generatePdfService;
        this.processZaehldatenService = processZaehldatenService;
        this.indexService = indexService;
        this.ladeZaehldatumMapper = ladeZaehldatumMapper;
    }

    public byte[] generateReportPdf(final List<BaseAsset> assetList, final String department) throws IOException {
        final ReportPdf reportPdf = new ReportPdf();

        final String html = this.fillReportPdf(reportPdf, assetList, department);

        return this.generatePdfService.createPdf(html);
    }

    public String fillReportPdf(final ReportPdf reportPdf, final List<BaseAsset> assetList, final String department) {
        FillPdfBeanService.fillPdfBeanWithData(reportPdf, department);
        this.generatePdfService.fillPdfBeanMustacheParts(reportPdf);
        final LogoAsset logoAsset = new LogoAsset();
        logoAsset.setType(AssetType.LOGO);
        assetList.add(0, logoAsset);
        // logoAsset hier nur als dummy benutzt, dataTableCssMustacheFixed ist nicht variabel und benötigt keine Bean zum funktionieren.
        reportPdf.setCssFixed(this.generatePdfService.getHtml(this.dataTableCssMustacheFixed, logoAsset));

        reportPdf.setBody(this.generateReportBody(assetList));
        // Muss nach generateReportBody ausgeführt werden damit die DatentabelleAssets gefüllt werden
        reportPdf.setCssCustom(this.generateReportCss(assetList));
        return this.generatePdfService.getHtml(this.reportMustache, reportPdf);
    }

    /**
     * Hier werden die flexiblen Anteile der CSS erstellt, die für die Datentabellen notwendig sind.
     * Jede Datentabelle benötigt hier einen eigenen CSS Anteil für Spaltenbreite etc.
     *
     * @param assetList Liste der im Frontend generierten Assets
     * @return Flexibler CSS Anteil als String
     */
    private String generateReportCss(final List<BaseAsset> assetList) {
        final StringBuilder sb = new StringBuilder();

        assetList.stream()
                .filter(asset -> asset.getType().equals(AssetType.DATATABLE))
                .forEach(asset -> {
                    sb.append(this.generatePdfService.getHtml(this.dataTableCssMustacheCustom, asset));
                });

        return sb.toString();
    }

    public String generateReportBody(final List<BaseAsset> assetList) {
        final StringBuilder sb = new StringBuilder();

        assetList.forEach(asset -> {
            if (asset.getType().equals(AssetType.TEXT)) {
                sb.append(this.generatePdfService.getHtml(this.textAssetMustache, asset));
            } else if (asset.getType().equals(AssetType.IMAGE)) {
                sb.append(this.generatePdfService.getHtml(this.imageAssetMustache, asset));
            } else if (asset.getType().equals(AssetType.PAGEBREAK)) {
                sb.append(this.generatePdfService.getHtml(this.pagebreakAssetMustache, asset));
            } else if (asset.getType().equals(AssetType.NEWLINE)) {
                sb.append(this.generatePdfService.getHtml(this.newlineAssetMustache, asset));
            } else if (asset.getType().equals(AssetType.HEADING1)) {
                sb.append(this.generatePdfService.getHtml(this.heading1AssetMustache, asset));
            } else if (asset.getType().equals(AssetType.HEADING2)) {
                sb.append(this.generatePdfService.getHtml(this.heading2AssetMustache, asset));
            } else if (asset.getType().equals(AssetType.HEADING3)) {
                sb.append(this.generatePdfService.getHtml(this.heading3AssetMustache, asset));
            } else if (asset.getType().equals(AssetType.HEADING4)) {
                sb.append(this.generatePdfService.getHtml(this.heading4AssetMustache, asset));
            } else if (asset.getType().equals(AssetType.HEADING5)) {
                sb.append(this.generatePdfService.getHtml(this.heading5AssetMustache, asset));
            } else if (asset.getType().equals(AssetType.LOGO)) {
                sb.append(this.generatePdfService.getHtml(this.logoAssetMustache, asset));
            } else if (asset.getType().equals(AssetType.DATATABLE)) {
                final DatatableAsset datatableAsset = (DatatableAsset) asset;
                try {
                    final DatentabellePdfZaehldaten datentabellePdfZaehldaten = this.fillPdfBeanService
                            .getDatentabellePdfZaehldaten(datatableAsset.getOptions(), datatableAsset.getZaehlungId());
                    datatableAsset.setDatentabelleZaehldaten(datentabellePdfZaehldaten);
                    datatableAsset.setRandomTableId(UUID.randomUUID().toString());

                    sb.append(this.generatePdfService.getHtml(this.dataTableMustache, datatableAsset));
                } catch (final DataNotFoundException dataNotFoundException) {
                    sb.append("Die Datentabelle konnte aufgrund eines technischen Fehlers nicht angezeigt werden.");
                }
            } else if (asset.getType().equals(AssetType.DATATABLE_MESSSTELLE)) {
                final MessstelleDatatableAsset datatableAsset = (MessstelleDatatableAsset) asset;
                try {
                    final DatentabellePdfZaehldaten datentabellePdfZaehldaten = this.fillPdfBeanService
                            .getDatentabellePdfMesswerte(datatableAsset.getOptions(), datatableAsset.getMstId());
                    datatableAsset.setDatentabelleZaehldaten(datentabellePdfZaehldaten);
                    datatableAsset.setRandomTableId(UUID.randomUUID().toString());

//                    DatatableAsset da = new DatatableAsset();
//                    da.setType(AssetType.DATATABLE);
//                    da.setText(datatableAsset.getText());
//                    da.setRandomTableId(datatableAsset.getRandomTableId());
//                    da.setRandomTableId(datatableAsset.getRandomTableId());
//                    da.setDatentabelleZaehldaten(datatableAsset.getDatentabelleZaehldaten());
//                    da.setZaehlungId(datatableAsset.getMstId());

                    sb.append(this.generatePdfService.getHtml(this.dataTableMustache, datatableAsset));
                } catch (final DataNotFoundException dataNotFoundException) {
                    sb.append("Die Datentabelle konnte aufgrund eines technischen Fehlers nicht angezeigt werden.");
                }
            } else if (asset.getType().equals(AssetType.ZAEHLUNGSKENNGROESSEN)) {
                final ZaehlungskenngroessenAsset zaehlungskenngroessenAsset = (ZaehlungskenngroessenAsset) asset;
                try {
                    final Zaehlung zaehlung = this.indexService.getZaehlung(zaehlungskenngroessenAsset.getZaehlungId());
                    zaehlungskenngroessenAsset.setPrintFuss(zaehlung.getKategorien().contains(Fahrzeug.FUSS));
                    zaehlungskenngroessenAsset.setPrintRad(zaehlung.getKategorien().contains(Fahrzeug.RAD));
                    zaehlungskenngroessenAsset.setPrintKfz(zaehlung.getKategorien().contains(Fahrzeug.KFZ));
                    final List<ZaehlungskenngroessenData> data = this.ladeZaehldatumMapper.ladeZaehldatumDtoListToZaehlungskenngroessenDataList(
                            this.processZaehldatenService.ladeZaehlungskenngroessen(zaehlung).getZaehldaten());

                    // Sortiert anhand Type durch den in ZaehlungskenngroessenData definierten compareTo
                    Collections.sort(data);
                    zaehlungskenngroessenAsset.setZaehldaten(data);
                    sb.append(this.generatePdfService.getHtml(this.zaehlungskenngroessenMustache, asset));
                } catch (final DataNotFoundException dataNotFoundException) {
                    sb.append("Die Zählungskenngrößen können aufgrund eines technischen Fehlers nicht angezeigt werden");
                }
            }
        });

        return sb.toString();
    }

    @PostConstruct
    void init() {
        log.info("initialisiere die Report Templates");
        final MustacheFactory mf = new DefaultMustacheFactory();
        this.textAssetMustache = this.generatePdfService.compileMustache(PDF_TEMPLATES_REPORT_PARTS_REPORT_TEXT_ASSET, mf);
        this.imageAssetMustache = this.generatePdfService.compileMustache(PDF_TEMPLATES_REPORT_PARTS_REPORT_IMAGE_ASSET, mf);
        this.heading1AssetMustache = this.generatePdfService.compileMustache(PDF_TEMPLATES_REPORT_PARTS_REPORT_HEADING1_ASSET, mf);
        this.heading2AssetMustache = this.generatePdfService.compileMustache(PDF_TEMPLATES_REPORT_PARTS_REPORT_HEADING2_ASSET, mf);
        this.heading3AssetMustache = this.generatePdfService.compileMustache(PDF_TEMPLATES_REPORT_PARTS_REPORT_HEADING3_ASSET, mf);
        this.heading4AssetMustache = this.generatePdfService.compileMustache(PDF_TEMPLATES_REPORT_PARTS_REPORT_HEADING4_ASSET, mf);
        this.heading5AssetMustache = this.generatePdfService.compileMustache(PDF_TEMPLATES_REPORT_PARTS_REPORT_HEADING5_ASSET, mf);
        this.pagebreakAssetMustache = this.generatePdfService.compileMustache(PDF_TEMPLATES_REPORT_PARTS_REPORT_PAGEBREAK_ASSET, mf);
        this.newlineAssetMustache = this.generatePdfService.compileMustache(PDF_TEMPLATES_REPORT_PARTS_REPORT_NEWLINE_ASSET, mf);
        this.logoAssetMustache = this.generatePdfService.compileMustache(PDF_TEMPLATES_REPORT_PARTS_REPORT_LOGO_ASSET, mf);
        this.dataTableMustache = this.generatePdfService.compileMustache(PDF_TEMPLATES_REPORT_PARTS_REPORT_DATENTABELLE_TABLE, mf);
        this.zaehlungskenngroessenMustache = this.generatePdfService.compileMustache(PDF_TEMPLATES_REPORT_PARTS_REPORT_ZAEHLUNGSKENNGROESSEN, mf);
        this.reportMustache = this.generatePdfService.compileMustache(PDF_TEMPLATES_REPORT, mf);

        // CSS
        this.dataTableCssMustacheCustom = this.generatePdfService.compileMustache(PDF_TEMPLATES_REPORT_PARTS_DATENTABELLE_CSS_CUSTOM, mf);
        this.dataTableCssMustacheFixed = this.generatePdfService.compileMustache(PDF_TEMPLATES_REPORT_PARTS_DATENTABELLE_CSS_FIXED, mf);

    }
}
