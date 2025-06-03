package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.configuration.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile({ "!konexternal && !prodexternal && !unittest" })
public class UnauffaelligeTageReceiver {

    private final UnauffaelligeTageService unauffaelligeTageService;

    /**
     * Die Methode aktualisiert regelmäßig die unauffälligen Tage.
     * Existiert für eine Messstelle ein Tagesaggregat,
     * so ist dieser Tag als unauffälliger Tag für diese Messstelle definiert.
     */
    @Scheduled(cron = "${dave.unauffaellige-tage.cron}")
    @SchedulerLock(
            name = "loadUnauffaelligeTageCron", lockAtMostFor = "${dave.unauffaellige-tage.shedlock}", lockAtLeastFor = "${dave.unauffaellige-tage.shedlock}"
    )
    @Transactional
    @LogExecutionTime
    public void loadUnauffaelligeTageCron() {
        LockAssert.assertLocked();
        log.info("#loadUnauffaelligeTage from MobidaM");
        try {
            // Daten aus MobidaM laden
            unauffaelligeTageService.loadAndSaveUnauffaelligeTageForEachMessstelle();
        } catch (final Exception exception) {
            log.error(exception.getMessage(), exception);
        }
    }
}
