package de.muenchen.dave.exceptions;

import lombok.Data;

@Data
public class DataNotFoundException extends Exception {

    public DataNotFoundException(final String message) {
        super(message);
    }

}
