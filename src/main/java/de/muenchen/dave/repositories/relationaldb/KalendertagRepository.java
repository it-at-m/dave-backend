/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.Kalendertag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface KalendertagRepository extends JpaRepository<Kalendertag, UUID> { //NOSONAR

    @Override
    Optional<Kalendertag> findById(UUID id);

}
