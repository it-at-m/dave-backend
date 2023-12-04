package de.muenchen.dave.services.messstelle;

import com.sun.mail.imap.IMAPFolder;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.geodateneai.gen.api.MessstelleApi;
import de.muenchen.dave.geodateneai.gen.model.MessstelleDto;
import de.muenchen.dave.services.ChatMessageService;
import de.muenchen.dave.services.email.ProcessEmailService;
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
import java.util.List;
import java.util.Properties;

/**
 * Die Klasse {@link StammdatenReceiveService} holt alle relevanten Messstellen aus MobidaM
 * und aktulisiert die in Dave gespeichereten Daten.
 */
@Slf4j
@Service
public class StammdatenReceiveService {

    private MessstelleApi messstelleApi;

    /**
     * Diese Methode verbindet sich mit dem EMail-Postfach und verarbeitet alle neuen E-Mails.
     * Wie oft gecheckt wird, kann in der application.yml geändert werden.
     * Falls keine E-Mail Adresse oder die dummy-Email-Adresse konfiguriert ist, wird nichts getan.
     */
    @Scheduled(cron = "${dave.messstellen.update-cron}")
    public void loadMessstellen() {
        // Daten aus MobidaM laden
        final List<MessstelleDto> body = messstelleApi.getMessstellenWithHttpInfo().block().getBody();
        // Daten aus Dave laden

        // Daten aktualisieren

        // Daten speichern
    }
}
