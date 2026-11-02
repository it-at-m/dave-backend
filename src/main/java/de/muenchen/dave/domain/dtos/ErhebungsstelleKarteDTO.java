package de.muenchen.dave.domain.dtos;

import de.muenchen.dave.domain.dtos.messstelle.MessstelleTooltipDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Data;

@Data
@Schema(
        oneOf = {
                de.muenchen.dave.domain.dtos.messstelle.MessstelleKarteDTO.class,
                de.muenchen.dave.domain.dtos.ZaehlstelleKarteDTO.class
        }
)
public class ErhebungsstelleKarteDTO implements Serializable {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private String id;
    @NotNull
    private String fachId;
    @NotNull
    private String type;
    @NotNull
    private Double longitude;
    @NotNull
    private Double latitude;
    @NotNull
    @Schema(oneOf = { MessstelleTooltipDTO.class, ZaehlstelleTooltipDTO.class })
    private ErhebungsstelleTooltipDTO tooltip;
    @NotNull
    private Boolean sichtbarDatenportal;

}
