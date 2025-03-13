package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.Kalendertag;
import de.muenchen.dave.domain.UnauffaelligerTag;
import de.muenchen.dave.domain.dtos.messstelle.AuffaelligeTageDTO;
import de.muenchen.dave.domain.dtos.messstelle.ValidateZeitraumAndTagestypForMessstelleDTO;
import de.muenchen.dave.domain.dtos.messstelle.ValidatedZeitraumAndTagestypDTO;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.services.KalendertagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessstelleOptionsmenuService {
    private final UnauffaelligeTageService unauffaelligeTageService;
    private final KalendertagService kalendertagService;

    public AuffaelligeTageDTO getAuffaelligeTageForMessstelle(final String mstId) {
        final List<UnauffaelligerTag> unauffaelligeTageForMessstelle = unauffaelligeTageService.getUnauffaelligeTageForMessstelle(mstId);
        final List<LocalDate> unauffaelligeTage = unauffaelligeTageForMessstelle
                .stream()
                .map(unauffaelligerTag -> unauffaelligerTag.getKalendertag().getDatum())
                .toList();
        final List<Kalendertag> auffaelligeKalendertage = kalendertagService.getAllKalendertageWhereDatumNotInExcludedDatesAndDatumIsBeforeLatestDate(
                unauffaelligeTage,
                LocalDate.now());
        final List<LocalDate> auffaelligeTage = auffaelligeKalendertage.stream().map(Kalendertag::getDatum).toList();
        final AuffaelligeTageDTO auffaelligeTageDTO = new AuffaelligeTageDTO();
        auffaelligeTageDTO.setAuffaelligeTage(auffaelligeTage);
        return auffaelligeTageDTO;
    }

    public ValidatedZeitraumAndTagestypDTO isZeitraumAndTagestypValid(final ValidateZeitraumAndTagestypForMessstelleDTO request) {
        final var tagestypen = TagesTyp.getIncludedTagestypen(request.getTagesTyp());
        final long numberOfRelevantKalendertage = kalendertagService.countAllKalendertageByDatumGreaterThanEqualAndDatumLessThanAndTagestypIn(
                request.getZeitraum().getFirst(),
                request.getZeitraum().getLast(), tagestypen);

        final long numberOfUnauffaelligeTage = unauffaelligeTageService
                .countAllUnauffaelligetageByMstIdAndKalendertagDatumGreaterThanEqualAndKalendertagDatumLessThanAndTagestypIn(request.getMstId(),
                        request.getZeitraum().getFirst(), request.getZeitraum().getLast(), tagestypen);

        boolean isValid = hasMinimuOfTwoUnauffaelligeTage(numberOfUnauffaelligeTage)
                && hasMinimuOfFiftyPercentUnauffaelligeTage(numberOfUnauffaelligeTage, numberOfRelevantKalendertage);

        return new ValidatedZeitraumAndTagestypDTO(isValid);
    }

    protected boolean hasMinimuOfTwoUnauffaelligeTage(final long numberOfUnauffaelligeTage) {
        return numberOfUnauffaelligeTage >= 2;
    }

    protected boolean hasMinimuOfFiftyPercentUnauffaelligeTage(final long numberOfUnauffaelligeTage, final long numberOfRelevantKalendertage) {
        return BigDecimal.valueOf(numberOfUnauffaelligeTage).multiply(BigDecimal.valueOf(numberOfRelevantKalendertage)).scaleByPowerOfTen(-2)
                .doubleValue() >= 0.5;
    }
}
