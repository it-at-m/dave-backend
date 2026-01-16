package de.muenchen.relationalimpl;

import de.muenchen.dave.configuration.CachingConfiguration;
import de.muenchen.dave.domain.analytics.detektor.Messstelle;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessstelleRepository extends JpaRepository<Messstelle, UUID> {

    @Override
    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.OPTIONSMENUE_SETTINGS_FOR_MESSSTELLEN },
            allEntries = true
    )
    void deleteAll();

    @Override
    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.OPTIONSMENUE_SETTINGS_FOR_MESSSTELLEN },
            allEntries = true
    )
    void deleteAll(Iterable<? extends Messstelle> var1);

    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.OPTIONSMENUE_SETTINGS_FOR_MESSSTELLEN },
            allEntries = true
    )
    void deleteById(String var1);

    @Override
    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.OPTIONSMENUE_SETTINGS_FOR_MESSSTELLEN },
            allEntries = true
    )
    void delete(Messstelle var1);

    @Override
    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.OPTIONSMENUE_SETTINGS_FOR_MESSSTELLEN },
            allEntries = true
    )
    <S extends Messstelle> S save(S var1);

    @Override
    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.OPTIONSMENUE_SETTINGS_FOR_MESSSTELLEN },
            allEntries = true
    )
    <S extends Messstelle> List<S> saveAll(Iterable<S> var1);

    @Override
    List<Messstelle> findAll(final Sort sort);

    @Query(value = "select z from Messstelle z order by id")
    Page<Messstelle> suggestSearch(String query, Pageable pageable);

    List<Messstelle> findAllBySichtbarDatenportalIsTrue();

    Optional<Messstelle> findByMstId(String mstId);

    Optional<Messstelle> findByMessquerschnitteId(UUID fromString);

}
