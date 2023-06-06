/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.security;

import java.util.concurrent.atomic.AtomicBoolean;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityContextInformationExtractor {

    public static final String NAME_UNAUTHENTICATED_USER = "unauthenticated";

    private static final String ATTRIBUTE_USER_NAME = "user_name";

    /**
     * The method extracts the username out of the {@link BearerTokenAuthentication}.
     *
     * @return The username or a placeholder if there is no {@link BearerTokenAuthentication} available.
     */
    static String getAuthenticatedUsername() {
        log.debug("get Username");
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof BearerTokenAuthentication) {
            return authentication.getName();
        } else {
            return NAME_UNAUTHENTICATED_USER;
        }
    }

    public static boolean isFachadmin() {
        log.debug("get isFachadmin");
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final AtomicBoolean isFachadmin = new AtomicBoolean(false);
        authentication.getAuthorities().forEach(grantedAuthority -> {
            if (grantedAuthority.getAuthority().equals("ROLE_" + AuthoritiesEnum.FACHADMIN.name())) {
                isFachadmin.set(true);
            }
        });
        return isFachadmin.get();
    }

    public static String getUserName() {
        log.debug("get user_name");
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof BearerTokenAuthentication) {
            return (String) ((BearerTokenAuthentication) authentication).getTokenAttributes().get(ATTRIBUTE_USER_NAME);
        } else {
            return "";
        }
    }

    public static boolean isAnwender() {
        log.debug("get isAnwender");
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Ein Nutzer ist immer mindestens Anwender
        final AtomicBoolean isAnwender = new AtomicBoolean(true);
        authentication.getAuthorities().forEach(grantedAuthority -> {
            if (grantedAuthority.getAuthority().equals("ROLE_" + AuthoritiesEnum.POWERUSER.name())) {
                isAnwender.set(false);
            }
            if (grantedAuthority.getAuthority().equals("ROLE_" + AuthoritiesEnum.FACHADMIN.name())) {
                isAnwender.set(false);
            }
        });
        return isAnwender.get();
    }

}
