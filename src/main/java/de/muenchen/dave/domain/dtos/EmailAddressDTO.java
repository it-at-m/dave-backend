package de.muenchen.dave.domain.dtos;

import lombok.Data;

import java.util.UUID;

// Definition of getter, setter, ...
@Data
public class EmailAddressDTO {

    private UUID id;
    private Long entityVersion;
    private int participantId;
    private String emailAddress;
}
