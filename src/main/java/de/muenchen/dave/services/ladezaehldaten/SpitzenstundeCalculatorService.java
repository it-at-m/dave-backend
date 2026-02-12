package de.muenchen.dave.services.ladezaehldaten;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.mapper.ZeitintervallMapper;
import de.muenchen.dave.util.dataimport.ZeitintervallGleitendeSpitzenstundeUtilNg;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpitzenstundeCalculatorService {

    private final ZeitintervallMapper zeitintervallMapper;

    private List<Zeitintervall> calculateSpitzenstundeForGivenZeitintervalle(
            final UUID zaehlungId,
            final List<Zeitintervall> zeitintervalleWithoutSpitzenstunde,
            final Set<TypeZeitintervall> types) {
        final var copyOfZeitintervalle = zeitintervallMapper.deepCopy(zeitintervalleWithoutSpitzenstunde);
        final var forCalculationRelevantZeitintervalle = getCopyOfZeitintervalleRelevantForCalculationOfSpitzenstunde(copyOfZeitintervalle, types);
        final var gleitendeSpitzenstunden = ZeitintervallGleitendeSpitzenstundeUtilNg
                .getGleitendeSpitzenstunden(zaehlungId, forCalculationRelevantZeitintervalle, types)
                .stream()
                .filter(spitzenstunde -> types.contains(spitzenstunde.getType()))
                .toList();
        copyOfZeitintervalle.addAll(gleitendeSpitzenstunden);
        copyOfZeitintervalle.sort(Comparator.comparing(Zeitintervall::getSortingIndex));
        return copyOfZeitintervalle;
    }

    private List<Zeitintervall> getCopyOfZeitintervalleRelevantForCalculationOfSpitzenstunde(
            final List<Zeitintervall> zeitintervalleWithoutSpitzenstunde,
            final Set<TypeZeitintervall> types) {
        final TypeZeitintervall zeitintervallTypeForSpitzenstunde;
        if (types.contains(TypeZeitintervall.STUNDE_VIERTEL)) {
            zeitintervallTypeForSpitzenstunde = TypeZeitintervall.STUNDE_VIERTEL;
        } else if (types.contains(TypeZeitintervall.STUNDE_HALB)) {
            zeitintervallTypeForSpitzenstunde = TypeZeitintervall.STUNDE_HALB;
        } else {
            zeitintervallTypeForSpitzenstunde = TypeZeitintervall.STUNDE_KOMPLETT;
        }
        return zeitintervalleWithoutSpitzenstunde.stream()
                .filter(zeitintervall -> zeitintervallTypeForSpitzenstunde.equals(zeitintervall.getType()))
                .toList();
    }

}
