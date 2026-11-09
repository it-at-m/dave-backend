package de.muenchen.dave.migrationespg.domain.mapper;

import de.muenchen.dave.domain.dtos.LeseZaehlstelleDTO;
import de.muenchen.dave.domain.dtos.LeseZaehlungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlstelleDTO;
import de.muenchen.dave.domain.dtos.external.ExternalZaehlstelleDTO;
import de.muenchen.dave.domain.dtos.laden.LadeAuswertungZaehlstelleKoordinateDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlstelleVisumDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlstelleWithUnreadMessageDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlungWithUnreadMessageDTO;
import de.muenchen.dave.domain.dtos.suche.SucheZaehlstelleSuggestDTO;
import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.ZaehlstelleEntity;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.ZaehlungEntity;
import de.muenchen.dave.migrationespg.util.SuchwortUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ZaehlstelleMapper {

    BearbeiteZaehlstelleDTO bean2bearbeiteDto(ZaehlstelleEntity bean, @Context StadtbezirkMapper stadtbezirkMapper);

    @Mapping(target = "zaehlungen", ignore = true)
    LadeZaehlstelleVisumDTO bean2ladeVisumDto(ZaehlstelleEntity bean);

    ZaehlstelleEntity bearbeiteDto2bean(BearbeiteZaehlstelleDTO dto, @Context StadtbezirkMapper stadtbezirkMapper);

    @AfterMapping
    default void toZaehlstelle(@MappingTarget ZaehlstelleEntity bean, BearbeiteZaehlstelleDTO dto, @Context StadtbezirkMapper stadtbezirkMapper) {

        bean.setLatitude(dto.getLat());
        bean.setLongitude(dto.getLng());

        bean.setStadtbezirk(stadtbezirkMapper.bezeichnungOf(bean.getStadtbezirkNummer()));

        // Suchworte setzen
        final Set<String> generatedSuchwoerter = SuchwortUtil.generateSuchworteOfZaehlstelle(bean);

        if (CollectionUtils.isEmpty(bean.getSuchwoerter())) {
            bean.setSuchwoerter(new ArrayList<>());
        }
        if (CollectionUtils.isNotEmpty(generatedSuchwoerter)) {
            bean.getSuchwoerter().addAll(generatedSuchwoerter);
        }
        if (CollectionUtils.isNotEmpty(dto.getCustomSuchwoerter())) {
            bean.getSuchwoerter().addAll(dto.getCustomSuchwoerter());
        }
    }

    SucheZaehlstelleSuggestDTO bean2SucheZaehlstelleSuggestDto(ZaehlstelleEntity bean);

    @AfterMapping
    default void toSucheZaehlstelleSuggestDto(@MappingTarget SucheZaehlstelleSuggestDTO dto, ZaehlstelleEntity bean) {
        dto.setText(bean.getNummer() + StringUtils.SPACE + bean.getStadtbezirk());
    }

    LeseZaehlstelleDTO bean2LeseZaehlstelleDto(ZaehlstelleEntity bean);

    LadeZaehlstelleWithUnreadMessageDTO bean2LadeZaehlstelleWithUnreadMessageDTO(ZaehlstelleEntity bean);

    @AfterMapping
    default void toLeseZaehlstelleDto(@MappingTarget LeseZaehlstelleDTO dto, ZaehlstelleEntity bean) {
        dto.setLat(bean.getLatitude());
        dto.setLng(bean.getLongitude());
    }

    ExternalZaehlstelleDTO bean2ExternalDto(ZaehlstelleEntity bean);

    LadeAuswertungZaehlstelleKoordinateDTO bean2LadeAuswertungZaehlstelleKoordinateDto(ZaehlstelleEntity bean);

    @AfterMapping
    default void toLadeAuswertungZaehlstelleKoordinateDto(@MappingTarget LadeAuswertungZaehlstelleKoordinateDTO dto, ZaehlstelleEntity bean) {
        dto.setLat(bean.getLatitude());
        dto.setLng(bean.getLongitude());
        final ZaehlungEntity newestZaehlung = bean.getZaehlungen().stream()
                .max(Comparator.comparing(ZaehlungEntity::getDatum))
                .orElse(null);
        dto.setLetzteZaehlung(newestZaehlung != null ? newestZaehlung.getDatum() : null);
    }

    LeseZaehlungDTO bean2LeseDto(Zaehlung bean);

    LadeZaehlungWithUnreadMessageDTO bean2LadeZaehlungWithUnreadMessageDTO(Zaehlung bean);

    @AfterMapping
    default void toLeseZaehlungDTO(@MappingTarget LeseZaehlungDTO dto, Zaehlung bean) {
        dto.setLat(bean.getPunkt().getLat());
        dto.setLng(bean.getPunkt().getLon());
    }

}
