package de.muenchen.dave.services;

import de.muenchen.dave.relational.zaehlstellen.KnotenarmEntity;
import de.muenchen.dave.relational.zaehlstellen.ZaehlungEntity;
import de.muenchen.dave.domain.enums.Status;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
     * Holt die neuste Zählung aus der Liste.
     *
     * @param zaehlungen
     * @return aktuellste ZaehlungEntity
     */
    public static ZaehlungEntity getLetzteAktiveZaehlung(final List<ZaehlungEntity> zaehlungen) {
        return CollectionUtils.emptyIfNull(zaehlungen)
                .stream()
                .filter(zaehlung -> Objects.equals(zaehlung.getStatus(),  Status.ACTIVE))
                .max(Comparator.comparing(ZaehlungEntity::getDatum))
                .orElse(null);
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
     * Diese Methode erstellt den Kreuzungsnamen konkateniert aus den Straßennamen der ZaehlungEntity falls
     * kein expliziter Kreuzungsname gesetzt ist.
     *
     * @param kreuzungsname welcher gegebenenfalls durch die konkatenierten Straßennamen ersetzt wird.
     * @param zaehlung zur Extraktion der Straßennamen.
     * @return den Kreuzungsname oder die Straßennamen falls vorher kein Kreuzungsname gesetzt.
     */
    public static String createKreuzungsname(final String kreuzungsname, final ZaehlungEntity zaehlung) {
        String newKreuzungsname = kreuzungsname;
        if (StringUtils.isEmpty(kreuzungsname) && CollectionUtils.isNotEmpty(zaehlung.getKnotenarme())) {
            newKreuzungsname = zaehlung.getKnotenarme().stream()
                    .sorted(Comparator.comparingInt(KnotenarmEntity::getNummer))
                    .map(KnotenarmEntity::getStrassenname)
                    .distinct()
                    .collect(Collectors.joining(KREUZUNGSNAME_REPLACEMENT_NAME_DIVIDER));
        }
        return newKreuzungsname;
    }

}
