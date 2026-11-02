package de.muenchen.dave.configuration.migration.flyway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.muenchen.dave.domain.elasticsearch.Hochrechnungsfaktor;
import de.muenchen.dave.domain.elasticsearch.Knotenarm;
import de.muenchen.dave.domain.elasticsearch.Laengsverkehr;
import de.muenchen.dave.domain.elasticsearch.Querungsverkehr;
import de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.enums.Bewegungsrichtung;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
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
        zaehlung.setKategorien(List.of(Fahrzeug.KFZ));

        final var knotenarm = new Knotenarm();
        knotenarm.setNummer(1);
        knotenarm.setStrassenname("Teststraße");
        knotenarm.setFilename("test.json");
        zaehlung.setKnotenarme(List.of(knotenarm));

        final var faktor = new Hochrechnungsfaktor();
        faktor.setMatrix("MATRIX-1");
        faktor.setKfz(1.1);
        faktor.setSv(1.2);
        faktor.setGv(1.3);
        faktor.setActive(true);

        final var laengsverkehr = new Laengsverkehr();
        laengsverkehr.setId("33333333-3333-3333-3333-333333333333");
        laengsverkehr.setKnotenarm(1);
        laengsverkehr.setRichtung(Bewegungsrichtung.EIN);
        laengsverkehr.setStrassenseite(Himmelsrichtung.N);
        laengsverkehr.setHochrechnungsfaktor(faktor);
        zaehlung.setLaengsverkehr(List.of(laengsverkehr));

        final var querungsverkehr = new Querungsverkehr();
        querungsverkehr.setId("44444444-4444-4444-4444-444444444444");
        querungsverkehr.setKnotenarm(1);
        querungsverkehr.setRichtung(Himmelsrichtung.S);
        querungsverkehr.setHochrechnungsfaktor(faktor);
        zaehlung.setQuerungsverkehr(List.of(querungsverkehr));

        final var verkehrsbeziehung = new Verkehrsbeziehung();
        verkehrsbeziehung.setId("55555555-5555-5555-5555-555555555555");
        verkehrsbeziehung.setIsKreuzung(true);
        verkehrsbeziehung.setVon(1);
        verkehrsbeziehung.setNach(2);
        verkehrsbeziehung.setHinein(true);
        verkehrsbeziehung.setHochrechnungsfaktor(faktor);
        zaehlung.setVerkehrsbeziehungen(List.of(verkehrsbeziehung));
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
                        "SELECT z.nummer, za.datum, za.latitude, za.longitude, "
                                + "(SELECT count(*) FROM kategorie WHERE zaehlung_id = za.id) AS kategorien_count, "
                                + "(SELECT count(*) FROM knotenarm WHERE zaehlung_id = za.id) AS knotenarme_count, "
                                + "(SELECT strassenname FROM knotenarm WHERE zaehlung_id = za.id) AS strassenname, "
                                + "(SELECT count(*) FROM laengsverkehr WHERE zaehlung_id = za.id) AS laengsverkehr_count, "
                                + "(SELECT richtung FROM laengsverkehr WHERE zaehlung_id = za.id) AS laengsrichtung, "
                                + "(SELECT count(*) FROM querungsverkehr WHERE zaehlung_id = za.id) AS querungsverkehr_count, "
                                + "(SELECT count(*) FROM verkehrsbeziehung WHERE zaehlung_id = za.id) AS verkehrsbeziehungen_count "
                                + "FROM zaehlstelle z JOIN zaehlung za ON za.zaehlstelle_id = z.id")) {
            try (var resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString("nummer")).isEqualTo("Z-100");
                assertThat(resultSet.getDate("datum")).isEqualTo(Date.valueOf("2025-05-20"));
                assertThat(resultSet.getDouble("latitude")).isEqualTo(48.1372);
                assertThat(resultSet.getDouble("longitude")).isEqualTo(11.5762);
                assertThat(resultSet.getInt("kategorien_count")).isEqualTo(1);
                assertThat(resultSet.getInt("knotenarme_count")).isEqualTo(1);
                assertThat(resultSet.getString("strassenname")).isEqualTo("Teststraße");
                assertThat(resultSet.getInt("laengsverkehr_count")).isEqualTo(1);
                assertThat(resultSet.getString("laengsrichtung")).isEqualTo("EIN");
                assertThat(resultSet.getInt("querungsverkehr_count")).isEqualTo(1);
                assertThat(resultSet.getInt("verkehrsbeziehungen_count")).isEqualTo(1);
                assertThat(resultSet.next()).isFalse();
            }
        }
    }
}
