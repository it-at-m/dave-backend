package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.NichtPlausibleTageResponseDTO;
import de.muenchen.dave.domain.dtos.ValidWochentageInPeriodEaiRequestDTO;
import de.muenchen.dave.geodateneai.gen.model.ChosenTagesTypValidDTO;
import de.muenchen.dave.geodateneai.gen.model.ChosenTagesTypValidRequestDto;
import de.muenchen.dave.geodateneai.gen.model.ValidWochentageInPeriodDto;
import de.muenchen.dave.services.MessstelleOptionsmenuService;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
@PreAuthorize("hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).ANWENDER.name(), " +
        "T(de.muenchen.dave.security.AuthoritiesEnum).POWERUSER.name())")
public class MessstelleOptionsmenuController {
    public static final String REQUEST_PARAM_MESSSTELLE_ID = "messstelle_id";

    private final MessstelleOptionsmenuService messstelleOptionsmenuService;

    @GetMapping("/nichtPlausibleTage")
    public ResponseEntity<NichtPlausibleTageResponseDTO> getPlausibleTage(@RequestParam(value = REQUEST_PARAM_MESSSTELLE_ID) @NotEmpty String messstelleId) {
        log.debug("#getPlausibleTage for MessstelleId {}", messstelleId);
        return ResponseEntity.ok(messstelleOptionsmenuService.getNichtPlausibleDatenFromEai(messstelleId));
    }

    @PostMapping("/validateTagesTyp")
    public ResponseEntity<ChosenTagesTypValidDTO> isTagesTypDataValid(@RequestBody @NotNull ChosenTagesTypValidRequestDto chosenTagesTypValidRequestDto) {
        final ChosenTagesTypValidDTO chosenTagesTypValidDTO = messstelleOptionsmenuService.isTagesTypValid(chosenTagesTypValidRequestDto);
        return ResponseEntity.ok(chosenTagesTypValidDTO);
    }

    @PostMapping("validWochentageInPeriod")
    public ResponseEntity<ValidWochentageInPeriodDto> getValidWochentageInPeriod(
            @RequestBody ValidWochentageInPeriodEaiRequestDTO validWochentageInPeriodRequestDto) {
        final ValidWochentageInPeriodDto validWochentageInPeriodDto = messstelleOptionsmenuService
                .getValidWochentageInPeriod(validWochentageInPeriodRequestDto);
        return ResponseEntity.ok(validWochentageInPeriodDto);
    }
}
