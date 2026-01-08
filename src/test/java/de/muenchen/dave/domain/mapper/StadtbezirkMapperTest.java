package de.muenchen.dave.domain.mapper;

import static de.muenchen.dave.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.dave.TestConstants.SPRING_TEST_PROFILE;

import com.github.javafaker.Faker;
import de.muenchen.dave.DaveBackendApplication;
import de.muenchen.dave.repositories.elasticsearch.CustomSuggestIndex;
import de.muenchen.dave.repositories.elasticsearch.CustomSuggestIndexElasticRepository;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndexElasticRepository;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndexElasticRepository;
import de.muenchen.dave.repositories.relationaldb.ZaehlstelleRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        classes = { DaveBackendApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
                "spring.datasource.url=jdbc:h2:mem:dave;DB_CLOSE_ON_EXIT=FALSE" }
)
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE })
@Slf4j
public class StadtbezirkMapperTest {

    private static final Faker fakerInstance = Faker.instance();
    @Autowired
    private StadtbezirkMapper stadtbezirkMapper;
    @MockitoBean
    private ZaehlstelleIndex zaehlstelleIndex;

    @MockitoBean
    private ZaehlstelleIndexElasticRepository zaehlstelleIndexElasticRepository;

    @MockitoBean
    private ZaehlstelleRepository zaehlstelleRepository;

    @MockitoBean
    private MessstelleIndexElasticRepository messstelleIndexElasticRepository;

    @MockitoBean
    private ZaehlstelleMapper zaehlstelleMapper;

    @MockitoBean
    private CustomSuggestIndexElasticRepository customSuggestIndexElasticRepository;

    @MockitoBean
    private MessstelleIndex messstelleIndex;
    @MockitoBean
    private CustomSuggestIndex customSuggestIndex;

    @Test
    public void testBezeichnungOfWithExistingStadtbezirk() {
        final int stadtbezirkNr = fakerInstance.number().numberBetween(1, 26);
        Assertions.assertThat(stadtbezirkMapper.bezeichnungOf(stadtbezirkNr)).isNotNull().isNotEmpty();
    }

    @Test
    public void testBezeichnungOfWithoutStadtbezirk() {
        final int stadtbezirkNr = 0;
        Assertions.assertThat(stadtbezirkMapper.bezeichnungOf(stadtbezirkNr)).isNotNull().isEmpty();
    }
}
