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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

@Slf4j
public final class MappingUtil {

    private static final Integer VALUE_TO_ROUND = 100;

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

            // Schlüssel bestimmen
            final Verkehrsbeziehung key = zeitintervall.getVerkehrsbeziehung();

            // Zwischenergebnisse in lokalen Variablen
            final boolean isTageswert = LadeZaehldatenService.isZeitintervallForTageswert(zeitintervall, options);
            final LadeZaehldatumDTO zaehldatum = LadeZaehldatenService.mapToZaehldatum(zeitintervall, zaehlung.getPkwEinheit(), options);
            final LadeZaehldatumDTO roundedZaehldatum = RoundingService.roundToNearestIfRoundingIsChosen(
                    zaehldatum,
                    VALUE_TO_ROUND,
                    options);

            log.debug("Mapping Zeitintervall -> key={}, isTageswert={}, zaehldatum={}, roundedZaehldatum={}",
                    key, isTageswert, zaehldatum, roundedZaehldatum);

            final ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum value = new ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum(
                    isTageswert, roundedZaehldatum);

            // Konflikt-Behandlung
            if (ladeZaehldatumBelastungsplan.containsKey(key)) {
                log.error("Fehler beim Berechnen der Daten: doppelte Bewegungsbeziehung für key={}", key);
                throw new IllegalStateException("Fehler beim Berechnen der Daten: doppelte Bewegungsbeziehung für key=" + key);
            }

            ladeZaehldatumBelastungsplan.put(key, value);
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
            final Laengsverkehr key = zi.getLaengsverkehr();
            try {
                final boolean isTageswert = LadeZaehldatenService.isZeitintervallForTageswert(zi, options);
                final LadeZaehldatumDTO mappedZaehldatum = LadeZaehldatenService.mapToZaehldatum(zi, zaehlung.getPkwEinheit(), options);
                final LadeZaehldatumDTO roundedZaehldatum = RoundingService.roundToNearestIfRoundingIsChosen(mappedZaehldatum, VALUE_TO_ROUND, options);

                final ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum value = new ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum(
                        isTageswert, roundedZaehldatum);

                if (ladeZaehldatumBelastungsplan.containsKey(key)) {
                    logDoppelteBewegungsbeziehung(key.getKnotenarm(), ladeZaehldatumBelastungsplan.get(key), value, zi);
                    throw new IllegalStateException("Fehler beim Berechnen der Daten: doppelte Bewegungsbeziehungen für Knotenarm "
                            + key.getKnotenarm());
                }

                ladeZaehldatumBelastungsplan.put(key, value);

                if (log.isDebugEnabled()) {
                    log.debug("Zuordnung: Knotenarm={}, isTageswert={}, zaehldatum(original)={}, zaehldatum(gerundet)={}, zeitintervall={}",
                            key.getKnotenarm(), isTageswert, mappedZaehldatum, roundedZaehldatum, zi);
                }
            } catch (Exception e) {
                log.error("Fehler beim Verarbeiten des Zeitintervalls {} (Knotenarm={}): {}",
                        zi, key != null ? key.getKnotenarm() : "null", e.getMessage(), e);
                throw e;
            }
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
            // Schlüssel bestimmen
            final Querungsverkehr key = zi.getQuerungsverkehr();
            try {
                // Zwischenergebnisse in lokalen Variablen
                final boolean isTageswert = LadeZaehldatenService.isZeitintervallForTageswert(zi, options);
                final LadeZaehldatumDTO mappedZaehldatum = LadeZaehldatenService.mapToZaehldatum(zi, zaehlung.getPkwEinheit(), options);
                final LadeZaehldatumDTO roundedZaehldatum = RoundingService.roundToNearestIfRoundingIsChosen(mappedZaehldatum, VALUE_TO_ROUND, options);

                final ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum value = new ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum(
                        isTageswert, roundedZaehldatum);

                // Konflikt-Behandlung
                if (ladeZaehldatumBelastungsplan.containsKey(key)) {
                    logDoppelteBewegungsbeziehung(key.getKnotenarm(), ladeZaehldatumBelastungsplan.get(key), value, zi);
                    throw new IllegalStateException("Fehler beim Berechnen der Daten: doppelte Bewegungsbeziehungen für Knotenarm "
                            + key.getKnotenarm());
                }

                ladeZaehldatumBelastungsplan.put(key, value);

                if (log.isDebugEnabled()) {
                    log.debug("Zuordnung: Knotenarm={}, isTageswert={}, zaehldatum(original)={}, zaehldatum(gerundet)={}, zeitintervall={}",
                            key.getKnotenarm(), isTageswert, mappedZaehldatum, roundedZaehldatum, zi);
                }
            } catch (Exception e) {
                log.error("Fehler beim Verarbeiten des Zeitintervalls {} (Knotenarm={}): {}",
                        zi, key != null ? key.getKnotenarm() : "null", e.getMessage(), e);
                throw e;
            }
        }

        return ladeZaehldatumBelastungsplan;
    }

    private static void logDoppelteBewegungsbeziehung(Integer knotenarm, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum existingValue, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum newValue, Zeitintervall zi) {
        log.error("Fehler beim Berechnen der Daten: doppelte Bewegungsbeziehungen für Knotenarm {}. " +
                        "Existierender Wert: {}, neuer Wert: {}, verursachendes Zeitintervall: {}",
                knotenarm,
                existingValue,
                newValue,
                zi);
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
