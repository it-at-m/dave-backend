package de.muenchen.dave.exceptions;

import lombok.Data;

@Data
public class PlausibilityException extends Exception {

    public PlausibilityException(final String message) {
        super(message);
    }

}
