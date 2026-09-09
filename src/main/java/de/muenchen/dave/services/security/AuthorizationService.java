package de.muenchen.dave.services.security;

import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.security.SecurityContextInformationExtractor;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    private final ZaehlstelleIndexService indexService;

    public AuthorizationService(final ZaehlstelleIndexService indexService) {
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
     * Überprüft, ob der Nutzer die Rolle FACHADMIN hat oder ob seine Dienstleisterkennung mit der
     * Dienstleisterkennung der {@link Zaehlung} übereinstimmt.
     *
     * @param zaehlungId der {@link Zaehlung}
     * @param errorMessage für die {@link AccessDeniedException}
     * @throws AccessDeniedException wenn der Nutzer nicht FACHADMIN ist und die Dienstleisterkennung
     *             nicht übereinstimmt
     */
    private void assertCanAccessZaehlung(final String zaehlungId, final String errorMessage) throws DataNotFoundException, AccessDeniedException {
        if (SecurityContextInformationExtractor.isFachadmin()) {
            return;
        }
        if (!matchesDienstleisterkennung(zaehlungId)) {
            throw new AccessDeniedException(errorMessage);
        }
    }

    /**
     * Stellt sicher, dass der Nutzer berechtigt ist, eine {@link Zaehlung} zu bearbeiten.
     *
     * @param zaehlungId der {@link Zaehlung}, welche der Nutzer bearbeiten will
     * @throws AccessDeniedException wenn der Nutzer nicht berechtigt ist, die {@link Zaehlung} zu
     *             bearbeiten
     */
    public void assertCanModifyZaehlung(final String zaehlungId) throws DataNotFoundException, AccessDeniedException {
        assertCanAccessZaehlung(zaehlungId, "Der Dienstleister ist nicht berechtigt, diese Zählung zu ändern.");
    }

    /**
     * Stellt sicher, dass der Nutzer berechtigt ist, eine {@link de.muenchen.dave.domain.ChatMessage}
     * zu lesen oder zu senden.
     *
     * @param zaehlungId der {@link Zaehlung}, für welche der Nutzer eine
     *            {@link de.muenchen.dave.domain.ChatMessage} lesen/senden will
     * @throws AccessDeniedException wenn der Nutzer nicht berechtigt ist, eine
     *             {@link de.muenchen.dave.domain.ChatMessage} für
     *             die {@link Zaehlung} zu lesen/senden
     */
    public void assertCanReadAndWriteMessagesForZaehlung(final String zaehlungId) throws DataNotFoundException, AccessDeniedException {
        assertCanAccessZaehlung(zaehlungId, "Der Dienstleister ist nicht berechtigt, Nachrichten für diese Zählung zu lesen oder zu senden.");
    }
}
