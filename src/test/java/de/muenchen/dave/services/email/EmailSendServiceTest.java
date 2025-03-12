package de.muenchen.dave.services.email;

import de.muenchen.dave.domain.ChatMessage;
import de.muenchen.dave.domain.dtos.EmailAddressDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.MessstelleStatus;
import de.muenchen.dave.domain.enums.Participant;
import de.muenchen.dave.domain.model.MessstelleChangeMessage;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.DienstleisterService;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailSendServiceTest {

    @Mock
    private EmailAddressService emailAddressService;

    @Mock
    private DienstleisterService dienstleisterService;

    @Mock
    private ZaehlstelleIndexService zaehlstelleIndexService;

    private EmailSendService emailSendService;

    @BeforeEach
    public void beforeEach() throws IllegalAccessException {
        emailSendService = new EmailSendService(emailAddressService, dienstleisterService, zaehlstelleIndexService);
        FieldUtils.writeField(emailSendService, "emailAddress", "dave.mail@xxx.yyy", true);
        FieldUtils.writeField(emailSendService, "urlAdminportal", "url-adminportal", true);
        FieldUtils.writeField(emailSendService, "urlSelfserviceportal", "url-selfserviceportal", true);
        FieldUtils.writeField(emailSendService, "serverHostname", "server-hostname", true);
        FieldUtils.writeField(emailSendService, "activeProfile", "local", true);
        Mockito.reset(emailAddressService, dienstleisterService, zaehlstelleIndexService);
    }

    @Test
    void sendEmailForChatMessageDienstleister() throws DataNotFoundException {
        final var chatMessage = new ChatMessage();
        chatMessage.setZaehlungId(UUID.randomUUID());
        chatMessage.setParticipantId(Participant.DIENSTLEISTER.getParticipantId());
        chatMessage.setContent("Der Content!");

        final var emailSendServiceSpy = Mockito.spy(this.emailSendService);

        final var emailAdress1 = new EmailAddressDTO();
        emailAdress1.setEmailAddress("mail1@xxx.yy");
        final var emailAdress2 = new EmailAddressDTO();
        emailAdress2.setEmailAddress("mail2@xxx.yy");

        Mockito.when(emailAddressService.loadEmailAddresses()).thenReturn(List.of(emailAdress1, emailAdress2));

        final var zaehlung = new Zaehlung();
        zaehlung.setId(chatMessage.getZaehlungId().toString());
        zaehlung.setDienstleisterkennung("dienstleisterkennung");
        zaehlung.setDatum(LocalDate.of(2025, 3, 5));
        zaehlung.setProjektName("Projektname");
        Mockito.when(zaehlstelleIndexService.getZaehlung(chatMessage.getZaehlungId().toString())).thenReturn(zaehlung);

        final var zaehlstelle = new Zaehlstelle();
        zaehlstelle.setId(UUID.randomUUID().toString());
        zaehlstelle.setNummer("54321");
        Mockito.when(zaehlstelleIndexService.getZaehlstelleByZaehlungId(chatMessage.getZaehlungId().toString())).thenReturn(zaehlstelle);

        emailSendServiceSpy.sendEmailForChatMessage(chatMessage);

        final var expectedTo = new String[] { emailAdress1.getEmailAddress(), emailAdress2.getEmailAddress() };
        final var expectedSubject = String.format("DAVe: Neue Nachricht vom Dienstleister [%s]", chatMessage.getZaehlungId().toString());
        final var expectedBody = "Zur Zählung 'Projektname' vom 05.03.2025 an der Zählstelle 54321 liegt folgende Nachricht vor: " +
                "\n\nDer Content!" +
                "\n\n" + String.format("Link zum Portal: url-adminportal/#/zaehlstelle/%s/%s", zaehlstelle.getId(), zaehlung.getId());
        Mockito.verify(emailSendServiceSpy, Mockito.times(1)).sendMail(expectedTo, expectedSubject, expectedBody);

        Mockito.verify(emailAddressService, Mockito.times(1)).loadEmailAddresses();

        Mockito.verify(zaehlstelleIndexService, Mockito.times(1)).getZaehlung(chatMessage.getZaehlungId().toString());

        Mockito.verify(zaehlstelleIndexService, Mockito.times(1)).getZaehlstelleByZaehlungId(chatMessage.getZaehlungId().toString());

        Mockito.verifyNoInteractions(dienstleisterService);
    }

    @Test
    void sendEmailForChatMessageMobilitaetsrefereat() throws DataNotFoundException {
        final var chatMessage = new ChatMessage();
        chatMessage.setZaehlungId(UUID.randomUUID());
        chatMessage.setParticipantId(Participant.MOBILITAETSREFERAT.getParticipantId());
        chatMessage.setContent("Der Content!");

        final var emailSendServiceSpy = Mockito.spy(this.emailSendService);

        final var zaehlung = new Zaehlung();
        zaehlung.setId(chatMessage.getZaehlungId().toString());
        zaehlung.setDienstleisterkennung("dienstleisterkennung");
        zaehlung.setDatum(LocalDate.of(2025, 3, 5));
        zaehlung.setProjektName("Projektname");
        Mockito.when(zaehlstelleIndexService.getZaehlung(chatMessage.getZaehlungId().toString())).thenReturn(zaehlung);

        Mockito.when(dienstleisterService.getDienstleisterEmailAddressByKennung(zaehlung.getDienstleisterkennung()))
                .thenReturn(List.of("mail1@xxx.yy", "mail2@xxx.yy"));

        final var zaehlstelle = new Zaehlstelle();
        zaehlstelle.setId(UUID.randomUUID().toString());
        zaehlstelle.setNummer("54321");
        Mockito.when(zaehlstelleIndexService.getZaehlstelleByZaehlungId(chatMessage.getZaehlungId().toString())).thenReturn(zaehlstelle);

        emailSendServiceSpy.sendEmailForChatMessage(chatMessage);

        final var expectedTo = new String[] { "mail1@xxx.yy", "mail2@xxx.yy" };
        final var expectedSubject = String.format("DAVe: Neue Nachricht vom Mobilitätsreferat [%s]", chatMessage.getZaehlungId().toString());
        final var expectedBody = "Zur Zählung 'Projektname' vom 05.03.2025 an der Zählstelle 54321 liegt folgende Nachricht vor: " +
                "\n\nDer Content!" +
                "\n\nLink zum Portal: url-selfserviceportal";
        Mockito.verify(emailSendServiceSpy, Mockito.times(1)).sendMail(expectedTo, expectedSubject, expectedBody);

        Mockito.verifyNoInteractions(emailAddressService);

        Mockito.verify(zaehlstelleIndexService, Mockito.times(1)).getZaehlung(chatMessage.getZaehlungId().toString());

        Mockito.verify(zaehlstelleIndexService, Mockito.times(1)).getZaehlstelleByZaehlungId(chatMessage.getZaehlungId().toString());

        Mockito.verify(dienstleisterService, Mockito.times(1)).getDienstleisterEmailAddressByKennung(zaehlung.getDienstleisterkennung());
    }

    @Test
    void sendMailForMessstelleChangeMessageForNewMessstelle() {
        final var emailSendServiceSpy = Mockito.spy(this.emailSendService);

        final var emailAdress1 = new EmailAddressDTO();
        emailAdress1.setEmailAddress("mail1@xxx.yy");
        final var emailAdress2 = new EmailAddressDTO();
        emailAdress2.setEmailAddress("mail2@xxx.yy");

        Mockito.when(emailAddressService.loadEmailAddresses()).thenReturn(List.of(emailAdress1, emailAdress2));

        final var messstelleChangeMessage = new MessstelleChangeMessage();

        messstelleChangeMessage.setTechnicalIdMst("1234");
        messstelleChangeMessage.setMstId("9876");
        messstelleChangeMessage.setStatusAlt(null);
        messstelleChangeMessage.setStatusNeu(MessstelleStatus.IN_BESTAND);

        emailSendServiceSpy.sendMailForMessstelleChangeMessage(messstelleChangeMessage);

        final var expectedTo = new String[] { emailAdress1.getEmailAddress(), emailAdress2.getEmailAddress() };
        final var expectedSubject = "DAVe: Neue Messstelle 9876";
        final var expectedBody = "Zur Messstelle \"9876\" liegt folgende Nachricht vor: \n\n"
                + "Es handelt sich um einen neue und in Status \"IN_BESTAND\" befindliche Messstelle. \n\n"
                + "url-adminportal/#/messstelle/1234";
        Mockito.verify(emailSendServiceSpy, Mockito.times(1)).sendMail(expectedTo, expectedSubject, expectedBody);

        Mockito.verify(emailAddressService, Mockito.times(1)).loadEmailAddresses();
    }

    @Test
    void sendMailForMessstelleChangeMessageForExistingMessstelle() {
        final var emailSendServiceSpy = Mockito.spy(this.emailSendService);

        final var emailAdress1 = new EmailAddressDTO();
        emailAdress1.setEmailAddress("mail1@xxx.yy");
        final var emailAdress2 = new EmailAddressDTO();
        emailAdress2.setEmailAddress("mail2@xxx.yy");

        Mockito.when(emailAddressService.loadEmailAddresses()).thenReturn(List.of(emailAdress1, emailAdress2));

        final var messstelleChangeMessage = new MessstelleChangeMessage();

        messstelleChangeMessage.setTechnicalIdMst("1234");
        messstelleChangeMessage.setMstId("9876");
        messstelleChangeMessage.setStatusAlt(MessstelleStatus.IN_PLANUNG);
        messstelleChangeMessage.setStatusNeu(MessstelleStatus.IN_BESTAND);

        emailSendServiceSpy.sendMailForMessstelleChangeMessage(messstelleChangeMessage);

        final var expectedTo = new String[] { emailAdress1.getEmailAddress(), emailAdress2.getEmailAddress() };
        final var expectedSubject = "DAVe: Statusänderung Messstelle 9876";
        final var expectedBody = "Zur Messstelle \"9876\" liegt folgende Nachricht vor: \n\n"
                + "Der Messstellenstatus hat sich von \"IN_PLANUNG\" auf \"IN_BESTAND\" geändert. \n\n"
                + "url-adminportal/#/messstelle/1234";
        Mockito.verify(emailSendServiceSpy, Mockito.times(1)).sendMail(expectedTo, expectedSubject, expectedBody);

        Mockito.verify(emailAddressService, Mockito.times(1)).loadEmailAddresses();
    }

}
