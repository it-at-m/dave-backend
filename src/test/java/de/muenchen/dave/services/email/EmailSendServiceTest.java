package de.muenchen.dave.services.email;

import de.muenchen.dave.domain.dtos.EmailAddressDTO;
import de.muenchen.dave.domain.enums.MessstelleStatus;
import de.muenchen.dave.domain.model.MessstelleChangeMessage;
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

import java.util.List;

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
    }

    @Test
    void sendMailForMessstelleChangeMessage() {
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

        final var expectedTo = new String[] {emailAdress1.getEmailAddress(), emailAdress2.getEmailAddress()};
        final var expectedSubject = "DAVe: Neue Messstelle 9876";
        final var expectedBody = "Zur Messstelle \"9876\" liegt folgende Nachricht vor: \n\n"
                + "Es handelt sich um einen neue und in Status \"IN_BESTAND\" befindliche Messstelle.\n\n"
                + "url-adminportal/#/messstelle/1234";
        Mockito.verify(emailSendServiceSpy, Mockito.times(1)).sendMail(expectedTo, expectedSubject, expectedBody);
    }

}
