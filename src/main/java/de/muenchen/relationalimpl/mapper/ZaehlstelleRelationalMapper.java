package de.muenchen.relationalimpl.mapper;

import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ZaehlstelleRelationalMapper {

    @Mapping(target = "zaehlungen", ignore = true)
    de.muenchen.dave.domain.analytics.Zaehlstelle elastic2analytics(@MappingTarget de.muenchen.dave.domain.analytics.Zaehlstelle analytics,
            Zaehlstelle elastic, @Context ZaehlungRelationalMapper zaehlungMapper, @Context FahrbeziehungRelationalMapper fahrbeziehungMapper);

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
            Zaehlstelle elastic, @Context ZaehlungRelationalMapper zaehlungMapper, @Context FahrbeziehungRelationalMapper fahrbeziehungMapper) {
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
            analyticsZaehlung = zaehlungMapper.elastic2analytics(analyticsZaehlung, elasticZaehlung, fahrbeziehungMapper);
            // Set bidirectional relationship
            analyticsZaehlung.setZaehlstelle(analytics);
            updatedZaehlungen.add(analyticsZaehlung);
        }

        // Clear and replace the collection content (preserves Hibernate wrapper)
        analytics.getZaehlungen().clear();
        analytics.getZaehlungen().addAll(updatedZaehlungen);
    }

    Iterable<de.muenchen.dave.domain.analytics.Zaehlstelle> elasticlist2analyticslist(Iterable<? extends Zaehlstelle> elastic, @Context ZaehlungRelationalMapper zaehlungMapper, @Context FahrbeziehungRelationalMapper fahrbeziehungMapper);

    Zaehlstelle analytics2elastic(de.muenchen.dave.domain.analytics.Zaehlstelle analytics);
}
