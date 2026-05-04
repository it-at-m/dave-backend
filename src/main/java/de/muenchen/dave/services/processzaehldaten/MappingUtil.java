package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.Laengsverkehr;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.services.messstelle.RoundingService;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

@Slf4j
public class MappingUtil {

    private static final Integer VALUE_TO_ROUND = 100;

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
    public static Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> mapVerkehrsbeziehungen(final OptionsDTO options,
            final Zaehlung zaehlung, final List<Zeitintervall> zeitintervalle) {
        final Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> ladeZaehldatumBelastungsplan = zeitintervalle.stream()
                .filter(MappingUtil::isVerkehrsbeziehungNachOrKreisverkehrSet)
                .collect(Collectors.toMap(
                        // Schlüssel-Mapper
                        Zeitintervall::getVerkehrsbeziehung,
                        // Wert-Mapper
                        zeitintervall -> new ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum(
                                LadeZaehldatenService.isZeitintervallForTageswert(zeitintervall, options),
                                RoundingService.roundToNearestIfRoundingIsChoosen(
                                        LadeZaehldatenService.mapToZaehldatum(zeitintervall, zaehlung.getPkwEinheit(), options),
                                        VALUE_TO_ROUND,
                                        options))));
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
     *         die die Bedingung für Verkehrsbeziehungen erfüllen, werden berücksichtigt.
     */
    public static Map<Laengsverkehr, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> mapLaengsverkehre(final OptionsDTO options,
            final Zaehlung zaehlung, final List<Zeitintervall> zeitintervalle) {
        final Map<Laengsverkehr, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> ladeZaehldatumBelastungsplan = zeitintervalle.stream()
                .filter(MappingUtil::isLaengsverkehrKnotenarm)
                .sorted(Comparator.comparing(
                        (Zeitintervall z) -> z.getLaengsverkehr().getKnotenarm(),
                        Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toMap(
                        // Schlüssel-Mapper
                        Zeitintervall::getLaengsverkehr,
                        // Wert-Mapper
                        zeitintervall -> new ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum(
                                LadeZaehldatenService.isZeitintervallForTageswert(zeitintervall, options),
                                RoundingService.roundToNearestIfRoundingIsChoosen(
                                        LadeZaehldatenService.mapToZaehldatum(zeitintervall, zaehlung.getPkwEinheit(), options),
                                        VALUE_TO_ROUND,
                                        options)),
                        // Konflikt-Löser
                        (existing, replacement) -> {
                            log.error("Fehler beim Berechnen der Daten: doppelte Bewegungsbeziehungen");
                            throw new IllegalStateException("Fehler beim Berechnen der Daten");
                        },
                        // Map-Typ
                        LinkedHashMap::new));
        return ladeZaehldatumBelastungsplan;
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

}
