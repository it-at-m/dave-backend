package de.muenchen.dave.security;

import java.util.Arrays;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SecurityContextInformationExtractorService {

    public static final String UNAUTHENTICATED_USER = "unauthenticated";

    private static final String TOKEN_USER_NAME = UserInfoDataService.CLAIM_USERNAME;

    private final Environment environment;

    /**
     * Extrahiert den Benutzernamen aus der {@link BearerTokenAuthentication}.
     *
     * @return Den Benutzernamen oder einen Platzhalter, falls keine {@link BearerTokenAuthentication}
     *         vorhanden ist.
     */
    public String getAuthenticatedUsername() {
        final var username = getUserName();
        return StringUtils.isNotBlank(username) ? username : UNAUTHENTICATED_USER;
    }

    /**
     * Prüft, ob der aktuell angemeldete Nutzer die Rolle ROLE_FACHADMIN besitzt.
     *
     * Wenn Security deaktiviert ist (Spring-Profil "no-security" aktiv), gibt die Methode
     * standardmäßig true zurück.
     *
     * Die Methode liest die aktuelle Authentication aus dem SecurityContext und prüft die Authorities.
     *
     * @return true, falls die Security deaktiviert ist oder der Nutzer die Rolle ROLE_FACHADMIN hat;
     *         andernfalls false
     */
    public boolean isFachadmin() {
        log.debug("get isFachadmin");
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var isFachadmin = true;
        if (isSecurityActivated()) {
            isFachadmin = CollectionUtils
                    .emptyIfNull(authentication.getAuthorities())
                    .stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + AuthoritiesEnum.FACHADMIN.name()));
        }
        return isFachadmin;
    }

    /**
     * Liest den Benutzernamen aus dem JWT im aktuellen SecurityContext.
     *
     * Die Methode prüft, ob eine Authentication vorhanden ist und ob das Principal
     * eine Jwt-Instanz ist. Falls ja, wird der Claim hinterlegt in der Konstante TOKEN_USER_NAME
     * aus dem Jwt gelesen und zurückgegeben.
     * Falls keine Authentication vorhanden ist oder das Principal kein Jwt ist,
     * wird ein leerer String zurückgegeben.
     *
     * @return Benutzernamen aus dem JWT-Claim oder ein leerer String wenn nicht vorhanden
     */
    public String getUserName() {
        String username = null;
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!ObjectUtils.isEmpty(authentication)) {
            final var principal = authentication.getPrincipal();
            if (Objects.equals(Jwt.class, principal.getClass())) {
                final var jwt = (Jwt) principal;
                username = jwt.getClaimAsString(TOKEN_USER_NAME);
            }
        }
        return StringUtils.isNotBlank(username) ? username : "";
    }

    /**
     * Prüft, ob der aktuell angemeldete Nutzer ein reiner Anwender ist.
     *
     * Ein Anwender ist definiert als ein Nutzer, der weder die Rolle ROLE_FACHADMIN
     * noch ROLE_POWERUSER besitzt. Die Prüfung wird nur durchgeführt, wenn die
     * Security aktiviert ist (das Spring-Profil "no-security" ist nicht aktiv).
     * Ist die Security deaktiviert, liefert die Methode false zurück.
     *
     * Die Methode liest die aktuelle Authentication aus dem SecurityContext und
     * prüft die Authorities des Nutzers.
     *
     * @return true, falls die Security aktiviert ist und der Nutzer weder FACHADMIN noch POWERUSER ist;
     *         andernfalls false
     */
    public boolean isAnwender() {
        log.debug("get isAnwender");
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var isAnwender = false;
        if (isSecurityActivated()) {
            isAnwender = CollectionUtils
                    .emptyIfNull(authentication.getAuthorities())
                    .stream()
                    .noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + AuthoritiesEnum.FACHADMIN.name())
                            || grantedAuthority.getAuthority().equals("ROLE_" + AuthoritiesEnum.POWERUSER.name()));
        }
        return isAnwender;
    }

    /**
     * Die Prüfung auf aktivierte Security wird anhand des Spring-Profils "no-security" durchgeführt.
     *
     * @return true, falls das Profil "no-security" im Spring-Kontext NICHT in Verwendung ist.
     *         Andernfalls wird false zurückgegeben.
     */
    protected boolean isSecurityActivated() {
        final var noSecurityProfile = "no-security";
        final var activeProfiles = Arrays.asList(ObjectUtils.getIfNull(environment.getActiveProfiles(), new String[0]));
        return !activeProfiles.contains(noSecurityProfile);
    }

}
