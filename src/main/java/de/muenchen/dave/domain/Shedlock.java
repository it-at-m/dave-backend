/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.domain;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entitaet fuer den Lock des Schedulers
 */
@Entity
@Table(name = "shedlock")
public class Shedlock {

    @Id
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "lock_until", nullable = false)
    private Timestamp lockUntil;

    @Column(name = "locked_at", nullable = false)
    private Timestamp lockedAt;

    @Column(name = "locked_by", nullable = false)
    private String lockedBy;
}
