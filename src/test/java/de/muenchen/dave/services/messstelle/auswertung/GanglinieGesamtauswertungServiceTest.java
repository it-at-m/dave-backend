package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.services.messstelle.Zeitraum;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;

class GanglinieGesamtauswertungServiceTest {

    private GanglinieGesamtauswertungService ganglinieGesamtauswertungService = new GanglinieGesamtauswertungService();

    @Test
    void

    @Test
    void getZeitraumForXaxis() {
        var zeitraum = new Zeitraum(YearMonth.of(2024, 11), null, AuswertungsZeitraum.SEPTEMBER);
        var result = ganglinieGesamtauswertungService.getZeitraumForXaxis(zeitraum);
        Assertions.assertThat(result).isNotNull().isEqualTo("09 2024");

        zeitraum = new Zeitraum(YearMonth.of(2024, 11), null, AuswertungsZeitraum.HALBJAHR_2);
        result = ganglinieGesamtauswertungService.getZeitraumForXaxis(zeitraum);
        Assertions.assertThat(result).isNotNull().isEqualTo("H2 2024");

        zeitraum = new Zeitraum(YearMonth.of(2024, 11), null, AuswertungsZeitraum.QUARTAL_3);
        result = ganglinieGesamtauswertungService.getZeitraumForXaxis(zeitraum);
        Assertions.assertThat(result).isNotNull().isEqualTo("Q3 2024");

        zeitraum = new Zeitraum(YearMonth.of(2024, 11), null, AuswertungsZeitraum.JAHRE);
        result = ganglinieGesamtauswertungService.getZeitraumForXaxis(zeitraum);
        Assertions.assertThat(result).isNotNull().isEqualTo("2024");
    }

}