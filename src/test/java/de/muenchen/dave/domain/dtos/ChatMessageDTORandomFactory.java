package de.muenchen.dave.domain.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public class ChatMessageDTORandomFactory {

    public static ChatMessageDTO getOne() {

        final var dto = new ChatMessageDTO();

        dto.setId(UUID.randomUUID().toString());
        dto.setZaehlungId(UUID.randomUUID().toString());
        dto.setContent("test");
        dto.setType("text");
        dto.setParticipantId(1);
        dto.setTimestamp(LocalDateTime.of(2021, 01, 01, 10, 30, 0, 0));
        dto.setUploaded(true);
        dto.setViewed(false);

        return dto;
    }
}
