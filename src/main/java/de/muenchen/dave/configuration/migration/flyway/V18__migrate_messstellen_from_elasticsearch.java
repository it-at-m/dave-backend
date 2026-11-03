package de.muenchen.dave.configuration.migration.flyway;

import de.muenchen.dave.domain.elasticsearch.detektor.Messfaehigkeit;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;

/** Migriert die Messstellen seitenweise aus Elasticsearch nach PostgreSQL. */
@Component
@RequiredArgsConstructor
@Slf4j
public class V18__migrate_messstellen_from_elasticsearch extends BaseJavaMigration {

    private static final int PAGE_SIZE = 500;

    private static final String INSERT_MESSSTELLE_SQL = """
            INSERT INTO messstelle (
                id, created_time, version, mst_id, name, status, realisierungsdatum, abbaudatum, stadtbezirk_nummer,
                bemerkung, fahrzeugklasse, detektierte_verkehrsart, hersteller,
                datum_letzte_plausible_messung, latitude, longitude, sichtbar_datenportal,
                geprueft, kommentar, standort, suchwoerter, custom_suchwoerter, lageplan_vorhanden
            ) VALUES (
                :id, :createdTime, :version, :mstId, :name, :status, :realisierungsdatum, :abbaudatum, :stadtbezirkNummer,
                :bemerkung, :fahrzeugklasse, :detektierteVerkehrsart, :hersteller,
                :datumLetztePlausibleMessung, :latitude, :longitude, :sichtbarDatenportal,
                :geprueft, :kommentar, :standort, CAST(:suchwoerter AS text[]),
                CAST(:customSuchwoerter AS text[]), :lageplanVorhanden
            )
            """;

    private static final String INSERT_MESSQUERSCHNITT_SQL = """
            INSERT INTO messquerschnitt (
                id, created_time, version, messstelle_id, mq_id, latitude, longitude, strassenname,
                lage_messquerschnitt, fahrtrichtung, anzahl_fahrspuren, anzahl_detektoren, standort
            ) VALUES (
                :id, :createdTime, :version, :messstelleId, :mqId, :latitude, :longitude, :strassenname,
                :lageMessquerschnitt, :fahrtrichtung, :anzahlFahrspuren, :anzahlDetektoren, :standort
            )
            """;

    private static final String INSERT_MESSFAEHIGKEIT_SQL = """
            INSERT INTO messfaehigkeit (
                id, created_time, version, messstelle_id, gueltig_ab, gueltig_bis, fahrzeugklasse, intervall
            ) VALUES (
                :id, :createdTime, :version, :messstelleId, :gueltigAb, :gueltigBis, :fahrzeugklasse, :intervall
            )
            """;

    private final MessstelleIndex messstelleIndex;
    private static final Long INITIAL_VERSION = 0L;

    @Override
    public void migrate(final Context context) throws SQLException {
        final var dataSource = new SingleConnectionDataSource(context.getConnection(), true);
        final var jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        Page<Messstelle> page;
        int pageNumber = 0;

        do {
            page = messstelleIndex.findAll(PageRequest.of(pageNumber, PAGE_SIZE));
            final var messstellen = new ArrayList<SqlParameterSource>();
            final var messquerschnitte = new ArrayList<SqlParameterSource>();
            final var messfaehigkeiten = new ArrayList<SqlParameterSource>();

            for (final var messstelle : page.getContent()) {
                messstellen.add(createMessstelleParameterSource(messstelle, context.getConnection()));
                for (final var messquerschnitt : nullSafe(messstelle.getMessquerschnitte())) {
                    messquerschnitte.add(new BeanPropertySqlParameterSource(
                            createMessquerschnittImport(messstelle, messquerschnitt)));
                }
                for (final var messfaehigkeit : nullSafe(messstelle.getMessfaehigkeiten())) {
                    messfaehigkeiten.add(new BeanPropertySqlParameterSource(
                            createMessfaehigkeitImport(messstelle, messfaehigkeit)));
                }
            }

            jdbcTemplate.batchUpdate(INSERT_MESSSTELLE_SQL, messstellen.toArray(SqlParameterSource[]::new));
            jdbcTemplate.batchUpdate(INSERT_MESSQUERSCHNITT_SQL, messquerschnitte.toArray(SqlParameterSource[]::new));
            jdbcTemplate.batchUpdate(INSERT_MESSFAEHIGKEIT_SQL, messfaehigkeiten.toArray(SqlParameterSource[]::new));
            pageNumber++;
        } while (page.hasNext());

        log.info("Messstellen wurden nach PostgreSQL migriert");
    }

    private MessstelleImport createMessstelleImport(final Messstelle source) {
        final var punkt = source.getPunkt();
        var target = MessstelleImport.builder()
                .id(UUID.fromString(source.getId()))
                .createdTime(LocalDateTime.now())
                .version(INITIAL_VERSION)
                .mstId(source.getMstId())
                .name(source.getName())
                .status(enumName(source.getStatus()))
                .realisierungsdatum(source.getRealisierungsdatum())
                .abbaudatum(source.getAbbaudatum())
                .stadtbezirkNummer(source.getStadtbezirkNummer())
                .bemerkung(source.getBemerkung())
                .fahrzeugklasse(enumName(source.getFahrzeugklasse()))
                .detektierteVerkehrsart(enumName(source.getDetektierteVerkehrsart()))
                .hersteller(source.getHersteller())
                .datumLetztePlausibleMessung(source.getDatumLetztePlausibleMessung())
                .latitude(punkt == null ? null : punkt.getLat())
                .longitude(punkt == null ? null : punkt.getLon())
                .sichtbarDatenportal(source.getSichtbarDatenportal())
                .geprueft(source.getGeprueft())
                .kommentar(source.getKommentar())
                .standort(source.getStandort())
                .suchwoerter(source.getSuchwoerter())
                .customSuchwoerter(source.getCustomSuchwoerter())
                .lageplanVorhanden(source.getLageplanVorhanden())
                .build();
        return target;
    }

