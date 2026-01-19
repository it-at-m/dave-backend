package de.muenchen.relationalimpl.mapper;

import de.muenchen.dave.domain.elasticsearch.Fahrbeziehung;
import de.muenchen.dave.domain.elasticsearch.Knotenarm;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.util.DaveConstants;
import java.time.format.DateTimeFormatter;
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
public interface ZaehlungRelationalMapper {

    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DaveConstants.DATE_FORMAT);

    @Mapping(target = "knotenarme", ignore = true)
    @Mapping(target = "fahrbeziehungen", ignore = true)
    de.muenchen.dave.domain.analytics.Zaehlung elastic2analytics(@MappingTarget de.muenchen.dave.domain.analytics.Zaehlung analytics,
            Zaehlung elastic, @Context FahrbeziehungRelationalMapper fahrbeziehungMapper);

    @BeforeMapping
    default void beforeElastic2Analytics(@MappingTarget de.muenchen.dave.domain.analytics.Zaehlung analytics) {
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

        if (analytics.getGeographie() == null) {
            analytics.setGeographie(new ArrayList<>());
        } else if (!(analytics.getGeographie() instanceof ArrayList)) {
            analytics.setGeographie(new ArrayList<>(analytics.getGeographie()));
        }

        if (analytics.getKategorien() == null) {
            analytics.setKategorien(new ArrayList<>());
        } else if (!(analytics.getKategorien() instanceof ArrayList)) {
            analytics.setKategorien(new ArrayList<>(analytics.getKategorien()));
        }
    }

    @AfterMapping
    default void afterElastic2Analytics(@MappingTarget de.muenchen.dave.domain.analytics.Zaehlung analytics,
            Zaehlung elastic, @Context FahrbeziehungRelationalMapper fahrbeziehungMapper) {

        // Initialize collection if null
        if (analytics.getKnotenarme() == null) {
            analytics.setKnotenarme(new ArrayList<>());
        }

        if (elastic.getKnotenarme() == null || elastic.getKnotenarme().isEmpty()) {
            analytics.getKnotenarme().clear();
            return;
        }

        // Create a map of existing knotenarme by ID for quick lookup
        Map<UUID, de.muenchen.dave.domain.analytics.Knotenarm> existingKnotenarmeMap = new HashMap<>();
        for (de.muenchen.dave.domain.analytics.Knotenarm k : analytics.getKnotenarme()) {
            if (k.getId() != null) {
                existingKnotenarmeMap.put(k.getId(), k);
            }
        }

        // Process incoming knotenarme
        List<de.muenchen.dave.domain.analytics.Knotenarm> updatedKnotenarme = new ArrayList<>();
        for (Knotenarm elasticKnotenarm : elastic.getKnotenarme()) {
            de.muenchen.dave.domain.analytics.Knotenarm analyticsKnotenarm;

            if (elasticKnotenarm.getId() != null && !elasticKnotenarm.getId().isBlank()) {
                UUID knotenarmId = UUID.fromString(elasticKnotenarm.getId());
                // Update existing knotenarm
                analyticsKnotenarm = existingKnotenarmeMap.get(knotenarmId);
                if (analyticsKnotenarm == null) {
                    analyticsKnotenarm = new de.muenchen.dave.domain.analytics.Knotenarm();
                }
            } else {
                // Create new knotenarm
                analyticsKnotenarm = new de.muenchen.dave.domain.analytics.Knotenarm();
            }

            // Map properties from elastic to analytics
            if (elasticKnotenarm.getId() != null && !elasticKnotenarm.getId().isBlank()) {
                analyticsKnotenarm.setId(UUID.fromString(elasticKnotenarm.getId()));
                analyticsKnotenarm.setVersion(elasticKnotenarm.getVersion());
            }
            analyticsKnotenarm.setNummer(elasticKnotenarm.getNummer());
            analyticsKnotenarm.setStrassenname(elasticKnotenarm.getStrassenname());
            analyticsKnotenarm.setFilename(elasticKnotenarm.getFilename());

            // Set bidirectional relationship
            analyticsKnotenarm.setZaehlung(analytics);
            updatedKnotenarme.add(analyticsKnotenarm);
        }

        // Clear and replace the collection content (preserves Hibernate wrapper)
        analytics.getKnotenarme().clear();
        analytics.getKnotenarme().addAll(updatedKnotenarme);

        // Initialize collection if null
        if (analytics.getFahrbeziehungen() == null) {
            analytics.setFahrbeziehungen(new ArrayList<>());
        }

        if (elastic.getFahrbeziehungen() == null || elastic.getFahrbeziehungen().isEmpty()) {
            analytics.getFahrbeziehungen().clear();
            return;
        }

        // Create a map of existing fahrbeziehungen by ID for quick lookup
        Map<UUID, de.muenchen.dave.domain.analytics.Fahrbeziehung> existingFahrbeziehungenMap = new HashMap<>();
        for (de.muenchen.dave.domain.analytics.Fahrbeziehung f : analytics.getFahrbeziehungen()) {
            if (f.getId() != null) {
                existingFahrbeziehungenMap.put(f.getId(), f);
            }
        }

        // Process incoming fahrbeziehungen
        List<de.muenchen.dave.domain.analytics.Fahrbeziehung> updatedFahrbeziehungen = new ArrayList<>();
        for (Fahrbeziehung elasticFahrbeziehung : elastic.getFahrbeziehungen()) {
            de.muenchen.dave.domain.analytics.Fahrbeziehung analyticsFahrbeziehung;

            if (elasticFahrbeziehung.getId() != null && !elasticFahrbeziehung.getId().isBlank()) {
                UUID fahrbeziehungId = UUID.fromString(elasticFahrbeziehung.getId());
                // Update existing fahrbeziehung
                analyticsFahrbeziehung = existingFahrbeziehungenMap.get(fahrbeziehungId);
                if (analyticsFahrbeziehung == null) {
                    analyticsFahrbeziehung = new de.muenchen.dave.domain.analytics.Fahrbeziehung();
                }
            } else {
                // Create new fahrbeziehung
                analyticsFahrbeziehung = new de.muenchen.dave.domain.analytics.Fahrbeziehung();
            }
            // Map properties from elastic to analytics
            analyticsFahrbeziehung = fahrbeziehungMapper.elastic2analytics(analyticsFahrbeziehung, elasticFahrbeziehung);
            // Set bidirectional relationship
            analyticsFahrbeziehung.setZaehlung(analytics);
            updatedFahrbeziehungen.add(analyticsFahrbeziehung);
        }

        // Clear and replace the collection content (preserves Hibernate wrapper)
        analytics.getFahrbeziehungen().clear();
        analytics.getFahrbeziehungen().addAll(updatedFahrbeziehungen);
    }

    Iterable<de.muenchen.dave.domain.analytics.Zaehlung> elasticlist2analyticslist(Iterable<? extends Zaehlung> elastic);

    Iterable<Zaehlung> analyticslist2elasticlist(Iterable<? extends de.muenchen.dave.domain.analytics.Zaehlung> elastic);

    Zaehlung analytics2elastic(de.muenchen.dave.domain.analytics.Zaehlung analytics);
}
