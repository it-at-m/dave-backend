package de.muenchen.dave.services.email;

import de.muenchen.dave.domain.dtos.ChatMessageDTO;
import de.muenchen.dave.domain.dtos.MessageTimeDTO;
import de.muenchen.dave.domain.enums.Participant;
import de.muenchen.dave.domain.mapper.ChatMessageMapperImpl;
import de.muenchen.dave.services.ChatMessageService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeMultipart;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Diese Klasse stellt eine Methode zum Verarbeiten einer Email bereit.
 */
@Slf4j
@Service
public class ProcessEmailService {

    @Value("${dave.email.receiver.cut-email-body.line-contains-strings}")
    private String containsStrings;

    @Value("${dave.email.receiver.cut-email-body.line-begins-with-strings}")
    private String beginsWithStrings;

    /**
     * Verarbeitet eine Email zu einem {@link ChatMessageDTO}.
     *
     * @param message die zu verarbeitende Email
     * @return das generierte {@link ChatMessageDTO}
     * @throws IOException Fehler bei der Verbindung zum Postfach
     * @throws MessagingException allgemeiner E-Mail Fehler
     */
    public ChatMessageDTO processEmail(final Message message) throws IOException, MessagingException {
        final String messageContent = processContent(message);
        return generateChatMessage(message, messageContent);
    }

    /**
     * Verarbeitet den Inhalt der Email.
     *
     * @param message die Nachricht
     * @return der verarbeitete Inhalt
     * @throws IOException Fehler bei der Verbindung zum Postfach
     * @throws MessagingException allgemeiner E-Mail Fehler
     */
    private String processContent(final Part message) throws MessagingException, IOException {
        // ContentTyp auslesen
        final String contentType = message.getContentType();
        if (ObjectUtils.isNotEmpty(contentType)) {
            // Content auslesen
            final Object content = message.getContent();
            if (ObjectUtils.isNotEmpty(content)) {
                // Verarbeitung je nach ContentTyp
                if (contentType.contains("text/html")) {
                    return getContentWithoutAppending(parseHtml(content.toString()));
                } else if (contentType.contains("text/plain")) {
                    return getContentWithoutAppending(content.toString());
                } else if (contentType.contains("multipart")) {
                    return processMultipartMessage((MimeMultipart) content);
                } else {
                    throw new MessagingException(String.format("Der ContentType %s wird nicht unterstützt.", contentType));
                }
            } else {
                log.warn("Der Inhalt der Email ist leer.");
                return "";
            }
        } else {
            throw new MessagingException("Der ContentType konnte nicht ermittelt werden.");
        }
    }

    /**
     * Erstellt aus dem HTML-Inhalt einen leserlichen Text. Entfernt ggf. die Signatur.
     *
     * @param htmlContent Inhalt der Nachricht im HTML-Format
     * @return Plain-Text Nachricht (evtl. ohne Signatur)
     */
    private String parseHtml(final String htmlContent) {
        // Holt sich nur das Body-Element aus dem Inhalt
        final Element body = Jsoup.parse(htmlContent).body();

        // Signatur entfernen, falls eine per HTML-Tag gefunden wird
        final Element signature = body.getElementById("Signature");
        if (ObjectUtils.isNotEmpty(signature)) {
            signature.remove();
        }

        // Plain-Text-Inhalt mit Zeilenumbrüchen zurückgeben
        return body.wholeText();
    }

    /**
     * Verarbeitet den Plain-Text-Inhalt wie folgt: Jede Zeile wird auf ein Match mit einem String aus
     * der Konfiguration geprüft.
     * Falls es eine Übereinstimmung gibt, wird der restliche Inhalt (Signatur oder ursprüngliche
     * Nachricht) verworfen.
     * Zuletzt werden führende und anhängende Leerzeichen und Zeilenumbrüche entfernt.
     *
     * @param content Plain-Text-Inhalt
     * @return Inhalt ohne Signatur und ursprünglicher Nachricht
     */
    private String getContentWithoutAppending(final String content) {
        if (ObjectUtils.isEmpty(content)) {
            return content;
        }

        // kompletten Inhalt in einzelne Zeilen zerlegen
        final StringBuilder clearContent = new StringBuilder();
        final String[] lines = content.split("\r\n");

        // Konfigurierte Strings in einzelne trennen
        final String[] containsStringsList = containsStrings.split(";");
        final String[] beginsWithStringsList = beginsWithStrings.split(";");

        // Zeilen durchlaufen
        for (String line : lines) {
            for (String containsString : containsStringsList) {
                if (line.contains(containsString)) {
                    return clearContent.toString().trim();
                }
            }
            for (String beginsWithString : beginsWithStringsList) {
                if (line.startsWith(beginsWithString)) {
                    return clearContent.toString().trim();
                }
            }

            // Zeile an gesäuberten Inhalt mit Zeilenumbruch anhängen
            clearContent.append(line).append("\n");
        }
        return clearContent.toString().trim();
    }

