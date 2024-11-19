/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum Zaehlart {

    // Standardzählung/Normal
    N(Arrays.asList("N", "Standardzählung", "Normal")),
    // Hauptverkehrsrichtung/Oberfläche/Hoch
    H(Arrays.asList("H", "Oberfläche", "Hoch", "Hauptverkehrsrichtung")),
    //Querschnitt
    Q(Arrays.asList("Q", "Querschnitt")),
    //Querschnitt/Sonderzählung"
    Q_(Arrays.asList("Q_", "Querschnitt", "Sonderzählung")),
    // Bahnschnitt
    QB(Arrays.asList("QB", "Bahn", "Bahnschnitt")),
    // Querschnitt/Hauptverkehrsrichtung/Oberfläche/Hoch
    QH(Arrays.asList("QH", "Querschnitt", "Hauptverkehrsrichtung", "Oberfläche", "Hoch")),
    //  Isarschnitt
    QI(Arrays.asList("QI", "Isar", "Isarschnitt")),
    // Stadtgrenzenzählung
    QS(Arrays.asList("QS", "Stadtgrenze")),
    // Querschnitt Tunnel/Unterführung/Tief
    QT(Arrays.asList("QT", "Querschnitt", "Tunnel", "Unterführung", "Tief")),
    // Querschnitt Radverkehr,
    QR(Arrays.asList("QR", "Querschnitt", "Radverkehr")),
    // Radverkehrszählung
    R(Arrays.asList("R", "Rad", "Fahrrad", "Radverkehr", "Radverkehrszählung")),
    // Tunnel / Unterführung / Tief
    T(Arrays.asList("T", "Tunnel", "Unterführung", "Tief")),
    // Teilknoten
    TK(Arrays.asList("TK", "Teilknoten"));

    /**
     * Das Zaehlartkürzel sowie dessen Bedeutungen.
     */
    private final List<String> bedeutung;

    /**
     * @param zaehlart für welche die Bedeutung zurückgegeben werden soll.
     * @return die Liste der Bedeutung für die im Parameter angegebene Zaehlart.
     */
    public static List<String> getBedeutungForZaehlart(final String zaehlart) {
        return getEnumattributeAsMap().get(zaehlart);
    }

    /**
     * @return Eine {@link Map} mit dem Zaehlartkürzel als Key und der Liste an Bedeutungen als Value.
     */
    public static Map<String, List<String>> getEnumattributeAsMap() {
        return Arrays.stream(Zaehlart.values())
                .collect(Collectors.toMap(
                        Zaehlart::getZaehlartkürzel,
                        Zaehlart::getBedeutung));
    }

    public static String getZaehlartkürzel(final Zaehlart zaehlart) {
        return zaehlart.getBedeutung().get(0);
    }

}
