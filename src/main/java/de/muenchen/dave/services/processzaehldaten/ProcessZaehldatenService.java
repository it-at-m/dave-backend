/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.configuration.CachingConfiguration;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeProcessedZaehldatenDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenHeatmapDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenTableDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.util.ZaehldatenProcessingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class ProcessZaehldatenService {

    private final LadeZaehldatenService ladeZaehldatenService;

    private final ProcessZaehldatenSteplineService processZaehldatenSteplineService;

    private final ProcessZaehldatenHeatmapService processZaehldatenHeatmapService;

    private final ZaehlstelleIndexService indexService;

    public ProcessZaehldatenService(final LadeZaehldatenService ladeZaehldatenService,
            final ProcessZaehldatenSteplineService processZaehldatenSteplineService,
            final ProcessZaehldatenHeatmapService processZaehldatenHeatmapService,
            final ZaehlstelleIndexService indexService) {
        this.ladeZaehldatenService = ladeZaehldatenService;
        this.processZaehldatenSteplineService = processZaehldatenSteplineService;
        this.processZaehldatenHeatmapService = processZaehldatenHeatmapService;
        this.indexService = indexService;
    }

    /**
     * Diese Methode gibt die Zaehldaten in aufbereiter Form zur Darstellung in der Listenausgabe, in
     * der Heatmap und im Stepline-Diagramm zurück.
     * <p>
     * Die Aufbereitung der Zaehldaten wird so durchgeführt, damit die Daten im Frontend nur noch an die
     * entsprechende Komponente übergeben werden müssen.
     *
     * @param zaehlungId Die Id der Zaehlung.
     * @param options Die durch den User im Frontend gewählten Optionen.
     * @return Die aufbereiteten Zaehldaten zur Darstellung in der Listenausgabe, in der Heatmap und im
     *         Stepline-Diagramm.
     * @throws DataNotFoundException wenn keine Zaehldaten geladen werden konnte
     */
    @Cacheable(value = CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN, key = "{#p0, #p1}")
    public LadeProcessedZaehldatenDTO ladeProcessedZaehldaten(final String zaehlungId,
            final OptionsDTO options) throws DataNotFoundException {
        log.debug(String.format("Zugriff auf #ladeProcessedZaehldaten mit %s und %s", zaehlungId, options.toString()));

        log.debug("Lade Zaehldaten for Table");
        final LadeProcessedZaehldatenDTO processedZaehldaten = new LadeProcessedZaehldatenDTO();
        final LadeZaehldatenTableDTO ladeZaehldatenTable = ladeZaehldatenService.ladeZaehldaten(
                UUID.fromString(zaehlungId),
                options);
        processedZaehldaten.setZaehldatenTable(ladeZaehldatenTable);

        log.debug("Process Zaehldaten Stepline");
        processedZaehldaten.setZaehldatenStepline(
                processZaehldatenSteplineService.ladeProcessedZaehldatenStepline(
                        ladeZaehldatenTable,
                        options));

        log.debug("Process Zaehldaten Heatmap");
        final LadeZaehldatenHeatmapDTO ladeZaehldatenHeatmap = processZaehldatenHeatmapService.ladeProcessedZaehldatenHeatmap(
                ladeZaehldatenTable,
                options);
        processedZaehldaten.setZaehldatenHeatmap(ladeZaehldatenHeatmap);

        return processedZaehldaten;
    }

    /**
     * Liefert die Zählungskenngrößen einer Zählung. Hier sollen die Werte für KFZ, GV, SV, Rad und Fuss
     * (jeweils sofern vorhanden) für folgende Zeitblöcke
     * geholt werden:
     * - Spitzenstunden KFZ (sofern vorhanden)
     * - Spitzenstunden RAD (sofern vorhanden)
     * - Spitzenstunden FUSS (sofern vorhanden)
     * - Alle Zeitblöcke (0 - 6, 6 - 10, ...)
     * - Gesamt- (bei 24-Std-Zhlg) oder Tageswert
     *
     * @param zaehlung Die Zählung von der die Zählungskenngrößen geholt werden soll
     * @return LadeZaehldatenTableDTO mit den für die Zählungskenngrößen benötigten Werten
     * @throws DataNotFoundException Wenn die Zählung nicht geladen werden kann
     */
    public LadeZaehldatenTableDTO ladeZaehlungskenngroessen(final Zaehlung zaehlung) throws DataNotFoundException {
        log.debug("Lade Zaehlungskenngroessen");

        return ladeZaehldatenService.ladeZaehldaten(
                UUID.fromString(zaehlung.getId()),
                ZaehldatenProcessingUtil.createHardcodedOptions(zaehlung));
    }
}
