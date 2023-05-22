/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.PkwEinheit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PkwEinheitRepository extends JpaRepository<PkwEinheit, UUID> { //NOSONAR

    @Override
    Optional<PkwEinheit> findById(UUID id);

    @Override
    <S extends PkwEinheit> S save(S pkwEinheit);

    @Override
    <S extends PkwEinheit> List<S> saveAll(Iterable<S> pkwEinheiten);

    @Override
    void deleteById(UUID id);

    @Override
    void delete(PkwEinheit pkwEinheit);

    @Override
    void deleteAll(Iterable<? extends PkwEinheit> pkwEinheiten);

    @Override
    void deleteAll();

    List<PkwEinheit> findAll(Sort sort);

    Optional<PkwEinheit> findTopByOrderByCreatedTimeDesc();

}
