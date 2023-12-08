package de.muenchen.dave.domain.mapper.detektor;

import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.geodateneai.gen.model.MessquerschnittDto;
import de.muenchen.dave.geodateneai.gen.model.MessstelleDto;
import de.muenchen.dave.services.IndexServiceUtils;
import de.muenchen.dave.util.SuchwortUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Mapper(componentModel = "spring")
public interface StammdatenMapper {

    Messstelle dtoToMessstelle(MessstelleDto dto);

    List<Messstelle> dtoToMessstelle(List<MessstelleDto> dto);

    Messstelle updateMessstelle(@MappingTarget Messstelle messstelleOld, Messstelle messstelleNew);

    @AfterMapping
    default void dtoToMessstelle(@MappingTarget Messstelle bean, MessstelleDto dto) {
        bean.setNummer(dto.getMstId());

        if (dto.getXcoordinate() != null && dto.getYcoordinate() != null) {
            bean.setPunkt(new GeoPoint(dto.getXcoordinate(), dto.getYcoordinate()));
        }

        bean.setStadtbezirk(IndexServiceUtils.getStadtbezirkBezeichnung(bean.getStadtbezirkNummer()));

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
    default void dtoToMessquerschnitt(@MappingTarget Messquerschnitt bean, MessquerschnittDto dto) {
        bean.setNummer(dto.getMstId());
    }

    Messquerschnitt updateMessquerschnitt(@MappingTarget Messquerschnitt messquerschnittOld, Messquerschnitt messquerschnittNew);

    List<Messquerschnitt> updateMessquerschnitt(@MappingTarget List<Messquerschnitt> messquerschnittOld, List<Messquerschnitt> messquerschnittNew);

    //    SucheZaehlstelleSuggestDTO bean2SucheZaehlstelleSuggestDto(Zaehlstelle bean);
    //
    //    @AfterMapping
    //    default void toSucheZaehlstelleSuggestDto(@MappingTarget SucheZaehlstelleSuggestDTO dto, Zaehlstelle bean) {
    //        dto.setText(bean.getNummer() + StringUtils.SPACE + bean.getStadtbezirk());
    //    }
}
