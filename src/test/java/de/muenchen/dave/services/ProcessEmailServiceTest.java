package de.muenchen.dave.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.dtos.ChatMessageDTO;
import de.muenchen.dave.domain.dtos.MessageTimeDTO;
import de.muenchen.dave.domain.enums.Participant;
import de.muenchen.dave.domain.mapper.ChatMessageMapperImpl;
import de.muenchen.dave.services.email.ProcessEmailService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;

public class ProcessEmailServiceTest {

    private final ProcessEmailService processEmailService;
    private final Message message;
    private final Date date;

    public ProcessEmailServiceTest() {
        this.processEmailService = new ProcessEmailService(
                "Landeshauptstadt München;Schuh&Co.GmbH",
                "Von:;-----Ursprüngliche Nachricht-----",
                new ChatMessageMapperImpl()
        );
        this.message = Mockito.mock(Message.class);
        this.date = new Date();
    }

    @BeforeEach
    void mockEmail() throws MessagingException, IOException {
        Mockito.when(message.getContentType()).thenReturn("text/plain; charset=utf-8");
        Mockito.when(message.getContent()).thenReturn("Antwort \r\n");
        Mockito.when(message.getSubject()).thenReturn("DAVe: Neue Nachricht vom Mobilitätsreferat [5ff15a92-7dac-434a-a5a9-a54914c38274]");
        Mockito.when(message.getFrom()).thenReturn(new Address[] { new InternetAddress("mob@muenchen.de") });
        Mockito.when(message.getSentDate()).thenReturn(date);
    }

    @Test
    void processCorrectEmail() throws IOException, MessagingException {
        // Email vom Mobilitätsreferat
        final ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        chatMessageDTO.setZaehlungId("5ff15a92-7dac-434a-a5a9-a54914c38274");
        chatMessageDTO.setParticipantId(Participant.MOBILITAETSREFERAT.getParticipantId());
        chatMessageDTO.setContent("Antwort");
        chatMessageDTO.setType("text");

        final MessageTimeDTO messageTimeDTO = new MessageTimeDTO();
        final LocalDateTime sentDate = date.toInstant().atZone(ChatMessageService.ZONE).toLocalDateTime();
        messageTimeDTO.setYear(sentDate.getYear());
        messageTimeDTO.setMonth(sentDate.getMonth().getValue());
        messageTimeDTO.setDay(sentDate.getDayOfMonth());
        messageTimeDTO.setHour(sentDate.getHour());
        messageTimeDTO.setMinute(sentDate.getMinute());
        messageTimeDTO.setSecond(sentDate.getSecond());
        chatMessageDTO.setMessageTimeDTO(messageTimeDTO);

        assertThat(processEmailService.processEmail(message), is(chatMessageDTO));

        // EMail vom Dienstleister
        Mockito.when(message.getSubject()).thenReturn("DAVe: Neue Nachricht vom Dienstleister [5ff15a92-7dac-434a-a5a9-a54914c38274]");
        Mockito.when(message.getFrom()).thenReturn(new Address[] { new InternetAddress("dl@dienstleister.de") });
        assertThat(processEmailService.processEmail(message).getParticipantId(), is(Participant.DIENSTLEISTER.getParticipantId()));

        // Email ohne Zeitstempel
        Mockito.when(message.getSentDate()).thenReturn(null);
        Assertions.assertNull(processEmailService.processEmail(message).getMessageTimeDTO());

        // Email ohne Inhalt
        Mockito.when(message.getContent()).thenReturn(null);
        assertThat(processEmailService.processEmail(message).getContent(), is(""));
        Mockito.when(message.getContent()).thenReturn("");
        assertThat(processEmailService.processEmail(message).getContent(), is(""));

        // Email mit html Inhalt
        Mockito.when(message.getContentType()).thenReturn("text/html; charset=UTF-8");
        Mockito.when(message.getContent()).thenReturn("<html><head>\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body><div style=\"font-family: Verdana;font-size: 12.0px;\"><div>Hallo</div></div></body></html>\n");
        assertThat(processEmailService.processEmail(message).getContent(), is("Hallo"));

        // Email mit Multipart Inhalt (z.B. Anhang)
        MimeMultipart mimeMultipart = Mockito.mock(MimeMultipart.class);
        BodyPart bodyPart = Mockito.mock(BodyPart.class);
        Mockito.when(mimeMultipart.getCount()).thenReturn(2);
        Mockito.when(mimeMultipart.getBodyPart(0)).thenReturn(bodyPart);
        Mockito.when(bodyPart.getContentType()).thenReturn("text/plain; charset=utf-8");
        Mockito.when(bodyPart.getContent()).thenReturn("Multipart Nachricht 1");

        Mockito.when(message.getContentType()).thenReturn("multipart/mixed; \n" +
                "\tboundary=_002_6d49c1225d51446e8ad5647eb9d9b3e3muenchende_");
        Mockito.when(message.getContent()).thenReturn(mimeMultipart);
        assertThat(processEmailService.processEmail(message).getContent(),
                is("Multipart Nachricht 1\r\n[Der Anhang wurde entfernt. Anhänge werden nicht unterstützt.]"));
    }

    @Test
    void processEmailWithoutContentType() throws MessagingException {
        Mockito.when(message.getContentType()).thenReturn(null);
        Assertions.assertThrows(MessagingException.class, () -> {
            processEmailService.processEmail(message);
        });
    }

    @Test
    void processEmailWithIncorrectSubject() throws MessagingException {
        // Kein Betreff
        Mockito.when(message.getSubject()).thenReturn(null);
        Assertions.assertThrows(MessagingException.class, () -> {
            processEmailService.processEmail(message);
        });

        Mockito.when(message.getSubject()).thenReturn("");
        Assertions.assertThrows(MessagingException.class, () -> {
            processEmailService.processEmail(message);
        });

        // Betreff ohne ZaehlungId
        Mockito.when(message.getSubject()).thenReturn("Ohne ZaehlungId");
        Assertions.assertThrows(MessagingException.class, () -> {
            processEmailService.processEmail(message);
        });

        // Betreff mit korrumpierter ZaehlungId
        Mockito.when(message.getSubject()).thenReturn("DAVe: Neue Nachricht vom Mobilitätsreferat [5ff15a92-434a-a5a9-a54914c38274]");
        Assertions.assertThrows(MessagingException.class, () -> {
            processEmailService.processEmail(message);
        });
    }

    @Test
    void processEmailWithIncorrectFromAddresses() throws MessagingException {
        // FromAddresses ist null
        Mockito.when(message.getFrom()).thenReturn(null);
        Assertions.assertThrows(MessagingException.class, () -> {
            processEmailService.processEmail(message);
        });

        // FromAddresses mehr als 1
        Mockito.when(message.getFrom()).thenReturn(new Address[] { new InternetAddress("mob@muenchen.de"), new InternetAddress("mob2@muenchen.de") });
        Assertions.assertThrows(MessagingException.class, () -> {
            processEmailService.processEmail(message);
        });
    }
}
