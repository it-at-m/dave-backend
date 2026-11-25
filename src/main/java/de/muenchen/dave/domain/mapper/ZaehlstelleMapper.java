package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.LeseZaehlstelleDto;
import de.muenchen.dave.domain.dtos.LeseZaehlungDto;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlstelleDto;
import de.muenchen.dave.domain.dtos.external.ExternalZaehlstelleDto;
import de.muenchen.dave.domain.dtos.laden.LadeAuswertungZaehlstelleKoordinateDto;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlstelleVisumDto;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlstelleWithUnreadMessageDto;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlungWithUnreadMessageDto;
import de.muenchen.dave.domain.dtos.suche.SucheZaehlstelleSuggestDto;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.util.SuchwortUtil;
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
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ZaehlstelleMapper {

    BearbeiteZaehlstelleDto bean2bearbeiteDto(Zaehlstelle bean, @Context StadtbezirkMapper stadtbezirkMapper);

    @Mapping(target = "zaehlungen", ignore = true)
    LadeZaehlstelleVisumDto bean2ladeVisumDto(Zaehlstelle bean);

    Zaehlstelle bearbeiteDto2bean(BearbeiteZaehlstelleDto dto, @Context StadtbezirkMapper stadtbezirkMapper);

    @AfterMapping
    default void toZaehlstelle(@MappingTarget Zaehlstelle bean, BearbeiteZaehlstelleDto dto, @Context StadtbezirkMapper stadtbezirkMapper) {
        bean.setPunkt(new GeoPoint(dto.getLat(), dto.getLng()));

        if (dto.getPunkt() != null && dto.getPunkt().getLat() > 0 && dto.getPunkt().getLon() > 0) {
            bean.setPunkt(dto.getPunkt());
        }
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

    SucheZaehlstelleSuggestDto bean2SucheZaehlstelleSuggestDto(Zaehlstelle bean);

    @AfterMapping
    default void toSucheZaehlstelleSuggestDto(@MappingTarget SucheZaehlstelleSuggestDto dto, Zaehlstelle bean) {
        dto.setText(bean.getNummer() + StringUtils.SPACE + bean.getStadtbezirk());
    }

    LeseZaehlstelleDto bean2LeseZaehlstelleDto(Zaehlstelle bean);

    LadeZaehlstelleWithUnreadMessageDto bean2LadeZaehlstelleWithUnreadMessageDTO(Zaehlstelle bean);

    @AfterMapping
    default void toLeseZaehlstelleDto(@MappingTarget LeseZaehlstelleDto dto, Zaehlstelle bean) {
        dto.setLat(bean.getPunkt().getLat());
        dto.setLng(bean.getPunkt().getLon());
    }

    ExternalZaehlstelleDto bean2ExternalDto(Zaehlstelle bean);

    LadeAuswertungZaehlstelleKoordinateDto bean2LadeAuswertungZaehlstelleKoordinateDto(Zaehlstelle bean);

    @AfterMapping
    default void toLadeAuswertungZaehlstelleKoordinateDto(@MappingTarget LadeAuswertungZaehlstelleKoordinateDto dto, Zaehlstelle bean) {
        dto.setLat(bean.getPunkt().getLat());
        dto.setLng(bean.getPunkt().getLon());
        final Zaehlung newestZaehlung = bean.getZaehlungen().stream()
                .max(Comparator.comparing(Zaehlung::getDatum))
                .orElse(null);
        dto.setLetzteZaehlung(newestZaehlung != null ? newestZaehlung.getDatum() : null);
    }

    LeseZaehlungDto bean2LeseDto(Zaehlung bean);

    LadeZaehlungWithUnreadMessageDto bean2LadeZaehlungWithUnreadMessageDTO(Zaehlung bean);

    @AfterMapping
    default void toLeseZaehlungDTO(@MappingTarget LeseZaehlungDto dto, Zaehlung bean) {
        dto.setLat(bean.getPunkt().getLat());
        dto.setLng(bean.getPunkt().getLon());
    }

}
