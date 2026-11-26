package de.muenchen.dave.domain.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean active;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean erasable;
    @NotNull
    private String emailAddressesAsString;
}
