package de.muenchen.dave.spring.services.auswertung;

import de.muenchen.dave.DaveBackendApplication;
import de.muenchen.dave.domain.Fahrbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.laden.LadeAuswertungSpitzenstundeDTO;
import de.muenchen.dave.domain.elasticsearch.PkwEinheit;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.repositories.elasticsearch.CustomSuggestIndex;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.services.auswertung.AuswertungSpitzenstundeService;
import de.muenchen.dave.util.DaveConstants;
import de.muenchen.dave.util.dataimport.ZeitintervallSortingIndexUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static de.muenchen.dave.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.dave.TestConstants.SPRING_TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(
        classes = { DaveBackendApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
                "spring.datasource.url=jdbc:h2:mem:dave;DB_CLOSE_ON_EXIT=FALSE" }
)
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE })
@Slf4j
class AuswertungSpitzenstundeServiceSpringTest {

    @MockBean
    private MessstelleIndex messstelleIndex;

    @MockBean
    private CustomSuggestIndex customSuggestIndex;

    @MockBean
    private ZaehlstelleIndex zaehlstelleIndex;

    @Autowired
    private AuswertungSpitzenstundeService auswertungSpitzenstundeService;

    @Test
    void mapToZaehldatum() {
        final Zeitintervall spitzenstunde = new Zeitintervall();
        spitzenstunde.setZaehlungId(UUID.randomUUID());
        spitzenstunde.setFahrbeziehungId(UUID.randomUUID());
        spitzenstunde.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(7, 15)));
        spitzenstunde.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(8, 15)));
        spitzenstunde.setSortingIndex(ZeitintervallSortingIndexUtil.getSortingIndexSpitzenStundeCompleteDayRad());
        spitzenstunde.setPkw(1);
        spitzenstunde.setLkw(2);
        spitzenstunde.setLastzuege(3);
        spitzenstunde.setBusse(4);
        spitzenstunde.setKraftraeder(5);
        spitzenstunde.setFahrradfahrer(6);
        spitzenstunde.setFussgaenger(7);
        spitzenstunde.setType(TypeZeitintervall.SPITZENSTUNDE_RAD);
        final Fahrbeziehung fahrbeziehung = new Fahrbeziehung();
        fahrbeziehung.setVon(2);
        fahrbeziehung.setNach(3);
        spitzenstunde.setFahrbeziehung(fahrbeziehung);
        final PkwEinheit pkwEinheit = new PkwEinheit();
        pkwEinheit.setPkw(BigDecimal.TEN);
        pkwEinheit.setLkw(BigDecimal.TEN);
        pkwEinheit.setLastzuege(BigDecimal.TEN);
        pkwEinheit.setBusse(BigDecimal.TEN);
        pkwEinheit.setKraftraeder(BigDecimal.TEN);
        pkwEinheit.setFahrradfahrer(BigDecimal.TEN);

        final LadeAuswertungSpitzenstundeDTO result = auswertungSpitzenstundeService.mapToAuswertungSpitzenstundeDTO(spitzenstunde, pkwEinheit);
        final LadeAuswertungSpitzenstundeDTO expected = new LadeAuswertungSpitzenstundeDTO();
        expected.setVon(2);
        expected.setNach(3);
        expected.setType("SpStdTag Rad");
        expected.setStartUhrzeit(LocalTime.of(7, 15));
        expected.setEndeUhrzeit(LocalTime.of(8, 15));
        expected.setPkw(1);
        expected.setLkw(2);
        expected.setLastzuege(3);
        expected.setBusse(4);
        expected.setKraftraeder(5);
        expected.setFahrradfahrer(6);
        expected.setFussgaenger(7);
        expected.setPkwEinheiten(210);

        assertThat(result, is(expected));
    }
}
