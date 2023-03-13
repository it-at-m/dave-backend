/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.exceptions;

import lombok.Data;


@Data
public class DataNotFoundException extends Exception {

    public DataNotFoundException(final String message) {
        super(message);
    }

}
