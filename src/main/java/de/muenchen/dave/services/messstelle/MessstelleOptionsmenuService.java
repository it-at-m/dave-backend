package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.configuration.CachingConfiguration;
import de.muenchen.dave.domain.Kalendertag;
import de.muenchen.dave.domain.UnauffaelligerTag;
import de.muenchen.dave.domain.dtos.messstelle.AuffaelligeTageDto;
import de.muenchen.dave.domain.dtos.messstelle.ValidateZeitraumAndTagestypForMessstelleDto;
import de.muenchen.dave.domain.dtos.messstelle.ValidatedZeitraumAndTagestypDto;
import de.muenchen.dave.services.KalendertagService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessstelleOptionsmenuService {
    private final UnauffaelligeTageService unauffaelligeTageService;
    private final KalendertagService kalendertagService;
    private final ValidierungService validierungService;

    @Cacheable(value = CachingConfiguration.AUFFAELLIGETAGE_FOR_MESSSTELLE, key = "{#p0}")
    public AuffaelligeTageDto getAuffaelligeTageForMessstelle(final String mstId) {
        log.debug("Zugriff auf #getAuffaelligeTageForMessstelle {}", mstId);
        final List<UnauffaelligerTag> unauffaelligeTageForMessstelle = unauffaelligeTageService.getUnauffaelligeTageForMessstelle(mstId);
        final List<LocalDate> unauffaelligeTage = unauffaelligeTageForMessstelle
                .stream()
                .map(unauffaelligerTag -> unauffaelligerTag.getKalendertag().getDatum())
                .toList();
        final List<Kalendertag> auffaelligeKalendertage = kalendertagService.getAllKalendertageWhereDatumNotInExcludedDatesAndDatumIsBeforeLatestDate(
                unauffaelligeTage,
                LocalDate.now());
        final List<LocalDate> auffaelligeTageList = auffaelligeKalendertage.stream().map(Kalendertag::getDatum).toList();
        final AuffaelligeTageDto auffaelligeTage = new AuffaelligeTageDto();
        auffaelligeTage.setAuffaelligeTage(auffaelligeTageList);
        return auffaelligeTage;
    }

    public ValidatedZeitraumAndTagestypDto isZeitraumAndTagestypValid(final ValidateZeitraumAndTagestypForMessstelleDto request) {
        final var response = new ValidatedZeitraumAndTagestypDto();
        response.setIsValid(validierungService.isZeitraumAndTagestypValid(request));
        return response;
    }
}
