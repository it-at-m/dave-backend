/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.controller;

import de.muenchen.dave.geodateneai.gen.model.GetMesswerteOfMessquerschnittIntervallResponse;
import de.muenchen.dave.geodateneai.gen.model.GetMesswerteOfMessquerschnittTagesaggregatResponse;
import de.muenchen.dave.services.MesswerteMessquerschnittService;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/messwerte/messquerschnitt")
@AllArgsConstructor
public class MesswerteMessquerschnittController {

    private static final String REQUEST_PARAMETER_MESSSTELLE_ID = "messstelle_id";
    private static final String REQUEST_PARAMETER_VON = "von";
    private static final String REQUEST_PARAMETER_BIS = "bis";
    private static final String REQUEST_PARAMETER_TAGESTYP = "tages_typ";

    private final MesswerteMessquerschnittService messwerteMessquerschnittService;

    /**
     * Rest-Endpunkt zur Bereitstellung der Daten einer Messstelle für das Gangliniendiagramm, die
     * Listenausgabe und die Heatmap.
     *
     * @param messstelleId Die Id der angefragten Messstelle.
     * @param von required Zeitpunkt der Daten.
     * @param bis optional Ende eines Zeitraums.
     * @param tagestyp Typ des Tages
     * @return Die aufbereiteten Daten einer Messstelle für die Diagramme im Frontend.
     */
    @PostMapping(value = "intervall", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<GetMesswerteOfMessquerschnittIntervallResponse> ladeMesswerteIntervall(
            @RequestParam(value = REQUEST_PARAMETER_MESSSTELLE_ID) @NotEmpty final long messstelleId,
            @RequestParam(value = REQUEST_PARAMETER_VON) @NotEmpty final String von,
            @RequestParam(value = REQUEST_PARAMETER_BIS) @NotEmpty final String bis,
            @RequestParam(value = REQUEST_PARAMETER_TAGESTYP) final String tagestyp) {
        log.info("ladeMesswerteIntervall für Messstelle {} aufgerufen", messstelleId);
        final GetMesswerteOfMessquerschnittIntervallResponse messwerte = messwerteMessquerschnittService.ladeMesswerteIntervall(messstelleId, von, bis,
                tagestyp);
        log.info("laden der Daten abgeschlossen.");
        log.debug("MesswerteIntervall: {}", messwerte.toString());
        return ResponseEntity.ok(messwerte);
    }

    /**
     * Rest-Endpunkt zur Bereitstellung der Daten einer Messstelle für den Belastungsplan.
     *
     * @param messstelleId Die Id der angefragten Messstelle.
     * @param von required Zeitpunkt der Daten.
     * @param bis optional Ende eines Zeitraums.
     * @param tagestyp Typ des Tages
     * @return Die aufbereiteten Daten einer Messstelle für die Diagramme im Frontend.
     */
    @PostMapping(value = "tagesaggregat", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<GetMesswerteOfMessquerschnittTagesaggregatResponse> ladeMesswerteTagesaggregat(
            @RequestParam(value = REQUEST_PARAMETER_MESSSTELLE_ID) @NotEmpty final long messstelleId,
            @RequestParam(value = REQUEST_PARAMETER_VON) @NotEmpty final String von,
            @RequestParam(value = REQUEST_PARAMETER_BIS) @NotEmpty final String bis,
            @RequestParam(value = REQUEST_PARAMETER_TAGESTYP) final String tagestyp) {
        log.info("ladeMesswerteTagesaggregat für Messstelle {} aufgerufen", messstelleId);
        final GetMesswerteOfMessquerschnittTagesaggregatResponse messwerte = messwerteMessquerschnittService.ladeMesswerteTagesaggregat(messstelleId, von, bis,
                tagestyp);
        log.info("laden der Daten abgeschlossen.");
        log.debug("MesswerteTageaggregat: {}", messwerte.toString());
        return ResponseEntity.ok(messwerte);
    }
}
