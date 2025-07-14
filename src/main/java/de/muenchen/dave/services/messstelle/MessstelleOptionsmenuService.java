package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.Kalendertag;
import de.muenchen.dave.domain.UnauffaelligerTag;
import de.muenchen.dave.domain.dtos.messstelle.AuffaelligeTageDTO;
import de.muenchen.dave.domain.dtos.messstelle.ValidateZeitraumAndTagestypForMessstelleDTO;
import de.muenchen.dave.domain.dtos.messstelle.ValidatedZeitraumAndTagestypDTO;
import de.muenchen.dave.services.KalendertagService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessstelleOptionsmenuService {
    private final UnauffaelligeTageService unauffaelligeTageService;
    private final KalendertagService kalendertagService;
    private final ValidierungService validierungService;

    public AuffaelligeTageDTO getAuffaelligeTageForMessstelle(final String mstId) {
        final List<UnauffaelligerTag> unauffaelligeTageForMessstelle = unauffaelligeTageService.getUnauffaelligeTageForMessstelle(mstId);
        final List<LocalDate> unauffaelligeTage = unauffaelligeTageForMessstelle
                .stream()
                .map(unauffaelligerTag -> unauffaelligerTag.getKalendertag().getDatum())
                .toList();
        final List<Kalendertag> auffaelligeKalendertage = kalendertagService.getAllKalendertageWhereDatumNotInExcludedDatesAndDatumIsBeforeLatestDate(
                unauffaelligeTage,
                LocalDate.now());
        final List<LocalDate> auffaelligeTageList = auffaelligeKalendertage.stream().map(Kalendertag::getDatum).toList();
        final AuffaelligeTageDTO auffaelligeTage = new AuffaelligeTageDTO();
        auffaelligeTage.setAuffaelligeTage(auffaelligeTageList);
        return auffaelligeTage;
    }

    public ValidatedZeitraumAndTagestypDTO isZeitraumAndTagestypValid(final ValidateZeitraumAndTagestypForMessstelleDTO request) {
        final var response = new ValidatedZeitraumAndTagestypDTO();
        response.setIsValid(validierungService.isZeitraumAndTagestypValid(request));
        return response;
    }
}
