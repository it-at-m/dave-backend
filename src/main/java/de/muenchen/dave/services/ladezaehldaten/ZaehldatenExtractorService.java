package de.muenchen.dave.services.ladezaehldaten;

import de.muenchen.dave.domain.Bewegungsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ZaehldatenExtractorService {

    final ZeitintervallRepository zeitintervallRepository;

    @Transactional
    public Map<Bewegungsbeziehung, List<Zeitintervall>> extractZaehldatenForZaehlungAccordingOptions(
            final UUID zaehlungId,
            final Zaehlung zaehlung,
            final OptionsDTO options) {
        final List<Zeitintervall> extractedZeitintervalle;
        final var zaehlart = Zaehlart.valueOf(zaehlung.getZaehlart());
        final var types = LadeZaehldatenService.getTypesAccordingChosenOptions(options);

        if (Zaehlart.FJS.equals(zaehlart)) {
            extractedZeitintervalle = CollectionUtils.emptyIfNull(options.getChosenLangsverkehre())
                    .parallelStream()
                    .flatMap(chosenLangsverkehr -> zeitintervallRepository
                            .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndLaengsverkehrKnotenarmAndLaengsverkehrRichtungAndLaengsverkehrStrassenseiteAndTypeInOrderBySortingIndexAsc(
                                    zaehlungId,
                                    options.getZeitblock().getStart(),
                                    options.getZeitblock().getEnd(),
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
                                    options.getZeitblock().getStart(),
                                    options.getZeitblock().getEnd(),
                                    chosenQuerungsverkehr.getKnotenarm(),
                                    chosenQuerungsverkehr.getRichtung(),
                                    types)
                            .stream())
                    .toList();
        } else if (Zaehlart.QJS.equals(zaehlart)) {
            extractedZeitintervalle = CollectionUtils.emptyIfNull(options.getChosenVerkehrsbeziehungen())
                    .parallelStream()
                    .flatMap(chosenVerkehrsbeziehung -> zeitintervallRepository
                            .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungNachAndVerkehrsbeziehungStrassenseiteAndTypeInOrderBySortingIndexAsc(
                                    zaehlungId,
                                    options.getZeitblock().getStart(),
                                    options.getZeitblock().getEnd(),
                                    chosenVerkehrsbeziehung.getVon(),
                                    chosenVerkehrsbeziehung.getNach(),
                                    chosenVerkehrsbeziehung.getStrassenseite(),
                                    types)
                            .stream())
                    .toList();
        } else {
            // alle nicht ausschließlichen Fuss und Radverkehrszählungen
            final FahrbewegungKreisverkehr fahrbewegungKreisverkehr;
            final Integer vonKnotenarm;
            final Integer nachKnotenarm;
            if (zaehlung.getKreisverkehr()) {
                /*
                 * In {@link de.muenchen.dave.domain.Verkehrsbeziehung} definiert das Attribut "von"
                 * den im Kreisverkehr jeweils betroffenen Knotenarm. Das Attribut "nach" ist immer "null".
                 */
                if (ObjectUtils.isNotEmpty(options.getVonKnotenarm()) && ObjectUtils.isEmpty(options.getNachKnotenarm())) {
                    // Hinein
                    vonKnotenarm = options.getVonKnotenarm();
                    fahrbewegungKreisverkehr = FahrbewegungKreisverkehr.HINEIN;
                    extractedZeitintervalle = zeitintervallRepository
                            .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungFahrbewegungKreisverkehrAndTypeInOrderBySortingIndexAsc(
                                    zaehlungId,
                                    options.getZeitblock().getStart(),
                                    options.getZeitblock().getEnd(),
                                    vonKnotenarm,
                                    fahrbewegungKreisverkehr,
                                    types);

                } else if (ObjectUtils.isEmpty(options.getVonKnotenarm()) && ObjectUtils.isNotEmpty(options.getNachKnotenarm())) {
                    // Heraus
                    vonKnotenarm = options.getNachKnotenarm();
                    fahrbewegungKreisverkehr = FahrbewegungKreisverkehr.HERAUS;

                    extractedZeitintervalle = zeitintervallRepository
                            .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungFahrbewegungKreisverkehrAndTypeInOrderBySortingIndexAsc(
                                    zaehlungId,
                                    options.getZeitblock().getStart(),
                                    options.getZeitblock().getEnd(),
                                    vonKnotenarm,
                                    fahrbewegungKreisverkehr,
                                    types);

                } else {
                    // Alles Hinein + Heraus + Vorbei
                    vonKnotenarm = null;
                    fahrbewegungKreisverkehr = null;

                    extractedZeitintervalle = zeitintervallRepository
                            .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndTypeInOrderBySortingIndexAsc(
                                    zaehlungId,
                                    options.getZeitblock().getStart(),
                                    options.getZeitblock().getEnd(),
                                    types);
                }
            } else {
                vonKnotenarm = options.getVonKnotenarm();
                nachKnotenarm = options.getNachKnotenarm();
                fahrbewegungKreisverkehr = null;

                if (ObjectUtils.isNotEmpty(vonKnotenarm) && ObjectUtils.isEmpty(nachKnotenarm)) {
                    extractedZeitintervalle = zeitintervallRepository
                            .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungFahrbewegungKreisverkehrAndTypeInOrderBySortingIndexAsc(
                                    zaehlungId,
                                    options.getZeitblock().getStart(),
                                    options.getZeitblock().getEnd(),
                                    vonKnotenarm,
                                    fahrbewegungKreisverkehr,
                                    types);
                } else if (ObjectUtils.isEmpty(vonKnotenarm) && ObjectUtils.isNotEmpty(nachKnotenarm)) {
                    extractedZeitintervalle = zeitintervallRepository
                            .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungNachAndTypeInOrderBySortingIndexAsc(
                                    zaehlungId,
                                    options.getZeitblock().getStart(),
                                    options.getZeitblock().getEnd(),
                                    nachKnotenarm,
                                    types);
                } else if (ObjectUtils.isNotEmpty(vonKnotenarm) && ObjectUtils.isNotEmpty(nachKnotenarm)) {
                    extractedZeitintervalle = zeitintervallRepository
                            .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungNachAndTypeInOrderBySortingIndexAsc(
                                    zaehlungId,
                                    options.getZeitblock().getStart(),
                                    options.getZeitblock().getEnd(),
                                    vonKnotenarm,
                                    nachKnotenarm,
                                    types);
                } else {
                    // options.getVonKnotenarm() und options.getNachKnotenarm() sind Empty
                    extractedZeitintervalle = zeitintervallRepository
                            .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndTypeInOrderBySortingIndexAsc(
                                    zaehlungId,
                                    options.getZeitblock().getStart(),
                                    options.getZeitblock().getEnd(),
                                    types);
                }

            }
        }

        return CollectionUtils.emptyIfNull(extractedZeitintervalle)
                .stream()
                .collect(Collectors
                        .groupingByConcurrent(zeitintervall -> this.getBewegungsbeziehungFromZeitintervallAccordingZaehlart(zeitintervall, zaehlart)));
    }

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
