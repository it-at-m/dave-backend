package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.configuration.LogExecutionTime;
import de.muenchen.dave.domain.Kalendertag;
import de.muenchen.dave.domain.UnauffaelligerTag;
import de.muenchen.dave.domain.mapper.detektor.MessstelleReceiverMapper;
import de.muenchen.dave.geodateneai.gen.api.MessstelleApi;
import de.muenchen.dave.geodateneai.gen.model.UnauffaelligerTagDto;
import de.muenchen.dave.repositories.relationaldb.KalendertagRepository;
import de.muenchen.dave.repositories.relationaldb.UnauffaelligeTageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.collections4.ListUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile({ "!konexternal && !prodexternal && !unittest" })
public class UnauffaelligeTageReceiver {

    private static final LocalDate EARLIEST_DAY = LocalDate.of(2006, 1, 1);

    private final UnauffaelligeTageRepository unauffaelligeTageRepository;

    private final KalendertagRepository kalendertagRepository;

    private final MessstelleReceiverMapper messstelleReceiverMapper;

    private final MessstelleApi messstelleApi;

    /**
     * Die Methode aktualisiert regelmäßig die unauffälligen Tage.
     *
     * Existiert für eine Messstelle ein Tagesaggregat,
     * so ist dieser Tag als unauffälliger Tag für diese Messstelle definiert.
     */
    @Scheduled(cron = "${dave.unauffaellige-tage.cron}")
    @SchedulerLock(
            name = "loadUnauffaelligeTageCron", lockAtMostFor = "${dave.unauffaellige-tage.shedlock}", lockAtLeastFor = "${dave.unauffaellige-tage.shedlock}"
    )
    @Transactional
    @LogExecutionTime
    public void loadMessstellenCron() {
        LockAssert.assertLocked();
        log.info("#loadUnauffaelligeTage from MobidaM");
        try {
            // Daten aus MobidaM laden
            loadAndSaveUnauffaelligeTageForEachMessstelle();
        } catch (final Exception exception) {
            log.error(exception.getMessage(), exception);
        }
    }

    /**
     * Die Methode ermittelt aus Mobidam die unauffälligen Tage und gibt diese als persistierbare
     * Entitäten zurück.
     *
     * @throws EntityNotFoundException falls kein {@link Kalendertag} für den
     *             unauffälligen Tag gefunden wurde.
     */
    protected void loadAndSaveUnauffaelligeTageForEachMessstelle() {
        final Optional<Kalendertag> byNextStartdayToLoadUnauffaelligeTageIsTrue = kalendertagRepository.findByNextStartdayToLoadUnauffaelligeTageIsTrue();
        LocalDate dateToCheck = EARLIEST_DAY;
        if (byNextStartdayToLoadUnauffaelligeTageIsTrue.isPresent()) {
            dateToCheck = byNextStartdayToLoadUnauffaelligeTageIsTrue.get().getDatum();
        }
        final LocalDate today = LocalDate.now();
        final List<UnauffaelligerTag> unauffaelligeTage = Stream.iterate(dateToCheck, date -> date.isBefore(today), date -> date.plusDays(1))
                .parallel()
                .flatMap(dayToCheck -> ListUtils
                        .emptyIfNull(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(dayToCheck, dayToCheck).block().getBody()).stream())
                .map(this::mapDto2Entity)
                .toList();

        log.debug("Save {} unauffaellige Tage in DB", unauffaelligeTage.size());
        unauffaelligeTageRepository.saveAllAndFlush(unauffaelligeTage);
        log.debug("Saved {} unauffaellige Tage in DB", unauffaelligeTage.size());

        byNextStartdayToLoadUnauffaelligeTageIsTrue.ifPresent(kalendertag -> {
            kalendertag.setNextStartdayToLoadUnauffaelligeTage(null);
            kalendertagRepository.save(kalendertag);
        });
        kalendertagRepository.findByDatum(today).ifPresent(kalendertag -> {
            kalendertag.setNextStartdayToLoadUnauffaelligeTage(true);
            kalendertagRepository.saveAndFlush(kalendertag);
        });
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
