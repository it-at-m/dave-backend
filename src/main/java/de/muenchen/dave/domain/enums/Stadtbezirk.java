/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum Stadtbezirk {

    ALTSTADT_LEHEL(1, "Altstadt-Lehel"),

    LUDWIGVORSTADT_ISARVORSTADT(2, "Ludwigsvorstadt-Isarvorstadt"),

    MAVORSTADT(3, "Maxvorstadt"),

    SCHWABING_WEST(4, "Schwabing-West"),

    AU_HAIDHAUSEN(5, "Au-Haidhausen"),

    SENDLING(6, "Sendling"),

    SENDLING_WESTPARK(7, "Sendling-Westpark"),

    SCHWANTHALERHOEHE(8, "Schwanthalerhöhe"),

    NEUHAUSEN_NYMPHENBURG(9, "Neuhausen-Nymphenburg"),

    MOOSACH(10, "Moosach"),

    MILBERTSHOFEN_AM_HART(11, "Milbertshofen-Am Hart"),

    SCHWABING_FREIMANN(12, "Schwabing-Freimann"),

    BOGENHAUSEN(13, "Bogenhausen"),

    BERG_AM_LAIM(14, "Berg am Laim"),

    TRUDERING_RIEM(15, "Trudering-Riem"),

    RAMERSDORF_PERLACH(16, "Ramersdorf-Perlach"),

    OBERGIESING_FASANGARTEN(17, "Obergiesing-Fasangarten"),

    UNTERGIESING_HARLACHING(18, "Untergiesing-Harlaching"),

    THALKIRCHEN_OBERSENDLING_FORSTENRIED_FUERSTENRIED_SOLLN(19, "Thalkirchen-Obersendling-Forstenried-Fürstenried-Solln"),

    HADERN(20, "Hadern"),

    PASING_OBERMENZING(21, "Pasing-Obermenzing"),

    AUBING_LOCHHAUSEN_LANGWIED(22, "Aubing-Lochhausen-Langwied"),

    ALLACH_UNTERMENZING(23, "Allach-Untermenzing"),

    FELDMOCHING_HASENBERGL(24, "Feldmoching-Hasenbergl"),

    LAIM(25, "Laim"),

    AUSERHALB(32, "Außerhalb der Stadtgrenze");

    private final int nummer;

    private final String bezeichnung;

    /**
     * @return Eine {@link Map} mit der Nummer als Key und der Bezeichnung als Value.
     */
    public static Map<Integer, String> getEnumattributesAsMap() {
        return Arrays.stream(Stadtbezirk.values())
                .collect(Collectors.toMap(Stadtbezirk::getNummer, Stadtbezirk::getBezeichnung));
    }

}
