package de.muenchen.dave.domain.dtos;

import lombok.Data;

import java.util.List;
import java.util.UUID;

// Definition of getter, setter, ...
@Data
public class DienstleisterDTO {

    private UUID id;
    private Long entityVersion;
    private String name;
    private String kennung;
    private List<String> emailAddresses;
    private boolean active;
    private boolean erasable;

    private String emailAddressesAsString;
}
