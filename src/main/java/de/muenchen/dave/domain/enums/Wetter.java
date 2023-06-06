package de.muenchen.dave.domain.enums;

import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Wetter {

    SUNNY(Arrays.asList("sonnig", "Sonne")),

    SUNNY_COLD(List.of("sonnig (kalt)")),

    CLOUDY(Arrays.asList("bewölkt", "wolkig")),

    RAINY(Arrays.asList("regnerisch", "Schauer", "Regen")),

    CONTINUOUS_RAINY(Arrays.asList("regnerisch (dauerhaft)", "Dauerregen", "Regen")),

    FOGGY(Arrays.asList("neblig", "Nebel")),

    SNOWY(Arrays.asList("Schnee", "Schneefall", "Schneetreiben")),

    NO_INFORMATION(Arrays.asList("keine Wetterinformation", "kein Wetter", "fehlende Wetterinformation"));

    private List<String> suchwoerter;

}
