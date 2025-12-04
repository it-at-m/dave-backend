package de.muenchen.dave.validation;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteKnotenarmDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Zaehlart;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Optional;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class BearbeiteZaehlungValidator implements ConstraintValidator<BearbeiteZaehlungValid, BearbeiteZaehlungDTO> {

    /**
     * Prüft, ob die zu speichernde Zählung valide ist.
     *
     * @param toValidate {@link BearbeiteZaehlungDTO} zum Validieren.
     * @param constraintValidatorContext in welchem die Validierung stattfindet.
     * @return true, wenn die Zählung valide ist, sonst false.
     */
    @Override
    public boolean isValid(final BearbeiteZaehlungDTO toValidate, final ConstraintValidatorContext constraintValidatorContext) {
        return toValidate != null && validateZaehlung(toValidate);
    }

    /**
     * Ruft die einzelnen Validierungsmethoden auf und sammelt die Ergebnisse.
     *
     * @param toValidate {@link BearbeiteZaehlungDTO} zum Validieren.
     * @return true, wenn alle Validierungen erfolgreich waren, sonst false.
     */
    private boolean validateZaehlung(final BearbeiteZaehlungDTO toValidate) {
        return areZaehlartAndSelectedCategoriesValid(toValidate) && areZaehlartAndSelctedKnotenarmeValid(toValidate);
    }

    /**
     * Validiert anhand der ausgewaehlten Zaehlart, ob exakt 2 sich gegenueberliegende Knotenarme
     * ausgewaehlt wurden.
     * Erlaubte Knotenarme bei der {@link Zaehlart}.QJS : 1 & 3 || 2 & 4 || 5 & 7 || 6 & 8
     *
     * @param toValidate {@link BearbeiteZaehlungDTO} zum Validieren.
     * @return true, wenn die Validierung erfolgreich waren, sonst false.
     */
    protected boolean areZaehlartAndSelctedKnotenarmeValid(final BearbeiteZaehlungDTO toValidate) {
        boolean isValid = true;
        if (Zaehlart.QJS.name().equals(toValidate.getZaehlart())) {
            isValid = toValidate.getKnotenarme().size() == 2;
            final Optional<Integer> reduce = toValidate.getKnotenarme()
                    .stream()
                    .map(BearbeiteKnotenarmDTO::getNummer)
                    .reduce((integer, integer2) -> Math.abs(integer - integer2));
            if (reduce.isPresent()) {
                isValid = isValid && reduce.get() == 2;
            }
        }
        return isValid;
    }

    /**
     * Validiert anhand der ausgewaehlten Zaehlart, ob die richtigen Fahrzeuge ausgewaehlt wurden.
     * Bei den {@link Zaehlart}.QJS FJS QU darf nur {@link Fahrzeug}.RAD und/oder .FUSS ausgewaehlt
     * sein.
     *
     * @param toValidate {@link BearbeiteZaehlungDTO} zum Validieren.
     * @return true, wenn die Validierung erfolgreich waren, sonst false.
     */
    protected boolean areZaehlartAndSelectedCategoriesValid(final BearbeiteZaehlungDTO toValidate) {
        // Wenn die Zählart QJS, FJS oder QU ist, dann darf nur RAD oder FUSS ausgewählt sein
        boolean isValid = true;
        final List<String> zaehlarten = List.of(Zaehlart.QJS.name(), Zaehlart.FJS.name(), Zaehlart.QU.name());
        if (toValidate.getZaehlart() != null && zaehlarten.contains(toValidate.getZaehlart())) {
            final List<Fahrzeug> selectedCategoriesWithoutRadAndFuss = toValidate.getKategorien()
                    .stream()
                    .filter(fahrzeug -> !(fahrzeug.equals(Fahrzeug.RAD) || fahrzeug.equals(Fahrzeug.FUSS)))
                    .toList();
            isValid = CollectionUtils.isEmpty(selectedCategoriesWithoutRadAndFuss);
        }
        return isValid;
    }
}
