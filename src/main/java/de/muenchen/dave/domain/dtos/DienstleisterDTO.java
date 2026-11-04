package de.muenchen.dave.domain.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class DienstleisterDTO {

    private UUID id;
    private Long entityVersion;
    @NotNull
    private String name;
    @NotNull
    private String kennung;
    @NotNull
    private List<@NotNull String> emailAddresses;
    private boolean active;
    private boolean erasable;

    private String emailAddressesAsString;
}
