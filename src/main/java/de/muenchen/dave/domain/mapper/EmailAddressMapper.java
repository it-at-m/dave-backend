package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.EmailAddress;
import de.muenchen.dave.domain.dtos.EmailAddressDto;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmailAddressMapper {

    EmailAddress dto2bean(EmailAddressDto dto);

    List<EmailAddress> dto2bean(List<EmailAddressDto> dto);

    EmailAddressDto bean2Dto(EmailAddress bean);

    List<EmailAddressDto> bean2Dto(List<EmailAddress> bean);
}
