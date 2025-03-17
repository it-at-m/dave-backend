package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.messstelle.ValidateZeitraumAndTagestypForMessstelleDTO;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.domain.model.messstelle.ValidateZeitraumAndTagesTypForMessstelleModel;
import de.muenchen.dave.services.KalendertagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
@RequiredArgsConstructor
public class ValidierungService {
    private final UnauffaelligeTageService unauffaelligeTageService;
    private final KalendertagService kalendertagService;

    public boolean isZeitraumAndTagestypValid(final ValidateZeitraumAndTagesTypForMessstelleModel request) {
        final var tagestypen = TagesTyp.getIncludedTagestypen(request.getTagesTyp());
        final long numberOfRelevantKalendertage = kalendertagService.countAllKalendertageByDatumAndTagestypen(
                request.getZeitraum().getFirst(),
                request.getZeitraum().getLast(), tagestypen);

        final long numberOfUnauffaelligeTage = unauffaelligeTageService
                .countAllUnauffaelligetageByMstIdAndTimerangeAndTagestypen(request.getMstId(),
                        request.getZeitraum().getFirst(), request.getZeitraum().getLast(), tagestypen);

        return hasMinimuOfTwoUnauffaelligeTage(numberOfUnauffaelligeTage)
                && hasMinimuOfFiftyPercentUnauffaelligeTage(numberOfUnauffaelligeTage, numberOfRelevantKalendertage);
    }

    protected boolean hasMinimuOfTwoUnauffaelligeTage(final long numberOfUnauffaelligeTage) {
        return numberOfUnauffaelligeTage >= 2;
    }

    protected boolean hasMinimuOfFiftyPercentUnauffaelligeTage(final long numberOfUnauffaelligeTage, final long numberOfRelevantKalendertage) {
        return BigDecimal.valueOf(numberOfUnauffaelligeTage).divide(BigDecimal.valueOf(numberOfRelevantKalendertage), RoundingMode.HALF_UP)
                .doubleValue() >= 0.5;
    }
}
