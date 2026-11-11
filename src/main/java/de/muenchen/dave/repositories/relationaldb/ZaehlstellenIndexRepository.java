package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.relational.zaehlstellen.ZaehlstelleEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZaehlstellenIndexRepository extends JpaRepository<ZaehlstelleEntity, UUID> {

    Optional<ZaehlstelleEntity> findByZaehlungen_Id(UUID zaehlungId);
}
