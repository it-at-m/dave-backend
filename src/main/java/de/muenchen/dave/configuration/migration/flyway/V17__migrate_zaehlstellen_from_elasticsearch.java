package de.muenchen.dave.configuration.migration.flyway;

import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;

/**
 * Migriert die Zählstellen seitenweise aus Elasticsearch nach PostgreSQL.
 *
 * <p>
 * Für das Schreiben wird bewusst kein JPA-Repository verwendet. Flyway verwaltet die
 * Transaktion über die in {@link Context} bereitgestellte Connection. Ein JPA-Repository
 * verwendet dagegen einen eigenen EntityManager und kann dadurch eine andere Connection
 * beziehungsweise Transaktion verwenden. Damit die Inserts sicher Teil derselben Flyway-
 * Transaktion sind und bei einem Fehler gemeinsam zurückgerollt werden, erfolgt der Import
 * über ein JdbcTemplate, das an die Flyway-Connection gebunden ist.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class V17__migrate_zaehlstellen_from_elasticsearch
        extends BaseJavaMigration {

    private static final int PAGE_SIZE = 1; // TODO: 500

    private static final String INSERT_ZAEHLSTELLE_SQL = """
            INSERT INTO zaehlstelle (
                 id,
                 created_time,
                version,
                nummer,
                stadtbezirk,
                stadtbezirk_nummer,
                kommentar,
                latitude,
                longitude,
                letzte_zaehlung_monat_nummer,
                letzte_zaehlung_monat,
                letzte_zaehlung_jahr,
                grund_letzte_zaehlung,
                sichtbar_datenportal,
                suchwoerter,
                custom_suchwoerter
            ) VALUES (
                :id,
                :createdTime,
                :version,
                :nummer,
                :stadtbezirk,
                :stadtbezirkNummer,
                :kommentar,
                :latitude,
                :longitude,
                :letzteZaehlungMonatNummer,
                :letzteZaehlungMonat,
                :letzteZaehlungJahr,
                :grundLetzteZaehlung,
                :sichtbarDatenportal,
                 CAST(:suchwoerter AS text[]),
                 CAST(:customSuchwoerter AS text[])
            )
            """;

    private static final String INSERT_ZAEHLUNG_SQL = """
            INSERT INTO zaehlung (
                 id, created_time, version, zaehlstelle_id, datum, jahr, monat, jahreszeit, zaehlart,
                latitude, longitude, tages_typ, projekt_nummer, projekt_name,
                kreuzungsname, sonderzaehlung, kreisverkehr, status,
                zaehlsituation, zaehlsituation_erweitert, zaehl_intervall, wetter,
                zaehldauer, quelle, kommentar, schul_zeiten, suchwoerter,
                 custom_suchwoerter, geographie, kategorien,
                unread_messages_mobilitaetsreferat, unread_messages_dienstleister,
                dienstleisterkennung
            ) VALUES (
                :id, :createdTime, :version, :zaehlstelleId, :datum, :jahr, :monat, :jahreszeit, :zaehlart,
                :latitude, :longitude, :tagesTyp, :projektNummer, :projektName,
                :kreuzungsname, :sonderzaehlung, :kreisverkehr, :status,
                :zaehlsituation, :zaehlsituationErweitert, :zaehlIntervall, :wetter,
                :zaehldauer, :quelle, :kommentar, :schulZeiten, CAST(:suchwoerter AS text[]),
                  CAST(:customSuchwoerter AS text[]), CAST(:geographie AS text[]), CAST(:kategorien AS text[]),
                 :unreadMessagesMobilitaetsreferat,
                :unreadMessagesDienstleister, :dienstleisterkennung
            )
            """;

    private static final String INSERT_KNOTENARM_SQL = """
            INSERT INTO knotenarm (id, created_time, version, zaehlung_id, nummer, strassenname, filename)
            VALUES (:id, :createdTime, :version, :zaehlungId, :nummer, :strassenname, :filename)
            """;

    private static final String INSERT_LAENGSVERKEHR_SQL = """
            INSERT INTO laengsverkehr (
                id, created_time, version, zaehlung_id, hochrechnungsfaktor_id, knotenarm, richtung, strassenseite
            ) VALUES (
                :id, :createdTime, :version, :zaehlungId, :hochrechnungsfaktorId, :knotenarm, :richtung, :strassenseite
            )
            """;

    private static final String INSERT_QUERUNGSVERKEHR_SQL = """
            INSERT INTO querungsverkehr (
                id, created_time, version, zaehlung_id, hochrechnungsfaktor_id, knotenarm, richtung
            ) VALUES (
                :id, :createdTime, :version, :zaehlungId, :hochrechnungsfaktorId, :knotenarm, :richtung
            )
            """;

    private static final String INSERT_VERKEHRSBEZIEHUNG_SQL = """
            INSERT INTO verkehrsbeziehung (
                id, created_time, version, zaehlung_id, is_kreuzung, von, nach, knotenarm, hinein, heraus, vorbei,
                strassenseite, vonknotvonstrnr, nachknotvonstrnr, von_strnr,
                vonknotennachstrnr, nachknotnachstrnr, nach_strnr,
                 hochrechnungsfaktor_id
            ) VALUES (
                :id, :createdTime, :version, :zaehlungId, :isKreuzung, :von, :nach, :knotenarm, :hinein, :heraus, :vorbei,
                 :strassenseite, :vonknotvonstrnr, :nachknotvonstrnr, :vonStrnr,
                 :vonknotennachstrnr, :nachknotnachstrnr, :nachStrnr,
                 :hochrechnungsfaktorId
             )
            """;

    private static final String FIND_HOCHRECHNUNGSFAKTOR_ID_SQL = """
            SELECT id
            FROM hochrechnungsfaktor
            WHERE matrix = :matrix
            """;
    private static final Long INITIAL_VERSION = 0L;

    private final ZaehlstelleIndex zaehlstelleIndex;

    @Override
    public void migrate(final Context context) throws SQLException {
        final var dataSource = new SingleConnectionDataSource(context.getConnection(), true);
        final var jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        Page<Zaehlstelle> page;
        int pageNumber = 0;
        final Map<String, UUID> hochrechnungsfaktoren = new HashMap<>();

        do {
            page = zaehlstelleIndex.findAll(PageRequest.of(pageNumber, PAGE_SIZE));

            final var zaehlstellenParameters = new ArrayList<SqlParameterSource>();
            final var zaehlungenParameters = new ArrayList<SqlParameterSource>();
            final var knotenarmeParameters = new ArrayList<SqlParameterSource>();
            final var laengsverkehrParameters = new ArrayList<SqlParameterSource>();
            final var querungsverkehrParameters = new ArrayList<SqlParameterSource>();
            final var verkehrsbeziehungenParameters = new ArrayList<SqlParameterSource>();
            for (final Zaehlstelle zaehlstelle : page.getContent()) {
                zaehlstellenParameters.add(createZaehlstelleParameterSource(zaehlstelle, context.getConnection()));

                for (final var zaehlung : zaehlstelle.getZaehlungen()) {
                    zaehlungenParameters.add(createZaehlungParameterSource(
                            zaehlstelle, zaehlung, context.getConnection()));
                    if (zaehlung.getKnotenarme() != null) {
                        for (final var knotenarm : zaehlung.getKnotenarme()) {
                            knotenarmeParameters.add(new BeanPropertySqlParameterSource(createImportObject(zaehlung, knotenarm)));
                        }
                    }
                    if (zaehlung.getLaengsverkehr() != null) {
                        for (final var laengsverkehr : zaehlung.getLaengsverkehr()) {
                            final var faktor = laengsverkehr.getHochrechnungsfaktor();
                            final var faktorId = findHochrechnungsfaktorId(faktor, hochrechnungsfaktoren, jdbcTemplate);
                            laengsverkehrParameters.add(new BeanPropertySqlParameterSource(
                                    createImportObject(zaehlung, laengsverkehr, faktorId)));
                        }
                    }
                    if (zaehlung.getQuerungsverkehr() != null) {
                        for (final var querungsverkehr : zaehlung.getQuerungsverkehr()) {
                            final var faktor = querungsverkehr.getHochrechnungsfaktor();
                            final var faktorId = findHochrechnungsfaktorId(faktor, hochrechnungsfaktoren, jdbcTemplate);
                            querungsverkehrParameters.add(new BeanPropertySqlParameterSource(
                                    createImportObject(zaehlung, querungsverkehr, faktorId)));
                        }
                    }
                    if (zaehlung.getVerkehrsbeziehungen() != null) {
                        for (final var verkehrsbeziehung : zaehlung.getVerkehrsbeziehungen()) {
                            final var faktor = verkehrsbeziehung.getHochrechnungsfaktor();
                            final var faktorId = findHochrechnungsfaktorId(faktor, hochrechnungsfaktoren, jdbcTemplate);
                            verkehrsbeziehungenParameters.add(new BeanPropertySqlParameterSource(
                                    createImportObject(zaehlung, verkehrsbeziehung, faktorId)));
                        }
                    }
                }
            }

            jdbcTemplate.batchUpdate(
                    INSERT_ZAEHLSTELLE_SQL, zaehlstellenParameters.toArray(SqlParameterSource[]::new));

            jdbcTemplate.batchUpdate(INSERT_ZAEHLUNG_SQL, zaehlungenParameters.toArray(SqlParameterSource[]::new));
            jdbcTemplate.batchUpdate(INSERT_KNOTENARM_SQL, knotenarmeParameters.toArray(SqlParameterSource[]::new));
            jdbcTemplate.batchUpdate(INSERT_LAENGSVERKEHR_SQL, laengsverkehrParameters.toArray(SqlParameterSource[]::new));
            jdbcTemplate.batchUpdate(INSERT_QUERUNGSVERKEHR_SQL, querungsverkehrParameters.toArray(SqlParameterSource[]::new));
            jdbcTemplate.batchUpdate(
                    INSERT_VERKEHRSBEZIEHUNG_SQL, verkehrsbeziehungenParameters.toArray(SqlParameterSource[]::new));
            pageNumber++;
        } while (page.hasNext());

        log.info("Zaehlstellen wurden nach PostgreSQL migriert");
    }

    private ZaehlstelleImport createImportObject(final Zaehlstelle source) {
        final var punkt = source.getPunkt();

        var target = ZaehlstelleImport.builder()
                .id(UUID.fromString(source.getId()))
                .createdTime(LocalDateTime.now())
                .version(INITIAL_VERSION)
                .nummer(source.getNummer())
                .stadtbezirk(source.getStadtbezirk())
                .stadtbezirkNummer(source.getStadtbezirkNummer())
                .kommentar(source.getKommentar())
                .latitude(punkt == null ? null : punkt.getLat())
                .longitude(punkt == null ? null : punkt.getLon())
                .letzteZaehlungMonatNummer(source.getLetzteZaehlungMonatNummer())
                .letzteZaehlungMonat(source.getLetzteZaehlungMonat())
                .letzteZaehlungJahr(source.getLetzteZaehlungJahr())
                .grundLetzteZaehlung(source.getGrundLetzteZaehlung())
                .suchwoerter(source.getSuchwoerter())
                .customSuchwoerter(source.getCustomSuchwoerter())
                .sichtbarDatenportal(source.getSichtbarDatenportal())
                .build();
        return target;
    }

    private MapSqlParameterSource createZaehlstelleParameterSource(
            final Zaehlstelle source, final Connection connection) throws SQLException {
        final var importObject = createImportObject(source);
        return new MapSqlParameterSource()
                .addValue("id", importObject.getId())
                .addValue("createdTime", importObject.getCreatedTime())
                .addValue("version", importObject.getVersion())
                .addValue("nummer", importObject.getNummer())
                .addValue("stadtbezirk", importObject.getStadtbezirk())
                .addValue("stadtbezirkNummer", importObject.getStadtbezirkNummer())
                .addValue("kommentar", importObject.getKommentar())
                .addValue("latitude", importObject.getLatitude())
                .addValue("longitude", importObject.getLongitude())
                .addValue("letzteZaehlungMonatNummer", importObject.getLetzteZaehlungMonatNummer())
                .addValue("letzteZaehlungMonat", importObject.getLetzteZaehlungMonat())
                .addValue("letzteZaehlungJahr", importObject.getLetzteZaehlungJahr())
                .addValue("grundLetzteZaehlung", importObject.getGrundLetzteZaehlung())
                .addValue("sichtbarDatenportal", importObject.getSichtbarDatenportal())
                .addValue("suchwoerter", createTextArray(connection, importObject.getSuchwoerter()))
                .addValue("customSuchwoerter", createTextArray(connection, importObject.getCustomSuchwoerter()));
    }

    private java.sql.Array createTextArray(final Connection connection, final List<String> values) throws SQLException {
        return values == null ? null : connection.createArrayOf("text", values.toArray());
    }

    private MapSqlParameterSource createZaehlungParameterSource(
            final Zaehlstelle zaehlstelle,
            final de.muenchen.dave.domain.elasticsearch.Zaehlung source,
            final Connection connection) throws SQLException {
        final var importObject = createImportObject(zaehlstelle, source);
        return new MapSqlParameterSource()
                .addValue("id", importObject.getId())
                .addValue("createdTime", importObject.getCreatedTime())
                .addValue("version", importObject.getVersion())
                .addValue("zaehlstelleId", importObject.getZaehlstelleId())
                .addValue("datum", importObject.getDatum())
                .addValue("jahr", importObject.getJahr())
                .addValue("monat", importObject.getMonat())
                .addValue("jahreszeit", importObject.getJahreszeit())
                .addValue("zaehlart", importObject.getZaehlart())
                .addValue("latitude", importObject.getLatitude())
                .addValue("longitude", importObject.getLongitude())
                .addValue("tagesTyp", importObject.getTagesTyp())
                .addValue("projektNummer", importObject.getProjektNummer())
                .addValue("projektName", importObject.getProjektName())
                .addValue("kreuzungsname", importObject.getKreuzungsname())
                .addValue("sonderzaehlung", importObject.getSonderzaehlung())
                .addValue("kreisverkehr", importObject.getKreisverkehr())
                .addValue("status", importObject.getStatus())
                .addValue("zaehlsituation", importObject.getZaehlsituation())
                .addValue("zaehlsituationErweitert", importObject.getZaehlsituationErweitert())
                .addValue("zaehlIntervall", importObject.getZaehlIntervall())
                .addValue("wetter", importObject.getWetter())
                .addValue("zaehldauer", importObject.getZaehldauer())
                .addValue("quelle", importObject.getQuelle())
                .addValue("kommentar", importObject.getKommentar())
                .addValue("schulZeiten", importObject.getSchulZeiten())
                .addValue("suchwoerter", createTextArray(connection, importObject.getSuchwoerter()))
                .addValue("customSuchwoerter", createTextArray(connection, importObject.getCustomSuchwoerter()))
                .addValue("geographie", createTextArray(connection, importObject.getGeographie()))
                .addValue("kategorien", createTextArray(connection, importObject.getKategorien()))
                .addValue("unreadMessagesMobilitaetsreferat", importObject.getUnreadMessagesMobilitaetsreferat())
                .addValue("unreadMessagesDienstleister", importObject.getUnreadMessagesDienstleister())
                .addValue("dienstleisterkennung", importObject.getDienstleisterkennung());
    }

    private ZaehlungImport createImportObject(final Zaehlstelle zaehlstelle, final de.muenchen.dave.domain.elasticsearch.Zaehlung source) {
        final var punkt = source.getPunkt();
        var target = ZaehlungImport.builder()
                .id(UUID.fromString(source.getId()))
                .createdTime(LocalDateTime.now())
                .version(INITIAL_VERSION)
                .zaehlstelleId(UUID.fromString(zaehlstelle.getId()))
                .datum(source.getDatum())
                .jahr(source.getJahr())
                .monat(source.getMonat())
                .jahreszeit(source.getJahreszeit())
                .zaehlart(source.getZaehlart())
                .latitude(punkt == null ? null : punkt.getLat())
                .longitude(punkt == null ? null : punkt.getLon())
                .tagesTyp(source.getTagesTyp())
                .projektNummer(source.getProjektNummer())
                .projektName(source.getProjektName())
                .kreuzungsname(source.getKreuzungsname())
                .sonderzaehlung(source.getSonderzaehlung())
                .kreisverkehr(source.getKreisverkehr())
                .status(source.getStatus())
                .zaehlsituation(source.getZaehlsituation())
                .zaehlsituationErweitert(source.getZaehlsituationErweitert())
                .zaehlIntervall(source.getZaehlIntervall())
                .wetter(source.getWetter())
                .zaehldauer(source.getZaehldauer())
                .quelle(source.getQuelle())
                .kommentar(source.getKommentar())
                .schulZeiten(source.getSchulZeiten())
                .suchwoerter(source.getSuchwoerter())
                .customSuchwoerter(source.getCustomSuchwoerter())
                .geographie(source.getGeographie())
                .kategorien(source.getKategorien() == null
                        ? null
                        : source.getKategorien().stream().map(Enum::name).toList())
                .unreadMessagesMobilitaetsreferat(source.getUnreadMessagesMobilitaetsreferat())
                .unreadMessagesDienstleister(source.getUnreadMessagesDienstleister())
                .dienstleisterkennung(source.getDienstleisterkennung())
                .build();
        return target;
    }

    private KnotenarmImport createImportObject(
            final de.muenchen.dave.domain.elasticsearch.Zaehlung zaehlung,
            final de.muenchen.dave.domain.elasticsearch.Knotenarm source) {
        var target = KnotenarmImport.builder()
                .id(UUID.randomUUID())
                .createdTime(LocalDateTime.now())
                .version(INITIAL_VERSION)
                .nummer(source.getNummer())
                .strassenname(source.getStrassenname())
                .filename(source.getFilename())
                .zaehlungId(UUID.fromString(zaehlung.getId()))
                .build();
        return target;
    }

    private LaengsverkehrImport createImportObject(
            final de.muenchen.dave.domain.elasticsearch.Zaehlung zaehlung,
            final de.muenchen.dave.domain.elasticsearch.Laengsverkehr source,
            final UUID hochrechnungsfaktorId) {
        return LaengsverkehrImport.builder()
                .id(UUID.fromString(source.getId()))
                .createdTime(LocalDateTime.now())
                .version(INITIAL_VERSION)
                .zaehlungId(UUID.fromString(zaehlung.getId()))
                .hochrechnungsfaktorId(hochrechnungsfaktorId)
                .knotenarm(source.getKnotenarm())
                .richtung(source.getRichtung() == null ? null : source.getRichtung().name())
                .strassenseite(source.getStrassenseite() == null ? null : source.getStrassenseite().name())
                .build();
    }

    private QuerungsverkehrImport createImportObject(
            final de.muenchen.dave.domain.elasticsearch.Zaehlung zaehlung,
            final de.muenchen.dave.domain.elasticsearch.Querungsverkehr source,
            final UUID hochrechnungsfaktorId) {
        return QuerungsverkehrImport.builder()
                .id(UUID.fromString(source.getId()))
                .createdTime(LocalDateTime.now())
                .version(INITIAL_VERSION)
                .zaehlungId(UUID.fromString(zaehlung.getId()))
                .hochrechnungsfaktorId(hochrechnungsfaktorId)
                .knotenarm(source.getKnotenarm())
                .richtung(source.getRichtung() == null ? null : source.getRichtung().name())
                .build();
    }

    private VerkehrsbeziehungImport createImportObject(
            final de.muenchen.dave.domain.elasticsearch.Zaehlung zaehlung,
            final de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung source,
            final UUID hochrechnungsfaktorId) {
        var target = VerkehrsbeziehungImport.builder()
                .id(UUID.fromString(source.getId()))
                .createdTime(LocalDateTime.now())
                .version(INITIAL_VERSION)
                .zaehlungId(UUID.fromString(zaehlung.getId()))
                .hochrechnungsfaktorId(hochrechnungsfaktorId)
                .isKreuzung(source.getIsKreuzung()).von(source.getVon()).nach(source.getNach()).knotenarm(source.getKnotenarm())
                .hinein(source.getHinein()).heraus(source.getHeraus()).vorbei(source.getVorbei())
                .strassenseite(source.getStrassenseite() == null ? null : source.getStrassenseite().name())
                .vonknotvonstrnr(source.getVonknotvonstrnr()).nachknotvonstrnr(source.getNachknotvonstrnr())
                .vonStrnr(source.getVon_strnr()).vonknotennachstrnr(source.getVonknotennachstrnr())
                .nachknotnachstrnr(source.getNachknotnachstrnr()).nachStrnr(source.getNach_strnr())
                .build();
        return target;
    }

    private UUID findHochrechnungsfaktorId(
            final de.muenchen.dave.domain.elasticsearch.Hochrechnungsfaktor source,
            final Map<String, UUID> hochrechnungsfaktoren,
            final NamedParameterJdbcTemplate jdbcTemplate) {
        if (source == null) {
            return null;
        }
        if (hochrechnungsfaktoren.containsKey(source.getMatrix())) {
            return hochrechnungsfaktoren.get(source.getMatrix());
        }

        final String faktorIdAsString;
        try {
            faktorIdAsString = jdbcTemplate.queryForObject(
                    FIND_HOCHRECHNUNGSFAKTOR_ID_SQL,
                    new MapSqlParameterSource("matrix", source.getMatrix()),
                    String.class);
        } catch (final EmptyResultDataAccessException exception) {
            throw new IllegalStateException(
                    "Kein Hochrechnungsfaktor für Matrix '" + source.getMatrix() + "' gefunden", exception);
        }
        if (faktorIdAsString == null) {
            throw new IllegalStateException(
                    "Kein Hochrechnungsfaktor für Matrix '" + source.getMatrix() + "' gefunden");
        }
        final UUID faktorId = UUID.fromString(faktorIdAsString);
        hochrechnungsfaktoren.put(source.getMatrix(), faktorId);
        return faktorId;
    }

    /**
     * Ein separates DTO ist hier besser als ZaehlstelleEntity, weil die Migration dadurch unabhängig
     * von JPA bleibt und nur die tatsächlich zu importierenden Felder enthält.
     * Die SQL-Struktur entspricht auch nicht 1:1 der Entity-Struktur.
     * z.B.: SQL erwartet zaehlstelleId, die Entity hat aber eine ZaehlstelleEntity-Relation
     */
    @Builder
    @Getter
    private static class ZaehlstelleImport {
        private UUID id;
        private LocalDateTime createdTime;
        private Long version;
        private String nummer;
        private String stadtbezirk;
        private Integer stadtbezirkNummer;
        private String kommentar;
        private Double latitude;
        private Double longitude;
        private Integer letzteZaehlungMonatNummer;
        private String letzteZaehlungMonat;
        private Integer letzteZaehlungJahr;
        private String grundLetzteZaehlung;
        private Boolean sichtbarDatenportal;
        private List<String> suchwoerter;
        private List<String> customSuchwoerter;
    }

    @Builder
    @Getter
    private static class ZaehlungImport {
        private UUID id;
        private LocalDateTime createdTime;
        private Long version;
        private java.time.LocalDate datum;
        private String jahr;
        private String monat;
        private String jahreszeit;
        private String zaehlart;
        private Double latitude;
        private Double longitude;
        private String tagesTyp;
        private String projektNummer;
        private String projektName;
        private String kreuzungsname;
        private Boolean sonderzaehlung;
        private Boolean kreisverkehr;
        private String status;
        private String zaehlsituation;
        private String zaehlsituationErweitert;
        private Integer zaehlIntervall;
        private String wetter;
        private String zaehldauer;
        private String quelle;
        private String kommentar;
        private String schulZeiten;
        private Boolean sichtbarDatenportal;
        private Boolean unreadMessagesMobilitaetsreferat;
        private Boolean unreadMessagesDienstleister;
        private String dienstleisterkennung;
        private List<String> suchwoerter;
        private List<String> customSuchwoerter;
        private List<String> geographie;
        private List<String> kategorien;
        private UUID zaehlstelleId;

    }

    @Builder
    @Getter
    private static class KnotenarmImport {
        private UUID id;
        private LocalDateTime createdTime;
        private Long version;
        private UUID zaehlungId;
        private Integer nummer;
        private String strassenname;
        private String filename;
    }

    @Builder
    @Getter
    private static class LaengsverkehrImport {
        private UUID id;
        private LocalDateTime createdTime;
        private Long version;
        private UUID hochrechnungsfaktorId;
        private Integer knotenarm;
        private String richtung;
        private String strassenseite;
        private UUID zaehlungId;
    }

    @Builder
    @Getter
    private static class QuerungsverkehrImport {
        private UUID id;
        private LocalDateTime createdTime;
        private Long version;
        private UUID zaehlungId;
        private UUID hochrechnungsfaktorId;
        private Integer knotenarm;
        private String richtung;
    }

    @Builder
    @Getter
    private static class VerkehrsbeziehungImport {
        private UUID id;
        private LocalDateTime createdTime;
        private Long version;
        private UUID zaehlungId;
        private UUID hochrechnungsfaktorId;
        private Boolean isKreuzung;
        private Integer von;
        private Integer nach;
        private Integer knotenarm;
        private Boolean hinein;
        private Boolean heraus;
        private Boolean vorbei;
        private String strassenseite;
        private String vonknotvonstrnr;
        private String nachknotvonstrnr;
        private String vonStrnr;
        private String vonknotennachstrnr;
        private String nachknotnachstrnr;
        private String nachStrnr;
    }

}
