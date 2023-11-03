package de.muenchen.dave.services.pdfgenerator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.domain.pdf.components.ZaehlstelleninformationenPdfComponent;
import de.muenchen.dave.domain.pdf.components.ZusatzinformationenPdfComponent;
import de.muenchen.dave.domain.pdf.templates.BasicPdf;
import de.muenchen.dave.spring.services.pdfgenerator.FillPdfBeanServiceSpringTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class FillPdfBeanServiceTest {

    @Autowired
    private FillPdfBeanService fillPdfBeanService;

    @Test
    public void fillBasicPdf() {
        final BasicPdf basicPdf = new BasicPdf();
        final Zaehlung zaehlung = FillPdfBeanServiceSpringTest.getZaehlung();
        final Zaehlstelle zaehlstelle = FillPdfBeanServiceSpringTest.getZaehlstelle(zaehlung);
        final String kreuzungsname = "Kreuzungsname";
        final String department = "TestOU";

        this.fillPdfBeanService.fillBasicPdf(basicPdf, zaehlung, kreuzungsname, zaehlstelle, department);

        assertThat(basicPdf.getFooterOrganisationseinheit(), is("TestOU"));
        assertThat(basicPdf.getFooterDate(), is(LocalDate.now().format(FillPdfBeanService.DDMMYYYY)));
    }

    @Test
    public void fillZaehlstelleninformationen() {
        final ZaehlstelleninformationenPdfComponent zaehlstelleninformationen = new ZaehlstelleninformationenPdfComponent();
        final String kreuzungsname = "Kreuzungsname";
        final Zaehlung zaehlung = FillPdfBeanServiceSpringTest.getZaehlung();

        this.fillPdfBeanService.fillZaehlstelleninformationen(zaehlstelleninformationen, kreuzungsname, zaehlung);
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

        this.fillPdfBeanService.fillZusatzinformationen(zusatzinformationenPdfComponent, zaehlstelle, zaehlung);
        assertThat(zusatzinformationenPdfComponent.isIstKommentarVorhanden(), is(false));
        assertThat(zusatzinformationenPdfComponent.isIstKommentarVorhandenZaehlstelle(), is(false));
        assertThat(zusatzinformationenPdfComponent.isIstKommentarVorhandenZaehlung(), is(false));
        assertThat(zusatzinformationenPdfComponent.getKommentarZaehlung(), is(""));
        assertThat(zusatzinformationenPdfComponent.getKommentarZaehlstelle(), is(""));

        zaehlung.setKommentar("Zaehlung Kommentar");
        zaehlstelle.setKommentar("Zaehlstellenkommentar");
        this.fillPdfBeanService.fillZusatzinformationen(zusatzinformationenPdfComponent, zaehlstelle, zaehlung);
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

        assertThat(this.fillPdfBeanService.createChartTitleFahrbeziehung(optionsDTO, zaehlung), is("Gesamte Zählstelle (Zulauf)"));

        optionsDTO.setVonKnotenarm(1);
        assertThat(this.fillPdfBeanService.createChartTitleFahrbeziehung(optionsDTO, zaehlung), is("von Cosimastr. (1) "));

        optionsDTO.setNachKnotenarm(4);
        assertThat(this.fillPdfBeanService.createChartTitleFahrbeziehung(optionsDTO, zaehlung), is("von Cosimastr. (1) nach Wahnfriedallee (4)"));

        optionsDTO.setVonKnotenarm(null);
        assertThat(this.fillPdfBeanService.createChartTitleFahrbeziehung(optionsDTO, zaehlung), is("nach Wahnfriedallee (4)"));

    }

    @Test
    public void convertZaehldata() {
        assertThat(this.fillPdfBeanService.convertZaehldata(new BigDecimal(102)), is("102"));
        assertThat(this.fillPdfBeanService.convertZaehldata(Integer.valueOf(123)), is("123"));
        assertThat(this.fillPdfBeanService.convertZaehldata(null), is(""));
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

}
