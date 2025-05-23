package de.muenchen.dave.repositories.elasticsearch;

import de.muenchen.dave.configuration.CachingConfiguration;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MessstelleIndex extends ElasticsearchRepository<Messstelle, String> {

    @CacheEvict(value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL }, allEntries = true)
    void deleteAll();

    @CacheEvict(value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL }, allEntries = true)
    void deleteAll(Iterable<? extends Messstelle> var1);

    @CacheEvict(value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL }, allEntries = true)
    void deleteById(String var1);

    @CacheEvict(value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL }, allEntries = true)
    void delete(Messstelle var1);

    @CacheEvict(value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL }, allEntries = true)
    <S extends Messstelle> S save(S var1);

    @CacheEvict(value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL }, allEntries = true)
    <S extends Messstelle> Iterable<S> saveAll(Iterable<S> var1);

    Optional<Messstelle> findById(String var1);

    @Query(
        "{\"simple_query_string\" : {" +
                "\"query\": \"?0\"," +
                "\"fields\": [\"suchwoerter^3\"]," +
                "\"analyze_wildcard\": true," +
                "\"default_operator\": \"AND\"," +
                "\"lenient\": true}}"
    )
    Page<Messstelle> suggestSearch(String query, Pageable pageable);

    List<Messstelle> findAll();

    List<Messstelle> findAllBySichtbarDatenportalIsTrue();

    Optional<Messstelle> findByMstId(String mstId);

    Optional<Messstelle> findByMessquerschnitteId(String id);
}