    private MapSqlParameterSource createMessstelleParameterSource(
            final Messstelle source, final Connection connection) throws SQLException {
        final var importObject = createMessstelleImport(source);
        return new MapSqlParameterSource()
                .addValue("id", importObject.getId())
                .addValue("createdTime", importObject.getCreatedTime())
                .addValue("version", importObject.getVersion())
                .addValue("mstId", importObject.getMstId())
                .addValue("name", importObject.getName())
                .addValue("status", importObject.getStatus())
                .addValue("realisierungsdatum", importObject.getRealisierungsdatum())
                .addValue("abbaudatum", importObject.getAbbaudatum())
                .addValue("stadtbezirkNummer", importObject.getStadtbezirkNummer())
                .addValue("bemerkung", importObject.getBemerkung())
                .addValue("fahrzeugklasse", importObject.getFahrzeugklasse())
                .addValue("detektierteVerkehrsart", importObject.getDetektierteVerkehrsart())
                .addValue("hersteller", importObject.getHersteller())
                .addValue("datumLetztePlausibleMessung", importObject.getDatumLetztePlausibleMessung())
                .addValue("latitude", importObject.getLatitude())
                .addValue("longitude", importObject.getLongitude())
                .addValue("sichtbarDatenportal", importObject.getSichtbarDatenportal())
                .addValue("geprueft", importObject.getGeprueft())
                .addValue("kommentar", importObject.getKommentar())
                .addValue("standort", importObject.getStandort())
                .addValue("suchwoerter", createTextArray(connection, importObject.getSuchwoerter()))
                .addValue("customSuchwoerter", createTextArray(connection, importObject.getCustomSuchwoerter()))
                .addValue("lageplanVorhanden", importObject.getLageplanVorhanden());
    }

    private java.sql.Array createTextArray(final Connection connection, final List<String> values) throws SQLException {
        return values == null ? null : connection.createArrayOf("text", values.toArray());
    }

    private MessquerschnittImport createMessquerschnittImport(
            final Messstelle messstelle, final Messquerschnitt source) {
        final var punkt = source.getPunkt();
        var target = MessquerschnittImport.builder()
                .id(source.getId() == null ? UUID.randomUUID() : UUID.fromString(source.getId()))
                .createdTime(LocalDateTime.now())
                .version(INITIAL_VERSION)
                .messstelleId(UUID.fromString(messstelle.getId())).mqId(source.getMqId())
                .latitude(punkt == null ? null : punkt.getLat()).longitude(punkt == null ? null : punkt.getLon())
                .strassenname(source.getStrassenname()).lageMessquerschnitt(source.getLageMessquerschnitt())
                .fahrtrichtung(source.getFahrtrichtung()).anzahlFahrspuren(source.getAnzahlFahrspuren())
                .anzahlDetektoren(source.getAnzahlDetektoren()).standort(source.getStandort()).build();
        return target;
    }

    private MessfaehigkeitImport createMessfaehigkeitImport(
            final Messstelle messstelle, final Messfaehigkeit source) {
        var target = MessfaehigkeitImport.builder()
                .id(UUID.randomUUID())
                .createdTime(LocalDateTime.now())
                .version(INITIAL_VERSION)
                .messstelleId(UUID.fromString(messstelle.getId()))
                .gueltigAb(source.getGueltigAb()).gueltigBis(source.getGueltigBis())
                .fahrzeugklasse(enumName(source.getFahrzeugklasse())).intervall(enumName(source.getIntervall())).build();
        return target;
    }

    private <T> List<T> nullSafe(final List<T> source) {
        return source == null ? List.of() : source;
    }

    private String enumName(final Enum<?> value) {
        return value == null ? null : value.name();
    }

    @Builder
    @Getter
    private static class MessstelleImport {
        private UUID id;
        private LocalDateTime createdTime;
        private Long version;
        private String mstId;
        private String name;
        private String status;
        private String bemerkung;
        private String fahrzeugklasse;
        private String detektierteVerkehrsart;
        private String hersteller;
        private java.time.LocalDate realisierungsdatum;
        private java.time.LocalDate abbaudatum;
        private java.time.LocalDate datumLetztePlausibleMessung;
        private Integer stadtbezirkNummer;
        private Double latitude;
        private Double longitude;
        private Boolean sichtbarDatenportal;
        private Boolean geprueft;
        private Boolean lageplanVorhanden;
        private String kommentar;
        private String standort;
        private List<String> suchwoerter;
        private List<String> customSuchwoerter;
    }

    @Builder
    @Getter
    private static class MessquerschnittImport {
        private UUID id;
        private LocalDateTime createdTime;
        private Long version;
        private UUID messstelleId;
        private String mqId;
        private String strassenname;
        private String lageMessquerschnitt;
        private String fahrtrichtung;
        private String standort;
        private Double latitude;
        private Double longitude;
        private Integer anzahlFahrspuren;
        private Integer anzahlDetektoren;
    }

    @Builder
    @Getter
    private static class MessfaehigkeitImport {
        private UUID id;
        private LocalDateTime createdTime;
        private Long version;
        private UUID messstelleId;
        private String fahrzeugklasse;
        private String intervall;
        private java.time.LocalDate gueltigAb;
        private java.time.LocalDate gueltigBis;
    }
}
