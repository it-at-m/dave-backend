package de.muenchen.dave.spring.services.pdfgenerator;

import de.muenchen.dave.DaveBackendApplication;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.domain.pdf.templates.ZeitreihePdf;
import de.muenchen.dave.services.pdfgenerator.FillZeitreihePdfBeanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static de.muenchen.dave.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.dave.TestConstants.SPRING_TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = { DaveBackendApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:dave;DB_CLOSE_ON_EXIT=FALSE",
        "refarch.gracefulshutdown.pre-wait-seconds=0" })
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE })
class FillZeitreihePdfBeanServiceSpringTest {

    @Autowired
    private FillZeitreihePdfBeanService fillZeitreihePdfBeanService;

    @Test
    void fillZusatzinformationenZeitreihe() {
        final Zaehlung zaehlung1 = FillPdfBeanServiceSpringTest.getZaehlung();
        final Zaehlung zaehlung2 = FillPdfBeanServiceSpringTest.getZaehlung();
        final Zaehlung zaehlung3 = FillPdfBeanServiceSpringTest.getZaehlung();
        zaehlung2.setId("huhu");
        zaehlung3.setId("abcdefghijklmnop");
        zaehlung1.setKommentar("Krasser Kommentar");
        zaehlung3.setKommentar("Beste Zählung");

        zaehlung2.setDatum(LocalDate.of(2009, 4, 3));
        zaehlung3.setDatum(LocalDate.of(2005, 1, 2));

        final OptionsDTO options = FillPdfBeanServiceSpringTest.getChosenOptionsDTO();
        options.setZeitblock(Zeitblock.ZB_06_10);
        final Zaehlstelle zaehlstelle = FillPdfBeanServiceSpringTest.getZaehlstelle(zaehlung1);
        zaehlstelle.getZaehlungen().add(zaehlung2);
        zaehlstelle.getZaehlungen().add(zaehlung3);
        final ZeitreihePdf zeitreihePdf = new ZeitreihePdf();

        options.setIdVergleichszaehlungZeitreihe(zaehlung3.getId());

        fillZeitreihePdfBeanService.fillZusatzinformationenZeitreihe(zeitreihePdf, zaehlung1, options, zaehlstelle);
        assertThat(zeitreihePdf.getZusatzinformationenZeitreihe().size(), is(2));
        assertThat(zeitreihePdf.getZusatzinformationenZeitreihe().get(0).getIdentifier(), is("Januar 2005:"));
        assertThat(zeitreihePdf.getZusatzinformationenZeitreihe().get(1).getIdentifier(), is("November 2020:"));

        zaehlstelle.setKommentar("Zählstellenkommentar");
        fillZeitreihePdfBeanService.fillZusatzinformationenZeitreihe(zeitreihePdf, zaehlung1, options, zaehlstelle);
        assertThat(zeitreihePdf.getZusatzinformationenZeitreihe().size(), is(3));
        assertThat(zeitreihePdf.getZusatzinformationenZeitreihe().get(0).getIdentifier(), is("Zählstellenkommentar:"));
        assertThat(zeitreihePdf.getZusatzinformationenZeitreihe().get(1).getIdentifier(), is("Januar 2005:"));
        assertThat(zeitreihePdf.getZusatzinformationenZeitreihe().get(2).getIdentifier(), is("November 2020:"));

        options.setIdVergleichszaehlungZeitreihe(zaehlung2.getId());
        fillZeitreihePdfBeanService.fillZusatzinformationenZeitreihe(zeitreihePdf, zaehlung1, options, zaehlstelle);
        assertThat(zeitreihePdf.getZusatzinformationenZeitreihe().size(), is(2));
        assertThat(zeitreihePdf.getZusatzinformationenZeitreihe().get(0).getIdentifier(), is("Zählstellenkommentar:"));
        assertThat(zeitreihePdf.getZusatzinformationenZeitreihe().get(1).getIdentifier(), is("November 2020:"));
    }

}
