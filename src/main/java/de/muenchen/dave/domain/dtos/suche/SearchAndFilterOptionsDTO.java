package de.muenchen.dave.domain.dtos.suche;

import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.domain.enums.Verkehrsart;
import de.muenchen.dave.domain.enums.Wetter;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.enums.Zaehldauer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class SearchAndFilterOptionsDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean searchInMessstellen;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean searchInZaehlstellen;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean kreisverkehr;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean sonderzaehlung;
    private List<Zaehlart> zaehlarten;
    private List<Zaehldauer> zaehldauern;
    private List<Integer> stadtbezirkNummern;
    private LocalDate zaehldatumVon;
    private LocalDate zaehldatumBis;
    private List<String> zaehlJahreszeiten;
    private List<Integer> zaehlMonate;
    private List<TagesTyp> tagesTypen;
    private List<Wetter> wetter;
    private List<Verkehrsart> verkehrsarten;
    private List<Fahrzeugklasse> fahrzeugklassen;
    @NotNull
    private List<Verkehrsart> messstelleVerkehrsart;
}
