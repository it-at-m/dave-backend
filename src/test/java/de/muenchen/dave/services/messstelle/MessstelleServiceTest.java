package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.mapper.FahrzeugklassenMapperImpl;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
import de.muenchen.dave.domain.mapper.detektor.MessstelleMapper;
import de.muenchen.dave.domain.mapper.detektor.MessstelleMapperImpl;
import de.muenchen.dave.domain.mapper.detektor.MessstelleReceiverMapperImpl;
import de.muenchen.dave.services.CustomSuggestIndexService;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
        messstelleService = new MessstelleService(
                messstelleIndexService,
                customSuggestIndexService,
                messstelleMapper,
                stadtbezirkMapper);
    }

    @Test
    void getMessfaehigkeitenForZeitraumForMessstelle() {

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