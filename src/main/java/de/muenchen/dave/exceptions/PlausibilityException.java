/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.exceptions;

import lombok.Data;

@Data
public class PlausibilityException extends Exception {

    public PlausibilityException(final String message) {
        super(message);
    }

}
