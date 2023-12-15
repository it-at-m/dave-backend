package de.muenchen.dave.repositories.elasticsearch;

import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MessstelleIndex extends ElasticsearchRepository<Messstelle, String> {

    //    @CacheEvict(value = { CachingConfiguration.SUCHE_ZAEHLSTELLE, CachingConfiguration.SUCHE_ZAEHLSTELLE_DATENPORTAL,
    //            CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
    //            CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO }, allEntries = true)
    void deleteAll();

    //    @CacheEvict(value = { CachingConfiguration.SUCHE_ZAEHLSTELLE, CachingConfiguration.SUCHE_ZAEHLSTELLE_DATENPORTAL,
    //            CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
    //            CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO }, allEntries = true)
    void deleteAll(Iterable<? extends Messstelle> var1);

    //    @CacheEvict(value = { CachingConfiguration.SUCHE_ZAEHLSTELLE, CachingConfiguration.SUCHE_ZAEHLSTELLE_DATENPORTAL,
    //            CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
    //            CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO }, allEntries = true)
    void deleteById(String var1);

    //    @CacheEvict(value = { CachingConfiguration.SUCHE_ZAEHLSTELLE, CachingConfiguration.SUCHE_ZAEHLSTELLE_DATENPORTAL,
    //            CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
    //            CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO }, allEntries = true)
    void delete(Messstelle var1);

    //    @CacheEvict(value = { CachingConfiguration.SUCHE_ZAEHLSTELLE, CachingConfiguration.SUCHE_ZAEHLSTELLE_DATENPORTAL,
    //            CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
    //            CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO }, allEntries = true)
    <S extends Messstelle> S save(S var1);

    //    @CacheEvict(value = { CachingConfiguration.SUCHE_ZAEHLSTELLE, CachingConfiguration.SUCHE_ZAEHLSTELLE_DATENPORTAL,
    //            CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
    //            CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO }, allEntries = true)
    <S extends Messstelle> Iterable<S> saveAll(Iterable<S> var1);

    Optional<Messstelle> findById(String var1);

//    @Query("{\"simple_query_string\": {" +
//            " \"fields\": [" +
//            "\"suchwoerter^3\"" +
//            "]," +
//            " \"query\": \"?0\"," +
//            " \"analyze_wildcard\": true," +
//            " \"default_operator\": \"AND\"," +
//            " \"lenient\": true" +
//            "}" +
//            "}")
    @Query("{\"simple_query_string\" : {\"query\": \"?0\",\"fields\": [\"nummer\"]}}")
    Page<Messstelle> suggestSearch(String query, Pageable pageable);

    List<Messstelle> findAll();

    Optional<Messstelle> findByNummer(String messstellenNummer);

    Optional<Messstelle> findByMessquerschnitteId(String id);
}
