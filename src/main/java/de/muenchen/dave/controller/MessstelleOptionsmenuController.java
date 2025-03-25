package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.messstelle.AuffaelligeTageDTO;
import de.muenchen.dave.domain.dtos.messstelle.ValidateZeitraumAndTagestypForMessstelleDTO;
import de.muenchen.dave.domain.dtos.messstelle.ValidatedZeitraumAndTagestypDTO;
import de.muenchen.dave.services.messstelle.MessstelleOptionsmenuService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messstelle-optionsmenu")
@AllArgsConstructor
@Slf4j
@PreAuthorize(
    "hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).ANWENDER.name(), " +
            "T(de.muenchen.dave.security.AuthoritiesEnum).POWERUSER.name())"
)
public class MessstelleOptionsmenuController {
    public static final String REQUEST_PARAM_MESSSTELLE_ID = "mst_id";

    private final MessstelleOptionsmenuService messstelleOptionsmenuService;

    @GetMapping("/auffaellige-tage")
    public ResponseEntity<AuffaelligeTageDTO> getAuffaelligeTage(
            @RequestParam(value = REQUEST_PARAM_MESSSTELLE_ID) @NotNull final String mstId) {
        log.debug("#getAuffaelligeTage for MessstelleId {}", mstId);
        return ResponseEntity.ok(messstelleOptionsmenuService.getAuffaelligeTageForMessstelle(mstId));
    }

    @PostMapping("/validate-zeitraum-and-tagestyp")
    public ResponseEntity<ValidatedZeitraumAndTagestypDTO> validateZeitraumAndTagestyp(
            @Valid @RequestBody @NotNull final ValidateZeitraumAndTagestypForMessstelleDTO request) {
        log.debug("#validateZeitraumAndTagestyp for MessstelleId {}", request.getMstId());
        return ResponseEntity.ok(messstelleOptionsmenuService.isZeitraumAndTagestypValid(request));
    }
}
