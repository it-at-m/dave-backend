package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
class AuswertungServiceTest {

    private final AuswertungService auswertungService = new AuswertungService(null);

    @Test
    void calculateZeitraeume() {
        final List<AuswertungsZeitraum> auswertungszeitraeume = new ArrayList<>();
        auswertungszeitraeume.add(AuswertungsZeitraum.QUARTAL_1);
        auswertungszeitraeume.add(AuswertungsZeitraum.FEBRUAR);
        final List<Integer> jahre = new ArrayList<>();
        jahre.add(2020);
        jahre.add(2021);

        final List<List<LocalDate>> result = auswertungService.calculateZeitraeume(auswertungszeitraeume, jahre);

        final List<List<LocalDate>> expected = new ArrayList<>();
        final List<LocalDate> expected2020Q1 = new ArrayList<>();
        expected2020Q1.add(LocalDate.of(2020, 1, 1));
        expected2020Q1.add(LocalDate.of(2020, 3, 31));
        final List<LocalDate> expected2021Q1 = new ArrayList<>();
        expected2021Q1.add(LocalDate.of(2021, 1, 1));
        expected2021Q1.add(LocalDate.of(2021, 3, 31));

        final List<LocalDate> expected2020Feb = new ArrayList<>();
        expected2020Feb.add(LocalDate.of(2020, 2, 1));
        expected2020Feb.add(LocalDate.of(2020, 2, 29));
        final List<LocalDate> expected2021Feb = new ArrayList<>();
        expected2021Feb.add(LocalDate.of(2021, 2, 1));
        expected2021Feb.add(LocalDate.of(2021, 2, 28));

        expected.add(expected2020Q1);
        expected.add(expected2021Q1);
        expected.add(expected2020Feb);
        expected.add(expected2021Feb);

        Assertions.assertThat(result)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFieldsMatchingRegexes(".*")
                .isEqualTo(expected);
    }

}
