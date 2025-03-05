package de.muenchen.dave.services.pdfgenerator;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.elasticsearch.Knotenarm;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Zeitauswahl;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.domain.mapper.ZeitreiheTableOptionsMapperImpl;
import de.muenchen.dave.domain.pdf.templates.ZeitreihePdf;
import de.muenchen.dave.spring.services.pdfgenerator.FillPdfBeanServiceSpringTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;

public class FillZeitreihePdfBeanServiceTest {

    private final FillZeitreihePdfBeanService fillZeitreihePdfBeanService = new FillZeitreihePdfBeanService(null, null, new ZeitreiheTableOptionsMapperImpl(),
            null);

    @Test
    void fillZaehlstelleninformationenZeitreihe() {
        final Zaehlung zaehlung = FillPdfBeanServiceSpringTest.getZaehlung();
        final ZeitreihePdf zeitreihePdf1 = new ZeitreihePdf();

        // Testcase 1
        Knotenarm knotenarm = new Knotenarm();
        knotenarm.setNummer(2);
        knotenarm.setStrassenname("Testingerstraße");
        zaehlung.getKnotenarme().add(knotenarm);
        zaehlung.setKreuzungsname("Superplatz");

        FillZeitreihePdfBeanService.fillZaehlstelleninformationenZeitreihe(zeitreihePdf1, zaehlung);
        assertThat(zeitreihePdf1.getZaehlstelleninformationenZeitreihe().getKnotenarme().get(0).getStrassenname(), is("Cosimastr."));
        assertThat(zeitreihePdf1.getZaehlstelleninformationenZeitreihe().getKnotenarme().get(1).getStrassenname(), is("Testingerstraße"));
        assertThat(zeitreihePdf1.getZaehlstelleninformationenZeitreihe().getKnotenarme().get(2).getStrassenname(), is("Cosimastr"));
        assertThat(zeitreihePdf1.getZaehlstelleninformationenZeitreihe().getKnotenarme().get(3).getStrassenname(), is("Wahnfriedallee"));
        assertThat(zeitreihePdf1.getZaehlstelleninformationenZeitreihe().getPlatz(), is("Superplatz"));
        assertThat(zeitreihePdf1.getZaehlstelleninformationenZeitreihe().isPlatzVorhanden(), is(true));

        // Testcase 2
        ZeitreihePdf zeitreihePdf2 = new ZeitreihePdf();
        zaehlung.setKreuzungsname(zaehlung.getKnotenarme().get(1).getStrassenname() + " - " + zaehlung.getKnotenarme().get(2).getStrassenname());

        FillZeitreihePdfBeanService.fillZaehlstelleninformationenZeitreihe(zeitreihePdf1, zaehlung);
        assertThat(zeitreihePdf2.getZaehlstelleninformationenZeitreihe().getPlatz(), is(emptyOrNullString()));
        assertThat(zeitreihePdf2.getZaehlstelleninformationenZeitreihe().isPlatzVorhanden(), is(false));
    }

    @Test
    void createChartTitleZeitauswahl() {
        final OptionsDTO options = new OptionsDTO();

        // Testcase 1
        options.setZeitauswahl(Zeitauswahl.TAGESWERT.getCapitalizedName());
        assertThat(fillZeitreihePdfBeanService.createChartTitleZeitauswahl(options), is("Tageswert"));
        // Testcase 2
        options.setZeitauswahl(Zeitauswahl.BLOCK.getCapitalizedName());
        options.setZeitblock(Zeitblock.ZB_00_06);
        assertThat(fillZeitreihePdfBeanService.createChartTitleZeitauswahl(options), is("Block 0 - 6 Uhr"));
        // Testcase 3
        options.setZeitauswahl(Zeitauswahl.STUNDE.getCapitalizedName());
        options.setZeitblock(Zeitblock.ZB_02_03);
        assertThat(fillZeitreihePdfBeanService.createChartTitleZeitauswahl(options), is("Stunde 2 - 3 Uhr"));
    }
}
