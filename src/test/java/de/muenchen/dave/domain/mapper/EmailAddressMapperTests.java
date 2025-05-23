package de.muenchen.dave.domain.mapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

import de.muenchen.dave.domain.EmailAddress;
import de.muenchen.dave.domain.dtos.EmailAddressDTO;
import de.muenchen.dave.domain.dtos.EmailAddressDTORandomFactory;
import de.muenchen.dave.domain.relationaldb.EmailAddressRandomFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class EmailAddressMapperTests {

    private final EmailAddressMapper mapper = new EmailAddressMapperImpl();

    @Test
    public void testDto2bean() {
        EmailAddressDTO dto = EmailAddressDTORandomFactory.getOne();
        EmailAddress bean = this.mapper.dto2bean(dto);

        assertThat(bean, hasProperty("participantId", equalTo(dto.getParticipantId())));
        assertThat(bean, hasProperty("emailAddress", equalTo(dto.getEmailAddress())));
    }

    @Test
    public void testBean2Dto() {
        EmailAddress bean = EmailAddressRandomFactory.getOne();
        EmailAddressDTO dto = this.mapper.bean2Dto(bean);

        assertThat(dto, hasProperty("participantId", equalTo(bean.getParticipantId())));
        assertThat(dto, hasProperty("emailAddress", equalTo(bean.getEmailAddress())));
    }

}
