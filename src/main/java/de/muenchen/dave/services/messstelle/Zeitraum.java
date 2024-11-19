package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.YearMonth;

@Data
@AllArgsConstructor
public class Zeitraum {
    YearMonth start;
    YearMonth end;
    AuswertungsZeitraum auswertungsZeitraum;
}
