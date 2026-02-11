package de.muenchen.dave.services.ladezaehldaten;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.mapper.ZeitintervallMapper;
import de.muenchen.dave.util.dataimport.ZeitintervallGleitendeSpitzenstundeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpitzenstundeCalculatorService {

    private final ZeitintervallMapper zeitintervallMapper;

    private List<Zeitintervall> calculateSpitzenstundeForGivenZeitintervalle(final List<Zeitintervall> zeitintervalleWithoutSpitzenstunde, final OptionsDTO options) {
        final var copyOfZeitintervalle = zeitintervallMapper.deepCopy(zeitintervalleWithoutSpitzenstunde);
        final var forCalculationRelevantZeitintervalle = getCopyOfZeitintervalleRelevantForCalculationOfSpitzenstunde(copyOfZeitintervalle, options);
        final var gleitendeSpitzenstunden = ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstunden(forCalculationRelevantZeitintervalle);
        copyOfZeitintervalle.addAll(gleitendeSpitzenstunden);
        copyOfZeitintervalle.sort(Comparator.comparing(Zeitintervall::getSortingIndex));
        return copyOfZeitintervalle;
    }

    private List<Zeitintervall> getCopyOfZeitintervalleRelevantForCalculationOfSpitzenstunde(final List<Zeitintervall> zeitintervalleWithoutSpitzenstunde, final OptionsDTO options) {
        final var types = LadeZaehldatenService.getTypesAccordingChosenOptions(options);
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
