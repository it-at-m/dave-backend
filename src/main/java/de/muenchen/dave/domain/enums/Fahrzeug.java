package de.muenchen.dave.domain.enums;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Fahrzeug implements Serializable {

    // Kategorien
    KFZ("KFZ", Arrays.asList("KFZ", "Kfz", "Kraftfahrzeugverkehr")),
    SV("SV", Arrays.asList("SV", "Sv", "Schwerverkehr")),
    GV("GV", Arrays.asList("GV", "Gv", "Güterverkehr")),
    SV_P("SV%", Arrays.asList("SV%", "Sv%", "Schwerverkehrsanteil")),
    GV_P("GV%", Arrays.asList("GV%", "Gv%", "Güterverkehrsanteil")),
    // Klassen
    PKW("PKW", Arrays.asList("PKW", "Personenkraftwagen")),
    LKW("LKW", Arrays.asList("LKW", "Lastkraftwagen")),
    LZ("LZ", Arrays.asList("LZ", "Lastzug")),
    BUS("BUS", List.of("Bus")),
    KRAD("KRAD", Arrays.asList("KRAD", "Motorrad", "Kraftrad")),
    RAD("RAD", Arrays.asList("Rad", "Radfahrer", "Radverkehr")),
    FUSS("FUSS", Arrays.asList("FUSS", "FUß", "Fussgänger", "Fußgänger", "Fussverkehr", "Fußverkehr")),
    PKW_EINHEIT("PKW_EINHEIT", Arrays.asList("Personenkraftwageneinheiten", "Personenkraftwagen Einheiten", "Personenkraftwagen-Einheiten"));

    private final String name;

    private final List<String> suchworte;
}
