package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.NichtPlausibleTageResponseDTO;
import de.muenchen.dave.geodateneai.gen.model.ChosenTagesTypValidDTO;
import de.muenchen.dave.services.MessstelleOptionsmenuService;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messstelleOptionsmenu")
@AllArgsConstructor
@Slf4j
public class MessstelleOptionsmenuController {
    public static final String REQUEST_PARAM_MESSSTELLE_ID = "messstelle_id";

    public static final String REQUEST_PARAM_START_DATE = "start_date";

    public static final String REQUEST_PARAM_END_DATE = "end_date";

    public static final String REQUEST_PARAM_TAGES_TYP = "tages_typ";

    private final MessstelleOptionsmenuService messstelleOptionsmenuService;

    @GetMapping("/nichtPlausibleTage")
    public ResponseEntity<NichtPlausibleTageResponseDTO> getPlausibleTage(@RequestParam(value = REQUEST_PARAM_MESSSTELLE_ID) @NotEmpty String messstelleId) {
        log.debug("#getPlausibleTage for MessstelleId {}", messstelleId);
        return ResponseEntity.ok(messstelleOptionsmenuService.getNichtPlausibleDatenFromEai(messstelleId));
    }

    @GetMapping("/validateTagesTyp")
    public ResponseEntity<ChosenTagesTypValidDTO> isTagesTypDataValid(@RequestParam(value = REQUEST_PARAM_START_DATE) String startDate,
            @RequestParam(value = REQUEST_PARAM_END_DATE) String endDate,
            @RequestParam(value = REQUEST_PARAM_TAGES_TYP) String tagesTyp) {
        final ChosenTagesTypValidDTO chosenTagesTypValidDTO = messstelleOptionsmenuService.isTagesTypValid(startDate, endDate, tagesTyp);
        return ResponseEntity.ok(chosenTagesTypValidDTO);
    }
}
