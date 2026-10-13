package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.*;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.services.messstelle.RoundingService;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

@Slf4j
public final class MappingUtil {

    /**
     * Prevent accidental instantiation/subclassing
     */
    private MappingUtil() {
    }

    /**
     * Baut aus einer Liste von {@link Zeitintervall} eine Zuordnung (Map) von
     * {@link Verkehrsbeziehung} auf
     * {@link ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum}.
     * <p>
     *
     * @param options Konfigurations- und Optionsdaten, z. B. ob gerundet werden soll.
     * @param zaehlung Die Zählung, deren Einheit für die Umwandlung der Zeitintervalle verwendet wird.
     * @param zeitintervalle Die Liste der zu verarbeitenden Zeitintervalle.
     * @return Eine Map von {@link Verkehrsbeziehung} auf
     *         {@link ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum}. Nur
     *         Zeitintervalle,
     *         die die Bedingung für Verkehrsbeziehungen erfüllen, werden berücksichtigt.
     */
    public static Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> mapVerkehrsbeziehungen(
            final OptionsDTO options,
            final Zaehlung zaehlung,
            final List<Zeitintervall> zeitintervalle) {
        final Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> ladeZaehldatumBelastungsplan = new LinkedHashMap<>();

        for (Zeitintervall zeitintervall : zeitintervalle) {
            // Filter-Bedingung
            if (!isVerkehrsbeziehungNachOrKreisverkehrSet(zeitintervall)) {
                continue;
            }
            calculateZaehldatum(zeitintervall, options, zaehlung, ladeZaehldatumBelastungsplan, Zeitintervall::getVerkehrsbeziehung, null);
        }
        return ladeZaehldatumBelastungsplan;
    }

    /**
     * Baut aus einer Liste von {@link Zeitintervall} eine Zuordnung (Map) von
     * {@link Laengsverkehr} auf
     * {@link ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum}.
     * <p>
     *
     * @param options Konfigurations- und Optionsdaten, z. B. ob gerundet werden soll.
     * @param zaehlung Die Zählung, deren Einheit für die Umwandlung der Zeitintervalle verwendet wird.
     * @param zeitintervalle Die Liste der zu verarbeitenden Zeitintervalle.
     *
     * @return Eine Map von {@link Laengsverkehr} auf
     *         {@link ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum}. Nur
     *         Zeitintervalle,
     *         die die Bedingung für Längsverkehre erfüllen, werden berücksichtigt.
     */
    public static Map<Laengsverkehr, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> mapLaengsverkehre(
            final OptionsDTO options,
            final Zaehlung zaehlung,
            final List<Zeitintervall> zeitintervalle) {
        final List<Zeitintervall> laengsverkehrIntervalle = zeitintervalle.stream()
                .filter(MappingUtil::isLaengsverkehrKnotenarm)
                .sorted(Comparator.comparingInt(z -> z.getLaengsverkehr().getKnotenarm()))
                .toList();

        final Map<Laengsverkehr, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> ladeZaehldatumBelastungsplan = new LinkedHashMap<>();

        for (Zeitintervall zi : laengsverkehrIntervalle) {
            calculateZaehldatum(zi, options, zaehlung, ladeZaehldatumBelastungsplan, Zeitintervall::getLaengsverkehr, Laengsverkehr::getKnotenarm);
        }
        return ladeZaehldatumBelastungsplan;
    }

    /**
     * Baut aus einer Liste von {@link Zeitintervall} eine Zuordnung (Map) von
     * {@link Querungsverkehr} auf
     * {@link ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum}.
     * <p>
     *
     * @param options Konfigurations- und Optionsdaten, z. B. ob gerundet werden soll.
     * @param zaehlung Die Zählung, deren Einheit für die Umwandlung der Zeitintervalle verwendet wird.
     * @param zeitintervalle Die Liste der zu verarbeitenden Zeitintervalle.
     *
     * @return Eine Map von {@link Querungsverkehr} auf
     *         {@link ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum}. Nur
     *         Zeitintervalle,
     *         die die Bedingungen für Querungsverkehre erfüllen, werden berücksichtigt.
     */
    public static Map<Querungsverkehr, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> mapQuerungsverkehre(
            final OptionsDTO options,
            final Zaehlung zaehlung,
            final List<Zeitintervall> zeitintervalle) {
        final List<Zeitintervall> querungsverkehrIntervalle = zeitintervalle.stream()
                .filter(MappingUtil::isQuerungsverkehrKnotenarm)
                .sorted(Comparator.comparingInt(z -> z.getQuerungsverkehr().getKnotenarm()))
                .toList();

        final Map<Querungsverkehr, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> ladeZaehldatumBelastungsplan = new LinkedHashMap<>();

        for (Zeitintervall zi : querungsverkehrIntervalle) {
            calculateZaehldatum(zi, options, zaehlung, ladeZaehldatumBelastungsplan, Zeitintervall::getQuerungsverkehr, Querungsverkehr::getKnotenarm);
        }
        return ladeZaehldatumBelastungsplan;
    }

