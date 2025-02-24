package de.muenchen.dave.services.lageplan;

import de.muenchen.dave.documentstorage.gen.api.LageplanApi;
import de.muenchen.dave.documentstorage.gen.model.LageplanDto;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class LageplanService {

    private static final String ERROR_MESSAGE = "Beim Laden des Lageplans ist ein Fehler aufgetreten";
    private final LageplanApi lageplanApi;

    /**
     * Lädt die Lageplan-Infos zur angegebenen Messstelle.
     */
    public LageplanDto ladeLageplan(final String mstId) {
        log.debug("#ladeLageplan {}", mstId);

        ResponseEntity<LageplanDto> response = lageplanApi.getLageplanWithHttpInfo(mstId).block();

        if (ObjectUtils.isEmpty(response) || ObjectUtils.isEmpty(response.getBody())) {
            log.error("Die Response beinhaltet keine Daten");
            throw new ResourceNotFoundException(ERROR_MESSAGE);
        }
        return response.getBody();
    }

    /**
     * Gibt die Info zurück, ob zur angegebenen Messstelle ein Lageplan gespeichert ist.
     */
    public Boolean lageplanVorhanden(final String mstId) {
        log.debug("#lageplanVorhanden {}", mstId);

        ResponseEntity<Boolean> response = lageplanApi.hasLageplanWithHttpInfo(mstId).block();

        if (ObjectUtils.isEmpty(response) || ObjectUtils.isEmpty(response.getBody())) {
            log.error("Die Response beinhaltet keine Daten");
            throw new ResourceNotFoundException(ERROR_MESSAGE);
        }
        return response.getBody();
    }

}
