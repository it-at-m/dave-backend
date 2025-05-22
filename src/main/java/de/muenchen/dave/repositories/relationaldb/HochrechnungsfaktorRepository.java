/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.Hochrechnungsfaktor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HochrechnungsfaktorRepository extends JpaRepository<Hochrechnungsfaktor, UUID> { //NOSONAR

    @Override
    Optional<Hochrechnungsfaktor> findById(UUID id);

    @Override
    <S extends Hochrechnungsfaktor> S save(S hochrechnungsfaktor);

    @Override
    <S extends Hochrechnungsfaktor> List<S> saveAll(Iterable<S> hochrechnungsfaktor);

    @Override
    void deleteById(UUID id);

    @Override
    void delete(Hochrechnungsfaktor hochrechnungsfaktor);

    @Override
    void deleteAll(Iterable<? extends Hochrechnungsfaktor> hochrechnungsfaktor);

    @Override
    void deleteAll();

    List<Hochrechnungsfaktor> findAll(Sort sort);

}
