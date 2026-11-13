package de.muenchen.dave.domain.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

// Definition of getter, setter, ...
@Data
public class EmailAddressDTO {

    private UUID id;
    private Long entityVersion;
    private int participantId;
    @NotNull
    private String emailAddress;
}
