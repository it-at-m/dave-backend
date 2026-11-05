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

    @SuppressWarnings("resource")
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
        new ResourceDatabasePopulator(new ClassPathResource("db/migration/V16__create_zaehlstelle.sql"))
                .execute(dataSource);
    }

    @Test
    void migratesZaehlstellenAndZaehlungen() throws Exception {
        final var zaehlstelle = new Zaehlstelle();
        zaehlstelle.setId("11111111-1111-1111-1111-111111111111");
        zaehlstelle.setNummer("Z-100");
        zaehlstelle.setStadtbezirk("Altstadt-Lehel");
        zaehlstelle.setStadtbezirkNummer(1);
        zaehlstelle.setKommentar("Zählstelle am Marienplatz");
        zaehlstelle.setPunkt(new GeoPoint(48.137154, 11.576124));
        zaehlstelle.setLetzteZaehlungMonatNummer(5);
        zaehlstelle.setLetzteZaehlungMonat("Mai");
        zaehlstelle.setLetzteZaehlungJahr(2024);
        zaehlstelle.setGrundLetzteZaehlung("Turnus");
        zaehlstelle.setSuchwoerter(List.of("zentrum", "innenstadt"));
        zaehlstelle.setCustomSuchwoerter(List.of("marienplatz"));
        zaehlstelle.setSichtbarDatenportal(false);

        final var zaehlung = new de.muenchen.dave.domain.elasticsearch.Zaehlung();
        zaehlung.setId("22222222-2222-2222-2222-222222222222");
        zaehlung.setDatum(LocalDate.of(2025, 5, 20));
        zaehlung.setJahr("2025");
        zaehlung.setMonat("Mai");
        zaehlung.setJahreszeit("Frühling");
        zaehlung.setZaehlart("N");
        zaehlung.setPunkt(new GeoPoint(48.1372, 11.5762));
        zaehlung.setTagesTyp("Werktag");
        zaehlung.setProjektNummer("P-123");
        zaehlung.setProjektName("Innenstadtverkehr");
        zaehlung.setKreuzungsname("Marienplatz");
        zaehlung.setSonderzaehlung(true);
        zaehlung.setKreisverkehr(false);
        zaehlung.setStatus("ACTIVE");
        zaehlung.setZaehlsituation("Normalbetrieb");
        zaehlung.setZaehlsituationErweitert("Keine Besonderheiten");
        zaehlung.setZaehlIntervall(15);
        zaehlung.setWetter("SUNNY");
        zaehlung.setZaehldauer("DAUER_24_STUNDEN");
        zaehlung.setQuelle("MANUALLY");
        zaehlung.setKommentar("Testzählung");
        zaehlung.setSchulZeiten("Schulferien");
        zaehlung.setSuchwoerter(List.of("verkehr", "knoten"));
        zaehlung.setCustomSuchwoerter(List.of("test"));
        zaehlung.setGeographie(List.of("München"));
        zaehlung.setKategorien(List.of(Fahrzeug.KFZ, Fahrzeug.KFZ));
        zaehlung.setUnreadMessagesMobilitaetsreferat(true);
        zaehlung.setUnreadMessagesDienstleister(false);
        zaehlung.setDienstleisterkennung("DL-42");

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

        final var zweiterLaengsverkehr = new Laengsverkehr();
        zweiterLaengsverkehr.setId("33333333-3333-3333-3333-333333333334");
        zweiterLaengsverkehr.setKnotenarm(2);
        zweiterLaengsverkehr.setRichtung(Bewegungsrichtung.AUS);
        zweiterLaengsverkehr.setStrassenseite(Himmelsrichtung.S);
        zweiterLaengsverkehr.setHochrechnungsfaktor(faktor);
        zaehlung.setLaengsverkehr(List.of(laengsverkehr, zweiterLaengsverkehr));

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
        verkehrsbeziehung.setKnotenarm(3);
        verkehrsbeziehung.setHinein(true);
        verkehrsbeziehung.setHeraus(false);
        verkehrsbeziehung.setVorbei(true);
        verkehrsbeziehung.setStrassenseite(Himmelsrichtung.W);
        verkehrsbeziehung.setVonknotvonstrnr("VK-VON-1");
        verkehrsbeziehung.setNachknotvonstrnr("VK-NACH-2");
        verkehrsbeziehung.setVon_strnr("VON-STR");
        verkehrsbeziehung.setVonknotennachstrnr("VKNACH-VON");
        verkehrsbeziehung.setNachknotnachstrnr("NKNACH-NACH");
        verkehrsbeziehung.setNach_strnr("NACH-STR");
        verkehrsbeziehung.setHochrechnungsfaktor(faktor);
        zaehlung.setVerkehrsbeziehungen(List.of(verkehrsbeziehung));
        zaehlstelle.setZaehlungen(List.of(zaehlung));

        final var index = mock(ZaehlstelleIndex.class);
        when(index.findAll(PageRequest.of(0, 500)))
                .thenReturn(new PageImpl<>(List.of(zaehlstelle), PageRequest.of(0, 500), 1));

        try (Connection connection = dataSource.getConnection();
                var statement = connection.prepareStatement(
                        "INSERT INTO hochrechnungsfaktor "
                                + "(id, created_time, version, matrix, kfz, sv, gv, active, default_faktor) "
                                + "VALUES (?, CURRENT_TIMESTAMP, 0, ?, ?, ?, ?, ?, false)")) {
            statement.setObject(1, "66666666-6666-6666-6666-666666666666");
            statement.setString(2, "MATRIX-1");
            statement.setDouble(3, 1.1);
            statement.setDouble(4, 1.2);
            statement.setDouble(5, 1.3);
            statement.setBoolean(6, true);
            statement.executeUpdate();
        }

        try (Connection connection = dataSource.getConnection()) {
            final var context = mock(Context.class);
            when(context.getConnection()).thenReturn(connection);

            new V17__migrate_zaehlstellen_from_elasticsearch(index).migrate(context);
        }

        try (Connection connection = dataSource.getConnection();
                var statement = connection.prepareStatement(
                        "SELECT * FROM zaehlstelle WHERE id = '11111111-1111-1111-1111-111111111111'")) {
            try (var resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString("nummer")).isEqualTo("Z-100");
                assertThat(resultSet.getString("stadtbezirk")).isEqualTo("Altstadt-Lehel");
                assertThat(resultSet.getInt("stadtbezirk_nummer")).isEqualTo(1);
                assertThat(resultSet.getString("kommentar")).isEqualTo("Zählstelle am Marienplatz");
                assertThat(resultSet.getDouble("latitude")).isEqualTo(48.137154);
                assertThat(resultSet.getDouble("longitude")).isEqualTo(11.576124);
                assertThat(resultSet.getInt("letzte_zaehlung_monat_nummer")).isEqualTo(5);
                assertThat(resultSet.getString("letzte_zaehlung_monat")).isEqualTo("Mai");
                assertThat(resultSet.getInt("letzte_zaehlung_jahr")).isEqualTo(2024);
                assertThat(resultSet.getString("grund_letzte_zaehlung")).isEqualTo("Turnus");
                assertThat((String[]) resultSet.getArray("suchwoerter").getArray())
                        .containsExactly("zentrum", "innenstadt");
                assertThat((String[]) resultSet.getArray("custom_suchwoerter").getArray())
                        .containsExactly("marienplatz");
                assertThat(resultSet.getBoolean("sichtbar_datenportal")).isFalse();
                assertThat(resultSet.getObject("created_time")).isNotNull();
                assertThat(resultSet.getLong("version")).isZero();
                assertThat(resultSet.getObject("id")).isNotNull();
            }
        }

        try (Connection connection = dataSource.getConnection();
                var statement = connection.prepareStatement(
                        "SELECT * FROM zaehlung WHERE id = '22222222-2222-2222-2222-222222222222'")) {
            try (var resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getDate("datum")).isEqualTo(Date.valueOf("2025-05-20"));
                assertThat(resultSet.getString("jahr")).isEqualTo("2025");
                assertThat(resultSet.getString("monat")).isEqualTo("Mai");
                assertThat(resultSet.getString("jahreszeit")).isEqualTo("Frühling");
                assertThat(resultSet.getString("zaehlart")).isEqualTo("N");
                assertThat(resultSet.getDouble("latitude")).isEqualTo(48.1372);
                assertThat(resultSet.getDouble("longitude")).isEqualTo(11.5762);
                assertThat(resultSet.getString("tages_typ")).isEqualTo("Werktag");
                assertThat(resultSet.getString("projekt_nummer")).isEqualTo("P-123");
                assertThat(resultSet.getString("projekt_name")).isEqualTo("Innenstadtverkehr");
                assertThat(resultSet.getString("kreuzungsname")).isEqualTo("Marienplatz");
                assertThat(resultSet.getBoolean("sonderzaehlung")).isTrue();
                assertThat(resultSet.getBoolean("kreisverkehr")).isFalse();
                assertThat(resultSet.getString("status")).isEqualTo("ACTIVE");
                assertThat(resultSet.getString("zaehlsituation")).isEqualTo("Normalbetrieb");
                assertThat(resultSet.getString("zaehlsituation_erweitert")).isEqualTo("Keine Besonderheiten");
                assertThat(resultSet.getInt("zaehl_intervall")).isEqualTo(15);
                assertThat(resultSet.getString("wetter")).isEqualTo("SUNNY");
                assertThat(resultSet.getString("zaehldauer")).isEqualTo("DAUER_24_STUNDEN");
                assertThat(resultSet.getString("quelle")).isEqualTo("MANUALLY");
                assertThat(resultSet.getString("kommentar")).isEqualTo("Testzählung");
                assertThat(resultSet.getString("schul_zeiten")).isEqualTo("Schulferien");
                assertThat((String[]) resultSet.getArray("suchwoerter").getArray())
                        .containsExactly("verkehr", "knoten");
                assertThat((String[]) resultSet.getArray("custom_suchwoerter").getArray()).containsExactly("test");
                assertThat((String[]) resultSet.getArray("geographie").getArray()).containsExactly("München");
                assertThat((String[]) resultSet.getArray("kategorien").getArray()).containsExactly("KFZ");
                assertThat(resultSet.getBoolean("unread_messages_mobilitaetsreferat")).isTrue();
                assertThat(resultSet.getBoolean("unread_messages_dienstleister")).isFalse();
                assertThat(resultSet.getString("dienstleisterkennung")).isEqualTo("DL-42");
                assertThat(resultSet.getString("zaehlstelle_id")).isEqualTo("11111111-1111-1111-1111-111111111111");
                assertThat(resultSet.getObject("created_time")).isNotNull();
                assertThat(resultSet.getLong("version")).isZero();
            }
        }

        try (Connection connection = dataSource.getConnection();
                var statement = connection.prepareStatement(
                        "SELECT * FROM knotenarm WHERE zaehlung_id = '22222222-2222-2222-2222-222222222222'")) {
            try (var resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getInt("nummer")).isEqualTo(1);
                assertThat(resultSet.getString("strassenname")).isEqualTo("Teststraße");
                assertThat(resultSet.getString("filename")).isEqualTo("test.json");
                assertThat(resultSet.getString("zaehlung_id")).isEqualTo("22222222-2222-2222-2222-222222222222");
                assertThat(resultSet.getObject("id")).isNotNull();
                assertThat(resultSet.getObject("created_time")).isNotNull();
                assertThat(resultSet.getLong("version")).isZero();
            }
        }

        try (Connection connection = dataSource.getConnection();
                var statement = connection.prepareStatement(
                        "SELECT * FROM laengsverkehr WHERE id = '33333333-3333-3333-3333-333333333333'")) {
            try (var resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString("zaehlung_id")).isEqualTo("22222222-2222-2222-2222-222222222222");
                assertThat(resultSet.getInt("knotenarm")).isEqualTo(1);
                assertThat(resultSet.getString("richtung")).isEqualTo("EIN");
                assertThat(resultSet.getString("strassenseite")).isEqualTo("N");
                assertThat(resultSet.getString("hochrechnungsfaktor_id")).isNotNull();
                assertThat(resultSet.getObject("created_time")).isNotNull();
                assertThat(resultSet.getLong("version")).isZero();
            }
        }

        try (Connection connection = dataSource.getConnection();
                var statement = connection.prepareStatement(
                        "SELECT count(*) FROM laengsverkehr WHERE zaehlung_id = '22222222-2222-2222-2222-222222222222'")) {
            try (var resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getLong(1)).isEqualTo(2);
            }
        }

        try (Connection connection = dataSource.getConnection();
                var statement = connection.prepareStatement(
                        "SELECT * FROM querungsverkehr WHERE id = '44444444-4444-4444-4444-444444444444'")) {
            try (var resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString("zaehlung_id")).isEqualTo("22222222-2222-2222-2222-222222222222");
                assertThat(resultSet.getInt("knotenarm")).isEqualTo(1);
                assertThat(resultSet.getString("richtung")).isEqualTo("S");
                assertThat(resultSet.getString("hochrechnungsfaktor_id")).isNotNull();
                assertThat(resultSet.getObject("created_time")).isNotNull();
                assertThat(resultSet.getLong("version")).isZero();
            }
        }

        try (Connection connection = dataSource.getConnection();
                var statement = connection.prepareStatement(
                        "SELECT * FROM verkehrsbeziehung WHERE id = '55555555-5555-5555-5555-555555555555'")) {
            try (var resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getBoolean("is_kreuzung")).isTrue();
                assertThat(resultSet.getInt("von")).isEqualTo(1);
                assertThat(resultSet.getInt("nach")).isEqualTo(2);
                assertThat(resultSet.getInt("knotenarm")).isEqualTo(3);
                assertThat(resultSet.getBoolean("hinein")).isTrue();
                assertThat(resultSet.getBoolean("heraus")).isFalse();
                assertThat(resultSet.getBoolean("vorbei")).isTrue();
                assertThat(resultSet.getString("strassenseite")).isEqualTo("W");
                assertThat(resultSet.getString("vonknotvonstrnr")).isEqualTo("VK-VON-1");
                assertThat(resultSet.getString("nachknotvonstrnr")).isEqualTo("VK-NACH-2");
                assertThat(resultSet.getString("von_strnr")).isEqualTo("VON-STR");
                assertThat(resultSet.getString("vonknotennachstrnr")).isEqualTo("VKNACH-VON");
                assertThat(resultSet.getString("nachknotnachstrnr")).isEqualTo("NKNACH-NACH");
                assertThat(resultSet.getString("nach_strnr")).isEqualTo("NACH-STR");
                assertThat(resultSet.getString("hochrechnungsfaktor_id")).isNotNull();
                assertThat(resultSet.getObject("created_time")).isNotNull();
                assertThat(resultSet.getLong("version")).isZero();
            }
        }

        try (Connection connection = dataSource.getConnection();
                var statement = connection.prepareStatement(
                        "SELECT * FROM hochrechnungsfaktor WHERE matrix = 'MATRIX-1'")) {
            try (var resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString("matrix")).isEqualTo("MATRIX-1");
                assertThat(resultSet.getDouble("kfz")).isEqualTo(1.1);
                assertThat(resultSet.getDouble("sv")).isEqualTo(1.2);
                assertThat(resultSet.getDouble("gv")).isEqualTo(1.3);
                assertThat(resultSet.getBoolean("active")).isTrue();
                assertThat(resultSet.getBoolean("default_faktor")).isFalse();
                assertThat(resultSet.getObject("id")).isNotNull();
                assertThat(resultSet.getObject("created_time")).isNotNull();
                assertThat(resultSet.getLong("version")).isZero();
            }
        }

        try (Connection connection = dataSource.getConnection();
                var statement = connection.prepareStatement(
                        "SELECT count(*) FROM zaehlstelle")) {
            try (var resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getLong(1)).isEqualTo(1);
                assertThat(resultSet.next()).isFalse();
            }
        }
    }
}
