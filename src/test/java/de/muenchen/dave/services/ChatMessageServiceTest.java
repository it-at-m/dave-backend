package de.muenchen.dave.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import de.muenchen.dave.TestUtils;
import de.muenchen.dave.domain.ChatMessage;
import de.muenchen.dave.domain.dtos.ChatMessageDTO;
import de.muenchen.dave.domain.dtos.ChatMessageDTORandomFactory;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.mapper.ChatMessageMapperImpl;
import de.muenchen.dave.domain.relationaldb.ChatMessageRandomFactory;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.relationaldb.ChatMessageRepository;
import de.muenchen.dave.services.email.EmailSendService;
import java.util.List;
import java.util.UUID;

import de.muenchen.dave.services.security.AuthorizationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;

public class ChatMessageServiceTest {

    private final ChatMessageService chatMessageService;
    private final ChatMessageRepository chatMessageRepository;
    private final ZaehlstelleIndexService indexService;
    private final EmailSendService emailSendService;

    public ChatMessageServiceTest() {
        this.chatMessageRepository = Mockito.mock(ChatMessageRepository.class);
        this.indexService = Mockito.mock(ZaehlstelleIndexService.class);
        this.emailSendService = Mockito.mock(EmailSendService.class);
        this.chatMessageService = new ChatMessageService(
                this.chatMessageRepository,
                new ChatMessageMapperImpl(),
                this.indexService,
                this.emailSendService,
                new AuthorizationService(this.indexService));
    }

    @BeforeEach
    void setUp() {
        // Setze Test-Nutzer mit Rolle Fachadmin
        TestUtils.setSecurityContext("test", true);
    }

    @AfterEach
    void tearDown() {
        TestUtils.clearSecurityContext();
    }

    @Test
    public void saveChatMessageTest() throws BrokenInfrastructureException, DataNotFoundException {
        final ChatMessageDTO chatMessageDTO = ChatMessageDTORandomFactory.getOne();
        final ChatMessage chatMessage = new ChatMessageMapperImpl().dto2bean(chatMessageDTO);
        when(chatMessageRepository.saveAndFlush(any())).thenReturn(chatMessage);
        ChatMessageDTO result = chatMessageService.saveChatMessage(chatMessageDTO);
        assertThat(result, is(chatMessageDTO));
    }

    @Test
    public void saveChatMessageTest_notAuthorizedUser_throwsAccessDeniedException() throws DataNotFoundException {
        final var id = UUID.randomUUID().toString();
        final var zaehlstelle = new Zaehlstelle();
        final var zaehlung = new Zaehlung();
        zaehlung.setId(id);
        zaehlung.setDienstleisterkennung("dl1");
        zaehlstelle.getZaehlungen().add(zaehlung);

        when(indexService.getZaehlung(id)).thenReturn(zaehlung);

        final ChatMessageDTO chatMessageDTO = ChatMessageDTORandomFactory.getOne();
        chatMessageDTO.setZaehlungId(id);

        TestUtils.setSecurityContext("tester", false);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> chatMessageService.saveChatMessage(chatMessageDTO));
        assertThat(ex.getMessage(), is("Der Dienstleister ist nicht berechtigt, Nachrichten für diese Zählung zu lesen oder zu senden."));
    }

    @Test
    public void loadChatMessagesTest() throws DataNotFoundException {
        final List<ChatMessage> chatMessageList = ChatMessageRandomFactory.getSome();
        when(chatMessageRepository.findAllByZaehlungIdOrderByTimestampAsc(any())).thenReturn(chatMessageList);
        final List<ChatMessageDTO> expected = new ChatMessageMapperImpl().beanList2DtoList(chatMessageList);
        final List<ChatMessageDTO> result = chatMessageService.loadChatMessages(UUID.randomUUID());
        assertThat(result, is(expected));
    }

    @Test
    public void loadChatMessageTest_notAuthorizedUser_throwsAccessDeniedException() throws DataNotFoundException {
        final var id = UUID.randomUUID().toString();
        final var zaehlstelle = new Zaehlstelle();
        final var zaehlung = new Zaehlung();
        zaehlung.setId(id);
        zaehlung.setDienstleisterkennung("dl1");
        zaehlstelle.getZaehlungen().add(zaehlung);

        when(indexService.getZaehlung(id)).thenReturn(zaehlung);

        TestUtils.setSecurityContext("tester", false);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> chatMessageService.loadChatMessages(UUID.fromString(id)));
        assertThat(ex.getMessage(), is("Der Dienstleister ist nicht berechtigt, Nachrichten für diese Zählung zu lesen oder zu senden."));
    }

}
