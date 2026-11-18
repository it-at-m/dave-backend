package de.muenchen.dave.spring.services.pdfgenerator;

import de.muenchen.dave.DaveBackendApplication;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.domain.pdf.templates.ZeitreihePdf;
import de.muenchen.dave.relational.zaehlstellen.ZaehlstelleEntity;
import de.muenchen.dave.relational.zaehlstellen.ZaehlungEntity;
import de.muenchen.dave.repositories.elasticsearch.CustomSuggestIndex;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.services.pdfgenerator.FillZeitreihePdfBeanService;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static de.muenchen.dave.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.dave.TestConstants.SPRING_TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(
        classes = { DaveBackendApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
                "spring.datasource.url=jdbc:h2:mem:dave;DB_CLOSE_ON_EXIT=FALSE" }
)
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE })
class FillZeitreihePdfBeanServiceSpringTest {

    @MockitoBean
    private ZaehlstelleIndex zaehlstelleIndex;

    @MockitoBean
    private MessstelleIndex messstelleIndex;

    @MockitoBean
    private CustomSuggestIndex customSuggestIndex;

    @Autowired
    private FillZeitreihePdfBeanService fillZeitreihePdfBeanService;

    @Test
    void fillZusatzinformationenZeitreihe() {
        final ZaehlungEntity zaehlung1 = FillPdfBeanServiceSpringTest.getZaehlung();
        final ZaehlungEntity zaehlung2 = FillPdfBeanServiceSpringTest.getZaehlung();
        final ZaehlungEntity zaehlung3 = FillPdfBeanServiceSpringTest.getZaehlung();
        zaehlung2.setId(UUID.fromString("huhu"));
        zaehlung3.setId(UUID.fromString("abcdefghijklmnop"));
        zaehlung1.setKommentar("Krasser Kommentar");
        zaehlung3.setKommentar("Beste Zählung");

        zaehlung2.setDatum(LocalDate.of(2009, 4, 3));
        zaehlung3.setDatum(LocalDate.of(2005, 1, 2));

        final OptionsDTO options = FillPdfBeanServiceSpringTest.getChosenOptionsDTO();
        options.setZeitblock(Zeitblock.ZB_06_10);
        final ZaehlstelleEntity zaehlstelle = FillPdfBeanServiceSpringTest.getZaehlstelle(zaehlung1);
        zaehlstelle.getZaehlungen().add(zaehlung2);
        zaehlstelle.getZaehlungen().add(zaehlung3);
        final ZeitreihePdf zeitreihePdf = new ZeitreihePdf();

        options.setIdVergleichszaehlungZeitreihe(String.valueOf(zaehlung3.getId()));

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

        options.setIdVergleichszaehlungZeitreihe(String.valueOf(zaehlung2.getId()));
        fillZeitreihePdfBeanService.fillZusatzinformationenZeitreihe(zeitreihePdf, zaehlung1, options, zaehlstelle);
        assertThat(zeitreihePdf.getZusatzinformationenZeitreihe().size(), is(2));
        assertThat(zeitreihePdf.getZusatzinformationenZeitreihe().get(0).getIdentifier(), is("Zählstellenkommentar:"));
        assertThat(zeitreihePdf.getZusatzinformationenZeitreihe().get(1).getIdentifier(), is("November 2020:"));
    }

}
