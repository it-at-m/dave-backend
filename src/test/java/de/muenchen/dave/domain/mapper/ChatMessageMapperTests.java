package de.muenchen.dave.domain.mapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

import de.muenchen.dave.domain.ChatMessage;
import de.muenchen.dave.domain.dtos.ChatMessageDTO;
import de.muenchen.dave.domain.dtos.ChatMessageDTORandomFactory;
import de.muenchen.dave.domain.dtos.MessageTimeDTO;
import de.muenchen.dave.domain.relationaldb.ChatMessageRandomFactory;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ChatMessageMapperTests {

    private final ChatMessageMapper mapper = new ChatMessageMapperImpl();

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");

    @Test
    public void testDto2bean() {
        ChatMessageDTO dto = ChatMessageDTORandomFactory.getOne();
        ChatMessage bean = this.mapper.dto2bean(dto);

        assertThat(bean, hasProperty("id", equalTo(UUID.fromString(dto.getId()))));
        assertThat(bean, hasProperty("zaehlungId", equalTo(UUID.fromString(dto.getZaehlungId()))));
        assertThat(bean, hasProperty("content", equalTo(dto.getContent())));
        assertThat(bean, hasProperty("type", equalTo(dto.getType())));
        assertThat(bean, hasProperty("participantId", equalTo(dto.getParticipantId())));
        assertThat(bean, hasProperty("uploaded", equalTo(dto.isUploaded())));
        assertThat(bean, hasProperty("viewed", equalTo(dto.isViewed())));

        LocalDateTime localDateTime = LocalDateTime.of(2021, 1, 1, 10, 30, 0, 0);
        assertThat(bean, hasProperty("timestamp", equalTo(localDateTime)));

        // Test für neue Nachricht ohne Zeitstempel
        dto.setMessageTimeDTO(null);
        bean = this.mapper.dto2bean(dto);
        // Millisekunden auf 0 setzen (sind sowieso irrelevant), da sonst LocalDateTime.now() nicht verglichen werden kann
        bean.setTimestamp(bean.getTimestamp().minusNanos(bean.getTimestamp().getNano()));
        LocalDateTime ldt = LocalDateTime.now(ZONE);
        ldt = ldt.minusNanos(ldt.getNano());
        assertThat(bean, hasProperty("timestamp", equalTo(ldt)));
    }

    @Test
    public void testBean2Dto() {
        ChatMessage bean = ChatMessageRandomFactory.getOne();
        ChatMessageDTO dto = this.mapper.bean2Dto(bean);

        assertThat(dto, hasProperty("id", equalTo(bean.getId().toString())));
        assertThat(dto, hasProperty("zaehlungId", equalTo(bean.getZaehlungId().toString())));
        assertThat(dto, hasProperty("content", equalTo(bean.getContent())));
        assertThat(dto, hasProperty("type", equalTo(bean.getType())));
        assertThat(dto, hasProperty("participantId", equalTo(bean.getParticipantId())));
        assertThat(dto, hasProperty("uploaded", equalTo(bean.getUploaded())));
        assertThat(dto, hasProperty("viewed", equalTo(bean.getViewed())));

        MessageTimeDTO messageTimeDTO = new MessageTimeDTO();
        messageTimeDTO.setYear(2021);
        messageTimeDTO.setMonth(1);
        messageTimeDTO.setDay(1);
        messageTimeDTO.setHour(10);
        messageTimeDTO.setMinute(30);
        messageTimeDTO.setSecond(0);
        messageTimeDTO.setMillisecond(0);
        assertThat(dto, hasProperty("messageTimeDTO", equalTo(messageTimeDTO)));
    }

}
