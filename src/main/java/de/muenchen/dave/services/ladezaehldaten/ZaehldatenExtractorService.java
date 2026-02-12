package de.muenchen.dave.services.ladezaehldaten;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehlart;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ZaehldatenExtractorService {

    private final ZeitintervallExtractorService zeitintervallExtractorService;

    private final ZaehldatenSummationService zaehldatenSummationService;

    private final SpitzenstundeCalculatorService spitzenstundeCalculatorService;

    public List<Zeitintervall> extractZeitintervalle(
            final UUID zaehlungId,
            final Zaehlart zaehlart,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final Boolean isKreisverkehr,
            final OptionsDTO options,
            final Set<TypeZeitintervall> types) {
        final var zeitintervalle = zeitintervallExtractorService.extractZeitintervalle(
                zaehlungId,
                zaehlart,
                startUhrzeit,
                endeUhrzeit,
                isKreisverkehr,
                options,
                types);

        return null;
    }

}
