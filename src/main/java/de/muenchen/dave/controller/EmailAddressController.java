package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.EmailAddressDTO;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.services.email.EmailAddressService;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/email-address")
public class EmailAddressController {

    private final EmailAddressService emailService;

    public EmailAddressController(final EmailAddressService emailService) {
        this.emailService = emailService;
    }

    /**
     * Rest-Endpunkt für das Speichern einer Email-Adresse (für das Admin-Portal).
     *
     * @param emailAddress Die zu speichernde Email-Adresse
     * @return Die gespeicherte Email-Adresse
     */
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    @RequestMapping(value = "/save", method = { RequestMethod.POST, RequestMethod.PUT }, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmailAddressDTO> saveEmailAddress(@RequestBody @NotNull final EmailAddressDTO emailAddress) {
        log.debug("Email-Adresse erneuern: {}", emailAddress.getEmailAddress());
        try {
            return ResponseEntity.ok(this.emailService.saveOrUpdateEmailAddress(emailAddress));
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im EmailAddressController beim Erneuern der Email-Adresse: {}", emailAddress, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Erneuern der Email-Adresse aufgetreten.");
        }
    }

    /**
     * Rest-Endpunkt für das Laden von Email-Adressen (für das Admin-Portal).
     *
     * @return Die Email-Adresse
     */
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    @GetMapping(value = "/emailAddresses", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<List<EmailAddressDTO>> getEmailAddresses() {
        log.debug("Lade die Email-Adresseen");
        try {
            return ResponseEntity.ok(this.emailService.loadEmailAddresses());
        } catch (final ResourceNotFoundException e) {
            log.error("Fehler im EmailAddressController, Email-Addressen konnte nicht gealaden werden.", e);
            throw e;
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im EmailAddressController beim Laden der Email-Adressen", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Laden der Email-Adressen aufgetreten.");
        }
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole(T(de.muenchen.dave.security.AuthoritiesEnum).FACHADMIN.name())")
    public ResponseEntity<?> deleteEmailaddress(@PathVariable(value = "id") final UUID id) {
        log.debug("E-Mail-Adresse löschen.");
        try {
            this.emailService.deleteEmailAddress(id);
            return ResponseEntity.noContent().build();
        } catch (final Exception e) {
            log.error("Unerwarteter Fehler im EmailAddressController beim Löschen der E-Mail-Adresse.", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Löschen des E-Mail-Adresse aufgetreten.");
        }
    }
}
