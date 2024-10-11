/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.EmailAddress;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailAddressRepository extends JpaRepository<EmailAddress, UUID> { //NOSONAR

    @Override
    Optional<EmailAddress> findById(UUID id);

    @Override
    <S extends EmailAddress> S save(S emailAddress);

    @Override
    <S extends EmailAddress> List<S> saveAll(Iterable<S> entities);

    @Override
    void deleteById(UUID id);

    @Override
    void delete(EmailAddress entity);

    @Override
    void deleteAll(Iterable<? extends EmailAddress> entities);

    @Override
    void deleteAll();

    List<EmailAddress> findAll(final Sort sort);

    Optional<EmailAddress> findByParticipantId(int participantId);

}
