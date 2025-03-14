package de.muenchen.dave.spring;

import de.muenchen.dave.DaveBackendApplication;
import de.muenchen.dave.domain.Kalendertag;
import de.muenchen.dave.domain.UnauffaelligerTag;
import de.muenchen.dave.domain.dtos.messstelle.AuffaelligeTageDTO;
import de.muenchen.dave.services.KalendertagService;
import de.muenchen.dave.services.messstelle.MessstelleOptionsmenuService;
import de.muenchen.dave.services.messstelle.UnauffaelligeTageService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static de.muenchen.dave.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.dave.TestConstants.SPRING_TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = { DaveBackendApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
                "spring.datasource.url=jdbc:h2:mem:dave;DB_CLOSE_ON_EXIT=FALSE" }
)
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE })
@Slf4j
class MessstelleOptionsmenuServiceTest {

    @MockitoBean
    private UnauffaelligeTageService unauffaelligeTageService;

    @MockitoBean
    private KalendertagService kalendertagService;

    @Autowired
    private MessstelleOptionsmenuService messstelleOptionsmenuService;

    @Test
    public void getAuffaelligeTageForMessstelle() {
        final List<UnauffaelligerTag> unauffaelligeTageForMessstelle = new ArrayList<>();
        final List<Kalendertag> kalendertage = new ArrayList<>();
        final List<LocalDate> dates = new ArrayList<>();
        for (int days = 0; days < 24; days++) {
            final UnauffaelligerTag unauffaelligerTag = new UnauffaelligerTag();
            final Kalendertag kalendertag = new Kalendertag();
            final LocalDate datum = LocalDate.of(2025, 3, 10).minusDays(days);
            kalendertag.setDatum(datum);
            unauffaelligerTag.setKalendertag(kalendertag);
            unauffaelligeTageForMessstelle.add(unauffaelligerTag);
            kalendertage.add(kalendertag);
            dates.add(datum);
        }
        when(unauffaelligeTageService.getUnauffaelligeTageForMessstelle(anyString()))
                .thenReturn(unauffaelligeTageForMessstelle);

        when(kalendertagService.getAllKalendertageWhereDatumNotInExcludedDatesAndDatumIsBeforeLatestDate(any(), any()))
                .thenReturn(kalendertage);

        final AuffaelligeTageDTO expected = new AuffaelligeTageDTO();
        expected.setAuffaelligeTage(dates);

        assertThat(messstelleOptionsmenuService.getAuffaelligeTageForMessstelle("4000"), is(expected));
    }

}
