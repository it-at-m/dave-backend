package de.muenchen.dave.services;

import com.google.common.base.Splitter;
import de.muenchen.dave.domain.elasticsearch.Knotenarm;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Status;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IndexServiceUtils {

    public static final String WINTER = "Winter";

    public static final String FRUEHLING = "Frühling";

    public static final String SOMMER = "Sommer";

    public static final String HERBST = "Herbst";

    public static final String TAGESTYP_WERKTAG = "Werktag";

    public static final String TAGESTYP_WOCHENENDE = "Wochenende";

    public static final DateTimeFormatter DDMMYYYY = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final String KREUZUNGSNAME_REPLACEMENT_NAME_DIVIDER = " - ";

    private static final String HTML_BREAK = "<br/>";

    public static List<String> splitStrings(String s) {
        return Splitter.on(",").omitEmptyStrings().trimResults().splitToList(s);
    }

    /**
     * Ermittelt die Jahreszeit für ein Datum.
     *
     * @param d Datum
     * @return Jahreszeit
     */
    public static String jahreszeitenDetector(LocalDate d) {
        int m = d.getMonthValue();
        if (m < 3 || m > 11) {
            return WINTER;
        }

        if (m > 2 && m < 6) {
            return FRUEHLING;
        }

        if (m > 5 && m < 9) {
            return SOMMER;
        }

        if (m > 8 && m < 12) {
            return HERBST;
        }

        log.error("Local Date with more than 12 month?!!?");
        return "";
    }

    /**
     * Erstellt eine Liste von Jahren aus allen Zählungen.
     *
     * @param zs Zaehlungen
     * @return Liste mit Jahren
     */
    public static List<Integer> getZaehljahre(List<Zaehlung> zs) {
        return zs.stream().map(z -> Integer.parseInt(z.getJahr())).collect(Collectors.toList());
    }

    /**
     * Holt die neuste Zählung aus der Liste.
     *
     * @param zs Zaehlungen
     * @return aktuellste Zaehlung
     */
    public static Zaehlung getLetzteAktiveZaehlung(final List<Zaehlung> zs) {
        return zs.stream().filter(zaehlung -> Status.ACTIVE.name().equalsIgnoreCase(zaehlung.getStatus())).max(Comparator.comparing(Zaehlung::getDatum))
                .orElseGet(() -> {
                    log.warn("List of 'Zaehlungen' is empty. I can't give you the last entry");
                    return null;
                });
    }

    /**
     * @param datum für welches der Tagestyp ermittelt werden soll.
     * @return Für die Tage Montag bis Freitag wird der Wert "Wochentag" zurückgegeben. Andernfalls wird
     *         der Wert "Wochenende" zurückgegeben.
     */
    public static String getTagesTyp(final LocalDate datum) {
        final DayOfWeek dayOfWeek = datum.getDayOfWeek();
        // 1 = Montag bis 7 = Sonntag
        final int isoDayNumber = dayOfWeek.getValue();
        final String tagesTyp;
        if (isoDayNumber < 6) {
            tagesTyp = TAGESTYP_WERKTAG;
        } else {
            tagesTyp = TAGESTYP_WOCHENENDE;
        }
        return tagesTyp;
    }

    /**
     * Diese Methode erstellt den Kreuzungsnamen konkateniert aus den Straßennamen der Zaehlung falls
     * kein expliziter Kreuzungsname gesetzt ist.
     *
     * @param kreuzungsname welcher gegebenenfalls durch die konkatenierten Straßennamen ersetzt wird.
     * @param zaehlung zur Extraktion der Straßennamen.
     * @return den Kreuzungsname oder die Straßennamen falls vorher kein Kreuzungsname gesetzt.
     */
    public static String createKreuzungsname(final String kreuzungsname, final Zaehlung zaehlung) {
        String newKreuzungsname = kreuzungsname;
        if (StringUtils.isEmpty(kreuzungsname) && CollectionUtils.isNotEmpty(zaehlung.getKnotenarme())) {
            newKreuzungsname = zaehlung.getKnotenarme().stream()
                    .sorted(Comparator.comparingInt(Knotenarm::getNummer))
                    .map(Knotenarm::getStrassenname)
                    .distinct()
                    .collect(Collectors.joining(KREUZUNGSNAME_REPLACEMENT_NAME_DIVIDER));
        }
        return newKreuzungsname;
    }

}
