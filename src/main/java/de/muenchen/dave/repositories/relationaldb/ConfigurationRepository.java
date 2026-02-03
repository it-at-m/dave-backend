package de.muenchen.dave.repositories.relationaldb;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import de.muenchen.dave.domain.ConfigurationEntity;

public interface ConfigurationRepository  extends JpaRepository<ConfigurationEntity, UUID> {

    ConfigurationEntity findByKeyname(String key);

}
