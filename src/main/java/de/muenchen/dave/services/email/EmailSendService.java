package de.muenchen.dave.services.email;

import de.muenchen.dave.domain.ChatMessage;
import de.muenchen.dave.domain.dtos.EmailAddressDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Participant;
import de.muenchen.dave.domain.model.MessstelleChangeMessage;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.DienstleisterService;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Diese Klasse versendet eine E-Mail auf Basis einer {@link ChatMessage} mit vorgegebener
 * Konfiguration.
 */
@Slf4j
@Service
public class EmailSendService {

    private final EmailAddressService emailAddressService;
    private final DienstleisterService dienstleisterService;
    private final ZaehlstelleIndexService indexService;
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

    public EmailSendService(
            final EmailAddressService emailAddressService,
            final DienstleisterService dienstleisterService,
            final @Lazy
            ZaehlstelleIndexService indexService) {
        this.emailAddressService = emailAddressService;
        this.dienstleisterService = dienstleisterService;
        this.indexService = indexService;
    }

    /**
     * Sendet eine Email mit dem Inhalt der übergebenen {@link ChatMessage} an den
     * jeweils anderen Teilnehmer (Participant).
     *
     * @param message mit den Informationen für den Mailversand.
     */
    public void sendEmailForChatMessage(final ChatMessage message) {
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
            to = this.loadMailAddressesReferat();
            // an die URL werden die Parameter für zaehlstelleId und zaehlungId angehängt
            link = String.format(
                    "%s/#/zaehlstelle/%s/%s",
                    this.createUrl(this.urlAdminportal),
                    zaehlstelle.getId(),
                    zaehlungId);
        } else if (message.getParticipantId() == Participant.MOBILITAETSREFERAT.getParticipantId()) {
            subject = String.format(subject, Participant.MOBILITAETSREFERAT.getName(), zaehlungId);
            to = this.loadMailAddressDienstleister(zaehlung.getDienstleisterkennung());
            link = this.createUrl(this.urlSelfserviceportal);
        }

        // Inhalt der E-Mail
        final String content = String.format(
                "Zur Zählung '%s' vom %s an der Zählstelle %s liegt folgende Nachricht vor: \n\n%s" +
                        "\n\nLink zum Portal: %s",
                zaehlung.getProjektName(),
                zaehlung.getDatum().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                zaehlstelle.getNummer(),
                message.getContent(),
                link);

        if (ArrayUtils.isEmpty(to)) {
            log.warn("Es wurde keine Email versandt, da keine Email-Adresse hinterlegt ist.");
            return;
        }

        this.sendMail(to, subject, content);
    }

    /**
     * Sendet eine Email mit dem Inhalt der übergebenen {@link MessstelleChangeMessage}
     * an den Participant {@link Participant#MOBILITAETSREFERAT}.
     *
     * @param message mit den Informationen für den Mailversand.
     */
    public void sendMailForMessstelleChangeMessage(final MessstelleChangeMessage message) {
        final var emailAdressesMobilitaetsreferat = this.loadMailAddressesReferat();

        final String subject;
        var content = String.format("Zur Messstelle \"%s\" liegt folgende Nachricht vor: \n\n", message.getMstId());
        if (Objects.isNull(message.getStatusAlt()) && !Objects.isNull(message.getStatusNeu())) {
            subject = String.format("DAVe: Neue Messstelle %s", message.getMstId());
            content = content
                    + String.format("Es handelt sich um einen neue und in Status \"%s\" befindliche Messstelle. \n\n", message.getStatusNeu());
        } else {
            // Statusänderung
            subject = String.format("DAVe: Statusänderung Messstelle %s", message.getMstId());
            content = content
                    + String.format("Der Messstellenstatus hat sich von \"%s\" auf \"%s\" geändert. \n\n", message.getStatusAlt(), message.getStatusNeu());
        }
        final var link = String.format("%s/#/messstelle/%s", this.createUrl(this.urlAdminportal), message.getTechnicalIdMst());
        content = content + link;

        this.sendMail(emailAdressesMobilitaetsreferat, subject, content);
    }

    /**
     * Die Methode erstellt und versendet die Email mit den im Parameter gegebenen Informationen.
     *
     * @param to als Emailadresse des Empfängers.
     * @param subject als Betreff.
     * @param content für Inhalt der Mail.
     */
    protected void sendMail(final String[] to, final String subject, final String content) {
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

    protected String createUrl(final String baseUrl) {

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

    private String[] loadMailAddressesReferat() {
        return this.emailAddressService.loadEmailAddresses()
                .stream()
                .map(EmailAddressDTO::getEmailAddress)
                .toArray(String[]::new);
    }

    private String[] loadMailAddressDienstleister(final String dienstleisterkennung) {
        return this.dienstleisterService.getDienstleisterEmailAddressByKennung(dienstleisterkennung).toArray(String[]::new);
    }
}
