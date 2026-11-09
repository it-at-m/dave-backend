package de.muenchen.dave.migrationespg.repositories.relationaldb;

import de.muenchen.dave.migrationespg.domain.relational.messtellen.MessstelleEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessstellenIndexRepository extends JpaRepository<MessstelleEntity, UUID> {

    List<MessstelleEntity> findAllBySichtbarDatenportalIsTrue();
    Optional<MessstelleEntity> findByMstId(String mstId);
    Page<MessstelleEntity> suggestSearch(String query, Pageable pageable);
}
