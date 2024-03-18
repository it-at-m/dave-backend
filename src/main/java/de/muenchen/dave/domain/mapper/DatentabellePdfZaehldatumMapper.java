package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.pdf.helper.DatentabellePdfZaehldatum;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

/**
 * Mapper wird benötigt, da Mustache nicht mit @JsonGetter umgehen kann.
 * Mustache ist eine Sprache für Templates aus denen die für die PDF benötigten HTML Strings
 * entstehen.
 */
@Mapper(componentModel = "spring")
public interface DatentabellePdfZaehldatumMapper {

    String UHRZEIT_23_59 = "23:59";
    String UHRZEIT_24_00 = "24:00";

    @Mappings({
            @Mapping(target = "endeUhrzeit", source = "ladeZaehldatumDTO.endeUhrzeit", dateFormat = "HH:mm"),
            @Mapping(target = "startUhrzeit", source = "ladeZaehldatumDTO.startUhrzeit", dateFormat = "HH:mm"),
    })
    DatentabellePdfZaehldatum ladeZaehldatumDTO2bean(LadeZaehldatumDTO ladeZaehldatumDTO);

    @AfterMapping
    default void toDatentabellePdfZaehldatum(@MappingTarget DatentabellePdfZaehldatum bean, LadeZaehldatumDTO dto) {
        if (StringUtils.equals(bean.getEndeUhrzeit(), UHRZEIT_23_59)) {
            bean.setEndeUhrzeit(UHRZEIT_24_00);
        }
    }

    List<DatentabellePdfZaehldatum> ladeZaehldatumDTOList2beanList(List<LadeZaehldatumDTO> ladeZaehldatumDTOList);

    @Mappings({
            @Mapping(target = "endeUhrzeit", source = "dto.endeUhrzeit", dateFormat = "HH:mm"),
            @Mapping(target = "startUhrzeit", source = "dto.startUhrzeit", dateFormat = "HH:mm"),
    })
    DatentabellePdfZaehldatum ladeMesswerteDTO2bean(LadeMesswerteDTO dto);

    @AfterMapping
    default void ladeMesswerteDTO2beanAfterMapping(@MappingTarget DatentabellePdfZaehldatum bean, LadeMesswerteDTO dto) {
        if (StringUtils.equals(bean.getEndeUhrzeit(), UHRZEIT_23_59)) {
            bean.setEndeUhrzeit(UHRZEIT_24_00);
        }
    }

    List<DatentabellePdfZaehldatum> ladeMesswerteDTOList2beanList(List<LadeMesswerteDTO> LadeMesswerteDTOList);

}
