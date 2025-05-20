/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

import java.util.Objects;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityContextInformationExtractor {

    public static final String UNAUTHENTICATED_USER = "unauthenticated";

    private static final String TOKEN_USER_NAME = UserInfoDataService.CLAIM_USERNAME;

    /**
     * The method extracts the username out of the {@link BearerTokenAuthentication}.
     *
     * @return The username or a placeholder if there is no {@link BearerTokenAuthentication} available.
     */
    static String getAuthenticatedUsername() {
        final var username = getUserName();
        return StringUtils.isNotBlank(username) ? username : UNAUTHENTICATED_USER;
    }

    public static boolean isFachadmin() {
        log.debug("get isFachadmin");
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return CollectionUtils
                .emptyIfNull(authentication.getAuthorities())
                .stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + AuthoritiesEnum.FACHADMIN.name()));
    }

    public static String getUserName() {
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

    public static boolean isAnwender() {
        log.debug("get isAnwender");
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Ein Nutzer ist immer mindestens Anwender
        return CollectionUtils
                .emptyIfNull(authentication.getAuthorities())
                .stream()
                .noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + AuthoritiesEnum.FACHADMIN.name())
                        || grantedAuthority.getAuthority().equals("ROLE_" + AuthoritiesEnum.POWERUSER.name()));
    }

}
