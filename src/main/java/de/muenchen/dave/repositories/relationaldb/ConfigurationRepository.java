package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.ConfigurationEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigurationRepository extends JpaRepository<ConfigurationEntity, UUID> {

    ConfigurationEntity findByKeyname(String key);

}
