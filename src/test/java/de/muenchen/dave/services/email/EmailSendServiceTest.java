package de.muenchen.dave.services.email;

import de.muenchen.dave.services.DienstleisterService;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
    public void beforeEach() {
        emailSendService = new EmailSendService(emailAddressService, dienstleisterService, zaehlstelleIndexService);
    }

}
