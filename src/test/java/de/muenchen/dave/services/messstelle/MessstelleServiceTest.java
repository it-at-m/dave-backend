package de.muenchen.dave.services.messstelle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.dtos.messstelle.ReadMessfaehigkeitDTO;
import de.muenchen.dave.domain.elasticsearch.detektor.Messfaehigkeit;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
import de.muenchen.dave.domain.mapper.detektor.MessstelleMapper;
import de.muenchen.dave.domain.mapper.detektor.MessstelleMapperImpl;
import de.muenchen.dave.services.CustomSuggestIndexService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MessstelleServiceTest {

    @Mock
    private MessstelleIndexService messstelleIndexService;

    @Mock
    private CustomSuggestIndexService customSuggestIndexService;

    private final MessstelleMapper messstelleMapper = new MessstelleMapperImpl();

    private final StadtbezirkMapper stadtbezirkMapper = new StadtbezirkMapper();

    private MessstelleService messstelleService;

    @BeforeEach
    public void beforeEach() throws IllegalAccessException {
        Mockito.reset(messstelleIndexService, customSuggestIndexService);
        FieldUtils.writeField(stadtbezirkMapper, "stadtbezirkeMap", new HashMap<String, String>(), true);
        messstelleService = new MessstelleService(
                messstelleIndexService,
                customSuggestIndexService,
                messstelleMapper,
                stadtbezirkMapper);
    }

    @Test
    void getMessfaehigkeitenForZeitraumForMessstelle() {
        final var messstelle = new Messstelle();
        messstelle.setMstId("1234");
        messstelle.setStadtbezirkNummer(1);
        messstelle.setPunkt(new GeoPoint(1d, 2d));

        final var messfaehigkeiten = new ArrayList<Messfaehigkeit>();
        messstelle.setMessfaehigkeiten(messfaehigkeiten);

        var messfaehigkeit = new Messfaehigkeit();
        messfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.ACHT_PLUS_EINS);
        messfaehigkeit.setIntervall(ZaehldatenIntervall.STUNDE_KOMPLETT);
        messfaehigkeit.setGueltigAb(LocalDate.of(2024, 3, 1));
        messfaehigkeit.setGueltigBis(LocalDate.of(2024, 3, 31));
        messfaehigkeiten.add(messfaehigkeit);

        messfaehigkeit = new Messfaehigkeit();
        messfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.ZWEI_PLUS_EINS);
        messfaehigkeit.setIntervall(ZaehldatenIntervall.STUNDE_KOMPLETT);
        messfaehigkeit.setGueltigAb(LocalDate.of(2024, 4, 1));
        messfaehigkeit.setGueltigBis(LocalDate.of(2024, 4, 30));
        messfaehigkeiten.add(messfaehigkeit);

        messfaehigkeit = new Messfaehigkeit();
        messfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.SUMME_KFZ);
        messfaehigkeit.setIntervall(ZaehldatenIntervall.STUNDE_KOMPLETT);
        messfaehigkeit.setGueltigAb(LocalDate.of(2024, 5, 15));
        messfaehigkeit.setGueltigBis(LocalDate.of(2024, 5, 31));
        messfaehigkeiten.add(messfaehigkeit);

        Mockito.when(messstelleIndexService.findByMstIdOrThrowException("1234")).thenReturn(messstelle);

        final var result = messstelleService.getMessfaehigkeitenForZeitraumForMessstelle(
                "1234",
                LocalDate.of(2024, 4, 1),
                LocalDate.of(2024, 4, 30));

        final var expected = new ArrayList<ReadMessfaehigkeitDTO>();
        var expectedMessfaehigkeit = new ReadMessfaehigkeitDTO();
        expectedMessfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.ZWEI_PLUS_EINS);
        expectedMessfaehigkeit.setIntervall(ZaehldatenIntervall.STUNDE_KOMPLETT);
        expectedMessfaehigkeit.setGueltigAb("2024-04-01");
        expectedMessfaehigkeit.setGueltigBis("2024-04-30");
        expected.add(expectedMessfaehigkeit);

        assertThat(result, is(expected));

        Mockito.verify(messstelleIndexService, Mockito.times(1)).findByMstIdOrThrowException("1234");
    }

    @Test
    void isDateBetweenZeitraumInklusive() {
        var date = LocalDate.of(2025, 7, 15);
        var startDateZeitraum = LocalDate.of(2025, 7, 14);
        var endDateZeitraum = LocalDate.of(2025, 7, 16);
        var result = messstelleService.isDateBetweenZeitraumInklusive(date, startDateZeitraum, endDateZeitraum);
        assertThat(result, is(true));

        startDateZeitraum = LocalDate.of(2025, 7, 14);
        endDateZeitraum = LocalDate.of(2025, 7, 15);
        result = messstelleService.isDateBetweenZeitraumInklusive(date, startDateZeitraum, endDateZeitraum);
        assertThat(result, is(true));

        startDateZeitraum = LocalDate.of(2025, 7, 15);
        endDateZeitraum = LocalDate.of(2025, 7, 16);
        result = messstelleService.isDateBetweenZeitraumInklusive(date, startDateZeitraum, endDateZeitraum);
        assertThat(result, is(true));

        startDateZeitraum = LocalDate.of(2025, 7, 15);
        endDateZeitraum = LocalDate.of(2025, 7, 15);
        result = messstelleService.isDateBetweenZeitraumInklusive(date, startDateZeitraum, endDateZeitraum);
        assertThat(result, is(true));

        startDateZeitraum = LocalDate.of(2025, 7, 10);
        endDateZeitraum = LocalDate.of(2025, 7, 14);
        result = messstelleService.isDateBetweenZeitraumInklusive(date, startDateZeitraum, endDateZeitraum);
        assertThat(result, is(false));

        startDateZeitraum = LocalDate.of(2025, 7, 16);
        endDateZeitraum = LocalDate.of(2025, 7, 20);
        result = messstelleService.isDateBetweenZeitraumInklusive(date, startDateZeitraum, endDateZeitraum);
        assertThat(result, is(false));

        startDateZeitraum = null;
        endDateZeitraum = LocalDate.of(2025, 7, 20);
        result = messstelleService.isDateBetweenZeitraumInklusive(date, startDateZeitraum, endDateZeitraum);
        assertThat(result, is(false));

        startDateZeitraum = LocalDate.of(2025, 7, 14);
        endDateZeitraum = null;
        result = messstelleService.isDateBetweenZeitraumInklusive(date, startDateZeitraum, endDateZeitraum);
        assertThat(result, is(false));

        startDateZeitraum = null;
        endDateZeitraum = null;
        result = messstelleService.isDateBetweenZeitraumInklusive(date, startDateZeitraum, endDateZeitraum);
        assertThat(result, is(false));

        date = null;
        startDateZeitraum = null;
        endDateZeitraum = null;
        result = messstelleService.isDateBetweenZeitraumInklusive(date, startDateZeitraum, endDateZeitraum);
        assertThat(result, is(false));
    }

}
