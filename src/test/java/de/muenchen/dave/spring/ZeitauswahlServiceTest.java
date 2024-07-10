package de.muenchen.dave.spring;

import static de.muenchen.dave.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.dave.TestConstants.SPRING_TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import de.muenchen.dave.DaveBackendApplication;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlungDTO;
import de.muenchen.dave.domain.dtos.laden.ZeitauswahlDTO;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.repositories.elasticsearch.CustomSuggestIndex;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import de.muenchen.dave.services.ZeitauswahlService;
import de.muenchen.dave.util.DaveConstants;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = { DaveBackendApplication.class },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.datasource.url=jdbc:h2:mem:dave;DB_CLOSE_ON_EXIT=FALSE"}
)
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE })
@Slf4j
class ZeitauswahlServiceTest {

    @MockBean
    private ZaehlstelleIndex zaehlstelleIndex;

    @MockBean
    private MessstelleIndex messstelleIndex;

    @MockBean
    private CustomSuggestIndex customSuggestIndex;

    @MockBean
    private ZeitintervallRepository zeitintervallRepository;

    @Autowired
    private ZeitauswahlService zeitauswahlService;

    @Test
    public void determinePossibleZeitauswahl() {
        final LadeZaehlungDTO ladeZaehlung = new LadeZaehlungDTO();
        ladeZaehlung.setId(UUID.randomUUID().toString());
        ladeZaehlung.setZaehldauer(Zaehldauer.SONSTIGE.name());

        final List<Zeitintervall> zeitintervalle = new ArrayList<>();

        for (int hour = 0; hour < 24; hour++) {
            for (int quarter = 0; quarter < 50; quarter += 15) {
                Zeitintervall intervall = new Zeitintervall();
                if (hour == 0) {
                    intervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(LocalTime.MIDNIGHT.getHour(), quarter)));
                } else {
                    intervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(hour, quarter)));
                }
                zeitintervalle.add(intervall);
            }
        }

        when(zeitintervallRepository.findByZaehlungId(UUID.fromString(ladeZaehlung.getId()), Sort.by(Sort.Direction.ASC, "startUhrzeit")))
                .thenReturn(zeitintervalle);

        final ZeitauswahlDTO choosableZeitauswahlDTO = zeitauswahlService.determinePossibleZeitauswahl(ladeZaehlung.getZaehldauer(), ladeZaehlung.getId(),
                false);

        assertThat(choosableZeitauswahlDTO, is(notNullValue()));
        assertThat(choosableZeitauswahlDTO.getBlocks(), is(notNullValue()));
        assertThat(choosableZeitauswahlDTO.getHours(), is(notNullValue()));

        assertThat(choosableZeitauswahlDTO.getBlocks().size(), is(5));
        assertThat(choosableZeitauswahlDTO.getHours().size(), is(24));
    }

}
