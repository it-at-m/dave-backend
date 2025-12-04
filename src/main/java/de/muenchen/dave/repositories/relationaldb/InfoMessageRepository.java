package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.InfoMessage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InfoMessageRepository extends JpaRepository<InfoMessage, UUID> { //NOSONAR

    @Override
    Optional<InfoMessage> findById(UUID id);

    @Override
    <S extends InfoMessage> S save(S chatMessage);

    @Override
    <S extends InfoMessage> List<S> saveAll(Iterable<S> entities);

    @Override
    void deleteById(UUID id);

    @Override
    void delete(InfoMessage entity);

    @Override
    void deleteAll(Iterable<? extends InfoMessage> entities);

    @Override
    void deleteAll();

    List<InfoMessage> findAllByAktivIsTrue();

    List<InfoMessage> findAllByOrderByCreatedTimeDesc();

    Optional<InfoMessage> findTopByAktivIsTrueOrderByCreatedTimeDesc();

}
