/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.configuration;

import de.muenchen.dave.domain.BaseEntity;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


/**
 * Die Annotation {@link EnableJpaAuditing} ist erforderlich um z.B.
 * die Annotation {@link CreatedDate} in Klasse {@link BaseEntity}
 * funktionsfähig zu machen.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration {
}

