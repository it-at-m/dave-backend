/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.OptionsmenueSettings;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OptionsmenueSettingsRepository extends JpaRepository<OptionsmenueSettings, UUID> {

    Optional<OptionsmenueSettings> findByFahrzeugklasseAndIntervall(final Fahrzeugklasse fahrzeugklasse, final ZaehldatenIntervall intervall);

    Optional<OptionsmenueSettings> findByFahrzeugklasseIsNullAndIntervallIsNull();
}
