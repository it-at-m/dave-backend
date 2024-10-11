package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.laden.LadeAuswertungSpitzenstundeDTO;
import de.muenchen.dave.domain.dtos.laden.LadeAuswertungVisumDTO;
import de.muenchen.dave.domain.dtos.laden.LadeAuswertungZaehlstelleKoordinateDTO;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.exceptions.IncorrectZeitauswahlException;
import de.muenchen.dave.services.auswertung.AuswertungSpitzenstundeService;
import de.muenchen.dave.services.auswertung.AuswertungVisumService;
import de.muenchen.dave.services.auswertung.AuswertungZaehlstellenKoordinateService;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
 * Der Controller stellt alle Endpunkt zur Verfügung welche ausschließlich durch die EAI aufgerufen werden.
 */
@RestController
@Slf4j
@Validated
public class EaiController {

    private static final String REQUEST_PARAMETER_ZAEHLSTELLE_NUMMER = "zaehlstelle_nummer";

    private static final String REQUEST_PARAMETER_ZAEHLART = "zaehlart";

    private static final String REQUEST_PARAMETER_ZAEHLDATUM = "zaehldatum";

    private static final String REQUEST_PARAMETER_ZEITBLOCK = "zeitblock";

    private static final String REQUEST_PARAMETER_ZEITAUSWAHL = "zeitauswahl";

    private static final String REQUEST_PARAMETER_JAHR = "jahr";

    private static final String REQUEST_PARAMETER_MONAT = "monat";

    private final AuswertungSpitzenstundeService auswertungSpitzenstundeService;

    private final AuswertungZaehlstellenKoordinateService auswertungZaehlstellenKoordinateService;

    private final AuswertungVisumService auswertungVisumService;

    public EaiController(final AuswertungSpitzenstundeService auswertungSpitzenstundeService,
            final AuswertungZaehlstellenKoordinateService auswertungZaehlstellenKoordinateService,
            final AuswertungVisumService auswertungVisumService) {
        this.auswertungSpitzenstundeService = auswertungSpitzenstundeService;
        this.auswertungZaehlstellenKoordinateService = auswertungZaehlstellenKoordinateService;
        this.auswertungVisumService = auswertungVisumService;
    }

    /**
     * Rest-Endpunkt zur Bereitstellung der Spitzenstundenauswertung.
     *
     * @param zaehlstellenNummer der Zählstelle für welche die Zähung stattgefunden hat.
     * @param zaehlart           der Zählung.
     * @param zaehldatum         der Zählung.
     * @param zeitblock          der Zählung.
     * @param zeitauswahl        darf nur die Ausprägung {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_KFZ},
     *                           {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_RAD} oder {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_FUSS}
     *                           annehmen.
     * @return die Spitzenstundenauswertung.
     */
    @GetMapping(value = "/lade-auswertung-spitzenstunde", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<List<LadeAuswertungSpitzenstundeDTO>> ladeAuswertungSpitzenstunde(
            @RequestParam(value = REQUEST_PARAMETER_ZAEHLSTELLE_NUMMER) @NotEmpty final String zaehlstellenNummer,
            @RequestParam(value = REQUEST_PARAMETER_ZAEHLART) @NotNull final Zaehlart zaehlart,
            @RequestParam(value = REQUEST_PARAMETER_ZAEHLDATUM) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull final LocalDate zaehldatum,
            @RequestParam(value = REQUEST_PARAMETER_ZEITBLOCK) @NotNull final Zeitblock zeitblock,
            @RequestParam(value = REQUEST_PARAMETER_ZEITAUSWAHL) @NotEmpty final String zeitauswahl) {
        log.info("ladeAuswertungSpitzenstunde für Zaehlstellennummer {}, Zaehlart {}, Zaehldatum {}, Zeitblock {} und Zeitauswahl {} aufgerufen",
                zaehlstellenNummer,
                zaehlart,
                zaehldatum,
                zeitblock,
                zeitauswahl);
        try {
            final List<LadeAuswertungSpitzenstundeDTO> ladeAuswertungSpitzenstunden = auswertungSpitzenstundeService.getAuswertungSpitzenstunde(
                    zaehlstellenNummer,
                    zaehlart,
                    zaehldatum,
                    zeitblock,
                    zeitauswahl);
            log.info("Laden der AuswertungSpitzenstunde abgeschlossen.");
            return ResponseEntity.ok(ladeAuswertungSpitzenstunden);
        } catch (DataNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (IncorrectZeitauswahlException exception) {
            final String message = "Der Parameter Zeitauswahl darf nur folgende Ausprägungen besitzen: "
                    .concat(LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_KFZ).concat(", ")
                    .concat(LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_RAD).concat(", ")
                    .concat(LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_FUSS);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    /**
     * Rest-Endpunkt zum Bereitstellung aller Zählstellen samt deren Koordinaten.
     *
     * @return die {@link LadeAuswertungZaehlstelleKoordinateDTO} je vorhandener Zählstelle.
     */
    @GetMapping(value = "/lade-auswertung-zaehlstellen-koordinate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<List<LadeAuswertungZaehlstelleKoordinateDTO>> ladeAuswertungZaehlstellenKoordinate() {
        log.info("ladeAuswertungZaehlstellenKoordinate aufgerufen");
        final List<LadeAuswertungZaehlstelleKoordinateDTO> ladeAuswertungSpitzenstunden = auswertungZaehlstellenKoordinateService
                .getAuswertungZaehlstellenKoordinate();
        log.info("Laden der Auswertung der Zaehlstellenkoordinaten abgeschlossen.");
        return ResponseEntity.ok(ladeAuswertungSpitzenstunden);
    }

    /**
     * Rest-Endpunkt zum Bereitstellung aller Zählungen eines bestimmten Monatszeitraums für Visum.
     *
     * @param jahr  welches ausgewertet werden soll.
     * @param monat im jahr welches ausgewertet werden soll.
     * @return die {@link LadeAuswertungZaehlstelleKoordinateDTO} je vorhandener Zählstelle.
     */
    @GetMapping(value = "/lade-auswertung-visum", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<LadeAuswertungVisumDTO> ladeAuswertungVisum(@RequestParam(value = REQUEST_PARAMETER_JAHR) @NotNull final Integer jahr,
            @RequestParam(value = REQUEST_PARAMETER_MONAT) @NotNull @Min(1) @Max(12) final Integer monat) {
        log.info("ladeAuswertungVisum aufgerufen");
        final var auswertungVisum = auswertungVisumService.getAuswertungVisum(jahr, monat);
        log.info("Laden der Visum-Auswertung abgeschlossen.");
        return ResponseEntity.ok(auswertungVisum);
    }

}
