package de.muenchen.dave.migrationespg.repositories.relationaldb;

import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.ZaehlstelleEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZaehlstellenIndexRepository extends JpaRepository<ZaehlstelleEntity, UUID> {

    Optional<ZaehlstelleEntity> findByNummer(String nummer);
    List<ZaehlstelleEntity> findAllByZaehlungenJahr(String jahr);
    Optional<ZaehlstelleEntity> findByZaehlungenId(String id);
    Page<ZaehlstelleEntity> suggestSearch(String query, Pageable pageable);
    List<ZaehlstelleEntity> findAllByNummerStartsWithAndStadtbezirkNummer(String nummer, Integer stadtbezirksnummer);
    Page<ZaehlstelleEntity> findAllByStatus(String query, Pageable pageable);
    List<ZaehlstelleEntity> findAllByZaehlungenStatus(String status);
    List<ZaehlstelleEntity> findAllByZaehlungenUnreadMessagesMobilitaetsreferatTrue();
    List<ZaehlstelleEntity> findAllByZaehlungenUnreadMessagesDienstleisterTrue();

}