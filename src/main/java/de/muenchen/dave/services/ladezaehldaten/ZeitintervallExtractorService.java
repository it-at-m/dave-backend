package de.muenchen.dave.services.ladezaehldaten;

import de.muenchen.dave.domain.Bewegungsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.sl.draw.geom.GuideIf;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ZeitintervallExtractorService {

    final ZeitintervallRepository zeitintervallRepository;

    @Transactional
    public Map<Bewegungsbeziehung, List<Zeitintervall>> extractZeitintervalleForZaehlungAccordingOptions(
            final UUID zaehlungId,
            final Zaehlart zaehlart,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final Boolean isKreisverkehr,
            final OptionsDTO options,
            final Set<TypeZeitintervall> types) {
        final List<Zeitintervall> extractedZeitintervalle;

        if (Zaehlart.FJS.equals(zaehlart)) {
            extractedZeitintervalle = CollectionUtils.emptyIfNull(options.getChosenLangsverkehre())
                    .parallelStream()
                    .flatMap(chosenLangsverkehr -> zeitintervallRepository
                            .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndLaengsverkehrKnotenarmAndLaengsverkehrRichtungAndLaengsverkehrStrassenseiteAndTypeInOrderBySortingIndexAsc(
                                    zaehlungId,
                                    startUhrzeit,
                                    endeUhrzeit,
                                    chosenLangsverkehr.getKnotenarm(),
                                    chosenLangsverkehr.getRichtung(),
                                    chosenLangsverkehr.getStrassenseite(),
                                    types)
                            .stream())
                    .toList();
        } else if (Zaehlart.QU.equals(zaehlart)) {
            extractedZeitintervalle = CollectionUtils.emptyIfNull(options.getChosenQuerungsverkehre())
                    .parallelStream()
                    .flatMap(chosenQuerungsverkehr -> zeitintervallRepository
                            .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndQuerungsverkehrKnotenarmAndQuerungsverkehrRichtungAndTypeInOrderBySortingIndexAsc(
                                    zaehlungId,
                                    startUhrzeit,
                                    endeUhrzeit,
                                    chosenQuerungsverkehr.getKnotenarm(),
                                    chosenQuerungsverkehr.getRichtung(),
                                    types)
                            .stream())
                    .toList();
        } else if (Zaehlart.QJS.equals(zaehlart)) {
            extractedZeitintervalle = CollectionUtils.emptyIfNull(options.getChosenVerkehrsbeziehungen())
                    .parallelStream()
                    .flatMap(chosenVerkehrsbeziehung -> zeitintervallRepository
                            .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungNachAndTypeInAndVerkehrsbeziehungStrassenseiteOrderBySortingIndexAsc(
                                    zaehlungId,
                                    startUhrzeit,
                                    endeUhrzeit,
                                    chosenVerkehrsbeziehung.getVon(),
                                    chosenVerkehrsbeziehung.getNach(),
                                    types,
                                    chosenVerkehrsbeziehung.getStrassenseite())
                            .stream())
                    .toList();
        } else {
            // alle nicht ausschließlichen Fuss und Radverkehrszählungen
            final FahrbewegungKreisverkehr fahrbewegungKreisverkehr;
            final Integer vonKnotenarm;
            final Integer nachKnotenarm;
            if (isKreisverkehr) {
                /*
                 * In {@link de.muenchen.dave.domain.Verkehrsbeziehung} definiert das Attribut "von"
                 * den im Kreisverkehr jeweils betroffenen Knotenarm. Das Attribut "nach" ist immer "null".
                 */
                if (ObjectUtils.isNotEmpty(options.getVonKnotenarm()) && ObjectUtils.isEmpty(options.getNachKnotenarm())) {
                    // Über den Knotenarm X in den Kreisverkehr einfahrend
                    vonKnotenarm = options.getVonKnotenarm();
                    fahrbewegungKreisverkehr = FahrbewegungKreisverkehr.HINEIN;
                    extractedZeitintervalle = zeitintervallRepository
                            .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungFahrbewegungKreisverkehrAndTypeInOrderBySortingIndexAsc(
                                    zaehlungId,
                                    startUhrzeit,
                                    endeUhrzeit,
                                    vonKnotenarm,
                                    fahrbewegungKreisverkehr,
                                    types);

                } else if (ObjectUtils.isEmpty(options.getVonKnotenarm()) && ObjectUtils.isNotEmpty(options.getNachKnotenarm())) {
                    // Über den Knotenarm Y in den Kreisverkehr ausfahrend
                    vonKnotenarm = options.getNachKnotenarm();
                    fahrbewegungKreisverkehr = FahrbewegungKreisverkehr.HERAUS;
                    extractedZeitintervalle = zeitintervallRepository
                            .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungFahrbewegungKreisverkehrAndTypeInOrderBySortingIndexAsc(
                                    zaehlungId,
                                    startUhrzeit,
                                    endeUhrzeit,
                                    vonKnotenarm,
                                    fahrbewegungKreisverkehr,
                                    types);

                } else {
                    // Der an allen Knotenarmen ein-, aus- und vorbeifahrende Verkehr
                    vonKnotenarm = null;
                    fahrbewegungKreisverkehr = null;
                    extractedZeitintervalle = zeitintervallRepository
                            .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndTypeInOrderBySortingIndexAsc(
                                    zaehlungId,
                                    startUhrzeit,
                                    endeUhrzeit,
                                    types);
                }
            } else {
                vonKnotenarm = options.getVonKnotenarm();
                nachKnotenarm = options.getNachKnotenarm();
                fahrbewegungKreisverkehr = null;
                if (ObjectUtils.isNotEmpty(vonKnotenarm) && ObjectUtils.isEmpty(nachKnotenarm)) {
                    // Knotenarm X nach ALLE Knotenarme
                    extractedZeitintervalle = zeitintervallRepository
                            .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungFahrbewegungKreisverkehrAndTypeInOrderBySortingIndexAsc(
                                    zaehlungId,
                                    startUhrzeit,
                                    endeUhrzeit,
                                    vonKnotenarm,
                                    fahrbewegungKreisverkehr,
                                    types);
                } else if (ObjectUtils.isEmpty(vonKnotenarm) && ObjectUtils.isNotEmpty(nachKnotenarm)) {
                    // ALLE Knotenarme nach Knotenarm Y
                    extractedZeitintervalle = zeitintervallRepository
                            .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungNachAndTypeInOrderBySortingIndexAsc(
                                    zaehlungId,
                                    startUhrzeit,
                                    endeUhrzeit,
                                    nachKnotenarm,
                                    types);
                } else if (ObjectUtils.isNotEmpty(vonKnotenarm) && ObjectUtils.isNotEmpty(nachKnotenarm)) {
                    // Knotenarm X nach Knotenarm Y
                    extractedZeitintervalle = zeitintervallRepository
                            .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungNachAndTypeInOrderBySortingIndexAsc(
                                    zaehlungId,
                                    startUhrzeit,
                                    endeUhrzeit,
                                    vonKnotenarm,
                                    nachKnotenarm,
                                    types);
                } else {
                    // ALLE Knotenarme nach ALLE Knotenarme
                    extractedZeitintervalle = zeitintervallRepository
                            .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndTypeInOrderBySortingIndexAsc(
                                    zaehlungId,
                                    startUhrzeit,
                                    endeUhrzeit,
                                    types);
                }

            }
        }

        return CollectionUtils.emptyIfNull(extractedZeitintervalle)
                .stream()
                .collect(Collectors
                        .groupingByConcurrent(zeitintervall -> this.getBewegungsbeziehungFromZeitintervallAccordingZaehlart(zeitintervall, zaehlart)));
    }

    /**
     * @param zeitintervall aus dem die Bewegungsbeziehung extrahiert werden soll.
     * @param zaehlart zur Ermittlung der entsprechenden Bewegungsbeziehung.
     * @return die zur Zählart passende Bewegungsbeziehung aus dem Zeitintervall.
     */
    protected Bewegungsbeziehung getBewegungsbeziehungFromZeitintervallAccordingZaehlart(final Zeitintervall zeitintervall, final Zaehlart zaehlart) {
        if (Zaehlart.FJS.equals(zaehlart)) {
            return zeitintervall.getLaengsverkehr();
        } else if (Zaehlart.QU.equals(zaehlart)) {
            return zeitintervall.getQuerungsverkehr();
        } else {
            return zeitintervall.getVerkehrsbeziehung();
        }
    }

}