    /**
     * Diese Methode verarbeitet MultipartMessages. Da aus technischem Aufwand keine Anhänge unterstützt
     * werden, wird
     * einfach nur der erste Teil der Nachricht verarbeitet, da dieser dem Hauptteil entspricht.
     *
     * @param multipartMessage die zu verarbeitende Nachricht
     * @return der verarbeitete Inhalt der Nachricht
     * @throws IOException Fehler bei der Verbindung zum Postfach
     * @throws MessagingException allgemeiner E-Mail Fehler
     */
    private String processMultipartMessage(final MimeMultipart multipartMessage) throws MessagingException, IOException {
        if (multipartMessage.getCount() > 0) {
            final BodyPart bodyPart = multipartMessage.getBodyPart(0);
            if (ObjectUtils.isNotEmpty(bodyPart)) {
                // Warnung, dass nicht alles verarbeitet wird
                log.warn("Die Email besteht aus mehreren Teilen (Multipart). Es wird nur der erste Teil (Hauptnachricht) verarbeitet.");
                return processContent(bodyPart) + "\r\n[Der Anhang wurde entfernt. Anhänge werden nicht unterstützt.]";
            } else {
                log.warn("Die Email besteht aus mehreren Teilen (Multipart). Der Inhalt des ersten Teils ist leer. " +
                        "Die anderen Teile werden nicht verarbeitet.");
                return "";
            }
        } else {
            log.warn("Der Inhalt der Email ist leer.");
            return "";
        }
    }

    /**
     * Generiert aus einer {@link Message} ein {@link ChatMessageDTO}.
     *
     * @param msg die Nachricht aus der Email
     * @param messageContent der Inhalt aus der Nachricht
     * @return das generierte {@link ChatMessageDTO}
     * @throws MessagingException allgemeiner E-Mail Fehler
     */
    private ChatMessageDTO generateChatMessage(final Message msg, final String messageContent) throws MessagingException {

        // Betreff auslesen
        final String subject = msg.getSubject();
        if (ObjectUtils.isEmpty(subject)) {
            throw new MessagingException("Es konnte keine ChatMessage generiert werden, da der Betreff fehlt.");
        }

        // zaehlungId aus Betreff extrahieren und überprüfen
        final int idStartIndex = subject.indexOf("[");
        final String zaehlungId = subject.substring(idStartIndex + 1, subject.length() - 1);
        try {
            UUID.fromString(zaehlungId);
        } catch (IllegalArgumentException e) {
            throw new MessagingException("Es konnte keine ChatMessage generiert werden, da die UUID im Betreff fehlt " +
                    "oder korrumpiert ist.", e);
        }

        // Wenn im Absender "@muenchen.de" steht, dann ist der Participant, der geantwortet hat, das Mobilitätsreferat
        // ansonsten der Dienstleister
        int participantId = Participant.DIENSTLEISTER.getParticipantId();
        final Address[] fromAddresses = msg.getFrom();
        if (ObjectUtils.isNotEmpty(fromAddresses) && fromAddresses.length == 1) {
            if (fromAddresses[0].toString().contains("@muenchen.de")) {
                participantId = Participant.MOBILITAETSREFERAT.getParticipantId();
            }
        } else {
            throw new MessagingException("Es konnte keine ChatMessage generiert werden, da die Anzahl der Absender nicht gleich 1 ist.");
        }

        // ChatMessageDTO erstellen
        final ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        chatMessageDTO.setZaehlungId(zaehlungId);
        chatMessageDTO.setContent(messageContent);
        chatMessageDTO.setParticipantId(participantId);
        chatMessageDTO.setType(ChatMessageService.MESSAGE_TYPE);

        // MessageTimeDTO setzen
        final Date sentDate = msg.getSentDate();
        if (ObjectUtils.isNotEmpty(sentDate)) {
            chatMessageDTO.setMessageTimeDTO(
                    generateMessageTimeDTO(sentDate.toInstant()
                            .atZone(ChatMessageService.ZONE)
                            .toLocalDateTime()));
        } else {
            log.warn("Der Sendezeitpunk der Email konnte nicht ermittelt werden. " +
                    "Der Zeitstempel der ChatMessage erhält beim Abspeichern die aktuelle Systemzeit.");
        }
        return chatMessageDTO;
    }

    // Erstellt das MessageTimeDTO
    private MessageTimeDTO generateMessageTimeDTO(final LocalDateTime sentDate) {
        return (new ChatMessageMapperImpl()).localDateTimeToMessageTimeDTO(sentDate);
    }
}
