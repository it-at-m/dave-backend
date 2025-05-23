/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.Dienstleister;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DienstleisterRepository extends JpaRepository<Dienstleister, UUID> { //NOSONAR

    @Override
    Optional<Dienstleister> findById(UUID id);

    @Override
    <S extends Dienstleister> S save(S Dienstleister);

    @Override
    <S extends Dienstleister> List<S> saveAll(Iterable<S> entities);

    @Override
    void deleteById(UUID id);

    @Override
    void delete(Dienstleister entity);

    @Override
    void deleteAll(Iterable<? extends Dienstleister> entities);

    @Override
    void deleteAll();

    List<Dienstleister> findAllByActiveIsTrue();

    Optional<Dienstleister> findByKennung(String kennung);

}
