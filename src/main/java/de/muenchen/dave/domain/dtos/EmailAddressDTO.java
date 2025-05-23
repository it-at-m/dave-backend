package de.muenchen.dave.domain.dtos;

import java.util.UUID;
import lombok.Data;

// Definition of getter, setter, ...
@Data
public class EmailAddressDTO {

    private UUID id;
    private Long entityVersion;
    private int participantId;
    private String emailAddress;
}
