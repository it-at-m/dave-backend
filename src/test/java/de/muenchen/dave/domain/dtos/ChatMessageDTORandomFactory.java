package de.muenchen.dave.domain.dtos;

import java.util.UUID;

public class ChatMessageDTORandomFactory {

    public static ChatMessageDTO getOne() {

        ChatMessageDTO dto = new ChatMessageDTO();

        dto.setId(UUID.randomUUID().toString());
        dto.setZaehlungId(UUID.randomUUID().toString());
        dto.setContent("test");
        dto.setType("text");
        dto.setParticipantId(1);
        MessageTimeDTO messageTimeDTO = new MessageTimeDTO();
        messageTimeDTO.setYear(2021);
        messageTimeDTO.setMonth(01);
        messageTimeDTO.setDay(01);
        messageTimeDTO.setHour(10);
        messageTimeDTO.setMinute(30);
        messageTimeDTO.setSecond(0);
        messageTimeDTO.setMillisecond(0);
        dto.setMessageTimeDTO(messageTimeDTO);
        dto.setUploaded(true);
        dto.setViewed(false);

        return dto;
    }
}
