package de.muenchen.dave.domain.dtos;

import lombok.Data;

// Definition of getter, setter, ...
@Data
public class ChatMessageDTO {

    private String id;
    private String zaehlungId;
    private String content;
    private int participantId;
    private MessageTimeDTO messageTimeDTO;
    private String type;
    private boolean uploaded;
    private boolean viewed;
}
