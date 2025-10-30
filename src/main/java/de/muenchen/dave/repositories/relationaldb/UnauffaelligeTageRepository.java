package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.configuration.CachingConfiguration;
import de.muenchen.dave.domain.UnauffaelligerTag;
import de.muenchen.dave.domain.enums.TagesTyp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnauffaelligeTageRepository extends JpaRepository<UnauffaelligerTag, UUID> {

    @CacheEvict(
            value = { CachingConfiguration.AUFFAELLIGETAGE_FOR_MESSSTELLE },
            allEntries = true
    )
    void deleteAll();

    @CacheEvict(
            value = { CachingConfiguration.AUFFAELLIGETAGE_FOR_MESSSTELLE },
            allEntries = true
    )
    void deleteAll(Iterable<? extends UnauffaelligerTag> var1);

    @CacheEvict(
            value = { CachingConfiguration.AUFFAELLIGETAGE_FOR_MESSSTELLE },
            allEntries = true
    )
    void deleteById(UUID var1);

    @CacheEvict(
            value = { CachingConfiguration.AUFFAELLIGETAGE_FOR_MESSSTELLE },
            allEntries = true
    )
    void delete(UnauffaelligerTag var1);

    @CacheEvict(
            value = { CachingConfiguration.AUFFAELLIGETAGE_FOR_MESSSTELLE },
            allEntries = true
    )
    void deleteAllByKalendertagDatum(final LocalDate kalendertagDatum);

    @CacheEvict(
            value = { CachingConfiguration.AUFFAELLIGETAGE_FOR_MESSSTELLE },
            allEntries = true
    )
    <S extends UnauffaelligerTag> S save(S var1);

    @CacheEvict(
            value = { CachingConfiguration.AUFFAELLIGETAGE_FOR_MESSSTELLE },
            allEntries = true
    )
    <S extends UnauffaelligerTag> List<S> saveAll(Iterable<S> var1);

    @CacheEvict(
            value = { CachingConfiguration.AUFFAELLIGETAGE_FOR_MESSSTELLE },
            allEntries = true
    )
    <S extends UnauffaelligerTag> List<S> saveAllAndFlush(Iterable<S> entities);

    List<UnauffaelligerTag> findByMstId(final String mstId);

    Optional<UnauffaelligerTag> findFirstByMstIdOrderByKalendertagDatumDesc(final String mstId);

    long countAllByMstIdAndKalendertagDatumGreaterThanEqualAndKalendertagDatumLessThanEqualAndKalendertagTagestypIn(
            final String mstId,
            final LocalDate startDateIncluded,
            final LocalDate endDateIncluded,
            final List<TagesTyp> tagesTyp);
}
