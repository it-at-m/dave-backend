package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.AuffaelligeTageDTO;
import de.muenchen.dave.domain.dtos.ChosenTageValidResponseDTO;
import de.muenchen.dave.domain.dtos.ChosenTagesTypValidEaiRequestDTO;
import de.muenchen.dave.services.MessstelleOptionsmenuService;
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
@RequestMapping("/messstelleOptionsmenu")
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
            @RequestParam(value = REQUEST_PARAM_MESSSTELLE_ID) @NotNull final Integer mstId) {
        log.debug("#getAuffaelligeTage for MessstelleId {}", mstId);
        return ResponseEntity.ok(messstelleOptionsmenuService.getAuffaelligeTageForMessstelle(mstId));
    }

    @PostMapping("/validateTagesTyp")
    public ResponseEntity<ChosenTageValidResponseDTO> isTagesTypDataValid(
            @RequestBody @NotNull ChosenTagesTypValidEaiRequestDTO chosenTagesTypValidEaiRequestDTO) {
        final ChosenTageValidResponseDTO chosenTageValidResponseDTO = messstelleOptionsmenuService.isTagesTypValid(chosenTagesTypValidEaiRequestDTO);
        return ResponseEntity.ok(chosenTageValidResponseDTO);
    }
}
