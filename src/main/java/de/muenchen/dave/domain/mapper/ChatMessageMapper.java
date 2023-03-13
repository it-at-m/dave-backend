package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.ChatMessage;
import de.muenchen.dave.domain.dtos.ChatMessageDTO;
import de.muenchen.dave.domain.dtos.MessageTimeDTO;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    ChatMessage dto2bean(ChatMessageDTO dto);

    List<ChatMessage> dtoList2beanList(List<ChatMessageDTO> dto);


    ChatMessageDTO bean2Dto(ChatMessage bean);

    List<ChatMessageDTO> beanList2DtoList(List<ChatMessage> bean);

    @AfterMapping
    default void toChatMessage(@MappingTarget ChatMessage bean, ChatMessageDTO dto) {
        final ZoneId zone = ZoneId.of("Europe/Berlin");
        MessageTimeDTO mtDTO = dto.getMessageTimeDTO();
        if (ObjectUtils.isNotEmpty(mtDTO)) {
            LocalDateTime localDateTime = LocalDateTime.of(mtDTO.getYear(), mtDTO.getMonth(), mtDTO.getDay(), mtDTO.getHour(),
                    mtDTO.getMinute(), mtDTO.getSecond(), mtDTO.getMillisecond());
            bean.setTimestamp(localDateTime);
        } else {
            bean.setTimestamp(LocalDateTime.now(zone));
        }
    }

    @AfterMapping
    default void toChatMessageDTO(@MappingTarget ChatMessageDTO dto, ChatMessage chatMessage) {
        dto.setMessageTimeDTO(localDateTimeToMessageTimeDTO(chatMessage.getTimestamp()));
    }

    default UUID stringToUUID(String value) {
        if (StringUtils.isNotEmpty(value)) {
            return UUID.fromString(value);
        }
        return null;
    }

    default String UUIDtoString(UUID value) {
        return value.toString();
    }

    default MessageTimeDTO localDateTimeToMessageTimeDTO(LocalDateTime timestamp) {
        final MessageTimeDTO messageTimeDTO = new MessageTimeDTO();
        messageTimeDTO.setYear(timestamp.getYear());
        messageTimeDTO.setMonth(timestamp.getMonth().getValue());
        messageTimeDTO.setDay(timestamp.getDayOfMonth());
        messageTimeDTO.setHour(timestamp.getHour());
        messageTimeDTO.setMinute(timestamp.getMinute());
        messageTimeDTO.setSecond(timestamp.getSecond());
        return messageTimeDTO;
    }
}
