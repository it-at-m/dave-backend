package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.ChatMessage;
import de.muenchen.dave.domain.dtos.ChatMessageDTO;
import de.muenchen.dave.domain.dtos.ChatMessageDTORandomFactory;
import de.muenchen.dave.domain.relationaldb.ChatMessageRandomFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

@Slf4j
public class ChatMessageMapperTests {

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private final ChatMessageMapper mapper = new ChatMessageMapperImpl();

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

        assertThat(bean, hasProperty("timestamp", equalTo(dto.getTimestamp())));

        // Test für neue Nachricht ohne Zeitstempel
        dto.setTimestamp(null);
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
        assertThat(dto, hasProperty("timestamp", equalTo(bean.getTimestamp())));
    }

}
