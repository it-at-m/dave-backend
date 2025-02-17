package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.configuration.LogExecutionTime;
import de.muenchen.dave.domain.UnauffaelligeTag;
import de.muenchen.dave.domain.mapper.detektor.MessstelleReceiverMapper;
import de.muenchen.dave.geodateneai.gen.api.MessstelleApi;
import de.muenchen.dave.repositories.relationaldb.UnauffaelligeTageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnauffaelligeTageService {

    private static final LocalDate EARLIEST_DAY = LocalDate.of(2006, 1, 1);

    private final UnauffaelligeTageRepository unauffaelligeTageRepository;

    private final MessstelleReceiverMapper messstelleReceiverMapper;

    private final MessstelleApi messstelleApi;

    /**
     * Die Methode aktualisiert regelmäßig die unauffälligen Tage.
     *
     * Existiert für eine Messstelle ein Tagesaggregat, so ist dieser Tag als unauffälliger Tag für
     * diese Messstelle defininert.
     */
    @Scheduled(cron = "${dave.messstelle.cron}")
    @SchedulerLock(name = "loadUnauffaelligeTage", lockAtMostFor = "${dave.messstelle.shedlock}", lockAtLeastFor = "${dave.messstelle.shedlock}")
    @Transactional
    @LogExecutionTime
    public void loadMessstellenCron() {
        LockAssert.assertLocked();
        log.info("#loadUnauffaelligeTage from MobidaM");
        // Daten aus MobidaM laden
        final var unauffaelligeTage = loadUnauffaelligeTageForEachMessstelle();
        unauffaelligeTageRepository.saveAllAndFlush(unauffaelligeTage);
    }

    protected List<UnauffaelligeTag> loadUnauffaelligeTageForEachMessstelle() {
        final var unaufaelligerTag = unauffaelligeTageRepository.findTopByOrderByDatumDesc();
        final LocalDate lastUnauffaelligerTag;
        if (unaufaelligerTag.isPresent()) {
            lastUnauffaelligerTag = unaufaelligerTag.get().getDatum();
        } else {
            lastUnauffaelligerTag = EARLIEST_DAY;
        }
        final LocalDate yesterday = LocalDate.now().minusDays(1);
        final var unauffaelligeTage = Objects
                .requireNonNull(messstelleApi.getUnauffaelligeTageForEachMessstelleWithHttpInfo(lastUnauffaelligerTag, yesterday).block().getBody());
        return unauffaelligeTage.stream().map(messstelleReceiverMapper::dto2Entity).toList();
    }

}
