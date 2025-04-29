package de.muenchen.dave.domain.dtos;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

// Definition of getter, setter, ...
@Data
public class ChatMessageDTO implements Serializable {

    private String id;
    private String zaehlungId;
    private String content;
    private int participantId;
    private LocalDateTime timestamp;
    private String type;
    private boolean uploaded;
    private boolean viewed;
}
