package de.muenchen.dave.domain.validation;

import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.domain.enums.TagesTyp;
import java.time.LocalDate;
import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TagestypValidatorTest {

    private final TagestypValidator validator = new TagestypValidator();

    @Test
    void isValidTagestyp() {
        MessstelleOptionsDTO options = new MessstelleOptionsDTO();

        options.setZeitraum(new ArrayList<>());
        Assertions.assertTrue(validator.isValid(options, null));

        options.getZeitraum().add(LocalDate.of(2020, 1, 1));
        Assertions.assertTrue(validator.isValid(options, null));

        options.getZeitraum().add(LocalDate.of(2020, 1, 1));
        Assertions.assertTrue(validator.isValid(options, null));

        options.getZeitraum().clear();
        options.getZeitraum().add(LocalDate.of(2020, 1, 1));
        options.getZeitraum().add(LocalDate.of(2024, 1, 1));
        Assertions.assertFalse(validator.isValid(options, null));

        options.setTagesTyp(TagesTyp.MO_SO);
        Assertions.assertTrue(validator.isValid(options, null));

        options.getZeitraum().clear();
        options.getZeitraum().add(LocalDate.of(2024, 1, 1));
        options.getZeitraum().add(LocalDate.of(2022, 1, 1));
        Assertions.assertTrue(validator.isValid(options, null));

        options.setTagesTyp(TagesTyp.UNSPECIFIED);
        Assertions.assertFalse(validator.isValid(options, null));

        options.setTagesTyp(null);
        Assertions.assertFalse(validator.isValid(options, null));
    }
}
