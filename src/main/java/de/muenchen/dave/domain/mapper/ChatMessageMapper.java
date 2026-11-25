package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.ChatMessage;
import de.muenchen.dave.domain.dtos.ChatMessageDto;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChatMessageMapper {

    ChatMessage dto2bean(ChatMessageDto dto);

    List<ChatMessage> dtoList2beanList(List<ChatMessageDto> dto);

    ChatMessageDto bean2Dto(ChatMessage bean);

    List<ChatMessageDto> beanList2DtoList(List<ChatMessage> bean);

    @AfterMapping
    default void toChatMessage(@MappingTarget ChatMessage bean, ChatMessageDto dto) {
        if (ObjectUtils.isEmpty(dto.getTimestamp())) {
            final ZoneId zone = ZoneId.of("Europe/Berlin");
            bean.setTimestamp(LocalDateTime.now(zone));
        }
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
}
