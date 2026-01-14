package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.LeseZaehlstelleDTO;
import de.muenchen.dave.domain.dtos.LeseZaehlungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlstelleDTO;
import de.muenchen.dave.domain.dtos.external.ExternalZaehlstelleDTO;
import de.muenchen.dave.domain.dtos.laden.LadeAuswertungZaehlstelleKoordinateDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlstelleVisumDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlstelleWithUnreadMessageDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlungWithUnreadMessageDTO;
import de.muenchen.dave.domain.dtos.suche.SucheZaehlstelleSuggestDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.util.SuchwortUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = ZaehlungMapper.class)
public interface ZaehlstelleMapper {

    BearbeiteZaehlstelleDTO bean2bearbeiteDto(Zaehlstelle bean, @Context StadtbezirkMapper stadtbezirkMapper);

    @Mapping(target = "zaehlungen", ignore = true)
    LadeZaehlstelleVisumDTO bean2ladeVisumDto(Zaehlstelle bean);

    Zaehlstelle bearbeiteDto2bean(BearbeiteZaehlstelleDTO dto, @Context StadtbezirkMapper stadtbezirkMapper);

    @Mapping(target = "zaehlungen", ignore = true)
     de.muenchen.dave.domain.analytics.Zaehlstelle elastic2analytics(@MappingTarget de.muenchen.dave.domain.analytics.Zaehlstelle analytics,
            Zaehlstelle elastic, @Context ZaehlungMapper zaehlungMapper);

    @BeforeMapping
    default void beforeElastic2Analytics(@MappingTarget de.muenchen.dave.domain.analytics.Zaehlstelle analytics) {
        // Ensure all list fields are mutable ArrayLists to avoid UnsupportedOperationException
        // when MapStruct tries to clear them during mapping
        if (analytics.getSuchwoerter() == null) {
            analytics.setSuchwoerter(new ArrayList<>());
        } else if (!(analytics.getSuchwoerter() instanceof ArrayList)) {
            analytics.setSuchwoerter(new ArrayList<>(analytics.getSuchwoerter()));
        }
        
        if (analytics.getCustomSuchwoerter() == null) {
            analytics.setCustomSuchwoerter(new ArrayList<>());
        } else if (!(analytics.getCustomSuchwoerter() instanceof ArrayList)) {
            analytics.setCustomSuchwoerter(new ArrayList<>(analytics.getCustomSuchwoerter()));
        }
    }


    @AfterMapping
    default void afterElastic2Analytics(@MappingTarget de.muenchen.dave.domain.analytics.Zaehlstelle analytics, 
            Zaehlstelle elastic, @Context ZaehlungMapper zaehlungMapper) {
        // Initialize collection if null
        if (analytics.getZaehlungen() == null) {
            analytics.setZaehlungen(new ArrayList<>());
        }
        
        if (elastic.getZaehlungen() == null || elastic.getZaehlungen().isEmpty()) {
            analytics.getZaehlungen().clear();
            return;
        }
        
        // Create a map of existing zaehlungen by ID for quick lookup
        Map<UUID, de.muenchen.dave.domain.analytics.Zaehlung> existingZaehlungenMap = new HashMap<>();
        for (de.muenchen.dave.domain.analytics.Zaehlung z : analytics.getZaehlungen()) {
            if (z.getId() != null) {
                existingZaehlungenMap.put(z.getId(), z);
            }
        }
        
        // Process incoming zaehlungen
        List<de.muenchen.dave.domain.analytics.Zaehlung> updatedZaehlungen = new ArrayList<>();
        for (Zaehlung elasticZaehlung : elastic.getZaehlungen()) {
            de.muenchen.dave.domain.analytics.Zaehlung analyticsZaehlung;
            
            if (elasticZaehlung.getId() != null && !elasticZaehlung.getId().isBlank()) {
                UUID zaehlungId = UUID.fromString(elasticZaehlung.getId());
                // Update existing zaehlung
                analyticsZaehlung = existingZaehlungenMap.get(zaehlungId);
                if (analyticsZaehlung == null) {
                    analyticsZaehlung = new de.muenchen.dave.domain.analytics.Zaehlung();
                }
            } else {
                // Create new zaehlung
                analyticsZaehlung = new de.muenchen.dave.domain.analytics.Zaehlung();
            }
            
            // Map properties from elastic to analytics
            analyticsZaehlung = zaehlungMapper.elastic2analytics(analyticsZaehlung, elasticZaehlung);
            // Set bidirectional relationship
            analyticsZaehlung.setZaehlstelle(analytics);
            updatedZaehlungen.add(analyticsZaehlung);
        }
        
        // Clear and replace the collection content (preserves Hibernate wrapper)
        analytics.getZaehlungen().clear();
        analytics.getZaehlungen().addAll(updatedZaehlungen);
    }

    Iterable<de.muenchen.dave.domain.analytics.Zaehlstelle> elasticlist2analyticslist(Iterable<? extends Zaehlstelle> elastic);

    Zaehlstelle analytics2elastic(de.muenchen.dave.domain.analytics.Zaehlstelle analytics);

    @AfterMapping
    default void toZaehlstelle(@MappingTarget Zaehlstelle bean, BearbeiteZaehlstelleDTO dto, @Context StadtbezirkMapper stadtbezirkMapper) {
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

    SucheZaehlstelleSuggestDTO bean2SucheZaehlstelleSuggestDto(Zaehlstelle bean);

    @AfterMapping
    default void toSucheZaehlstelleSuggestDto(@MappingTarget SucheZaehlstelleSuggestDTO dto, Zaehlstelle bean) {
        dto.setText(bean.getNummer() + StringUtils.SPACE + bean.getStadtbezirk());
    }

    LeseZaehlstelleDTO bean2LeseZaehlstelleDto(Zaehlstelle bean);

    LadeZaehlstelleWithUnreadMessageDTO bean2LadeZaehlstelleWithUnreadMessageDTO(Zaehlstelle bean);

    @AfterMapping
    default void toLeseZaehlstelleDto(@MappingTarget LeseZaehlstelleDTO dto, Zaehlstelle bean) {
        dto.setLat(bean.getPunkt().getLat());
        dto.setLng(bean.getPunkt().getLon());
    }

    @AfterMapping
    default void bean2LadeZaehlstelleWithUnreadMessageDTO(@MappingTarget LadeZaehlstelleWithUnreadMessageDTO dto, Zaehlstelle bean) {
        dto.setLat(bean.getPunkt().getLat());
        dto.setLng(bean.getPunkt().getLon());
    }

    ExternalZaehlstelleDTO bean2ExternalDto(Zaehlstelle bean);

    LadeAuswertungZaehlstelleKoordinateDTO bean2LadeAuswertungZaehlstelleKoordinateDto(Zaehlstelle bean);

    @AfterMapping
    default void toLadeAuswertungZaehlstelleKoordinateDto(@MappingTarget LadeAuswertungZaehlstelleKoordinateDTO dto, Zaehlstelle bean) {
        dto.setLat(bean.getPunkt().getLat());
        dto.setLng(bean.getPunkt().getLon());
        final Zaehlung newestZaehlung = bean.getZaehlungen().stream()
                .max(Comparator.comparing(Zaehlung::getDatum))
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
