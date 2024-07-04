package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.csv.DatentabelleCsvZaehldatum;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DatentabelleCsvZaehldatumMapper {

    String UHRZEIT_23_59 = "23:59";
    String UHRZEIT_24_00 = "24:00";

    @Mappings({
            @Mapping(target = "endeUhrzeit", source = "ladeZaehldatumDTO.endeUhrzeit", dateFormat = "HH:mm"),
            @Mapping(target = "startUhrzeit", source = "ladeZaehldatumDTO.startUhrzeit", dateFormat = "HH:mm"),
    })
    DatentabelleCsvZaehldatum ladeZaehldatumDTO2bean(LadeZaehldatumDTO ladeZaehldatumDTO);

    @AfterMapping
    default void toDatentabelleCsvZaehldatum(@MappingTarget DatentabelleCsvZaehldatum bean, LadeZaehldatumDTO dto) {
        if (StringUtils.equals(bean.getEndeUhrzeit(), UHRZEIT_23_59)) {
            bean.setEndeUhrzeit(UHRZEIT_24_00);
        }
    }

    List<DatentabelleCsvZaehldatum> ladeZaehldatumDTOList2beanList(List<LadeZaehldatumDTO> ladeZaehldatumDTOList);

}
