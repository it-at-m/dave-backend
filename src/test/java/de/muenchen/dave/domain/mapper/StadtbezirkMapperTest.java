package de.muenchen.dave.domain.mapper;

import static de.muenchen.dave.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.dave.TestConstants.SPRING_TEST_PROFILE;

import com.github.javafaker.Faker;
import de.muenchen.dave.DaveBackendApplication;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = { DaveBackendApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:dave;DB_CLOSE_ON_EXIT=FALSE",
        "refarch.gracefulshutdown.pre-wait-seconds=0" })
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE })
@Slf4j
public class StadtbezirkMapperTest {

    @Autowired
    private StadtbezirkMapper stadtbezirkMapper;

    private static final Faker fakerInstance = Faker.instance();

    @Test
    public void testBezeichnungOfWithExistingStadtbezirk() {
        int stadtbezirkNr = fakerInstance.number().numberBetween(1, 26);
        Assertions.assertThat(stadtbezirkMapper.bezeichnungOf(stadtbezirkNr)).isNotNull().isNotEmpty();
    }

    @Test
    public void testBezeichnungOfWithoutStadtbezirk() {
        int stadtbezirkNr = 0;
        Assertions.assertThat(stadtbezirkMapper.bezeichnungOf(stadtbezirkNr)).isNotNull().isEmpty();
    }
}
