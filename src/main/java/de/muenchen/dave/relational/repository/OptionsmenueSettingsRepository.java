package de.muenchen.dave.relational.repository;

import de.muenchen.dave.domain.OptionsmenueSettings;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionsmenueSettingsRepository extends JpaRepository<OptionsmenueSettings, UUID> {
}
