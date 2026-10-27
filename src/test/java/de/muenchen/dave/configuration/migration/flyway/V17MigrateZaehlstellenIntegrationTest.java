package de.muenchen.dave.configuration.migration.flyway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class V17MigrateZaehlstellenIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("dave")
            .withUsername("dave")
            .withPassword("dave");

    private static DriverManagerDataSource dataSource;

    @BeforeAll
    static void setUpDatabase() {
        dataSource = new DriverManagerDataSource(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        new ResourceDatabasePopulator(new ClassPathResource("db/migration/V16__create_zaehlstelle_dummy.sql"))
                .execute(dataSource);
    }

    @Test
    void migratesZaehlstellenAndZaehlungen() throws Exception {
        final var zaehlstelle = new Zaehlstelle();
        zaehlstelle.setId("11111111-1111-1111-1111-111111111111");
        zaehlstelle.setNummer("Z-100");
        zaehlstelle.setStadtbezirk("Altstadt-Lehel");
        zaehlstelle.setPunkt(new GeoPoint(48.137154, 11.576124));

        final var zaehlung = new de.muenchen.dave.domain.elasticsearch.Zaehlung();
        zaehlung.setId("22222222-2222-2222-2222-222222222222");
        zaehlung.setDatum(LocalDate.of(2025, 5, 20));
        zaehlung.setZaehlart("Knotenpunktzählung");
        zaehlung.setPunkt(new GeoPoint(48.1372, 11.5762));
        zaehlstelle.setZaehlungen(List.of(zaehlung));

        final var index = mock(ZaehlstelleIndex.class);
        when(index.findAll(PageRequest.of(0, 500)))
                .thenReturn(new PageImpl<>(List.of(zaehlstelle), PageRequest.of(0, 500), 1));

        try (Connection connection = dataSource.getConnection()) {
            final var context = mock(Context.class);
            when(context.getConnection()).thenReturn(connection);

            new V17__migrate_zaehlstellen_from_elasticsearch(index, new com.fasterxml.jackson.databind.ObjectMapper())
                    .migrate(context);
        }

        try (Connection connection = dataSource.getConnection();
                var statement = connection.prepareStatement(
                        "SELECT z.nummer, za.datum, za.latitude, za.longitude "
                                + "FROM zaehlstelle z JOIN zaehlung za ON za.zaehlstelle_id = z.id")) {
            try (var resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString("nummer")).isEqualTo("Z-100");
                assertThat(resultSet.getDate("datum")).isEqualTo(Date.valueOf("2025-05-20"));
                assertThat(resultSet.getDouble("latitude")).isEqualTo(48.1372);
                assertThat(resultSet.getDouble("longitude")).isEqualTo(11.5762);
                assertThat(resultSet.next()).isFalse();
            }
        }
    }
}
