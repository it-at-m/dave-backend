package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.domain.model.messstelle.ValidateZeitraumAndTagesTypForMessstelleModel;
import de.muenchen.dave.services.KalendertagService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ValidierungService {
    private final UnauffaelligeTageService unauffaelligeTageService;
    private final KalendertagService kalendertagService;

    public boolean isZeitraumAndTagestypValid(final ValidateZeitraumAndTagesTypForMessstelleModel request) {
        final var tagestypen = TagesTyp.getIncludedTagestypen(request.getTagesTyp());
        final long numberOfRelevantKalendertage = kalendertagService.countAllKalendertageByDatumAndTagestypen(
                request.getZeitraum().getFirst(),
                request.getZeitraum().getLast(), tagestypen);

        final long numberOfUnauffaelligeTage = unauffaelligeTageService
                .countAllUnauffaelligetageByMstIdAndTimerangeAndTagestypen(request.getMstId(),
                        request.getZeitraum().getFirst(), request.getZeitraum().getLast(), tagestypen);

        return hasMinimuOfTwoUnauffaelligeTage(numberOfUnauffaelligeTage)
                && hasMinimuOfFiftyPercentUnauffaelligeTage(numberOfUnauffaelligeTage, numberOfRelevantKalendertage);
    }

    /* Rename */
    public boolean isZeitraumAndTagestypValid(final ValidateZeitraumAndTagesTypForMessstelleModel request, final FahrzeugOptionsDTO fahrzeugoptions) {
        final var fahrzeugklasseAccordingChoosenFahrzeugoptions = getFahrzeugklasseAccordingChoosenFahrzeugoptions(fahrzeugoptions);
        final var relevantMessfaehigkeiten = request.getMessfaehigkeiten()
                .stream()
                .filter(messfaehigkeit -> containsFahrzeugklasseTheGivenFahrzeugklasseToCompare(
                        messfaehigkeit.getFahrzeugklasse(),
                        fahrzeugklasseAccordingChoosenFahrzeugoptions
                ))
                .toList();
        return true;
    }

    public boolean isZeitraumAndTagestypValid(
            final String mstId,
            final LocalDate startDateZeitraum,
            final LocalDate endDateZeitraum,
            final TagesTyp tagesTyp) {
        final var tagestypen = TagesTyp.getIncludedTagestypen(tagesTyp);

        final long numberOfRelevantKalendertage = kalendertagService.countAllKalendertageByDatumAndTagestypen(
                startDateZeitraum,
                endDateZeitraum,
                tagestypen);

        final long numberOfUnauffaelligeTage = unauffaelligeTageService
                .countAllUnauffaelligetageByMstIdAndTimerangeAndTagestypen(
                        mstId,
                        startDateZeitraum,
                        endDateZeitraum,
                        tagestypen);

        return hasMinimuOfTwoUnauffaelligeTage(numberOfUnauffaelligeTage)
                && hasMinimuOfFiftyPercentUnauffaelligeTage(numberOfUnauffaelligeTage, numberOfRelevantKalendertage);
    }

    protected boolean hasMinimuOfTwoUnauffaelligeTage(final long numberOfUnauffaelligeTage) {
        return numberOfUnauffaelligeTage >= 2;
    }

    protected boolean hasMinimuOfFiftyPercentUnauffaelligeTage(final long numberOfUnauffaelligeTage, final long numberOfRelevantKalendertage) {
        return BigDecimal.valueOf(numberOfUnauffaelligeTage).divide(BigDecimal.valueOf(numberOfRelevantKalendertage), RoundingMode.HALF_UP)
                .doubleValue() >= 0.5;
    }

    protected Fahrzeugklasse getFahrzeugklasseAccordingChoosenFahrzeugoptions(final FahrzeugOptionsDTO fahrzeugOptions) {
        if (areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions)) {
            return Fahrzeugklasse.SUMME_KFZ;
        } else if (areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions)) {
            return Fahrzeugklasse.ZWEI_PLUS_EINS;
        } else if (areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions)) {
            return Fahrzeugklasse.ACHT_PLUS_EINS;
        } else {
            return Fahrzeugklasse.RAD;
        }
    }

    protected boolean areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(final FahrzeugOptionsDTO fahrzeugOptions) {
        return fahrzeugOptions.isKraftfahrzeugverkehr() ||
                fahrzeugOptions.isSchwerverkehr() ||
                fahrzeugOptions.isSchwerverkehrsanteilProzent() ||
                fahrzeugOptions.isGueterverkehr() ||
                fahrzeugOptions.isGueterverkehrsanteilProzent() ||
                fahrzeugOptions.isLastkraftwagen() ||
                fahrzeugOptions.isLastzuege() ||
                fahrzeugOptions.isBusse() ||
                fahrzeugOptions.isKraftraeder() ||
                fahrzeugOptions.isPersonenkraftwagen() ||
                fahrzeugOptions.isLieferwagen();
    }

    protected boolean areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(final FahrzeugOptionsDTO fahrzeugOptions) {
        return fahrzeugOptions.isKraftfahrzeugverkehr() ||
                fahrzeugOptions.isSchwerverkehr() ||
                fahrzeugOptions.isSchwerverkehrsanteilProzent() &&
                        !fahrzeugOptions.isGueterverkehr() &&
                        !fahrzeugOptions.isGueterverkehrsanteilProzent() &&
                        !fahrzeugOptions.isLastkraftwagen() &&
                        !fahrzeugOptions.isLastzuege() &&
                        !fahrzeugOptions.isBusse() &&
                        !fahrzeugOptions.isKraftraeder() &&
                        !fahrzeugOptions.isPersonenkraftwagen() &&
                        !fahrzeugOptions.isLieferwagen();
    }

    protected boolean areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(final FahrzeugOptionsDTO fahrzeugOptions) {
        return fahrzeugOptions.isKraftfahrzeugverkehr() &&
                !fahrzeugOptions.isSchwerverkehr() &&
                !fahrzeugOptions.isSchwerverkehrsanteilProzent() &&
                !fahrzeugOptions.isGueterverkehr() &&
                !fahrzeugOptions.isGueterverkehrsanteilProzent() &&
                !fahrzeugOptions.isLastkraftwagen() &&
                !fahrzeugOptions.isLastzuege() &&
                !fahrzeugOptions.isBusse() &&
                !fahrzeugOptions.isKraftraeder() &&
                !fahrzeugOptions.isPersonenkraftwagen() &&
                !fahrzeugOptions.isLieferwagen();
    }

    protected boolean containsFahrzeugklasseTheGivenFahrzeugklasseToCompare(final Fahrzeugklasse fahrzeugklasse, final Fahrzeugklasse fahrzeugklasseToCompare) {
        final var isContainedInAchtPlusEins = Fahrzeugklasse.ACHT_PLUS_EINS.equals(fahrzeugklasse)
                && (Fahrzeugklasse.ACHT_PLUS_EINS.equals(fahrzeugklasseToCompare) ||
                        Fahrzeugklasse.ZWEI_PLUS_EINS.equals(fahrzeugklasseToCompare) ||
                        Fahrzeugklasse.SUMME_KFZ.equals(fahrzeugklasseToCompare));

        final var isContainedInZweiPlusEins = Fahrzeugklasse.ZWEI_PLUS_EINS.equals(fahrzeugklasse)
                && (Fahrzeugklasse.ZWEI_PLUS_EINS.equals(fahrzeugklasseToCompare) ||
                        Fahrzeugklasse.SUMME_KFZ.equals(fahrzeugklasseToCompare));

        final var isContainedInSummeKfz = Fahrzeugklasse.SUMME_KFZ.equals(fahrzeugklasse) &&
                Fahrzeugklasse.SUMME_KFZ.equals(fahrzeugklasseToCompare);

        final var isContainedInRad = Fahrzeugklasse.RAD.equals(fahrzeugklasse) &&
                Fahrzeugklasse.RAD.equals(fahrzeugklasseToCompare);

        return isContainedInAchtPlusEins || isContainedInZweiPlusEins || isContainedInSummeKfz || isContainedInRad;
    }
}
