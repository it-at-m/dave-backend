package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.EmailAddress;
import de.muenchen.dave.domain.dtos.EmailAddressDTO;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmailAddressMapper {

    EmailAddress dto2bean(EmailAddressDTO dto);

    List<EmailAddress> dto2bean(List<EmailAddressDTO> dto);

    EmailAddressDTO bean2Dto(EmailAddress bean);

    List<EmailAddressDTO> bean2Dto(List<EmailAddress> bean);
}
