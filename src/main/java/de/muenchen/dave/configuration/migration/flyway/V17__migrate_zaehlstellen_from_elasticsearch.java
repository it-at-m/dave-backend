package de.muenchen.dave.configuration.migration.flyway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import java.sql.SQLException;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
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

    private static final int PAGE_SIZE = 500;

    private static final String INSERT_ZAEHLSTELLE_SQL = """
            INSERT INTO zaehlstelle (
                id,
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
                sichtbar_datenportal
            ) VALUES (
                :id,
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
                :sichtbarDatenportal
            )
            """;

    private static final String INSERT_ZAEHLUNG_SQL = """
            INSERT INTO zaehlung (
                id, zaehlstelle_id, datum, jahr, monat, jahreszeit, zaehlart,
                latitude, longitude, tages_typ, projekt_nummer, projekt_name,
                kreuzungsname, sonderzaehlung, kreisverkehr, kategorien, status,
                zaehlsituation, zaehlsituation_erweitert, zaehl_intervall, wetter,
                zaehldauer, quelle, kommentar, schul_zeiten, suchwoerter,
                custom_suchwoerter, pkw_einheit, geographie, knotenarme,
                laengsverkehr, querungsverkehr, verkehrsbeziehungen,
                unread_messages_mobilitaetsreferat, unread_messages_dienstleister,
                dienstleisterkennung
            ) VALUES (
                :id, :zaehlstelleId, :datum, :jahr, :monat, :jahreszeit, :zaehlart,
                :latitude, :longitude, :tagesTyp, :projektNummer, :projektName,
                :kreuzungsname, :sonderzaehlung, :kreisverkehr, CAST(:kategorien AS jsonb), :status,
                :zaehlsituation, :zaehlsituationErweitert, :zaehlIntervall, :wetter,
                :zaehldauer, :quelle, :kommentar, :schulZeiten, CAST(:suchwoerter AS jsonb),
                CAST(:customSuchwoerter AS jsonb), CAST(:pkwEinheit AS jsonb), CAST(:geographie AS jsonb),
                CAST(:knotenarme AS jsonb), CAST(:laengsverkehr AS jsonb), CAST(:querungsverkehr AS jsonb),
                CAST(:verkehrsbeziehungen AS jsonb), :unreadMessagesMobilitaetsreferat,
                :unreadMessagesDienstleister, :dienstleisterkennung
            )
            """;

    private final ZaehlstelleIndex zaehlstelleIndex;
    private final ObjectMapper objectMapper;

    @Override
    public void migrate(final Context context) throws SQLException {
        final var dataSource = new SingleConnectionDataSource(context.getConnection(), true);
        final var jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        Page<Zaehlstelle> page;
        int pageNumber = 0;

        do {
            page = zaehlstelleIndex.findAll(PageRequest.of(pageNumber, PAGE_SIZE));

            final var zaehlstellenParameters = new ArrayList<SqlParameterSource>();
            final var zaehlungenParameters = new ArrayList<SqlParameterSource>();

            for (final Zaehlstelle zaehlstelle : page.getContent()) {
                zaehlstellenParameters.add(new BeanPropertySqlParameterSource(createImportObject(zaehlstelle)));

                for (final var zaehlung : zaehlstelle.getZaehlungen()) {
                    zaehlungenParameters.add(
                            new BeanPropertySqlParameterSource(createImportObject(zaehlstelle, zaehlung)));
                }
            }

            jdbcTemplate.batchUpdate(
                    INSERT_ZAEHLSTELLE_SQL, zaehlstellenParameters.toArray(SqlParameterSource[]::new));

            jdbcTemplate.batchUpdate(INSERT_ZAEHLUNG_SQL, zaehlungenParameters.toArray(SqlParameterSource[]::new));

            pageNumber++;
        } while (page.hasNext());

        log.info("Zaehlstellen wurden nach PostgreSQL migriert");
    }

    private ZaehlstelleImport createImportObject(final Zaehlstelle source) {
        final var punkt = source.getPunkt();

        return new ZaehlstelleImport(
                source.getId(),
                source.getNummer(),
                source.getStadtbezirk(),
                source.getStadtbezirkNummer(),
                source.getKommentar(),
                punkt == null ? null : punkt.getLat(),
                punkt == null ? null : punkt.getLon(),
                source.getLetzteZaehlungMonatNummer(),
                source.getLetzteZaehlungMonat(),
                source.getLetzteZaehlungJahr(),
                source.getGrundLetzteZaehlung(),
                source.getSichtbarDatenportal());
    }

    private ZaehlungImport createImportObject(final Zaehlstelle zaehlstelle, final de.muenchen.dave.domain.elasticsearch.Zaehlung source) {
        try {
            final var punkt = source.getPunkt();
            return new ZaehlungImport(
                    source.getId(),
                    zaehlstelle.getId(),
                    source.getDatum(),
                    source.getJahr(),
                    source.getMonat(),
                    source.getJahreszeit(),
                    source.getZaehlart(),
                    punkt == null ? null : punkt.getLat(),
                    punkt == null ? null : punkt.getLon(),
                    source.getTagesTyp(),
                    source.getProjektNummer(),
                    source.getProjektName(),
                    source.getKreuzungsname(),
                    source.getSonderzaehlung(),
                    source.getKreisverkehr(),
                    toJson(source.getKategorien()),
                    source.getStatus(),
                    source.getZaehlsituation(),
                    source.getZaehlsituationErweitert(),
                    source.getZaehlIntervall(),
                    source.getWetter(),
                    source.getZaehldauer(),
                    source.getQuelle(),
                    source.getKommentar(),
                    source.getSchulZeiten(),
                    toJson(source.getSuchwoerter()),
                    toJson(source.getCustomSuchwoerter()),
                    toJson(source.getPkwEinheit()),
                    toJson(source.getGeographie()),
                    toJson(source.getKnotenarme()),
                    toJson(source.getLaengsverkehr()),
                    toJson(source.getQuerungsverkehr()),
                    toJson(source.getVerkehrsbeziehungen()),
                    source.getUnreadMessagesMobilitaetsreferat(),
                    source.getUnreadMessagesDienstleister(),
                    source.getDienstleisterkennung());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Zaehlung konnte nicht als JSON serialisiert werden", exception);
        }
    }

    private String toJson(final Object value) throws JsonProcessingException {
        return value == null ? null : objectMapper.writeValueAsString(value);
    }

    /**
     * Ein separates DTO ist hier besser als ZaehlstelleEntity, weil die Migration dadurch unabhängig
     * von JPA bleibt und nur die tatsächlich zu importierenden Felder enthält.
     */
    private record ZaehlstelleImport(
            String id,
            String nummer,
            String stadtbezirk,
            Integer stadtbezirkNummer,
            String kommentar,
            Double latitude,
            Double longitude,
            Integer letzteZaehlungMonatNummer,
            String letzteZaehlungMonat,
            Integer letzteZaehlungJahr,
            String grundLetzteZaehlung,
            Boolean sichtbarDatenportal) {
    }

    private record ZaehlungImport(
            String id,
            String zaehlstelleId,
            java.time.LocalDate datum,
            String jahr,
            String monat,
            String jahreszeit,
            String zaehlart,
            Double latitude,
            Double longitude,
            String tagesTyp,
            String projektNummer,
            String projektName,
            String kreuzungsname,
            Boolean sonderzaehlung,
            Boolean kreisverkehr,
            String kategorien,
            String status,
            String zaehlsituation,
            String zaehlsituationErweitert,
            int zaehlIntervall,
            String wetter,
            String zaehldauer,
            String quelle,
            String kommentar,
            String schulZeiten,
            String suchwoerter,
            String customSuchwoerter,
            String pkwEinheit,
            String geographie,
            String knotenarme,
            String laengsverkehr,
            String querungsverkehr,
            String verkehrsbeziehungen,
            Boolean unreadMessagesMobilitaetsreferat,
            Boolean unreadMessagesDienstleister,
            String dienstleisterkennung) {
    }
}
