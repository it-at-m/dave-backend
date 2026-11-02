package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.relational.messtellen.MessstelleEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessstellenIndexRepository extends JpaRepository<MessstelleEntity, UUID> {
}
