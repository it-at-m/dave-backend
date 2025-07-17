package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import java.time.LocalDate;
import java.time.YearMonth;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Zeitraum {
    private YearMonth start;
    private YearMonth end;
    private AuswertungsZeitraum auswertungsZeitraum;

    public LocalDate getStartDate() {
        return start.atDay(1);
    }

    public LocalDate getEndDate() {
        return end.atEndOfMonth();
    }
}
