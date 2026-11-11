package de.muenchen.dave.migrationespg.services.prozesszaehldaten;

import de.muenchen.dave.domain.Bewegungsbeziehung;
import de.muenchen.dave.domain.Laengsverkehr;
import de.muenchen.dave.domain.PkwEinheit;
import de.muenchen.dave.domain.Querungsverkehr;
import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.LaengsverkehrEntity;
import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.QuerungsverkehrEntity;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.migrationespg.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.migrationespg.services.ladezaehldaten.LadeZaehldatenServiceMig;
import de.muenchen.dave.migrationespg.services.messstelle.RoundingServiceMig;
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
     * {@link ProcessZaehldatenBelastungsplanServiceMig.TupelTageswertZaehldatum}.
     * <p>
     *
     * @param options Konfigurations- und Optionsdaten, z. B. ob gerundet werden soll.
     * @param pkwEinheit Die pkwEinheit, deren Einheit für die Umwandlung der Zeitintervalle verwendet wird.
     * @param zeitintervalle Die Liste der zu verarbeitenden Zeitintervalle.
     * @return Eine Map von {@link Verkehrsbeziehung} auf
     *         {@link ProcessZaehldatenBelastungsplanServiceMig.TupelTageswertZaehldatum}. Nur
     *         Zeitintervalle,
     *         die die Bedingung für Verkehrsbeziehungen erfüllen, werden berücksichtigt.
     */
    public static Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanServiceMig.TupelTageswertZaehldatum> mapVerkehrsbeziehungen(
            final OptionsDTO options,
            final PkwEinheit pkwEinheit,
            final List<Zeitintervall> zeitintervalle) {
        final Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanServiceMig.TupelTageswertZaehldatum> ladeZaehldatumBelastungsplan = new LinkedHashMap<>();

        for (Zeitintervall zeitintervall : zeitintervalle) {
            // Filter-Bedingung
            if (!isVerkehrsbeziehungNachOrKreisverkehrSet(zeitintervall)) {
                continue;
            }
            calculateZaehldatum(zeitintervall, options, pkwEinheit, ladeZaehldatumBelastungsplan, Zeitintervall::getVerkehrsbeziehung, null);
        }
        return ladeZaehldatumBelastungsplan;
    }

    /**
     * Baut aus einer Liste von {@link Zeitintervall} eine Zuordnung (Map) von
     * {@link LaengsverkehrEntity} auf
     * {@link ProcessZaehldatenBelastungsplanServiceMig.TupelTageswertZaehldatum}.
     * <p>
     *
     * @param options Konfigurations- und Optionsdaten, z. B. ob gerundet werden soll.
     * @param pkwEinheit Die pkwEinheit, deren Einheit für die Umwandlung der Zeitintervalle verwendet wird.
     * @param zeitintervalle Die Liste der zu verarbeitenden Zeitintervalle.
     *
     * @return Eine Map von {@link LaengsverkehrEntity} auf
     *         {@link ProcessZaehldatenBelastungsplanServiceMig.TupelTageswertZaehldatum}. Nur
     *         Zeitintervalle,
     *         die die Bedingung für Längsverkehre erfüllen, werden berücksichtigt.
     */
    public static Map<Laengsverkehr, ProcessZaehldatenBelastungsplanServiceMig.TupelTageswertZaehldatum> mapLaengsverkehre(
            final OptionsDTO options,
            final PkwEinheit pkwEinheit,
            final List<Zeitintervall> zeitintervalle) {
        final List<Zeitintervall> laengsverkehrIntervalle = zeitintervalle.stream()
                .filter(MappingUtil::isLaengsverkehrKnotenarm)
                .sorted(Comparator.comparingInt(z -> z.getLaengsverkehr().getKnotenarm()))
                .toList();

        final Map<Laengsverkehr, ProcessZaehldatenBelastungsplanServiceMig.TupelTageswertZaehldatum> ladeZaehldatumBelastungsplan = new LinkedHashMap<>();

        for (Zeitintervall zi : laengsverkehrIntervalle) {
            calculateZaehldatum(zi, options, pkwEinheit, ladeZaehldatumBelastungsplan, Zeitintervall::getLaengsverkehr, Laengsverkehr::getKnotenarm);
        }
        return ladeZaehldatumBelastungsplan;
    }

    /**
     * Baut aus einer Liste von {@link Zeitintervall} eine Zuordnung (Map) von
     * {@link QuerungsverkehrEntity} auf
     * {@link ProcessZaehldatenBelastungsplanServiceMig.TupelTageswertZaehldatum}.
     * <p>
     *
     * @param options Konfigurations- und Optionsdaten, z. B. ob gerundet werden soll.
     * @param pkwEinheit Die PkwEinheit, deren Einheit für die Umwandlung der Zeitintervalle verwendet wird.
     * @param zeitintervalle Die Liste der zu verarbeitenden Zeitintervalle.
     *
     * @return Eine Map von {@link QuerungsverkehrEntity} auf
     *         {@link ProcessZaehldatenBelastungsplanServiceMig.TupelTageswertZaehldatum}. Nur
     *         Zeitintervalle,
     *         die die Bedingungen für Querungsverkehre erfüllen, werden berücksichtigt.
     */
    public static Map<Querungsverkehr, ProcessZaehldatenBelastungsplanServiceMig.TupelTageswertZaehldatum> mapQuerungsverkehre(
            final OptionsDTO options,
            final PkwEinheit pkwEinheit,
            final List<Zeitintervall> zeitintervalle) {
        final List<Zeitintervall> querungsverkehrIntervalle = zeitintervalle.stream()
                .filter(MappingUtil::isQuerungsverkehrKnotenarm)
                .sorted(Comparator.comparingInt(z -> z.getQuerungsverkehr().getKnotenarm()))
                .toList();

        final Map<Querungsverkehr, ProcessZaehldatenBelastungsplanServiceMig.TupelTageswertZaehldatum> ladeZaehldatumBelastungsplan = new LinkedHashMap<>();

        for (Zeitintervall zi : querungsverkehrIntervalle) {
            calculateZaehldatum(zi, options, pkwEinheit, ladeZaehldatumBelastungsplan, Zeitintervall::getQuerungsverkehr, Querungsverkehr::getKnotenarm);
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
     * @param pkwEinheit Die Zählung, deren Einheit für die Umwandlung der Zeitintervalle verwendet wird.
     * @param ladeZaehldatumBelastungsplan Die zu befüllende Map von {@link Bewegungsbeziehung} auf
     *            {@link ProcessZaehldatenBelastungsplanServiceMig.TupelTageswertZaehldatum}.
     * @param keyExtractor Funktion zur Ermittlung der Bewegungsbeziehung aus dem Zeitintervall.
     * @param knotenarmExtractor Funktion zur Ermittlung des Knotenarms aus der
     *            {@link Bewegungsbeziehung} bei
     *            {@link LaengsverkehrEntity} und {@link QuerungsverkehrEntity}.
     */
    private static <T extends Bewegungsbeziehung> void calculateZaehldatum(
            Zeitintervall zi,
            OptionsDTO options,
            PkwEinheit pkwEinheit,
            Map<T, ProcessZaehldatenBelastungsplanServiceMig.TupelTageswertZaehldatum> ladeZaehldatumBelastungsplan,
            Function<Zeitintervall, T> keyExtractor,
            Function<T, Integer> knotenarmExtractor) {
        // Schlüssel bestimmen
        final T key = keyExtractor.apply(zi);
        try {
            // Zwischenergebnisse in lokalen Variablen
            final boolean isTageswert = LadeZaehldatenServiceMig.isZeitintervallForTageswert(zi, options);
            final LadeZaehldatumDTO mappedZaehldatum = LadeZaehldatenServiceMig.mapToZaehldatum(zi, pkwEinheit, options);
            final LadeZaehldatumDTO roundedZaehldatum = RoundingServiceMig.roundToNearestIfRoundingIsChosen(mappedZaehldatum, options);

            final ProcessZaehldatenBelastungsplanServiceMig.TupelTageswertZaehldatum value = new ProcessZaehldatenBelastungsplanServiceMig.TupelTageswertZaehldatum(
                    isTageswert, roundedZaehldatum);

            // Konfliktbehandlung
            if (ladeZaehldatumBelastungsplan.containsKey(key)) {
                if (key instanceof Verkehrsbeziehung) {
                    log.error("Fehler beim Berechnen der Daten: doppelte Bewegungsbeziehung für key={}", key);
                    throw new IllegalStateException("Fehler beim Berechnen der Daten: doppelte Bewegungsbeziehung für key=" + key);
                } else {
                    log.error("Fehler beim Berechnen der Daten: doppelte Bewegungsbeziehungen für KnotenarmEntity {}. " +
                            "Existierender Wert: {}, neuer Wert: {}, verursachendes Zeitintervall: {}",
                            knotenarmExtractor.apply(key), ladeZaehldatumBelastungsplan.get(key), value, zi);
                    throw new IllegalStateException("Fehler beim Berechnen der Daten: doppelte Bewegungsbeziehungen für KnotenarmEntity "
                            + knotenarmExtractor.apply(key));
                }
            }

            ladeZaehldatumBelastungsplan.put(key, value);

            if (log.isDebugEnabled()) {
                if (key instanceof Verkehrsbeziehung) {
                    log.debug("Mapping Zeitintervall -> key={}, isTageswert={}, zaehldatum={}, roundedZaehldatum={}",
                            key, isTageswert, mappedZaehldatum, roundedZaehldatum);
                } else {
                    log.debug("Zuordnung: KnotenarmEntity={}, isTageswert={}, zaehldatum(original)={}, zaehldatum(gerundet)={}, zeitintervall={}",
                            knotenarmExtractor.apply(key), isTageswert, mappedZaehldatum, roundedZaehldatum, zi);

                }
            }
        } catch (Exception e) {
            if (key instanceof Verkehrsbeziehung) {
                log.error("Fehler beim Verarbeiten des Zeitintervalls {} (Key={}): {}",
                        zi, key, e.getMessage(), e);
            } else {
                log.error("Fehler beim Verarbeiten des Zeitintervalls {} (KnotenarmEntity={}): {}",
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
