package de.muenchen.dave.repositories.elasticsearch;

import de.muenchen.dave.configuration.CachingConfiguration;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
import java.util.Optional;

public interface ZaehlstelleIndex extends ElasticsearchRepository<Zaehlstelle, String> {

    @CacheEvict(value = { CachingConfiguration.SUCHE_ZAEHLSTELLE, CachingConfiguration.SUCHE_ZAEHLSTELLE_DATENPORTAL,
            CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
            CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO }, allEntries = true)
    void deleteAll();

    @CacheEvict(value = { CachingConfiguration.SUCHE_ZAEHLSTELLE, CachingConfiguration.SUCHE_ZAEHLSTELLE_DATENPORTAL,
            CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
            CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO }, allEntries = true)
    void deleteAll(Iterable<? extends Zaehlstelle> var1);

    @CacheEvict(value = { CachingConfiguration.SUCHE_ZAEHLSTELLE, CachingConfiguration.SUCHE_ZAEHLSTELLE_DATENPORTAL,
            CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
            CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO }, allEntries = true)
    void deleteById(String var1);

    @CacheEvict(value = { CachingConfiguration.SUCHE_ZAEHLSTELLE, CachingConfiguration.SUCHE_ZAEHLSTELLE_DATENPORTAL,
            CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
            CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO }, allEntries = true)
    void delete(Zaehlstelle var1);

    @CacheEvict(value = { CachingConfiguration.SUCHE_ZAEHLSTELLE, CachingConfiguration.SUCHE_ZAEHLSTELLE_DATENPORTAL,
            CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
            CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO }, allEntries = true)
    <S extends Zaehlstelle> S save(S var1);

    @CacheEvict(value = { CachingConfiguration.SUCHE_ZAEHLSTELLE, CachingConfiguration.SUCHE_ZAEHLSTELLE_DATENPORTAL,
            CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
            CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO }, allEntries = true)
    <S extends Zaehlstelle> Iterable<S> saveAll(Iterable<S> var1);

    Optional<Zaehlstelle> findById(String var1);

    @Query("{\"simple_query_string\": {" +
            " \"fields\": [" +
            "\"nummer^5\"," +
            "\"stadtbezirk^2\"," +
            "\"kreuzungsname^4\"," +
            "\"suchwoerter^3\"," +
            "\"zaehlungen.suchwoerter^3\"," +
            "\"zaehlungen.zaehlsituation\"," +
            "\"zaehlungen.zaehlsituationErweitert\"" +
            "]," +
            " \"query\": \"?0\"," +
            " \"analyze_wildcard\": true," +
            " \"default_operator\": \"AND\"," +
            " \"lenient\": true" +
            "}" +
            "}")
    Page<Zaehlstelle> suggestSearch(String query, Pageable pageable);

    @Query("{" +
            "\"query_string\": {" +
            "\"query\": " +
            "\"?0\"" +
            "}" +
            "}")
    Page<Zaehlstelle> findAllByStatus(String query, Pageable pageable);

    List<Zaehlstelle> findAll();

    Optional<Zaehlstelle> findByZaehlungenId(String id);

    List<Zaehlstelle> findAllByNummerStartsWithAndStadtbezirkNummer(String nummer, Integer stadtbezirksnummer);

    Optional<Zaehlstelle> findByNummer(String nummer);

    List<Zaehlstelle> findAllByZaehlungenStatus(String status);

    List<Zaehlstelle> findAllByZaehlungenJahr(String jahr);

    List<Zaehlstelle> findAllByZaehlungenUnreadMessagesMobilitaetsreferatTrue();

    List<Zaehlstelle> findAllByZaehlungenUnreadMessagesDienstleisterTrue();

}