    /**
     * Berechnet für ein {@link Zeitintervall} das zugehörige {@link LadeZaehldatumDTO} und fügt es
     * unter der aus dem Zeitintervall extrahierten {@link Bewegungsbeziehung} in die übergebene Map
     * ein.
     * <p>
     * Bereits vorhandene Einträge für dieselbe Bewegungsbeziehung führen zu einer
     * {@link IllegalStateException}.
     *
     * @param zi Das zu verarbeitende {@link Zeitintervall}.
     * @param options Konfigurations- und Optionsdaten, z. B. ob gerundet werden soll.
     * @param zaehlung Die Zählung, deren Einheit für die Umwandlung der Zeitintervalle verwendet wird.
     * @param ladeZaehldatumBelastungsplan Die zu befüllende Map von {@link Bewegungsbeziehung} auf
     *            {@link de.muenchen.dave.services.processzaehldaten.ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum}.
     * @param keyExtractor Funktion zur Ermittlung der Bewegungsbeziehung aus dem Zeitintervall.
     * @param knotenarmExtractor Funktion zur Ermittlung des Knotenarms aus der
     *            {@link Bewegungsbeziehung} bei
     *            {@link Laengsverkehr} und {@link Querungsverkehr}.
     */
    private static <T extends Bewegungsbeziehung> void calculateZaehldatum(
            Zeitintervall zi,
            OptionsDTO options,
            Zaehlung zaehlung,
            Map<T, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> ladeZaehldatumBelastungsplan,
            Function<Zeitintervall, T> keyExtractor,
            Function<T, Integer> knotenarmExtractor) {
        // Schlüssel bestimmen
        final T key = keyExtractor.apply(zi);
        try {
            // Zwischenergebnisse in lokalen Variablen
            final boolean isTageswert = LadeZaehldatenService.isZeitintervallForTageswert(zi, options);
            final LadeZaehldatumDTO mappedZaehldatum = LadeZaehldatenService.mapToZaehldatum(zi, zaehlung.getPkwEinheit(), options);
            final LadeZaehldatumDTO roundedZaehldatum = RoundingService.roundToNearestIfRoundingIsChosen(mappedZaehldatum, options);

            final ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum value = new ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum(
                    isTageswert, roundedZaehldatum);

            // Konfliktbehandlung
            if (ladeZaehldatumBelastungsplan.containsKey(key)) {
                if (key instanceof Verkehrsbeziehung) {
                    log.error("Fehler beim Berechnen der Daten: doppelte Bewegungsbeziehung für key={}", key);
                    throw new IllegalStateException("Fehler beim Berechnen der Daten: doppelte Bewegungsbeziehung für key=" + key);
                } else {
                    log.error("Fehler beim Berechnen der Daten: doppelte Bewegungsbeziehungen für Knotenarm {}. " +
                            "Existierender Wert: {}, neuer Wert: {}, verursachendes Zeitintervall: {}",
                            knotenarmExtractor.apply(key), ladeZaehldatumBelastungsplan.get(key), value, zi);
                    throw new IllegalStateException("Fehler beim Berechnen der Daten: doppelte Bewegungsbeziehungen für Knotenarm "
                            + knotenarmExtractor.apply(key));
                }
            }

            ladeZaehldatumBelastungsplan.put(key, value);

            if (log.isDebugEnabled()) {
                if (key instanceof Verkehrsbeziehung) {
                    log.debug("Mapping Zeitintervall -> key={}, isTageswert={}, zaehldatum={}, roundedZaehldatum={}",
                            key, isTageswert, mappedZaehldatum, roundedZaehldatum);
                } else {
                    log.debug("Zuordnung: Knotenarm={}, isTageswert={}, zaehldatum(original)={}, zaehldatum(gerundet)={}, zeitintervall={}",
                            knotenarmExtractor.apply(key), isTageswert, mappedZaehldatum, roundedZaehldatum, zi);

                }
            }
        } catch (Exception e) {
            if (key instanceof Verkehrsbeziehung) {
                log.error("Fehler beim Verarbeiten des Zeitintervalls {} (Key={}): {}",
                        zi, key, e.getMessage(), e);
            } else {
                log.error("Fehler beim Verarbeiten des Zeitintervalls {} (Knotenarm={}): {}",
                        zi, key != null ? knotenarmExtractor.apply(key) : "null", e.getMessage(), e);
            }
            throw e;
        }
    }

    private static boolean isVerkehrsbeziehungNachOrKreisverkehrSet(final Zeitintervall zeitintervall) {
        return ObjectUtils.isNotEmpty(zeitintervall.getVerkehrsbeziehung())
                && (ObjectUtils.isNotEmpty(zeitintervall.getVerkehrsbeziehung().getNach())
                        || ObjectUtils.isNotEmpty(zeitintervall.getVerkehrsbeziehung().getFahrbewegungKreisverkehr()));
    }

    private static boolean isLaengsverkehrKnotenarm(final Zeitintervall zeitintervall) {
        return ObjectUtils.isNotEmpty(zeitintervall.getLaengsverkehr())
                && ObjectUtils.isNotEmpty(zeitintervall.getLaengsverkehr().getKnotenarm());
    }

    private static boolean isQuerungsverkehrKnotenarm(final Zeitintervall zeitintervall) {
        return ObjectUtils.isNotEmpty(zeitintervall.getQuerungsverkehr())
                && ObjectUtils.isNotEmpty(zeitintervall.getQuerungsverkehr().getKnotenarm());
    }

}
