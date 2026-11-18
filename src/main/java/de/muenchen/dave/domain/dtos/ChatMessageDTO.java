package de.muenchen.dave.domain.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

// Definition of getter, setter, ...
@Data
public class ChatMessageDTO implements Serializable {

    private UUID id;
    private UUID zaehlungId;
    private String content;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int participantId;
    @NotNull
    private LocalDateTime timestamp;
    private String type;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean uploaded;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean viewed;
}
