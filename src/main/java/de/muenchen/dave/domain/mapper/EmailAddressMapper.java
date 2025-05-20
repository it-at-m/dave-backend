package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.EmailAddress;
import de.muenchen.dave.domain.dtos.EmailAddressDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmailAddressMapper {

    EmailAddress dto2bean(EmailAddressDTO dto);

    List<EmailAddress> dto2bean(List<EmailAddressDTO> dto);

    EmailAddressDTO bean2Dto(EmailAddress bean);

    List<EmailAddressDTO> bean2Dto(List<EmailAddress> bean);
}
