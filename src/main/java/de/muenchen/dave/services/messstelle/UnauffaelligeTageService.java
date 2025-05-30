package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.Kalendertag;
import de.muenchen.dave.domain.UnauffaelligerTag;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.domain.mapper.detektor.MessstelleReceiverMapper;
import de.muenchen.dave.geodateneai.gen.api.MessstelleApi;
import de.muenchen.dave.geodateneai.gen.model.UnauffaelligerTagDto;
import de.muenchen.dave.repositories.relationaldb.KalendertagRepository;
import de.muenchen.dave.repositories.relationaldb.UnauffaelligeTageRepository;
import jakarta.persistence.EntityNotFoundException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnauffaelligeTageService {

    private static final LocalDate EARLIEST_DAY = LocalDate.of(2006, 1, 1);

    private final UnauffaelligeTageRepository unauffaelligeTageRepository;

    private final KalendertagRepository kalendertagRepository;

    private final MessstelleReceiverMapper messstelleReceiverMapper;

    private final MessstelleApi messstelleApi;

    private final MessstelleService messstelleService;

    public List<UnauffaelligerTag> getUnauffaelligeTageForMessstelle(final String mstId) {
        return unauffaelligeTageRepository.findByMstId(mstId);
    }

    public Optional<UnauffaelligerTag> findFirstByMstIdOrderByKalendertagDatumDesc(final String mstId) {
        return unauffaelligeTageRepository.findFirstByMstIdOrderByKalendertagDatumDesc(mstId);
    }

    public long countAllUnauffaelligetageByMstIdAndTimerangeAndTagestypen(
            final String mstId,
            final LocalDate startDateIncluded,
            final LocalDate endDateIncluded,
            final List<TagesTyp> tagesTyp) {
        return unauffaelligeTageRepository.countAllByMstIdAndKalendertagDatumGreaterThanEqualAndKalendertagDatumLessThanEqualAndKalendertagTagestypIn(
                mstId,
                startDateIncluded,
                endDateIncluded,
                tagesTyp);
    }

    public void deleteAndReloadUnauffaelligerTagByDatum(final LocalDate dateToReload) {
        unauffaelligeTageRepository.deleteAllByKalendertagDatum(dateToReload);
        final List<UnauffaelligerTag> unauffaelligeTage = getUnauffaelligeTageForEachMessstelle(dateToReload).parallelStream()
                .map(this::mapDto2Entity)
                .toList();

        log.debug("Save {} unauffaellige Tage in DB", unauffaelligeTage.size());
        unauffaelligeTageRepository.saveAllAndFlush(unauffaelligeTage);
    }

    /**
     * Die Methode ermittelt aus Mobidam die unauffälligen Tage und gibt diese als persistierbare
     * Entitäten zurück.
     *
     * @throws EntityNotFoundException falls kein {@link Kalendertag} für den
     *             unauffälligen Tag gefunden wurde.
     */
    public void loadAndSaveUnauffaelligeTageForEachMessstelle() {
        final Optional<Kalendertag> nextStartDate = kalendertagRepository.findByNextStartDateToLoadUnauffaelligeTageIsTrue();
        LocalDate dateToCheck = EARLIEST_DAY;
        if (nextStartDate.isPresent()) {
            dateToCheck = nextStartDate.get().getDatum();
        }
        final LocalDate today = LocalDate.now();
        final List<UnauffaelligerTag> unauffaelligeTage = Stream.iterate(dateToCheck, date -> date.isBefore(today), date -> date.plusDays(1))
                .parallel()
                .flatMap(dayToCheck -> getUnauffaelligeTageForEachMessstelle(dayToCheck).stream())
                .map(this::mapDto2Entity)
                .peek(this::updateMessstelleWithUnauffaelligerTag)
                .toList();

        log.debug("Save {} unauffaellige Tage in DB", unauffaelligeTage.size());
        unauffaelligeTageRepository.saveAllAndFlush(unauffaelligeTage);

        nextStartDate.ifPresent(kalendertag -> {
            kalendertag.setNextStartDateToLoadUnauffaelligeTage(null);
            kalendertagRepository.save(kalendertag);
        });
        kalendertagRepository.findByDatum(today).ifPresent(kalendertag -> {
            kalendertag.setNextStartDateToLoadUnauffaelligeTage(true);
            kalendertagRepository.saveAndFlush(kalendertag);
        });
    }

    public List<UnauffaelligerTagDto> getUnauffaelligeTageForEachMessstelle(final LocalDate dayToCheck) {
        Optional<ResponseEntity<List<UnauffaelligerTagDto>>> optional = messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(dayToCheck,
                dayToCheck).blockOptional();
        if (optional.isPresent()) {
            return ListUtils.emptyIfNull(optional.get().getBody());
        } else {
            log.warn("Die Response von 'getUnauffaelligeTageForEachMessstelleWithHttpInfo({})' beinhaltet keine Daten", dayToCheck);
            return List.of();
        }
    }

    private void updateMessstelleWithUnauffaelligerTag(final UnauffaelligerTag unauffaelligerTag) {
        messstelleService.updateLetztePlausibleMessungOfMessstelle(unauffaelligerTag.getMstId(), unauffaelligerTag.getKalendertag().getDatum());
    }

    /**
     * Die Methode führt das Mapping des unauffälligen Tags vom DTO zur Entität durch.
     *
     * @param unauffaelligerTag als DTO.
     * @return die Entität des unauffälligen Tags mit der referenz zum Kalendertag.
     * @throws EntityNotFoundException falls kein {@link Kalendertag} für den
     *             unauffälligen Tag gefunden wurde.
     */
    protected UnauffaelligerTag mapDto2Entity(final UnauffaelligerTagDto unauffaelligerTag) {
        final var kalendertag = kalendertagRepository.findByDatum(unauffaelligerTag.getDatum())
                .orElseThrow(() -> {
                    final var message = MessageFormat.format(
                            "Kalendertag for date {0} not found",
                            unauffaelligerTag.getDatum() != null ? unauffaelligerTag.getDatum().toString() : null);
                    return new EntityNotFoundException(message);
                });
        return messstelleReceiverMapper.dto2Entity(unauffaelligerTag, kalendertag);
    }
}
