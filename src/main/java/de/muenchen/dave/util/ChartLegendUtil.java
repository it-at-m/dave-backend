/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChartLegendUtil {

    public static final String PKW = "Pkw";

    public static final String LKW = "Lkw";

    public static final String LASTZUEGE = "Lz";

    public static final String BUSSE = "Busse";

    public static final String KRAFTRAEDER = "Krad";

    public static final String RAD = "Rad";

    public static final String FUSSGAENGER = "Fuß";

    public static final String KFZ = "Kfz";

    public static final String SCHWERVERKEHR = "SV";

    public static final String SCHWERVERKEHR_ANTEIL_PROZENT = "SV %";

    public static final String GUETERVERKEHR = "GV";

    public static final String GUETERVERKEHR_ANTEIL_PROZENT = "GV %";

    public static final String PKW_EINHEITEN = "Pkw-Einheiten";
    public static final String LFW = "Lfw";

    // Nur fuer die Heatmap
    public static final String PKW_HEATMAP = "Personenkraftwagen";

    public static final String LKW_HEATMAP = "Lastkraftwagen";
    public static final String LFW_HEATMAP = "Lieferwagen";

    public static final String LASTZUEGE_HEATMAP = "Lastzüge";

    public static final String BUSSE_HEATMAP = "Busse";

    public static final String KRAFTRAEDER_HEATMAP = "Krafträder";

    public static final String RAD_HEATMAP = "Fahrräder";

    public static final String FUSSGAENGER_HEATMAP = "Fußgänger";

    public static final String KFZ_HEATMAP = "Kraftfahrzeuge";

    public static final String SCHWERVERKEHR_HEATMAP = "Schwerverkehr";

    public static final String GUETERVERKEHR_HEATMAP = "Güterverkehr";

    public static final String PKW_EINHEITEN_HEATMAP = "PKW-Einheiten";

    public static List<String> checkAndAddToLegendWhenNotAvailable(
            final List<String> legend,
            final String legendEntryToAdd) {
        if (!legend.contains(legendEntryToAdd)) {
            legend.add(legendEntryToAdd);
        }
        return legend;
    }

}
