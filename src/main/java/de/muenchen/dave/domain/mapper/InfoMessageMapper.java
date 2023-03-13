package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.InfoMessage;
import de.muenchen.dave.domain.dtos.InfoMessageDTO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.time.LocalDate;
import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface InfoMessageMapper {

    InfoMessage dto2bean(InfoMessageDTO dto);

    InfoMessageDTO bean2Dto(InfoMessage bean);

    @AfterMapping
    default void toInfoMessageDTO(@MappingTarget InfoMessageDTO dto, InfoMessage chatMessage) {
        final LocalDate now = LocalDate.now(ZoneId.of("Europe/Berlin"));
        if (chatMessage.getGueltigVon() == null || chatMessage.getGueltigBis() == null) {
            dto.setGueltig(false);
        } else {
            dto.setGueltig((chatMessage.getGueltigVon().isBefore(now) || chatMessage.getGueltigVon().isEqual(now)) && (chatMessage.getGueltigBis().isEqual(now) || chatMessage.getGueltigBis().isAfter(now)));
        }
    }
}
