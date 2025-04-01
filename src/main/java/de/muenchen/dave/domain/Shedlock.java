/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.sql.Timestamp;

/**
 * Entitaet fuer den Lock des Schedulers
 */
@Entity
@Table(name = Shedlock.TABLE_NAME)
public class Shedlock {

    public static final String TABLE_NAME = "shedlock";

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
