package de.muenchen.dave.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;

import de.muenchen.dave.domain.EmailAddress;
import de.muenchen.dave.domain.dtos.EmailAddressDTO;
import de.muenchen.dave.domain.dtos.EmailAddressDTORandomFactory;
import de.muenchen.dave.domain.mapper.EmailAddressMapperImpl;
import de.muenchen.dave.repositories.relationaldb.EmailAddressRepository;
import de.muenchen.dave.services.email.EmailAddressService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class EmailAddressServiceTest {

    private final EmailAddressService emailAddressService;
    private final EmailAddressRepository emailAddressRepository;

    public EmailAddressServiceTest() {
        this.emailAddressRepository = Mockito.mock(EmailAddressRepository.class);
        this.emailAddressService = new EmailAddressService(this.emailAddressRepository, new EmailAddressMapperImpl());
    }

    @Test
    public void updateEmailAddressTest() {
        // Test wenn keine Email-Adresse zur ParticipantId existiert
        final EmailAddressDTO emailAddressDTO = EmailAddressDTORandomFactory.getOne();
        final int participantId = emailAddressDTO.getParticipantId();
        final EmailAddress emailAddress = new EmailAddressMapperImpl().dto2bean(emailAddressDTO);
        Mockito.when(this.emailAddressRepository.saveAndFlush(any())).thenReturn(emailAddress);
        EmailAddressDTO result = this.emailAddressService.saveOrUpdateEmailAddress(emailAddressDTO);
        assertThat(result, is(emailAddressDTO));

        // Test wenn eine Email-Adresse zur ParticipantId existiert
        result = this.emailAddressService.saveOrUpdateEmailAddress(emailAddressDTO);
        assertThat(result, is(emailAddressDTO));
    }

}
