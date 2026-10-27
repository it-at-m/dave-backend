package de.muenchen.dave.domain.espgmigration.repositories;

import de.muenchen.dave.domain.espgmigration.zaehlstellen.ZaehlstelleEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZaehlstellenIndexRepository extends JpaRepository<ZaehlstelleEntity, UUID> {
}
