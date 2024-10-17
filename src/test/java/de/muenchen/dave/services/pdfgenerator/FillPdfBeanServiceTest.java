package de.muenchen.dave.services.pdfgenerator;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.domain.elasticsearch.MessstelleRandomFactory;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.domain.pdf.components.MessstelleninformationenPdfComponent;
import de.muenchen.dave.domain.pdf.components.ZaehlstelleninformationenPdfComponent;
import de.muenchen.dave.domain.pdf.components.ZusatzinformationenPdfComponent;
import de.muenchen.dave.domain.pdf.templates.BasicPdf;
import de.muenchen.dave.spring.services.pdfgenerator.FillPdfBeanServiceSpringTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        final var basicPdf = new de.muenchen.dave.domain.pdf.templates.messstelle.BasicPdf();
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
                is(String.format("%s - %s", optionsDTO.getZeitraum().get(0).format(DDMMYYYY), optionsDTO.getZeitraum().get(0).format(DDMMYYYY))));
        assertThat(informationen.getWochentag(), is(tagesTyp));
        assertThat(informationen.isWochentagNeeded(), is(true));
        assertThat(informationen.getKommentar(), is(messstelle.getKommentar()));

        informationen = new MessstelleninformationenPdfComponent();
        optionsDTO.setZeitraum(List.of(LocalDate.now()));
        FillPdfBeanService.fillMessstelleninformationen(informationen, messstelle, optionsDTO, tagesTyp);
        assertThat(informationen.getStandort(), is(messstelle.getStandort()));
        assertThat(informationen.getDetektierteFahrzeuge(), is(messstelle.getDetektierteVerkehrsarten()));
        assertThat(informationen.getMesszeitraum(), is(optionsDTO.getZeitraum().get(0).format(DDMMYYYY)));
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

        final Messquerschnitt messquerschnitt = messstelle.getMessquerschnitte().get(0);
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
}
