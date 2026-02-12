package de.muenchen.dave.services.ladezaehldaten;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ZaehldatenExtractorService {

    private final ZeitintervallExtractorService zeitintervallExtractorService;

    private final ZaehldatenCalculatorService zaehldatenCalculatorService;

    private final SpitzenstundeCalculatorService spitzenstundeCalculatorService;

    public List<Zeitintervall> extractZeitintervalle(
            final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final Boolean isKreisverkehr,
            final OptionsDTO options,
            final Set<TypeZeitintervall> types) {




        return null;
    }




}
