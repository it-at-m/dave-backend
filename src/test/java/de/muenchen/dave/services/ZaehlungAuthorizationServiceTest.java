package de.muenchen.dave.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import de.muenchen.dave.TestUtils;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.exceptions.DataNotFoundException;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;

class ZaehlungAuthorizationServiceTest {

    @AfterEach
    void tearDown() {
        TestUtils.clearSecurityContext();
    }

    @Test
    void assertCanModifyZaehlung_asFachadmin_noException() throws DataNotFoundException {
        // Arrange
        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final ZaehlungAuthorizationService authorizationService = new ZaehlungAuthorizationService(mockIndexService);

        final String id = "zf1";
        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setId(id);
        zaehlung.setDatum(LocalDate.now().plusDays(2));
        zaehlung.setDienstleisterkennung("dl1");

        when(mockIndexService.getZaehlung(id)).thenReturn(zaehlung);

        // Setze einen Nutzer mit Fachadmin-Rolle
        TestUtils.setSecurityContext("fachadmin", true);

        // Act and Assert
        assertDoesNotThrow(() -> authorizationService.assertCanModifyZaehlung(id));
    }

    @Test
    void assertCanModifyZaehlung_asAuthorizedUser_noException() throws DataNotFoundException {
        // Arrange
        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final ZaehlungAuthorizationService authorizationService = new ZaehlungAuthorizationService(mockIndexService);

        final String id = "zf1";
        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setId(id);
        zaehlung.setDatum(LocalDate.now().plusDays(2));
        zaehlung.setDienstleisterkennung("dl1");

        when(mockIndexService.getZaehlung(id)).thenReturn(zaehlung);

        // Setze einen Nutzer mit der Dienstleisterkennung der Zählung ohne Fachadmin-Rolle
        TestUtils.setSecurityContext("dl1", false);

        // Act and Assert
        assertDoesNotThrow(() -> authorizationService.assertCanModifyZaehlung(id));
    }

    @Test
    void assertCanModifyZaehlung_asNotAuthorizedUser_throwsAccessDeniedException() throws DataNotFoundException {
        // Arrange
        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final ZaehlungAuthorizationService authorizationService = new ZaehlungAuthorizationService(mockIndexService);

        final String id = "zf1";
        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setId(id);
        zaehlung.setDatum(LocalDate.now().plusDays(2));
        zaehlung.setDienstleisterkennung("dl1");

        when(mockIndexService.getZaehlung(id)).thenReturn(zaehlung);

        // Setze einen Nutzer mit einem anderen Username als der Dienstleisterkennung der Zählung und ohne Rolle Fachadmin
        TestUtils.setSecurityContext("tester", false);

        // Act and Assert
        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> authorizationService.assertCanModifyZaehlung(id));
        assertEquals("Der Dienstleister ist nicht berechtigt, diese Zählung zu ändern.", ex.getMessage());
    }

    @Test
    void assertCanModifyZaehlung_withoutSecurityContext_throwsAccessDeniedException() throws DataNotFoundException {
        // Arrange
        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final ZaehlungAuthorizationService authorizationService = new ZaehlungAuthorizationService(mockIndexService);

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
