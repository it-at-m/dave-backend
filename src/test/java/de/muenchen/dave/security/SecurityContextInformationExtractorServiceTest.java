package de.muenchen.dave.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
class SecurityContextInformationExtractorServiceTest {

    @Mock
    private Environment environment;

    private SecurityContextInformationExtractorService service;

    private String tokenUserNameClaimKey;

    @BeforeEach
    void setUp() throws Exception {
        // Service mit gemocktem Environment erzeugen
        service = new SecurityContextInformationExtractorService(environment);

        // Reflexion verwenden, um den privaten TOKEN_USER_NAME Wert auszulesen.
        // Dadurch müssen wir nicht hartkodieren, wie der Claim heißt.
        final Field tokenField = SecurityContextInformationExtractorService.class.getDeclaredField("TOKEN_USER_NAME");
        tokenField.setAccessible(true);
        tokenUserNameClaimKey = (String) tokenField.get(null);
    }

    @AfterEach
    void tearDown() {
        // SecurityContext nach jedem Test bereinigen
        SecurityContextHolder.clearContext();
    }

/**
     * Testet den Weg, in dem ein Jwt im SecurityContext vorhanden ist
     * und der erwartete Username-Claim gesetzt ist -> sollte der Username zurückgegeben werden.
     */
    @Test
    void testGetAuthenticatedUsername_withJwtClaim_returnsUsername() {

        // Jwt mit dem erwarteten Claim erzeugen
        final Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of(tokenUserNameClaimKey, "max.mustermann"));

        // Authentication mit Jwt als Principal setzen
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(jwt, null, Collections.emptyList()));

        final String username = service.getAuthenticatedUsername();
        assertEquals("max.mustermann", username);
    }

    @Test
    void testGetAuthenticatedUsername_withoutAuthentication_returnsUnauthenticatedUser() {
        // Testet den Fall, dass keine Authentication im SecurityContext vorhanden ist
        // -> es muss der Platzhalter UNAUTHENTICATED_USER zurückgegeben werden.

        SecurityContextHolder.clearContext();

        final String username = service.getAuthenticatedUsername();
        assertEquals(SecurityContextInformationExtractorService.UNAUTHENTICATED_USER, username);
    }

    @Test
    void testGetUserName_principalNotJwt_returnsEmpty() {
        // Wenn das Principal-Objekt keine Jwt-Instanz ist, muss getUserName "" liefern.

        // Authentication mit einfachem String-Principal setzen
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("not-a-jwt", null, Collections.emptyList()));

        final String username = service.getUserName();
        assertEquals("", username);
    }

    @Test
    void testGetUserName_jwtWithoutMatchingClaim_returnsEmpty() {
        // Ein Jwt ist vorhanden, jedoch fehlt der erwartete Claim -> leerer String

        final Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of("claim-not-expected", "claim-not-expected") // keine Claims notwendig, es darf in Klasse JWT jedoch keine leere Map übergeben werden.
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(jwt, null, Collections.emptyList()));

        final String username = service.getUserName();
        assertEquals("", username);
    }

    @Test
    void testIsFachadmin_securityActivated_withRoleFachadmin_returnsTrue() {
        // Security ist aktiviert und der Nutzer hat die Rolle FACHADMIN -> true

        org.mockito.Mockito.when(environment.getActiveProfiles()).thenReturn(new String[] {});

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "principal",
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_FACHADMIN"))));

        assertTrue(service.isFachadmin());
    }

    @Test
    void testIsFachadmin_securityActivated_withoutRoleFachadmin_returnsFalse() {
        // Security ist aktiviert und der Nutzer hat nicht die Rolle FACHADMIN -> false

        org.mockito.Mockito.when(environment.getActiveProfiles()).thenReturn(new String[] {});

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "principal",
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));

        assertFalse(service.isFachadmin());
    }

    @Test
    void testIsFachadmin_securityDeactivated_returnsTrue() {
        // Wenn Security deaktiviert ist (Profil "no-security" aktiv), wird standardmäßig true zurückgegeben.

        org.mockito.Mockito.when(environment.getActiveProfiles()).thenReturn(new String[] { "no-security" });

        // Auch ohne Authorities -> sollte true sein
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("principal", null, Collections.emptyList()));

        assertTrue(service.isFachadmin());
    }

    @Test
    void testIsAnwender_securityActivated_noAdminOrPoweruser_returnsTrue() {
        // Security aktiviert und Nutzer hat weder FACHADMIN noch POWERUSER -> Anwender true

        org.mockito.Mockito.when(environment.getActiveProfiles()).thenReturn(new String[] {});

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("principal", null, Collections.emptyList()));

        assertTrue(service.isAnwender());
    }

    @Test
    void testIsAnwender_withPoweruser_returnsFalse() {
        // Nutzer hat ROLE_POWERUSER -> isAnwender muss false liefern

        org.mockito.Mockito.when(environment.getActiveProfiles()).thenReturn(new String[] {});

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "principal",
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_POWERUSER"))));

        assertFalse(service.isAnwender());
    }

    @Test
    void testIsAnwender_withFachadmin_returnsFalse() {
        // Nutzer hat ROLE_FACHADMIN -> isAnwender muss false liefern

        org.mockito.Mockito.when(environment.getActiveProfiles()).thenReturn(new String[] {});

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "principal",
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_FACHADMIN"))));

        assertFalse(service.isAnwender());
    }

    @Test
    void testIsAnwender_securityDeactivated_returnsTrue() {
        // Wenn Security deaktiviert ist (Profil "no-security" aktiv), wird standardmäßig true zurückgegeben.

        org.mockito.Mockito.when(environment.getActiveProfiles()).thenReturn(new String[] { "no-security" });

        // Auch ohne Authorities -> sollte true sein
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("principal", null, Collections.emptyList()));

        assertFalse(service.isAnwender());
    }

    @Test
    void testIsSecurityActivated_profilesContainNoSecurity_returnsFalse() {
        // Wenn das Profil "no-security" aktiv ist -> isSecurityActivated liefert false
        org.mockito.Mockito.when(environment.getActiveProfiles()).thenReturn(new String[] { "no-security" });
        assertFalse(service.isSecurityActivated());
    }

    @Test
    void testIsSecurityActivated_profilesContainNoSecurityAndOtherProfile_returnsFalse() {
        // Wenn das Profil "no-security" und ein weiteres Profil aktiv ist -> isSecurityActivated liefert false
        org.mockito.Mockito.when(environment.getActiveProfiles()).thenReturn(new String[] { "no-security", "dev" });
        assertFalse(service.isSecurityActivated());
    }

    @Test
    void testIsSecurityActivated_profilesDoNotContainNoSecurity_returnsTrue() {
        // Wenn das Profil "no-security" NICHT aktiv ist -> isSecurityActivated liefert true
        org.mockito.Mockito.when(environment.getActiveProfiles()).thenReturn(new String[] { "dev", "test" });
        assertTrue(service.isSecurityActivated());
    }

    @Test
    void testIsSecurityActivated_profilesDoNotContainProfiles_returnsTrue() {
        // Wenn das Profil "no-security" NICHT aktiv und kein andere Profil gesetzt ist -> isSecurityActivated liefert true
        org.mockito.Mockito.when(environment.getActiveProfiles()).thenReturn(new String[] {});
        assertTrue(service.isSecurityActivated());
    }
}
