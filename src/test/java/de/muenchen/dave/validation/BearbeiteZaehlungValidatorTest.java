package de.muenchen.dave.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteKnotenarmDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Zaehlart;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

public class BearbeiteZaehlungValidatorTest {

    private final BearbeiteZaehlungValidator validator = new BearbeiteZaehlungValidator();

    @Test
    void isValidTest() {
        assertThat(this.validator.isValid(null, null), is(false));
        assertThat(this.validator.isValid(new BearbeiteZaehlungDTO(), null), is(true));
    }

    @Test
    void areZaehlartAndSelctedKnotenarmeValidTest() {
        final BearbeiteZaehlungDTO toValidate = new BearbeiteZaehlungDTO();
        toValidate.setZaehlart(Zaehlart.FJS.name());
        assertThat(this.validator.areZaehlartAndSelctedKnotenarmeValid(toValidate), is(true));
        toValidate.setZaehlart(Zaehlart.QJS.name());
        toValidate.setKnotenarme(new ArrayList<>());
        assertThat(this.validator.areZaehlartAndSelctedKnotenarmeValid(toValidate), is(false));
        final BearbeiteKnotenarmDTO node1 = new BearbeiteKnotenarmDTO();
        node1.setNummer(1);
        toValidate.getKnotenarme().add(node1);
        final BearbeiteKnotenarmDTO node2 = new BearbeiteKnotenarmDTO();
        toValidate.getKnotenarme().add(node2);
        assertThat(this.validator.areZaehlartAndSelctedKnotenarmeValid(toValidate), is(false));
        node2.setNummer(2);
        assertThat(this.validator.areZaehlartAndSelctedKnotenarmeValid(toValidate), is(false));
        node2.setNummer(3);
        assertThat(this.validator.areZaehlartAndSelctedKnotenarmeValid(toValidate), is(true));
        toValidate.getKnotenarme().add(new BearbeiteKnotenarmDTO());
        assertThat(this.validator.areZaehlartAndSelctedKnotenarmeValid(toValidate), is(false));
    }

    @Test
    void areZaehlartAndSelectedCategoriesValidTest() {
        final BearbeiteZaehlungDTO toValidate = new BearbeiteZaehlungDTO();
        assertThat(this.validator.areZaehlartAndSelectedCategoriesValid(toValidate), is(true));
        toValidate.setZaehlart(null);
        assertThat(this.validator.areZaehlartAndSelectedCategoriesValid(toValidate), is(true));
        toValidate.setZaehlart(Zaehlart.N.name());
        assertThat(this.validator.areZaehlartAndSelectedCategoriesValid(toValidate), is(true));
        toValidate.setZaehlart(Zaehlart.QJS.name());
        toValidate.setKategorien(new ArrayList<>());
        assertThat(this.validator.areZaehlartAndSelectedCategoriesValid(toValidate), is(true));
        toValidate.getKategorien().add(Fahrzeug.FUSS);
        assertThat(this.validator.areZaehlartAndSelectedCategoriesValid(toValidate), is(true));
        toValidate.setZaehlart(Zaehlart.QU.name());
        toValidate.getKategorien().add(Fahrzeug.RAD);
        assertThat(this.validator.areZaehlartAndSelectedCategoriesValid(toValidate), is(true));
        toValidate.setZaehlart(Zaehlart.FJS.name());
        assertThat(this.validator.areZaehlartAndSelectedCategoriesValid(toValidate), is(true));
        toValidate.getKategorien().add(Fahrzeug.KFZ);
        assertThat(this.validator.areZaehlartAndSelectedCategoriesValid(toValidate), is(false));
    }

}
