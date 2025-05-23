/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.OptionsmenueSettings;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionsmenueSettingsRepository extends JpaRepository<OptionsmenueSettings, UUID> {
}
