package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.analytics.Zaehlstelle;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ZaehlstelleRepository extends JpaRepository<Zaehlstelle, UUID> { //NOSONAR

    @Override
    Optional<Zaehlstelle> findById(UUID id);

    @Override
    <S extends Zaehlstelle> S save(S zaehlstelle);

    @Override
    <S extends Zaehlstelle> List<S> saveAll(Iterable<S> entities);

    @Override
    void deleteById(UUID id);

    @Override
    void delete(Zaehlstelle entity);

    @Override
    void deleteAll(Iterable<? extends Zaehlstelle> entities);

    @Override
    void deleteAll();

    List<Zaehlstelle> findAll(final Sort sort);

    @Query(value = """
            select z from Zaehlstelle z
            where z.nummer like %?1%
            or z.stadtbezirk like %?1%
            or z.kommentar like %?1%
            or cast(z.suchwoerter as string) like %?1%
            or cast(z.customSuchwoerter as string) like %?1%
                    """)
    Page<Zaehlstelle> suggestSearch(String query, Pageable pageable);

    @Query(value = """
            select z from Zaehlstelle z
            join z.zaehlungen za
            where za.status in ('counting', 'correction', 'instructed')
                    """)
    Page<Zaehlstelle> findAllByStatus(String query, Pageable pageable);

    List<Zaehlstelle> findAll();

    @Query(value = "select z from Zaehlstelle z join z.zaehlungen za where za.id = ?1")
    Optional<Zaehlstelle> findByZaehlungenId(UUID id);

    List<Zaehlstelle> findAllByNummerStartsWithAndStadtbezirkNummer(String nummer, Integer stadtbezirksnummer);

    Optional<Zaehlstelle> findByNummer(String nummer);

    @Query(value = "select z from Zaehlstelle z join z.zaehlungen za where za.status = ?1")
    List<Zaehlstelle> findAllByZaehlungenStatus(String status);

    @Query(value = "select z from Zaehlstelle z join z.zaehlungen za where za.jahr = ?1 order by z.id")
    List<Zaehlstelle> findAllByZaehlungenJahr(String jahr);

    List<Zaehlstelle> findAllByZaehlungenUnreadMessagesMobilitaetsreferatTrue();

    List<Zaehlstelle> findAllByZaehlungenUnreadMessagesDienstleisterTrue();
}
