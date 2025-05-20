package de.muenchen.dave.services.email;

import com.sun.mail.imap.IMAPFolder;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.ChatMessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.io.IOException;
import java.util.Properties;

/**
 * Die Klasse {@link EmailReceiveService} checkt neue Emails im Postfach der technischen
 * E-Mail-Adresse und verarbeitet diese.
 */
@Slf4j
@Service
public class EmailReceiveService {

    private static final String DUMMY_EMAIL_ADDRESS = "dave@dummy.de";
    private final ChatMessageService chatMessageService;
    private final ProcessEmailService processEmailService;
    @Value("${dave.email.address}")
    private String emailAddress;
    @Value("${dave.email.password}")
    private String emailPassword;
    @Value("${dave.email.receiver.hostname}")
    private String serverHostname;
    @Value("${dave.email.receiver.protocol}")
    private String protocol;
    @Value("${dave.email.receiver.port}")
    private String port;
    @Value("${dave.email.receiver.folder-success}")
    private String folderSuccessName;
    @Value("${dave.email.receiver.folder-error}")
    private String folderErrorName;
    private IMAPFolder folderInbox;
    private IMAPFolder folderSuccess;
    private IMAPFolder folderError;
    private Store store;

    public EmailReceiveService(final ChatMessageService chatMessageService, final ProcessEmailService processEmailService) {
        this.chatMessageService = chatMessageService;
        this.processEmailService = processEmailService;
    }

    /**
     * Diese Methode verbindet sich mit dem EMail-Postfach und verarbeitet alle neuen E-Mails. Wie oft
     * gecheckt wird, kann in der application.yml geändert
     * werden. Falls keine E-Mail Adresse oder die dummy-Email-Adresse konfiguriert ist, wird nichts
     * getan.
     */
    @Scheduled(fixedDelayString = "${dave.email.receiver.update-interval}")
    public void checkEmails() {
        if (ObjectUtils.isNotEmpty(emailAddress) && !emailAddress.equals(DUMMY_EMAIL_ADDRESS)) {
            log.debug("Scheduler EmailReceiveService: Check nach neuen E-Mails im Postfach {}", emailAddress);
            try {
                connect();
                processFolderInbox();
                disconnect();
            } catch (MessagingException e) {
                log.error("Fehler in der Verbindung zum MessageStore.", e);
            }
        }
    }

    // Verbindung zum Postfach aufbauen
    private void connect() throws MessagingException {
        final Properties properties = getServerProperties();
        final Session session = Session.getDefaultInstance(properties);

        // Stellt Verbindung zum MessageStore her
        store = session.getStore(protocol);
        store.connect(emailAddress, emailPassword);

        // Öffnet den Ordner Inbox (Posteingang)
        folderInbox = (IMAPFolder) store.getFolder("INBOX");
        folderInbox.open(Folder.READ_WRITE);

        // Legt die Ordner Success und Error an, falls sie noch nicht existieren
        folderSuccess = createImapFolderIfNotExists(folderSuccessName);
        folderError = createImapFolderIfNotExists(folderErrorName);
    }

    // Verbindung zum Postfach schließen
    private void disconnect() throws MessagingException {
        folderInbox.close(false);
        store.close();
    }

    // Emails verarbeiten
    private void processFolderInbox() throws MessagingException {
        // alle Emails aus dem Postfach holen
        final Message[] messages;
        messages = folderInbox.getMessages();

        // Emails verarbeiten und abspeichern
        for (final Message message : messages) {
            try {
                chatMessageService.saveChatMessage(processEmailService.processEmail(message));
                // Flag auf gelesen setzen und verschieben wenn Verarbeitung erfolgreich war
                folderInbox.setFlags(new Message[] { message }, new Flags(Flags.Flag.SEEN), true);
                moveMessage(message, folderSuccess);
            } catch (MessagingException | IOException e) {
                log.error("Es ist ein Fehler beim Verarbeiten der E-Mail aufgetreten.", e);
                moveMessage(message, folderError);
            } catch (BrokenInfrastructureException | DataNotFoundException e) {
                log.error("Es ist ein Fehler beim Speichern der E-Mail aufgetreten.", e);
                moveMessage(message, folderError);
            }
        }
    }

    // Verschiebt eine Email
    private void moveMessage(Message message, IMAPFolder folder) {
        try {
            folderInbox.moveMessages(new Message[] { message }, folder);
        } catch (MessagingException e) {
            log.error("Es ist ein Fehler beim Verschieben der E-Mail aufgetreten.", e);
        }
    }

    // Erstellt neuen Ordner, falls er noch nicht existiert
    private IMAPFolder createImapFolderIfNotExists(String folderName) throws MessagingException {
        final IMAPFolder folder = (IMAPFolder) store.getFolder(folderName);
        if (!folder.exists()) {
            folder.create(Folder.HOLDS_MESSAGES);
            folder.setSubscribed(true);
            log.debug(String.format("Ordner %s im Postfach %s erstellt.", folderName, emailAddress));
        }
        return folder;
    }

    /**
     * Gibt ein {@link Properties} Objekt zurück, das konfiguriert ist für POP3/IMAP Server
     *
     * @return ein Properties Object
     */
    private Properties getServerProperties() {
        final Properties properties = new Properties();

        // server setting
        properties.put(String.format("mail.%s.host", protocol), serverHostname);
        properties.put(String.format("mail.%s.port", protocol), port);

        // SSL setting
        properties.setProperty(
                String.format("mail.%s.socketFactory.class", protocol),
                "javax.net.ssl.SSLSocketFactory");
        properties.setProperty(
                String.format("mail.%s.socketFactory.fallback", protocol),
                "false");
        properties.setProperty(
                String.format("mail.%s.socketFactory.port", protocol), port);

        return properties;
    }
}
