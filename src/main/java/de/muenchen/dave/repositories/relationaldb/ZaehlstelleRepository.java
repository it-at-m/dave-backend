package de.muenchen.dave.repositories.relationaldb;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import de.muenchen.dave.domain.analytics.Zaehlstelle;

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

        Page<Zaehlstelle> suggestSearch(String query, Pageable pageable);

    @Query(value = "select * from zaehlstelle order by id")
    Page<Zaehlstelle> findAllByStatus(String query, Pageable pageable);

    List<Zaehlstelle> findAll();

    @Query(value = "select * from zaehlstelle where zaehlstelle.id = ?1")
    Optional<Zaehlstelle> findByZaehlungenId(String id);

    List<Zaehlstelle> findAllByNummerStartsWithAndStadtbezirkNummer(String nummer, Integer stadtbezirksnummer);

    Optional<Zaehlstelle> findByNummer(String nummer);

    @Query(value = "select * from zaehlstelle order by id")
    List<Zaehlstelle> findAllByZaehlungenStatus(String status);

    @Query(value = "select * from zaehlstelle order by id")
    List<Zaehlstelle> findAllByZaehlungenJahr(String jahr);

    @Query(value = "select * from zaehlstelle order by id")
    List<Zaehlstelle> findAllByZaehlungenUnreadMessagesMobilitaetsreferatTrue();

    @Query(value = "select * from zaehlstelle order by id")
    List<Zaehlstelle> findAllByZaehlungenUnreadMessagesDienstleisterTrue();
}
