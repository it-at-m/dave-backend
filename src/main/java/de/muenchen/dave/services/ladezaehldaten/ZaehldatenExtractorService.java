package de.muenchen.dave.services.ladezaehldaten;

import de.muenchen.dave.domain.Laengsverkehr;
import de.muenchen.dave.domain.Querungsverkehr;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehlart;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ZaehldatenExtractorService {

    private final ZeitintervallExtractorService zeitintervallExtractorService;

    private final ZeitintervallSummationService zeitintervallSummationService;

    private final SpitzenstundeCalculatorService spitzenstundeCalculatorService;

    public List<Zeitintervall> extractZeitintervalle(
            final UUID zaehlungId,
            final Zaehlart zaehlart,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final Boolean isKreisverkehr,
            final OptionsDTO options,
            final Set<TypeZeitintervall> types) {

        // Extrahieren der Zeitintervalle für jede Bewegungsbeziehung
        final var zeitintervalleByBewegungsbeziehung = zeitintervallExtractorService.extractZeitintervalle(
                zaehlungId,
                zaehlart,
                startUhrzeit,
                endeUhrzeit,
                isKreisverkehr,
                options,
                types);

        // Summieren der Zeitintervalle über die Bewegungsbeziehung
        final var overBewegungsbeziehungSummedZeitintervalle = zeitintervallSummationService
                .sumZeitintervelleOverBewegungsbeziehung(zeitintervalleByBewegungsbeziehung);

        // Ermittlung der Spitzenstunden
        if (CollectionUtils.containsAny(types, TypeZeitintervall.SPITZENSTUNDE_KFZ, TypeZeitintervall.SPITZENSTUNDE_RAD,
                TypeZeitintervall.SPITZENSTUNDE_FUSS)) {
            final var spitzenstunden = spitzenstundeCalculatorService.calculateAndAddSpitzenstundeForGivenZeitintervalle(zaehlungId, options.getZeitblock(),
                    overBewegungsbeziehungSummedZeitintervalle, types);
            if (types.size() == 1) {
                // Es wurde nur die Spitzenstunde gewählt.
                overBewegungsbeziehungSummedZeitintervalle.clear();
            }
            overBewegungsbeziehungSummedZeitintervalle.addAll(spitzenstunden);
        }

        // Anreichern der Zeitintervalle um die entsprechende Bewegungsbeziehung.
        /**
         * {@link Zaehlart.QU}
         */
        if (CollectionUtils.isNotEmpty(options.getChosenQuerungsverkehre())) {
            var querungsverkehr = new Querungsverkehr();
            if (options.getChosenQuerungsverkehre().size() == 1) {
                var chosenQuerungsverkehr = options.getChosenQuerungsverkehre().getFirst();
                querungsverkehr.setKnotenarm(chosenQuerungsverkehr.getKnotenarm());
                querungsverkehr.setRichtung(chosenQuerungsverkehr.getRichtung());
            }
            overBewegungsbeziehungSummedZeitintervalle.forEach(zeitintervall -> zeitintervall.setQuerungsverkehr(querungsverkehr));
        }

        /**
         * {@link Zaehlart.FJS}
         */
        if (CollectionUtils.isNotEmpty(options.getChosenLangsverkehre())) {
            var langsverkehr = new Laengsverkehr();
            if (options.getChosenLangsverkehre().size() == 1) {
                var chosenLaengsverkehr = options.getChosenLangsverkehre().getFirst();
                langsverkehr.setKnotenarm(chosenLaengsverkehr.getKnotenarm());
                langsverkehr.setRichtung(chosenLaengsverkehr.getRichtung());
                langsverkehr.setStrassenseite(chosenLaengsverkehr.getStrassenseite());
            }
            overBewegungsbeziehungSummedZeitintervalle.forEach(zeitintervall -> zeitintervall.setLaengsverkehr(langsverkehr));
        }

        /**
         * {@link Zaehlart.QJS}
         */
        if (CollectionUtils.isNotEmpty(options.getChosenVerkehrsbeziehungen())) {
            var verkehrsbeziehung = new Verkehrsbeziehung();
            if (options.getChosenVerkehrsbeziehungen().size() == 1) {
                var chosenVerkehrsbeziehung = options.getChosenVerkehrsbeziehungen().getFirst();
                verkehrsbeziehung.setVon(chosenVerkehrsbeziehung.getVon());
                verkehrsbeziehung.setNach(chosenVerkehrsbeziehung.getNach());
                verkehrsbeziehung.setStrassenseite(chosenVerkehrsbeziehung.getStrassenseite());
            }
            overBewegungsbeziehungSummedZeitintervalle.forEach(zeitintervall -> zeitintervall.setVerkehrsbeziehung(verkehrsbeziehung));
        }

        /**
         * Alle anderen Zaehlarten.
         */
        var verkehrsbeziehung = new Verkehrsbeziehung();
        verkehrsbeziehung.setVon(options.getVonKnotenarm());
        verkehrsbeziehung.setNach(options.getNachKnotenarm());
        if (isKreisverkehr) {
            if (ObjectUtils.isNotEmpty(options.getVonKnotenarm()) && ObjectUtils.isEmpty(options.getNachKnotenarm())) {
                verkehrsbeziehung.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.HINEIN);
            } else if (ObjectUtils.isEmpty(options.getVonKnotenarm()) && ObjectUtils.isNotEmpty(options.getNachKnotenarm())) {
                verkehrsbeziehung.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.HERAUS);
            } else {
                verkehrsbeziehung.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.VORBEI);
            }
        }
        overBewegungsbeziehungSummedZeitintervalle.forEach(zeitintervall -> zeitintervall.setVerkehrsbeziehung(verkehrsbeziehung));

        return overBewegungsbeziehungSummedZeitintervalle.stream().sorted(Comparator.comparing(Zeitintervall::getSortingIndex)).toList();
    }

}
