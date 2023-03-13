/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@Getter
public enum Zaehldauer {

    /**
     * Kurzzeiterhebung (6 bis 10 Uhr; 15 bis 19 Uhr)
     */
    DAUER_2_X_4_STUNDEN(Arrays.asList("Kurzzeiterhebung", "Kurzzeiterhebung (2x4h)", "Kurzzeiterhebung (6 bis 10 Uhr; 15 bis 19 Uhr)", "2x4h", "2*4h", "2*4 Stunden", "2x4 Stunden", "2*4Stunden", "2x4Stunden"), 32),

    /**
     * 24 Stunden
     */
    DAUER_24_STUNDEN(Arrays.asList("Tageszählung", "Ganztageszählung", "Tag", "24-Stundenzählung", "24Stundenzählung", "24h", "24Stunden"), 96),

    /**
     * 16 Stunden
     */
    DAUER_16_STUNDEN(Arrays.asList("16h", "16Stunden"), 64),

    /**
     * Kurzzeiterhebung (6 bis 19Uhr)
     */
    DAUER_13_STUNDEN(Arrays.asList("13h", "13Stunden"), 52),

    /**
     * Sonstige
     */
    SONSTIGE(Arrays.asList("Sonderzähldauer", "Sonstige-Zähldauer"), 0);

    private List<String> suchwoerter;

    private int anzahlZeitintervalle;

}
