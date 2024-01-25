package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.NichtPlausibleTageResponseDTO;
import de.muenchen.dave.services.TagesaggregatMessquerschnittService;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tagesaggregat")
@AllArgsConstructor
public class TagesaggregatMessquerschnittController {
    public static final String REQUEST_PARAM_MESSQUERSCHNITT_ID = "messquerschnittId";

    private final TagesaggregatMessquerschnittService tagesaggregatMessquerschnittService;
    @GetMapping("/nichtPlausibleTage")
    public NichtPlausibleTageResponseDTO getPlausibleTage(@RequestParam(value = REQUEST_PARAM_MESSQUERSCHNITT_ID) @NotEmpty String messquerschnittId) {
        return tagesaggregatMessquerschnittService.getNichtPlausibleDatenFromEai();
    }
}
