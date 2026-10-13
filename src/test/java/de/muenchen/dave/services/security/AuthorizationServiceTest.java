package de.muenchen.dave.services.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.security.SecurityContextInformationExtractorService;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;

class AuthorizationServiceTest {

    @Test
    void assertCanModifyZaehlung_asFachadmin_noException() throws DataNotFoundException {
        // Arrange
        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final SecurityContextInformationExtractorService mockSecService = Mockito.mock(SecurityContextInformationExtractorService.class);
        final AuthorizationService authorizationService = new AuthorizationService(mockIndexService, mockSecService);

        final String id = "zf1";
        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setId(id);
        zaehlung.setDatum(LocalDate.now().plusDays(2));
        zaehlung.setDienstleisterkennung("dl1");

        when(mockIndexService.getZaehlung(id)).thenReturn(zaehlung);

        // Setze einen Nutzer mit Fachadmin-Rolle
        when(mockSecService.isFachadmin()).thenReturn(true);

        // Act and Assert
        assertDoesNotThrow(() -> authorizationService.assertCanModifyZaehlung(id));
    }

    @Test
    void assertCanModifyZaehlung_asAuthorizedUser_noException() throws DataNotFoundException {
        // Arrange
        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final SecurityContextInformationExtractorService mockSecService = Mockito.mock(SecurityContextInformationExtractorService.class);
        final AuthorizationService authorizationService = new AuthorizationService(mockIndexService, mockSecService);

        final String id = "zf1";
        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setId(id);
        zaehlung.setDatum(LocalDate.now().plusDays(2));
        zaehlung.setDienstleisterkennung("dl1");

        when(mockIndexService.getZaehlung(id)).thenReturn(zaehlung);

        // Setze einen Nutzer mit der Dienstleisterkennung der Zählung ohne Fachadmin-Rolle
        when(mockSecService.getAuthenticatedUsername()).thenReturn("dl1");
        when(mockSecService.isFachadmin()).thenReturn(false);

        // Act and Assert
        assertDoesNotThrow(() -> authorizationService.assertCanModifyZaehlung(id));
    }

    @Test
    void assertCanModifyZaehlung_asNotAuthorizedUser_throwsAccessDeniedException() throws DataNotFoundException {
        // Arrange
        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final SecurityContextInformationExtractorService mockSecService = Mockito.mock(SecurityContextInformationExtractorService.class);
        final AuthorizationService authorizationService = new AuthorizationService(mockIndexService, mockSecService);

        final String id = "zf1";
        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setId(id);
        zaehlung.setDatum(LocalDate.now().plusDays(2));
        zaehlung.setDienstleisterkennung("dl1");

        when(mockIndexService.getZaehlung(id)).thenReturn(zaehlung);

        // Setze einen Nutzer mit einem anderen Username als der Dienstleisterkennung der Zählung und ohne Rolle Fachadmin
        when(mockSecService.getAuthenticatedUsername()).thenReturn("tester");
        when(mockSecService.isFachadmin()).thenReturn(false);

        // Act and Assert
        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> authorizationService.assertCanModifyZaehlung(id));
        assertEquals("Der Dienstleister ist nicht berechtigt, diese Zählung zu ändern.", ex.getMessage());
    }

    @Test
    void assertCanModifyZaehlung_withoutSecurityContext_throwsAccessDeniedException() throws DataNotFoundException {
        // Arrange
        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final SecurityContextInformationExtractorService mockSecService = Mockito.mock(SecurityContextInformationExtractorService.class);
        final AuthorizationService authorizationService = new AuthorizationService(mockIndexService, mockSecService);

        final String id = "zf1";
        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setId(id);
        zaehlung.setDatum(LocalDate.now().plusDays(2));
        zaehlung.setDienstleisterkennung("dl1");

        when(mockIndexService.getZaehlung(id)).thenReturn(zaehlung);

        // Keinen Security-Context setzen

        // Act and Assert
        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> authorizationService.assertCanModifyZaehlung(id));
        assertEquals("Der Dienstleister ist nicht berechtigt, diese Zählung zu ändern.", ex.getMessage());
    }
}
