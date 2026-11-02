package de.muenchen.dave.configuration.migration.flyway;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
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
                sichtbar_datenportal,
                suchwoerter,
                custom_suchwoerter
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
                :sichtbarDatenportal,
                CAST(:suchwoerter AS jsonb),
                CAST(:customSuchwoerter AS jsonb)
            )
            """;

    private static final String INSERT_ZAEHLUNG_SQL = """
            INSERT INTO zaehlung (
                id, zaehlstelle_id, datum, jahr, monat, jahreszeit, zaehlart,
                latitude, longitude, tages_typ, projekt_nummer, projekt_name,
                kreuzungsname, sonderzaehlung, kreisverkehr, status,
                zaehlsituation, zaehlsituation_erweitert, zaehl_intervall, wetter,
                zaehldauer, quelle, kommentar, schul_zeiten, suchwoerter,
                custom_suchwoerter, geographie,
                unread_messages_mobilitaetsreferat, unread_messages_dienstleister,
                dienstleisterkennung
            ) VALUES (
                :id, :zaehlstelleId, :datum, :jahr, :monat, :jahreszeit, :zaehlart,
                :latitude, :longitude, :tagesTyp, :projektNummer, :projektName,
                :kreuzungsname, :sonderzaehlung, :kreisverkehr, :status,
                :zaehlsituation, :zaehlsituationErweitert, :zaehlIntervall, :wetter,
                :zaehldauer, :quelle, :kommentar, :schulZeiten, CAST(:suchwoerter AS jsonb),
                 CAST(:customSuchwoerter AS jsonb), CAST(:geographie AS jsonb),
                 :unreadMessagesMobilitaetsreferat,
                :unreadMessagesDienstleister, :dienstleisterkennung
            )
            """;

    private static final String INSERT_KATEGORIE_SQL = """
            INSERT INTO kategorie (zaehlung_id, wert) VALUES (:zaehlungId, :wert)
            ON CONFLICT DO NOTHING
            """;

    private static final String INSERT_KNOTENARM_SQL = """
            INSERT INTO knotenarm (zaehlung_id, nummer, strassenname, filename)
            VALUES (:zaehlungId, :nummer, :strassenname, :filename)
            """;

    private static final String INSERT_LAENGSVERKEHR_SQL = """
            INSERT INTO laengsverkehr (
                id, zaehlung_id, knotenarm, richtung, strassenseite,
                hochrechnungsfaktor_matrix, hochrechnungsfaktor_kfz,
                hochrechnungsfaktor_sv, hochrechnungsfaktor_gv, hochrechnungsfaktor_active
            ) VALUES (
                :id, :zaehlungId, :knotenarm, :richtung, :strassenseite,
                :matrix, :kfz, :sv, :gv, :active
            )
            """;

    private static final String INSERT_QUERUNGSVERKEHR_SQL = """
            INSERT INTO querungsverkehr (
                id, zaehlung_id, knotenarm, richtung,
                hochrechnungsfaktor_matrix, hochrechnungsfaktor_kfz,
                hochrechnungsfaktor_sv, hochrechnungsfaktor_gv, hochrechnungsfaktor_active
            ) VALUES (
                :id, :zaehlungId, :knotenarm, :richtung,
                :matrix, :kfz, :sv, :gv, :active
            )
            """;

    private static final String INSERT_VERKEHRSBEZIEHUNG_SQL = """
            INSERT INTO verkehrsbeziehung (
                id, zaehlung_id, is_kreuzung, von, nach, knotenarm, hinein, heraus, vorbei,
                strassenseite, vonknotvonstrnr, nachknotvonstrnr, von_strnr,
                vonknotennachstrnr, nachknotnachstrnr, nach_strnr,
                hochrechnungsfaktor_matrix, hochrechnungsfaktor_kfz,
                hochrechnungsfaktor_sv, hochrechnungsfaktor_gv, hochrechnungsfaktor_active
            ) VALUES (
                :id, :zaehlungId, :isKreuzung, :von, :nach, :knotenarm, :hinein, :heraus, :vorbei,
                :strassenseite, :vonknotvonstrnr, :nachknotvonstrnr, :vonStrnr,
                :vonknotennachstrnr, :nachknotnachstrnr, :nachStrnr,
                :matrix, :kfz, :sv, :gv, :active
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
            final var kategorienParameters = new ArrayList<SqlParameterSource>();
            final var knotenarmeParameters = new ArrayList<SqlParameterSource>();
            final var laengsverkehrParameters = new ArrayList<SqlParameterSource>();
            final var querungsverkehrParameters = new ArrayList<SqlParameterSource>();
            final var verkehrsbeziehungenParameters = new ArrayList<SqlParameterSource>();

            for (final Zaehlstelle zaehlstelle : page.getContent()) {
                zaehlstellenParameters.add(new BeanPropertySqlParameterSource(createImportObject(zaehlstelle)));

                for (final var zaehlung : zaehlstelle.getZaehlungen()) {
                    zaehlungenParameters.add(
                            new BeanPropertySqlParameterSource(createImportObject(zaehlstelle, zaehlung)));
                    if (zaehlung.getKategorien() != null) {
                        for (final var kategorie : zaehlung.getKategorien()) {
                            kategorienParameters.add(new BeanPropertySqlParameterSource(createImportObject(zaehlung, kategorie)));
                        }
                    }
                    if (zaehlung.getKnotenarme() != null) {
                        for (final var knotenarm : zaehlung.getKnotenarme()) {
                            knotenarmeParameters.add(new BeanPropertySqlParameterSource(createImportObject(zaehlung, knotenarm)));
                        }
                    }
                    if (zaehlung.getLaengsverkehr() != null) {
                        for (final var laengsverkehr : zaehlung.getLaengsverkehr()) {
                            laengsverkehrParameters.add(new BeanPropertySqlParameterSource(createImportObject(zaehlung, laengsverkehr)));
                        }
                    }
                    if (zaehlung.getQuerungsverkehr() != null) {
                        for (final var querungsverkehr : zaehlung.getQuerungsverkehr()) {
                            querungsverkehrParameters.add(new BeanPropertySqlParameterSource(createImportObject(zaehlung, querungsverkehr)));
                        }
                    }
                    if (zaehlung.getVerkehrsbeziehungen() != null) {
                        for (final var verkehrsbeziehung : zaehlung.getVerkehrsbeziehungen()) {
                            verkehrsbeziehungenParameters.add(new BeanPropertySqlParameterSource(createImportObject(zaehlung, verkehrsbeziehung)));
                        }
                    }
                }
            }

            jdbcTemplate.batchUpdate(
                    INSERT_ZAEHLSTELLE_SQL, zaehlstellenParameters.toArray(SqlParameterSource[]::new));

            jdbcTemplate.batchUpdate(INSERT_ZAEHLUNG_SQL, zaehlungenParameters.toArray(SqlParameterSource[]::new));
            jdbcTemplate.batchUpdate(INSERT_KATEGORIE_SQL, kategorienParameters.toArray(SqlParameterSource[]::new));
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
                .id(source.getId())
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
                .suchwoerter(toJson(source.getSuchwoerter()))
                .customSuchwoerter(toJson(source.getCustomSuchwoerter()))
                .sichtbarDatenportal(source.getSichtbarDatenportal())
                .build();
        return target;
    }

    private ZaehlungImport createImportObject(final Zaehlstelle zaehlstelle, final de.muenchen.dave.domain.elasticsearch.Zaehlung source) {
        final var punkt = source.getPunkt();
        var target = ZaehlungImport.builder()
                .id(source.getId())
                .zaehlstelleId(zaehlstelle.getId())
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
                .suchwoerter(toJson(source.getSuchwoerter()))
                .customSuchwoerter(toJson(source.getCustomSuchwoerter()))
                .geographie(toJson(source.getGeographie()))
                .unreadMessagesMobilitaetsreferat(source.getUnreadMessagesMobilitaetsreferat())
                .unreadMessagesDienstleister(source.getUnreadMessagesDienstleister())
                .dienstleisterkennung(source.getDienstleisterkennung())
                .build();
        return target;
    }

    private KategorieImport createImportObject(
            final de.muenchen.dave.domain.elasticsearch.Zaehlung zaehlung, final de.muenchen.dave.domain.enums.Fahrzeug source) {
        var target = KategorieImport.builder().zaehlungId(zaehlung.getId()).wert(source.name()).build();
        return target;
    }

    private KnotenarmImport createImportObject(
            final de.muenchen.dave.domain.elasticsearch.Zaehlung zaehlung,
            final de.muenchen.dave.domain.elasticsearch.Knotenarm source) {
        var target = KnotenarmImport.builder().zaehlungId(zaehlung.getId()).nummer(source.getNummer())
                .strassenname(source.getStrassenname()).filename(source.getFilename()).build();
        return target;
    }

    private LaengsverkehrImport createImportObject(
            final de.muenchen.dave.domain.elasticsearch.Zaehlung zaehlung,
            final de.muenchen.dave.domain.elasticsearch.Laengsverkehr source) {
        final var faktor = source.getHochrechnungsfaktor();
        var target = LaengsverkehrImport.builder().id(source.getId()).zaehlungId(zaehlung.getId()).knotenarm(source.getKnotenarm())
                .richtung(source.getRichtung() == null ? null : source.getRichtung().name())
                .strassenseite(source.getStrassenseite() == null ? null : source.getStrassenseite().name())
                .matrix(faktor == null ? null : faktor.getMatrix()).kfz(faktor == null ? null : faktor.getKfz())
                .sv(faktor == null ? null : faktor.getSv()).gv(faktor == null ? null : faktor.getGv())
                .active(faktor != null && faktor.isActive()).build();
        return target;
    }

    private QuerungsverkehrImport createImportObject(
            final de.muenchen.dave.domain.elasticsearch.Zaehlung zaehlung,
            final de.muenchen.dave.domain.elasticsearch.Querungsverkehr source) {
        final var faktor = source.getHochrechnungsfaktor();
        var target = QuerungsverkehrImport.builder().id(source.getId()).zaehlungId(zaehlung.getId()).knotenarm(source.getKnotenarm())
                .richtung(source.getRichtung() == null ? null : source.getRichtung().name())
                .matrix(faktor == null ? null : faktor.getMatrix()).kfz(faktor == null ? null : faktor.getKfz())
                .sv(faktor == null ? null : faktor.getSv()).gv(faktor == null ? null : faktor.getGv())
                .active(faktor != null && faktor.isActive()).build();
        return target;
    }

    private VerkehrsbeziehungImport createImportObject(
            final de.muenchen.dave.domain.elasticsearch.Zaehlung zaehlung,
            final de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung source) {
        final var faktor = source.getHochrechnungsfaktor();
        var target = VerkehrsbeziehungImport.builder().id(source.getId()).zaehlungId(zaehlung.getId())
                .isKreuzung(source.getIsKreuzung()).von(source.getVon()).nach(source.getNach()).knotenarm(source.getKnotenarm())
                .hinein(source.getHinein()).heraus(source.getHeraus()).vorbei(source.getVorbei())
                .strassenseite(source.getStrassenseite() == null ? null : source.getStrassenseite().name())
                .vonknotvonstrnr(source.getVonknotvonstrnr()).nachknotvonstrnr(source.getNachknotvonstrnr())
                .vonStrnr(source.getVon_strnr()).vonknotennachstrnr(source.getVonknotennachstrnr())
                .nachknotnachstrnr(source.getNachknotnachstrnr()).nachStrnr(source.getNach_strnr())
                .matrix(faktor == null ? null : faktor.getMatrix()).kfz(faktor == null ? null : faktor.getKfz())
                .sv(faktor == null ? null : faktor.getSv()).gv(faktor == null ? null : faktor.getGv())
                .active(faktor != null && faktor.isActive()).build();
        return target;
    }

    private String toJson(final Object source) {
        try {
            return objectMapper.writeValueAsString(source == null ? List.of() : source);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Elasticsearchdaten konnten nicht als JSON serialisiert werden", e);
        }
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
        private String id;
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
        private String suchwoerter;
        private String customSuchwoerter;
    }

    @Builder
    @Getter
    private static class ZaehlungImport {
        private String id;
        private String zaehlstelleId;
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
        private int zaehlIntervall;
        private String wetter;
        private String zaehldauer;
        private String quelle;
        private String kommentar;
        private String schulZeiten;
        private String suchwoerter;
        private String customSuchwoerter;
//        private String pkwEinheit;
        private String geographie;
        private Boolean unreadMessagesMobilitaetsreferat;
        private Boolean unreadMessagesDienstleister;
        private String dienstleisterkennung;
    }

    @Builder
    @Getter
    private static class KategorieImport {
        private String zaehlungId;
        private String wert;
    }

    @Builder
    @Getter
    private static class KnotenarmImport {
        private String zaehlungId;
        private int nummer;
        @JsonProperty("Strassenname")
        private String strassenname;
        private String filename;
    }

    @Builder
    @Getter
    private static class LaengsverkehrImport {
        private String id;
        private String zaehlungId;
        private HochrechnungsfaktorImport hochrechnungsfaktor;
        private Integer knotenarm;
        private String richtung;
        private String strassenseite;
        private String matrix;
        private Double kfz;
        private Double sv;
        private Double gv;
        private boolean active;
    }

    @Builder
    @Getter
    private static class QuerungsverkehrImport {
        private String id;
        private String zaehlungId;
        private HochrechnungsfaktorImport hochrechnungsfaktor;
        private Integer knotenarm;
        private String richtung;
        private String matrix;
        private Double kfz;
        private Double sv;
        private Double gv;
        private boolean active;
    }

    @Builder
    @Getter
    private static class VerkehrsbeziehungImport {
        private String id;
        private String zaehlungId;
        private HochrechnungsfaktorImport hochrechnungsfaktor;
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
        private String matrix;
        private Double kfz;
        private Double sv;
        private Double gv;
        private boolean active;
    }

    @Builder
    @Getter
    private static class HochrechnungsfaktorImport {
        private String matrix;
        private Double kfz;
        private Double sv;
        private Double gv;
        private boolean active;
    }

}
