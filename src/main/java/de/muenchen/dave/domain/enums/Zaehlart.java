package de.muenchen.dave.domain.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.EnumUtils;

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
    // Querschnitt je Straßenseite
    QJS(Arrays.asList("QjS", "Querschnitt", "Straßenseite")),
    // Fuß & Rad je Straßenseite
    FJS(Arrays.asList("FjS", "Fußverkehr", "Radverkehr", "Straßenseite")),
    // Querung
    QU(Arrays.asList("Qu", "Querung")),
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
     * @param zaehlart für welche das Kürzel zurückgegeben werden soll.
     * @return das Kürzel der Zählart oder null falls die Zählart nicht existiert.
     */
    public static String getKuerzelForZaehlartOrNullIfZaehlartNotExisting(final String zaehlart) {
        final var zaehlartAsEnum = EnumUtils.getEnum(Zaehlart.class, zaehlart);
        return Objects.isNull(zaehlartAsEnum) ? null : getZaehlartKuerzel(zaehlartAsEnum);
    }

    /**
     * @return Eine {@link Map} mit dem Zaehlartkürzel als Key und der Liste an Bedeutungen als Value.
     */
    public static Map<String, List<String>> getEnumattributeAsMap() {
        return Arrays.stream(Zaehlart.values())
                .collect(Collectors.toMap(
                        Zaehlart::getZaehlartKuerzel,
                        Zaehlart::getBedeutung));
    }

    public static String getZaehlartKuerzel(final Zaehlart zaehlart) {
        return zaehlart.getBedeutung().getFirst();
    }

}
