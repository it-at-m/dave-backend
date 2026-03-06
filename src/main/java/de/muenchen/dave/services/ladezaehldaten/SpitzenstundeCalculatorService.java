package de.muenchen.dave.services.ladezaehldaten;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.domain.mapper.ZeitintervallMapper;
import de.muenchen.dave.util.dataimport.ZeitintervallGleitendeSpitzenstundeUtil;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpitzenstundeCalculatorService {

    private final ZeitintervallMapper zeitintervallMapper;

    /**
     * Ermittelt für die übergebenen Zeitintervalle die Spitzenstunde auf Basis der relevanten
     * {@link TypeZeitintervall}.
     *
     * Die Relevanz eines Intervalls ergibt sich aus den Typen dokumentiert in Methode
     * {@link SpitzenstundeCalculatorService#getZeitintervalleRelevantForCalculationOfSpitzenstunde(List, Set)}
     *
     * @param zaehlungId
     * @param zeitblock
     * @param zeitintervalle
     * @param types
     * @return die Spitzenstunden aus den übergebenen Zeitintervallen.
     */
    public List<Zeitintervall> calculateSpitzenstundeForGivenZeitintervalle(
            final UUID zaehlungId,
            final Zeitblock zeitblock,
            final List<Zeitintervall> zeitintervalle,
            final Set<TypeZeitintervall> types) {
        final var copyOfZeitintervalle = zeitintervallMapper.deepCopy(zeitintervalle);
        final List<Zeitintervall> forCalculationRelevantZeitintervalle = getZeitintervalleRelevantForCalculationOfSpitzenstunde(copyOfZeitintervalle, types);
        return ZeitintervallGleitendeSpitzenstundeUtil
                .getGleitendeSpitzenstunden(zaehlungId, zeitblock, forCalculationRelevantZeitintervalle, types)
                .stream()
                .filter(spitzenstunde -> types.contains(spitzenstunde.getType()))
                .toList();
    }

    /**
     * Extrahiert aus den gegebenen Zeitintervallen die für die Spitzenstundenermittlung relevanten
     * Intervalle.
     *
     * - Befindet sich im Parameter "types" unter anderem der Typ
     * {@link TypeZeitintervall#STUNDE_VIERTEL} so wird die Spitzenstunde auf Basis der
     * Viertelstundenintervalle ermittelt.
     * - Befindet sich im Parameter "types" unter anderem der Typ {@link TypeZeitintervall#STUNDE_HALB}
     * jedoch kein Intervall des Typs {@link TypeZeitintervall#STUNDE_VIERTEL} so wird die Spitzenstunde
     * auf Basis der Halbstundenintervalle ermittelt.
     * - Befindet sich im Parameter "types" unter anderem der Typ
     * {@link TypeZeitintervall#STUNDE_KOMPLETT} jedoch kein Intervall des Typs
     * {@link TypeZeitintervall#STUNDE_VIERTEL} und {@link TypeZeitintervall#STUNDE_HALB} so wird die
     * Spitzenstunde auf Basis der Stundenintervalle ermittelt.
     *
     * @param zeitintervalle
     * @param types
     * @return die relevanten Zeitintervalle zur Spitzenstundenermittlung.
     */
    protected List<Zeitintervall> getZeitintervalleRelevantForCalculationOfSpitzenstunde(
            final List<Zeitintervall> zeitintervalle,
            final Set<TypeZeitintervall> types) {
        final TypeZeitintervall relevantZeitintervalle;
        if (types.contains(TypeZeitintervall.STUNDE_VIERTEL)) {
            relevantZeitintervalle = TypeZeitintervall.STUNDE_VIERTEL;
        } else if (types.contains(TypeZeitintervall.STUNDE_HALB)) {
            relevantZeitintervalle = TypeZeitintervall.STUNDE_HALB;
        } else {
            relevantZeitintervalle = TypeZeitintervall.STUNDE_KOMPLETT;
        }
        return zeitintervalle.stream()
                .filter(zeitintervall -> relevantZeitintervalle.equals(zeitintervall.getType()))
                .toList();
    }

}
