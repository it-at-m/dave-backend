package de.muenchen.dave.configuration.migration.flyway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.muenchen.dave.domain.elasticsearch.detektor.Messfaehigkeit;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class V18MigrateMessstellenIntegrationTest {

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
        new ResourceDatabasePopulator(new ClassPathResource("db/migration/V18__create_messstelle_dummy.sql"))
                .execute(dataSource);
    }

    @Test
    void migratesMessstellenAndNestedData() throws Exception {
        final var messstelle = new Messstelle();
        messstelle.setId("11111111-1111-1111-1111-111111111111");
        messstelle.setMstId("M-100");
        messstelle.setName("Testmessstelle");
        messstelle.setRealisierungsdatum(LocalDate.of(2020, 1, 2));
        messstelle.setPunkt(new GeoPoint(48.137154, 11.576124));
        messstelle.setSuchwoerter(List.of("München", "Test"));

        final var messquerschnitt = new Messquerschnitt();
        messquerschnitt.setId("22222222-2222-2222-2222-222222222222");
        messquerschnitt.setMqId("MQ-100");
        messquerschnitt.setPunkt(new GeoPoint(48.1372, 11.5762));
        messquerschnitt.setStrassenname("Teststraße");
        messstelle.setMessquerschnitte(List.of(messquerschnitt));

        final var messfaehigkeit = new Messfaehigkeit();
        messfaehigkeit.setGueltigAb(LocalDate.of(2024, 1, 1));
        messfaehigkeit.setGueltigBis(LocalDate.of(2024, 12, 31));
        messfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.SUMME_KFZ);
        messfaehigkeit.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL);
        messstelle.setMessfaehigkeiten(List.of(messfaehigkeit));

        final var index = mock(MessstelleIndex.class);
        when(index.findAll(PageRequest.of(0, 500)))
                .thenReturn(new PageImpl<>(List.of(messstelle), PageRequest.of(0, 500), 1));

        try (Connection connection = dataSource.getConnection()) {
            final var context = mock(Context.class);
            when(context.getConnection()).thenReturn(connection);
            new V18__migrate_messstellen_from_elasticsearch(index, new com.fasterxml.jackson.databind.ObjectMapper())
                    .migrate(context);
        }

        try (Connection connection = dataSource.getConnection();
                var statement = connection.prepareStatement(
                        "SELECT m.mst_id, m.realisierungsdatum, m.latitude, m.longitude, "
                                + "(SELECT count(*) FROM messquerschnitt WHERE messstelle_id = m.id) AS mq_count, "
                                + "(SELECT mq.strassenname FROM messquerschnitt mq WHERE mq.messstelle_id = m.id) AS strassenname, "
                                + "(SELECT count(*) FROM messfaehigkeit WHERE messstelle_id = m.id) AS mf_count, "
                                + "(SELECT mf.fahrzeugklasse FROM messfaehigkeit mf WHERE mf.messstelle_id = m.id) AS fahrzeugklasse "
                                + "FROM messstelle m")) {
            try (var resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString("mst_id")).isEqualTo("M-100");
                assertThat(resultSet.getDate("realisierungsdatum")).isEqualTo(Date.valueOf("2020-01-02"));
                assertThat(resultSet.getDouble("latitude")).isEqualTo(48.137154);
                assertThat(resultSet.getDouble("longitude")).isEqualTo(11.576124);
                assertThat(resultSet.getInt("mq_count")).isEqualTo(1);
                assertThat(resultSet.getString("strassenname")).isEqualTo("Teststraße");
                assertThat(resultSet.getInt("mf_count")).isEqualTo(1);
                assertThat(resultSet.getString("fahrzeugklasse")).isEqualTo("SUMME_KFZ");
                assertThat(resultSet.next()).isFalse();
            }
        }
    }
}
