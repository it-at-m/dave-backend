package de.muenchen.dave.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;

import de.muenchen.dave.domain.ChatMessage;
import de.muenchen.dave.domain.dtos.ChatMessageDTO;
import de.muenchen.dave.domain.dtos.ChatMessageDTORandomFactory;
import de.muenchen.dave.domain.mapper.ChatMessageMapperImpl;
import de.muenchen.dave.domain.relationaldb.ChatMessageRandomFactory;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.relationaldb.ChatMessageRepository;
import de.muenchen.dave.services.email.EmailSendService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ChatMessageServiceTest {

    private final ChatMessageService chatMessageService;
    private final ChatMessageRepository chatMessageRepository;
    private final IndexService indexService;
    private final EmailSendService emailSendService;

    public ChatMessageServiceTest() {
        this.chatMessageRepository = Mockito.mock(ChatMessageRepository.class);
        this.indexService = Mockito.mock(IndexService.class);
        this.emailSendService = Mockito.mock(EmailSendService.class);
        this.chatMessageService = new ChatMessageService(
                this.chatMessageRepository,
                new ChatMessageMapperImpl(),
                this.indexService,
                this.emailSendService);
    }

    @Test
    public void saveChatMessageTest() throws BrokenInfrastructureException, DataNotFoundException {
        final ChatMessageDTO chatMessageDTO = ChatMessageDTORandomFactory.getOne();
        final ChatMessage chatMessage = new ChatMessageMapperImpl().dto2bean(chatMessageDTO);
        Mockito.when(chatMessageRepository.saveAndFlush(any())).thenReturn(chatMessage);
        ChatMessageDTO result = chatMessageService.saveChatMessage(chatMessageDTO);
        assertThat(result, is(chatMessageDTO));
    }

    @Test
    public void loadChatMessagesTest() {
        final List<ChatMessage> chatMessageList = ChatMessageRandomFactory.getSome();
        Mockito.when(chatMessageRepository.findAllByZaehlungIdOrderByTimestampAsc(any())).thenReturn(chatMessageList);
        final List<ChatMessageDTO> expected = new ChatMessageMapperImpl().beanList2DtoList(chatMessageList);
        final List<ChatMessageDTO> result = chatMessageService.loadChatMessages(UUID.randomUUID());
        assertThat(result, is(expected));
    }

}
