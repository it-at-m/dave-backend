package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.HochrechnungsfaktorDTO;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.HochrechnungsfaktorService;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/hochrechnungsfaktor")
public class HochrechnungsfaktorController {

    private final HochrechnungsfaktorService hochrechnungsfaktorService;

    public HochrechnungsfaktorController(final HochrechnungsfaktorService hochrechnungsfaktorService) {
        this.hochrechnungsfaktorService = hochrechnungsfaktorService;
    }

    @RequestMapping(value = "/save", method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    public ResponseEntity<HochrechnungsfaktorDTO> saveHochrechnungsfaktor(@RequestBody @NotNull final HochrechnungsfaktorDTO hochrechnungsfaktorDTO) {
        try {
            return ResponseEntity.ok(
                    hochrechnungsfaktorService.saveHochrechnungsfaktor(hochrechnungsfaktorDTO));
        } catch (final DataNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

    @GetMapping(value = "/getAll", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(
        "hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).ANWENDER.name(), " +
                "T(de.muenchen.dave.security.AuthoritiesEnum).POWERUSER.name(), " +
                "T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())"
    )
    public ResponseEntity<List<HochrechnungsfaktorDTO>> getAllHochrechnungsfaktoren() {
        try {
            return ResponseEntity.ok(
                    hochrechnungsfaktorService.getHochrechnungsfaktoren());
        } catch (final DataNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    public ResponseEntity<?> deleteHochrechnungsfaktor(@PathVariable(value = "id") final UUID id) {
        hochrechnungsfaktorService.deleteHochrechnungsfaktor(id);
        return ResponseEntity.noContent().build();
    }

}
