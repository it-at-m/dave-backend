package de.muenchen.dave.domain.dtos.messstelle;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Data;

@Data
public class FahrzeugOptionsDTO implements Serializable {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean kraftfahrzeugverkehr;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean schwerverkehr;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean gueterverkehr;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean schwerverkehrsanteilProzent;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean gueterverkehrsanteilProzent;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean radverkehr;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean fussverkehr;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean lastkraftwagen;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean lastzuege;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean busse;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean kraftraeder;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean personenkraftwagen;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean lieferwagen;
}
