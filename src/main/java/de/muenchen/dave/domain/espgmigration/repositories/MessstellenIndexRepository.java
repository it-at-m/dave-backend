package de.muenchen.dave.domain.espgmigration.repositories;

import de.muenchen.dave.domain.espgmigration.messtellen.MessstelleEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessstellenIndexRepository extends JpaRepository<MessstelleEntity, UUID> {
}
