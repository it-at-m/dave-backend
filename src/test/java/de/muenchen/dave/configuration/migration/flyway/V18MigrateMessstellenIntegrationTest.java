package de.muenchen.dave.configuration.migration.flyway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.muenchen.dave.domain.elasticsearch.detektor.Messfaehigkeit;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.MessstelleStatus;
import de.muenchen.dave.domain.enums.Verkehrsart;
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
        new ResourceDatabasePopulator(new ClassPathResource("db/migration/V1__dave-Initialisierung.sql"))
                .execute(dataSource);
        new ResourceDatabasePopulator(new ClassPathResource("db/migration/V16__create_zaehlstelle_messstelle.sql"))
                .execute(dataSource);
    }

    @Test
    void migratesMessstellenAndNestedData() throws Exception {
        final var messstelle = new Messstelle();
        messstelle.setId("11111111-1111-1111-1111-111111111111");
        messstelle.setMstId("M-100");
        messstelle.setName("Testmessstelle");
        messstelle.setStatus(MessstelleStatus.IN_BESTAND);
        messstelle.setRealisierungsdatum(LocalDate.of(2020, 1, 2));
        messstelle.setAbbaudatum(LocalDate.of(2030, 1, 2));
        messstelle.setStadtbezirkNummer(1);
        messstelle.setBemerkung("Testbemerkung");
        messstelle.setFahrzeugklasse(Fahrzeugklasse.SUMME_KFZ);
        messstelle.setDetektierteVerkehrsart(Verkehrsart.KFZ);
        messstelle.setHersteller("Testhersteller");
        messstelle.setDatumLetztePlausibleMessung(LocalDate.of(2024, 6, 30));
        messstelle.setPunkt(new GeoPoint(48.137154, 11.576124));
        messstelle.setSichtbarDatenportal(true);
        messstelle.setGeprueft(true);
        messstelle.setKommentar("Testkommentar");
        messstelle.setStandort("Teststandort");
        messstelle.setSuchwoerter(List.of("München", "Test"));
        messstelle.setCustomSuchwoerter(List.of("Integration"));
        messstelle.setLageplanVorhanden(true);

        final var messquerschnitt = new Messquerschnitt();
        messquerschnitt.setId("22222222-2222-2222-2222-222222222222");
        messquerschnitt.setMqId("MQ-100");
        messquerschnitt.setPunkt(new GeoPoint(48.1372, 11.5762));
        messquerschnitt.setStrassenname("Teststraße");
        messquerschnitt.setLageMessquerschnitt("Fahrbahn");
        messquerschnitt.setFahrtrichtung("N");
        messquerschnitt.setAnzahlFahrspuren(2);
        messquerschnitt.setAnzahlDetektoren(3);
        messquerschnitt.setStandort("MQ-Teststandort");
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
            new V18__migrate_messstellen_from_elasticsearch(index).migrate(context);
        }

        try (Connection connection = dataSource.getConnection();
                var statement = connection.prepareStatement(
                        "SELECT * FROM messstelle WHERE id = '11111111-1111-1111-1111-111111111111'")) {
            try (var resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString("id")).isEqualTo("11111111-1111-1111-1111-111111111111");
                assertThat(resultSet.getString("mst_id")).isEqualTo("M-100");
                assertThat(resultSet.getString("name")).isEqualTo("Testmessstelle");
                assertThat(resultSet.getString("status")).isEqualTo("IN_BESTAND");
                assertThat(resultSet.getDate("realisierungsdatum")).isEqualTo(Date.valueOf("2020-01-02"));
                assertThat(resultSet.getDate("abbaudatum")).isEqualTo(Date.valueOf("2030-01-02"));
                assertThat(resultSet.getInt("stadtbezirk_nummer")).isEqualTo(1);
                assertThat(resultSet.getString("bemerkung")).isEqualTo("Testbemerkung");
                assertThat(resultSet.getString("fahrzeugklasse")).isEqualTo("SUMME_KFZ");
                assertThat(resultSet.getString("detektierte_verkehrsart")).isEqualTo("KFZ");
                assertThat(resultSet.getString("hersteller")).isEqualTo("Testhersteller");
                assertThat(resultSet.getDate("datum_letzte_plausible_messung"))
                        .isEqualTo(Date.valueOf("2024-06-30"));
                assertThat(resultSet.getDouble("latitude")).isEqualTo(48.137154);
                assertThat(resultSet.getDouble("longitude")).isEqualTo(11.576124);
                assertThat(resultSet.getBoolean("sichtbar_datenportal")).isTrue();
                assertThat(resultSet.getBoolean("geprueft")).isTrue();
                assertThat(resultSet.getString("kommentar")).isEqualTo("Testkommentar");
                assertThat(resultSet.getString("standort")).isEqualTo("Teststandort");
                assertThat((String[]) resultSet.getArray("suchwoerter").getArray())
                        .containsExactly("München", "Test");
                assertThat((String[]) resultSet.getArray("custom_suchwoerter").getArray())
                        .containsExactly("Integration");
                assertThat(resultSet.getBoolean("lageplan_vorhanden")).isTrue();
                assertThat(resultSet.getObject("created_time")).isNotNull();
                assertThat(resultSet.getLong("version")).isZero();
                assertThat(resultSet.next()).isFalse();
            }
        }

        try (Connection connection = dataSource.getConnection();
                var statement = connection.prepareStatement(
                        "SELECT * FROM messquerschnitt WHERE id = '22222222-2222-2222-2222-222222222222'")) {
            try (var resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString("id")).isEqualTo("22222222-2222-2222-2222-222222222222");
                assertThat(resultSet.getString("messstelle_id")).isEqualTo("11111111-1111-1111-1111-111111111111");
                assertThat(resultSet.getString("mq_id")).isEqualTo("MQ-100");
                assertThat(resultSet.getDouble("latitude")).isEqualTo(48.1372);
                assertThat(resultSet.getDouble("longitude")).isEqualTo(11.5762);
                assertThat(resultSet.getString("strassenname")).isEqualTo("Teststraße");
                assertThat(resultSet.getString("lage_messquerschnitt")).isEqualTo("Fahrbahn");
                assertThat(resultSet.getString("fahrtrichtung")).isEqualTo("N");
                assertThat(resultSet.getInt("anzahl_fahrspuren")).isEqualTo(2);
                assertThat(resultSet.getInt("anzahl_detektoren")).isEqualTo(3);
                assertThat(resultSet.getString("standort")).isEqualTo("MQ-Teststandort");
                assertThat(resultSet.getObject("created_time")).isNotNull();
                assertThat(resultSet.getLong("version")).isZero();
                assertThat(resultSet.next()).isFalse();
            }
        }

        try (Connection connection = dataSource.getConnection();
                var statement = connection.prepareStatement(
                        "SELECT * FROM messfaehigkeit WHERE messstelle_id = '11111111-1111-1111-1111-111111111111'")) {
            try (var resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getObject("id")).isNotNull();
                assertThat(resultSet.getString("messstelle_id")).isEqualTo("11111111-1111-1111-1111-111111111111");
                assertThat(resultSet.getDate("gueltig_ab")).isEqualTo(Date.valueOf("2024-01-01"));
                assertThat(resultSet.getDate("gueltig_bis")).isEqualTo(Date.valueOf("2024-12-31"));
                assertThat(resultSet.getString("fahrzeugklasse")).isEqualTo("SUMME_KFZ");
                assertThat(resultSet.getString("intervall")).isEqualTo("STUNDE_VIERTEL");
                assertThat(resultSet.getObject("created_time")).isNotNull();
                assertThat(resultSet.getLong("version")).isZero();
                assertThat(resultSet.next()).isFalse();
            }
        }
    }
}
