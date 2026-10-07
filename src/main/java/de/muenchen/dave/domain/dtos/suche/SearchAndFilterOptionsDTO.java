package de.muenchen.dave.domain.dtos.suche;

import de.muenchen.dave.domain.enums.Verkehrsart;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class SearchAndFilterOptionsDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean searchInMessstellen;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean searchInZaehlstellen;
    @NotNull
    private List<Verkehrsart> messstelleVerkehrsart;
}
