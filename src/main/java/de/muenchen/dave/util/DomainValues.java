package de.muenchen.dave.util;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor
public final class DomainValues {

    public static final String MIN_X_KOORDINATE = "674118";
    public static final String MAX_X_KOORDINATE = "706515";
    public static final String MIN_Y_KOORDINATE = "5318905";
    public static final String MAX_Y_KOORDINATE = "5348049";

    public static String getWetterValue(String wetter) {
        if (StringUtils.equals(wetter, "SUNNY")) {
            return "Sonnig";
        } else if (StringUtils.equals(wetter, "SUNNY_COLD")) {
            return "Sonnig kalt";
        } else if (StringUtils.equals(wetter, "CLOUDY")) {
            return "Bewölkt";
        } else if (StringUtils.equals(wetter, "RAINY")) {
            return "Regnerisch (Schauer)";
        } else if (StringUtils.equals(wetter, "CONTINUOUS_RAINY")) {
            return "Regnerisch (dauerhaft)";
        } else if (StringUtils.equals(wetter, "FOGGY")) {
            return "neblig";
        } else if (StringUtils.equals(wetter, "SNOWY")) {
            return "Schnee";
        } else {
            return "Keine Angabe";
        }
    }

    public static String getZaehldauerValue(String zaehldauer) {
        if (StringUtils.equals(zaehldauer, "DAUER_2_X_4_STUNDEN")) {
            return "Kurzzeiterhebung (6 bis 10 Uhr; 15 bis 19 Uhr)";
        } else if (StringUtils.equals(zaehldauer, "DAUER_24_STUNDEN")) {
            return "24-Stundenzählung (0 bis 24 Uhr)";
        } else if (StringUtils.equals(zaehldauer, "DAUER_16_STUNDEN")) {
            return "Kurzzeiterhebung (6 bis 22 Uhr)";
        } else if (StringUtils.equals(zaehldauer, "DAUER_13_STUNDEN")) {
            return "Kurzzeiterhebung (6 bis 19 Uhr)";
        } else if (StringUtils.equals(zaehldauer, "SONSTIGE")) {
            return "Sonstige";
        } else {
            return "Keine Angabe";
        }
    }

}
