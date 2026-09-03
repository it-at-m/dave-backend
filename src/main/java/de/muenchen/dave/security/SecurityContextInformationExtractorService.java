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
public final class SecurityContextInformationExtractorService {

    public static final String UNAUTHENTICATED_USER = "unauthenticated";

    private static final String TOKEN_USER_NAME = UserInfoDataService.CLAIM_USERNAME;

    private final Environment environment;

    /**
     * The method extracts the username out of the {@link BearerTokenAuthentication}.
     *
     * @return The username or a placeholder if there is no {@link BearerTokenAuthentication} available.
     */
    public String getAuthenticatedUsername() {
        final var username = getUserName();
        return StringUtils.isNotBlank(username) ? username : UNAUTHENTICATED_USER;
    }

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

    public boolean isAnwender() {
        log.debug("get isAnwender");
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Ein Nutzer ist immer mindestens Anwender
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
     * @return true, falls das Profil "no-security" im Spring-Kontext in Verwendung ist. Andernfalls
     *         wird false zurückgegeben.
     */
    private boolean isSecurityActivated() {
        final var noSecurityProfile = "no-security";
        final var activeProfiles = Arrays.asList(ObjectUtils.getIfNull(environment.getActiveProfiles(), new String[0]));
        return !activeProfiles.contains(noSecurityProfile);
    }

}
