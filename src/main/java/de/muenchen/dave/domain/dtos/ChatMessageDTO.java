package de.muenchen.dave.domain.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

// Definition of getter, setter, ...
@Data
public class ChatMessageDTO implements Serializable {

    private String id;
    @NotNull
    private String zaehlungId;
    @NotNull
    private String content;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int participantId;
    @NotNull
    private LocalDateTime timestamp;
    @NotNull
    private String type;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean uploaded;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean viewed;
}
