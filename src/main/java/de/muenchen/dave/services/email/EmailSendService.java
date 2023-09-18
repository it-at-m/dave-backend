package de.muenchen.dave.services.email;

import de.muenchen.dave.domain.ChatMessage;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Participant;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.DienstleisterService;
import de.muenchen.dave.services.IndexService;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Diese Klasse versendet eine E-Mail auf Basis einer {@link ChatMessage} mit vorgegebener
 * Konfiguration.
 */
@Slf4j
@Service
public class EmailSendService {

    private final EmailAddressService emailAddressService;
    private final DienstleisterService dienstleisterService;
    private final IndexService indexService;
    @Value("${dave.email.address}")
    private String emailAddress;
    @Value("${dave.email.sender.url.adminportal}")
    private String urlAdminportal;
    @Value("${dave.email.sender.url.selfserviceportal}")
    private String urlSelfserviceportal;
    @Value("${dave.email.sender.hostname}")
    private String serverHostname;
    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    public EmailSendService(final EmailAddressService emailAddressService, final DienstleisterService dienstleisterService,
            final @Lazy IndexService indexService) {
        this.emailAddressService = emailAddressService;
        this.dienstleisterService = dienstleisterService;
        this.indexService = indexService;
    }

    /**
     * Sendet eine Email mit dem Inhalt der übergebenen {@link ChatMessage} an den jeweils anderen
     * Teilnehmer (Participant).
     *
     * @param message Die Chat-Nachricht
     */
    public void sendEmail(final ChatMessage message) {
        String subject = "DAVe: Neue Nachricht vom %s [%s]";
        String[] to = ArrayUtils.EMPTY_STRING_ARRAY;
        String link = "";

        // Zählung und Zählstelle werden für zusätzliche Informationen für den Inhalt der EMail benötigt
        final String zaehlungId = message.getZaehlungId().toString();
        final Zaehlung zaehlung;
        final Zaehlstelle zaehlstelle;
        try {
            zaehlung = this.indexService.getZaehlung(zaehlungId);
            zaehlstelle = this.indexService.getZaehlstelleByZaehlungId(zaehlungId);
        } catch (final DataNotFoundException e) {
            log.error("Es wurde keine Email versandt, da die Zählung oder die Zählstelle nicht gefunden wurde.");
            return;
        }

        // Je nachdem, von welchem Teilnehmer die Nachricht geschickt wurde, wird der Betreff, der Empfänger und der Link
        // aufgebaut (ParticipantId == DIENSTLEISTER_ID bedeutet, dass die Nachricht vom Dienstleister kommt)
        if (message.getParticipantId() == Participant.DIENSTLEISTER.getParticipantId()) {
            subject = String.format(subject, Participant.DIENSTLEISTER.getName(), zaehlungId);
            to = this.loadMailAddressReferat();
            // an die URL werden die Parameter für zaehlstelleId und zaehlungId angehängt
            link = String.format("%s/#/zaehlstelle/%s/%s", this.createUrl(this.urlAdminportal), zaehlstelle.getId(), zaehlungId);
        } else if (message.getParticipantId() == Participant.MOBILITAETSREFERAT.getParticipantId()) {
            subject = String.format(subject, Participant.MOBILITAETSREFERAT.getName(), zaehlungId);
            to = this.loadMailAddressDienstleister(zaehlung.getDienstleisterkennung());
            link = this.createUrl(this.urlSelfserviceportal);
        }

        // Inhalt der E-Mail
        final String content = String.format("Zur Zählung '%s' vom %s an der Zählstelle %s liegt folgende Nachricht vor: \n\n%s" +
                "\n\nLink zum Portal: %s", zaehlung.getProjektName(),
                zaehlung.getDatum().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), zaehlstelle.getNummer(), message.getContent(), link);

        if (ArrayUtils.isEmpty(to)) {
            log.warn("Es wurde keine Email versandt, da keine Email-Adresse hinterlegt ist.");
            return;
        }

        // Email erstellen und abschicken
        try {
            final Email email = new SimpleEmail();
            email.setHostName(this.serverHostname);
            email.addTo(to);
            email.setFrom(this.emailAddress);
            email.setSubject(subject);
            email.setMsg(content);
            email.send();
            log.debug("Es wurde eine Email an {} gesendet.", ArrayUtils.toString(to));
        } catch (final EmailException e) {
            log.error("Aufgrund eines Fehlers wurde keine Email versandt.", e);
        }
    }

    private String createUrl(final String baseUrl) {

        final String[] profiles = this.activeProfile.split(",");

        // Falls Profil local wird die baseUrl nicht geändert
        if (profiles[0].equalsIgnoreCase("local")) {
            return baseUrl;
        }

        // Falls Profil kon, wird der Profilname auf test geändert, damit die URL stimmt
        if (profiles[0].equalsIgnoreCase("kon") || profiles[0].equalsIgnoreCase("konexternal")) {
            profiles[0] = "test";
        }

        // Falls Profil prod, wird die baseUrl nicht geändert
        if (profiles[0].equalsIgnoreCase("prod") || profiles[0].equalsIgnoreCase("prodexternal")) {
            return String.format("%s%s", baseUrl, ".muenchen.de");
        }

        // Profilname (z.B. demo, dev, test,...) in die URL einbauen
        return String.format("%s-%s%s", baseUrl, profiles[0], ".muenchen.de");
    }

    private String[] loadMailAddressReferat() {
        final List<String> mails = new ArrayList<>();
        this.emailAddressService.loadEmailAddresses().forEach(emailAddressDTO -> mails.add(emailAddressDTO.getEmailAddress()));
        return mails.toArray(String[]::new);
    }

    private String[] loadMailAddressDienstleister(final String dienstleisterkennung) {
        return this.dienstleisterService.getDienstleisterEmailAddressByKennung(dienstleisterkennung).toArray(String[]::new);
    }
}
