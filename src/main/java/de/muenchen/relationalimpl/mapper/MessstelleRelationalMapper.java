package de.muenchen.relationalimpl.mapper;

import de.muenchen.dave.domain.elasticsearch.detektor.Messfaehigkeit;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
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
public interface MessstelleRelationalMapper {

    @Mapping(target = "messquerschnitte", ignore = true)
    @Mapping(target = "messfaehigkeiten", ignore = true)
    de.muenchen.dave.domain.analytics.detektor.Messstelle elastic2analytics(
            @MappingTarget de.muenchen.dave.domain.analytics.detektor.Messstelle analytics,
            Messstelle elastic,
            @Context MessquerschnittRelationalMapper messquerschnittMapper,
            @Context MessfaehigkeitRelationalMapper messfaehigkeitMapper);

    @BeforeMapping
    default void beforeElastic2Analytics(@MappingTarget de.muenchen.dave.domain.analytics.detektor.Messstelle analytics) {
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
    default void afterElastic2Analytics(
            @MappingTarget de.muenchen.dave.domain.analytics.detektor.Messstelle analytics,
            Messstelle elastic,
            @Context MessquerschnittRelationalMapper messquerschnittMapper,
            @Context MessfaehigkeitRelationalMapper messfaehigkeitMapper) {

        // Handle messquerschnitte mapping
        if (analytics.getMessquerschnitte() == null) {
            analytics.setMessquerschnitte(new ArrayList<>());
        }

        if (elastic.getMessquerschnitte() == null || elastic.getMessquerschnitte().isEmpty()) {
            analytics.getMessquerschnitte().clear();
        } else {
            // Create a map of existing messquerschnitte by ID for quick lookup
            Map<UUID, de.muenchen.dave.domain.analytics.detektor.Messquerschnitt> existingMap = new HashMap<>();
            for (de.muenchen.dave.domain.analytics.detektor.Messquerschnitt mq : analytics.getMessquerschnitte()) {
                if (mq.getId() != null) {
                    existingMap.put(mq.getId(), mq);
                }
            }

            // Process incoming messquerschnitte
            List<de.muenchen.dave.domain.analytics.detektor.Messquerschnitt> updated = new ArrayList<>();
            for (Messquerschnitt elasticMq : elastic.getMessquerschnitte()) {
                de.muenchen.dave.domain.analytics.detektor.Messquerschnitt analyticsMq;

                if (elasticMq.getId() != null && !elasticMq.getId().isBlank()) {
                    UUID mqId = UUID.fromString(elasticMq.getId());
                    analyticsMq = existingMap.get(mqId);
                    if (analyticsMq == null) {
                        analyticsMq = new de.muenchen.dave.domain.analytics.detektor.Messquerschnitt();
                    }
                } else {
                    analyticsMq = new de.muenchen.dave.domain.analytics.detektor.Messquerschnitt();
                }

                analyticsMq = messquerschnittMapper.elastic2analytics(analyticsMq, elasticMq);
                analyticsMq.setMessstelle(analytics);
                updated.add(analyticsMq);
            }

            analytics.getMessquerschnitte().clear();
            analytics.getMessquerschnitte().addAll(updated);
        }

        // Handle messfaehigkeiten mapping
        if (analytics.getMessfaehigkeiten() == null) {
            analytics.setMessfaehigkeiten(new ArrayList<>());
        }

        if (elastic.getMessfaehigkeiten() == null || elastic.getMessfaehigkeiten().isEmpty()) {
            analytics.getMessfaehigkeiten().clear();
        } else {
            // Create a map of existing messfaehigkeiten by ID for quick lookup
            Map<UUID, de.muenchen.dave.domain.analytics.detektor.Messfaehigkeit> existingMap = new HashMap<>();
            for (de.muenchen.dave.domain.analytics.detektor.Messfaehigkeit mf : analytics.getMessfaehigkeiten()) {
                if (mf.getId() != null) {
                    existingMap.put(mf.getId(), mf);
                }
            }

            // Process incoming messfaehigkeiten
            List<de.muenchen.dave.domain.analytics.detektor.Messfaehigkeit> updated = new ArrayList<>();
            for (Messfaehigkeit elasticMf : elastic.getMessfaehigkeiten()) {
                de.muenchen.dave.domain.analytics.detektor.Messfaehigkeit analyticsMf;

                // Note: Messfaehigkeit in elasticsearch doesn't have @Id, so we can't use ID matching
                // We'll always create new instances
                analyticsMf = new de.muenchen.dave.domain.analytics.detektor.Messfaehigkeit();
                analyticsMf = messfaehigkeitMapper.elastic2analytics(analyticsMf, elasticMf);
                analyticsMf.setMessstelle(analytics);
                updated.add(analyticsMf);
            }

            analytics.getMessfaehigkeiten().clear();
            analytics.getMessfaehigkeiten().addAll(updated);
        }
    }

    Iterable<de.muenchen.dave.domain.analytics.detektor.Messstelle> elasticlist2analyticslist(
            Iterable<? extends Messstelle> elastic,
            @Context MessquerschnittRelationalMapper messquerschnittMapper,
            @Context MessfaehigkeitRelationalMapper messfaehigkeitMapper);

    Messstelle analytics2elastic(de.muenchen.dave.domain.analytics.detektor.Messstelle analytics);
}
