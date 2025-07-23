package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessfaehigkeitDTO;
import de.muenchen.dave.domain.dtos.messstelle.ValidateZeitraumAndTagestypForMessstelleDTO;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.domain.model.messstelle.ValidateZeitraumAndTagesTypForMessstelleModel;
import de.muenchen.dave.services.KalendertagService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.ArithmeticUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ValidierungService {

    @Data
    public static final class ValidationResult {

        private boolean valid;

        private Long numberOfRelevantKalendertage;

        private Long numberOfUnauffaelligeTage;

    }

    private final UnauffaelligeTageService unauffaelligeTageService;

    private final KalendertagService kalendertagService;

    public boolean isZeitraumAndTagestypValid(final ValidateZeitraumAndTagestypForMessstelleDTO request) {
        final var tagestypen = TagesTyp.getIncludedTagestypen(request.getTagesTyp());

        final long numberOfRelevantKalendertage = kalendertagService.countAllKalendertageByDatumAndTagestypen(
                request.getZeitraum().getFirst(),
                request.getZeitraum().getLast(),
                tagestypen);

        final long numberOfUnauffaelligeTage = unauffaelligeTageService.countAllUnauffaelligetageByMstIdAndTimerangeAndTagestypen(
                request.getMstId(),
                request.getZeitraum().getFirst(),
                request.getZeitraum().getLast(),
                tagestypen);

        return hasMinimuOfTwoUnauffaelligeTage(numberOfUnauffaelligeTage)
                && hasMinimuOfFiftyPercentUnauffaelligeTage(numberOfUnauffaelligeTage, numberOfRelevantKalendertage);
    }

    public ValidationResult areZeitraeumeAndTagesTypForMessstelleValid(
            final String mstId,
            final List<List<LocalDate>> zeitraeume,
            final TagesTyp tagesTyp) {
        final var tagestypen = TagesTyp.getIncludedTagestypen(tagesTyp);
        final var validationResult = new ValidationResult();

        final long numberOfRelevantKalendertage = zeitraeume
                .stream()
                .map(zeitraum -> kalendertagService.countAllKalendertageByDatumAndTagestypen(
                        zeitraum.getFirst(),
                        zeitraum.getLast(),
                        tagestypen))
                .reduce(0L, ArithmeticUtils::addAndCheck);
        validationResult.setNumberOfRelevantKalendertage(numberOfRelevantKalendertage);

        final long numberOfUnauffaelligeTage = zeitraeume
                .stream()
                .map(zeitraum -> unauffaelligeTageService.countAllUnauffaelligetageByMstIdAndTimerangeAndTagestypen(
                        mstId,
                        zeitraum.getFirst(),
                        zeitraum.getLast(),
                        tagestypen))
                .reduce(0L, ArithmeticUtils::addAndCheck);
        validationResult.setNumberOfUnauffaelligeTage(numberOfUnauffaelligeTage);

        final boolean isValid = hasMinimuOfTwoUnauffaelligeTage(numberOfUnauffaelligeTage)
                && hasMinimuOfFiftyPercentUnauffaelligeTage(numberOfUnauffaelligeTage, numberOfRelevantKalendertage);
        validationResult.setValid(isValid);

        return validationResult;
    }

    protected boolean hasMinimuOfTwoUnauffaelligeTage(final long numberOfUnauffaelligeTage) {
        return numberOfUnauffaelligeTage >= 2;
    }

    protected boolean hasMinimuOfFiftyPercentUnauffaelligeTage(final long numberOfUnauffaelligeTage, final long numberOfRelevantKalendertage) {
        return numberOfRelevantKalendertage > 0 &&
                BigDecimal.valueOf(numberOfUnauffaelligeTage).divide(BigDecimal.valueOf(numberOfRelevantKalendertage), RoundingMode.HALF_UP)
                        .doubleValue() >= 0.5;
    }

    public List<ReadMessfaehigkeitDTO> getRelevantMessfaehigkeitenAccordingFahrzeugklasse(
            final ValidateZeitraumAndTagesTypForMessstelleModel request,
            final Fahrzeugklasse fahrzeugklasse) {
        final var relevantMessfaehigkeiten = request.getMessfaehigkeiten()
                .stream()
                .filter(messfaehigkeit -> isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(
                        fahrzeugklasse,
                        messfaehigkeit.getFahrzeugklasse()))
                .toList();
        return relevantMessfaehigkeiten;
    }

    /**
     * Gibt für die gegebenen Fahrzeugoptions auf Basis der Attribute mit dem Wert true die passende
     * Fahrzeugklasse zurück.
     *
     * @param fahrzeugOptions
     * @return die Fahrzeugklasse auf Basis der gewählten Fahrzeugoptions.
     */
    public Fahrzeugklasse getFahrzeugklasseAccordingChoosenFahrzeugoptions(final FahrzeugOptionsDTO fahrzeugOptions) {
        if (areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions)) {
            return Fahrzeugklasse.SUMME_KFZ;
        } else if (areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions)) {
            return Fahrzeugklasse.ZWEI_PLUS_EINS;
        } else if (areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions)) {
            return Fahrzeugklasse.ACHT_PLUS_EINS;
        } else if (areFahrzeugoptionsForFahrzeugklasseRadChoosen(fahrzeugOptions)) {
            return Fahrzeugklasse.RAD;
        } else {
            return null;
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
        return (fahrzeugOptions.isKraftfahrzeugverkehr() ||
                fahrzeugOptions.isSchwerverkehr() ||
                fahrzeugOptions.isSchwerverkehrsanteilProzent()) &&
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

    protected boolean areFahrzeugoptionsForFahrzeugklasseRadChoosen(final FahrzeugOptionsDTO fahrzeugOptions) {
        return fahrzeugOptions.isRadverkehr();
    }

    /**
     * Überprüft, ob die gegebene Fahrzeugklasse in der Vergleichsfahrzeugklasse enthalten ist.
     *
     * Diese Methode prüft spezifische Bedingungen für die Fahrzeugklassen:
     * - {@link Fahrzeugklasse#ACHT_PLUS_EINS} ist nur in {@link Fahrzeugklasse#ACHT_PLUS_EINS}
     * enthalten.
     * - {@link Fahrzeugklasse#ZWEI_PLUS_EINS} ist in {@link Fahrzeugklasse#ZWEI_PLUS_EINS} und
     * {@link Fahrzeugklasse#ACHT_PLUS_EINS} enthalten.
     * - {@link Fahrzeugklasse#SUMME_KFZ} ist in {@link Fahrzeugklasse#ACHT_PLUS_EINS},
     * {@link Fahrzeugklasse#ZWEI_PLUS_EINS} und sich selbst enthalten.
     * - {@link Fahrzeugklasse#RAD} ist nur in {@link Fahrzeugklasse#RAD} enthalten.
     *
     * @param fahrzeugklasse die Fahrzeugklasse, die überprüft werden soll
     * @param fahrzeugklasseToCompare die Fahrzeugklasse, mit der verglichen wird
     * @return true, wenn die Fahrzeugklasse in der Vergleichsfahrzeugklasse enthalten ist, andernfalls
     *         false
     */
    protected boolean isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(
            final Fahrzeugklasse fahrzeugklasse,
            final Fahrzeugklasse fahrzeugklasseToCompare) {
        final var isContainedInAchtPlusEins = Fahrzeugklasse.ACHT_PLUS_EINS.equals(fahrzeugklasse) &&
                Fahrzeugklasse.ACHT_PLUS_EINS.equals(fahrzeugklasseToCompare);

        final var isContainedInZweiPlusEins = Fahrzeugklasse.ZWEI_PLUS_EINS.equals(fahrzeugklasse)
                && (Fahrzeugklasse.ZWEI_PLUS_EINS.equals(fahrzeugklasseToCompare) ||
                        Fahrzeugklasse.ACHT_PLUS_EINS.equals(fahrzeugklasseToCompare));

        final var isContainedInSummeKfz = Fahrzeugklasse.SUMME_KFZ.equals(fahrzeugklasse) &&
                (Fahrzeugklasse.ACHT_PLUS_EINS.equals(fahrzeugklasseToCompare) ||
                        Fahrzeugklasse.ZWEI_PLUS_EINS.equals(fahrzeugklasseToCompare) ||
                        Fahrzeugklasse.SUMME_KFZ.equals(fahrzeugklasseToCompare));

        final var isContainedInRad = Fahrzeugklasse.RAD.equals(fahrzeugklasse) &&
                Fahrzeugklasse.RAD.equals(fahrzeugklasseToCompare);

        return isContainedInAchtPlusEins || isContainedInZweiPlusEins || isContainedInSummeKfz || isContainedInRad;
    }
}
