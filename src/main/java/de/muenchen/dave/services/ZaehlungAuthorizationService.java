package de.muenchen.dave.services;

import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.security.SecurityContextInformationExtractor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class ZaehlungAuthorizationService {

    private final ZaehlstelleIndexService indexService;

    public ZaehlungAuthorizationService(final ZaehlstelleIndexService indexService) {
        this.indexService = indexService;
    }

    /**
     * Überprüft, ob der Username des eingeloggten Nutzers mit der Dienstleisterkennung der
     * {@link Zaehlung} übereinstimmt.
     *
     * @param zaehlungId der {@link Zaehlung}, welche bearbeitet werden soll
     * @return {@code true}, wenn Username und Dienstleisterkennung übereinstimmen
     */
    private boolean matchesDienstleisterkennung(String zaehlungId) throws DataNotFoundException {
        final Zaehlung zaehlung = this.indexService.getZaehlung(zaehlungId);
        final String dienstleisterkennung = zaehlung.getDienstleisterkennung();
        final String currentUser = SecurityContextInformationExtractor.getUserName();

        return currentUser != null && !currentUser.isBlank()
                && dienstleisterkennung != null && !dienstleisterkennung.isBlank()
                && currentUser.equals(dienstleisterkennung);
    }

    /**
     * Stellt sicher, dass der Nutzer berechtigt ist, eine {@link Zaehlung} zu bearbeiten.
     *
     * @param zaehlungId der {@link Zaehlung}, welche der Nutzer bearbeiten will
     * @throws DataNotFoundException wenn der Nutzer nicht berechtigt ist, die {@link Zaehlung} zu
*                  bearbeiten
     */
    public void assertCanModifyZaehlung(final String zaehlungId) throws DataNotFoundException {
        if (SecurityContextInformationExtractor.isFachadmin()) {
            return;
        }
        if (!matchesDienstleisterkennung(zaehlungId)) {
            throw new AccessDeniedException("Der Dienstleister ist nicht berechtigt, diese Zählung zu ändern.");
        }
    }
}
