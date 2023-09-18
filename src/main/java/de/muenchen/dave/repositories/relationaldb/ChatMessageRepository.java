/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.ChatMessage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> { //NOSONAR

    @Override
    Optional<ChatMessage> findById(UUID id);

    @Override
    <S extends ChatMessage> S save(S chatMessage);

    @Override
    <S extends ChatMessage> List<S> saveAll(Iterable<S> entities);

    @Override
    void deleteById(UUID id);

    @Override
    void delete(ChatMessage entity);

    @Override
    void deleteAll(Iterable<? extends ChatMessage> entities);

    @Override
    void deleteAll();

    List<ChatMessage> findAll(final Sort sort);

    List<ChatMessage> findAllByZaehlungIdOrderByTimestampAsc(UUID zaehlungId);

}
