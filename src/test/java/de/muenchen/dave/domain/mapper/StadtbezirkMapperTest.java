package de.muenchen.dave.domain.mapper;

import static de.muenchen.dave.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.dave.TestConstants.SPRING_TEST_PROFILE;

import com.github.javafaker.Faker;
import de.muenchen.dave.DaveBackendApplication;
import de.muenchen.dave.domain.CityDistrictEntity;
import de.muenchen.dave.domain.ConfigurationEntity;
import de.muenchen.dave.domain.enums.ConfigDataTypes;
import de.muenchen.dave.repositories.elasticsearch.CustomSuggestIndex;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.repositories.relationaldb.CityDistrictRepository;
import de.muenchen.dave.repositories.relationaldb.ZaehlstelleRepository;
import de.muenchen.dave.services.ConfigurationService;
import de.muenchen.elasticimpl.CustomSuggestIndexElasticRepository;
import de.muenchen.elasticimpl.MessstelleIndexElasticRepository;
import de.muenchen.elasticimpl.ZaehlstelleIndexElasticRepository;
import de.muenchen.relationalimpl.MessstelleRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
    private ConfigurationService configurationService;

    @Autowired
    private CityDistrictRepository cityDistrictRepository;

    @Autowired
    private StadtbezirkMapper stadtbezirkMapper;

    @MockitoBean
    private ZaehlstelleIndex zaehlstelleIndex;

    @MockitoBean
    private ZaehlstelleIndexElasticRepository zaehlstelleIndexElasticRepository;

    @MockitoBean
    private MessstelleIndexElasticRepository messstelleIndexElasticRepository;

    @MockitoBean
    private CustomSuggestIndexElasticRepository customSuggestIndexElasticRepository;

    @MockitoBean
    private ZaehlstelleRepository zaehlstelleRepository;

    @MockitoBean
    private MessstelleRepository messstelleRepository;

    @MockitoBean
    private ZaehlstelleMapper zaehlstelleMapper;

    @MockitoBean
    private MessstelleIndex messstelleIndex;
    @MockitoBean
    private CustomSuggestIndex customSuggestIndex;

    @BeforeEach
    private void beforeEach() {

        configurationService.deleteAll();
        ConfigurationEntity cityConfig = new ConfigurationEntity();
        cityConfig.setKeyname("city");
        cityConfig.setValuefield("München");
        cityConfig.setCategory("general");
        cityConfig.setDatatype(ConfigDataTypes.STRING);
        configurationService.saveOrUpdate(cityConfig);

        cityDistrictRepository.deleteAll();
        cityDistrictRepository.saveAll(
                java.util.List.of(
                        new CityDistrictEntity("Altstadt-Lehel", "München", 1),
                        new CityDistrictEntity("Ludwigsvorstadt-Isarvorstadt", "München", 2),
                        new CityDistrictEntity("Maxvorstadt", "München", 3),
                        new CityDistrictEntity("Schwabing-West", "München", 4),
                        new CityDistrictEntity("Au-Haidhausen", "München", 5),
                        new CityDistrictEntity("Thalkirchen-Obersendling-Forstenried-Grünwald", "München", 19)));
    }

    @Test
    public void testBezeichnungOfWithExistingStadtbezirk() {
        Assertions.assertThat(configurationService.findAll().size()).isNotZero();
        configurationService.findByKeyname("city").getValuefield();
        final int stadtbezirkNr = fakerInstance.number().numberBetween(1, 5);
        Assertions.assertThat(stadtbezirkMapper.bezeichnungOf(stadtbezirkNr)).isNotNull().isNotEmpty();
    }

    @Test
    public void testBezeichnungOfWithoutStadtbezirk() {
        final int stadtbezirkNr = 0;
        Assertions.assertThat(stadtbezirkMapper.bezeichnungOf(stadtbezirkNr)).isNotNull().isEqualTo("Unbekannt");
    }
}
