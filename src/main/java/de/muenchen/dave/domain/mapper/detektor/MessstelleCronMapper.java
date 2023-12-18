package de.muenchen.dave.domain.mapper.detektor;

import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.geodateneai.gen.model.MessquerschnittDto;
import de.muenchen.dave.geodateneai.gen.model.MessstelleDto;
import de.muenchen.dave.util.SuchwortUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Mapper(componentModel = "spring")
public interface MessstelleCronMapper {

    Messstelle dtoToMessstelle(MessstelleDto dto);

    Messquerschnitt dtoToMessquerschnitt(MessquerschnittDto dto);

    List<Messquerschnitt> dtoToMessquerschnitt(List<MessquerschnittDto> dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sichtbarDatenportal", ignore = true)
    @Mapping(target = "kommentar", ignore = true)
    @Mapping(target = "standort", ignore = true)
    @Mapping(target = "customSuchwoerter", ignore = true)
    @Mapping(target = "geprueft", ignore = true)
    Messstelle updateMessstelle(@MappingTarget Messstelle existing, MessstelleDto dto);

    @AfterMapping
    default void dtoToMessstelleAfterMapping(@MappingTarget Messstelle bean, MessstelleDto dto) {
        if (dto.getXcoordinate() != null && dto.getYcoordinate() != null) {
            bean.setPunkt(new GeoPoint(dto.getXcoordinate(), dto.getYcoordinate()));
        }

        // Suchworte setzen
        final Set<String> generatedSuchwoerter = SuchwortUtil.generateSuchworteOfMessstelle(bean);

        if (CollectionUtils.isEmpty(bean.getSuchwoerter())) {
            bean.setSuchwoerter(new ArrayList<>());
        }
        if (CollectionUtils.isNotEmpty(generatedSuchwoerter)) {
            bean.getSuchwoerter().addAll(generatedSuchwoerter);
        }

        if (CollectionUtils.isEmpty(bean.getMessquerschnitte())) {
            bean.setMessquerschnitte(new ArrayList<>());
        }
    }

    @AfterMapping
    default void dtoToMessstelleAfterMapping(@MappingTarget Messquerschnitt bean, MessquerschnittDto dto) {
        if (dto.getXcoordinate() != null && dto.getYcoordinate() != null) {
            bean.setPunkt(new GeoPoint(dto.getXcoordinate(), dto.getYcoordinate()));
        }
    }
}
