package de.muenchen.dave;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.security.UserInfoDataService;
import de.muenchen.dave.util.DaveConstants;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Assertions;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestUtils {

    public static List<Zeitintervall> makeDeepCopy(final List<Zeitintervall> zeitintervalle) {
        final List<Zeitintervall> copyZeitintervalle = new ArrayList<>();
        zeitintervalle.forEach(zeitintervall -> {
            Zeitintervall copyZeitintervall = new Zeitintervall();
            copyZeitintervall.setPkw(zeitintervall.getPkw());
            copyZeitintervall.setLkw(zeitintervall.getLkw());
            copyZeitintervall.setLastzuege(zeitintervall.getLastzuege());
            copyZeitintervall.setBusse(zeitintervall.getBusse());
            copyZeitintervall.setKraftraeder(zeitintervall.getKraftraeder());
            copyZeitintervall.setFahrradfahrer(zeitintervall.getFahrradfahrer());
            copyZeitintervall.setFussgaenger(zeitintervall.getFussgaenger());
            copyZeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE,
                    LocalTime.of(zeitintervall.getStartUhrzeit().getHour(), zeitintervall.getStartUhrzeit().getMinute())));
            copyZeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE,
                    LocalTime.of(zeitintervall.getEndeUhrzeit().getHour(), zeitintervall.getEndeUhrzeit().getMinute())));
            copyZeitintervalle.add(copyZeitintervall);
        });
        return copyZeitintervalle;
    }

    /**
     * @param zaehlungId Die Zaehlungs-ID
     * @param startUhrzeit Die Startuhrzeit für den Zeitintervall. Die Endeuhrzeit entspricht der um 15
     *            Minuten erhöhten Startuhrzeit.
     * @param value Der Wert wird bei alle Fahrzeugklassen und den hochgerechneten Fahrzeugkategorien
     *            gesetzt.
     * @param vonVerkehrsbeziehung Die Nummer des Knotenarms
     * @param nachVerkehrsbeziehung Die Nummer des Knotenarms
     * @param fahrbewegungKreisverkehr Information ob {@link FahrbewegungKreisverkehr#HINEIN},
     *            {@link FahrbewegungKreisverkehr#HERAUS} und
     *            {@link FahrbewegungKreisverkehr#VORBEI}.
     * @return
     */
    public static Zeitintervall createZeitintervall(final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final Integer value,
            final Integer vonVerkehrsbeziehung,
            final Integer nachVerkehrsbeziehung,
            final FahrbewegungKreisverkehr fahrbewegungKreisverkehr) {
        final Zeitintervall zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setBewegungsbeziehungId(UUID.randomUUID());
        zeitintervall.setStartUhrzeit(startUhrzeit);
        zeitintervall.setEndeUhrzeit(
                startUhrzeit.equals(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 45)))
                        ? LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 59))
                        : startUhrzeit.plusMinutes(15));
        zeitintervall.setPkw(value);
        zeitintervall.setLkw(value);
        zeitintervall.setLastzuege(value);
        zeitintervall.setBusse(value);
        zeitintervall.setKraftraeder(value);
        zeitintervall.setFahrradfahrer(value);
        zeitintervall.setFussgaenger(value);
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(ObjectUtils.isNotEmpty(value) ? BigDecimal.valueOf(value) : null);
        zeitintervall.getHochrechnung().setHochrechnungGv(ObjectUtils.isNotEmpty(value) ? BigDecimal.valueOf(value) : null);
        zeitintervall.getHochrechnung().setHochrechnungSv(ObjectUtils.isNotEmpty(value) ? BigDecimal.valueOf(value) : null);
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        zeitintervall.getVerkehrsbeziehung().setVon(vonVerkehrsbeziehung);
        zeitintervall.getVerkehrsbeziehung().setNach(nachVerkehrsbeziehung);
        zeitintervall.getVerkehrsbeziehung().setFahrbewegungKreisverkehr(fahrbewegungKreisverkehr);
        zeitintervall.getVerkehrsbeziehung().setStrassenseite(null);
        zeitintervall.setSortingIndex(1);
        return zeitintervall;
    }

    /**
     * @param privateMethodName Der Name der privaten statischen Methode.
     * @param classToTest Die Klasse in welcher diese statische Methode vorhanden ist.
     * @param classMethodParameter Die Typen {@link Class} welche als Parameter in der statischen
     *            Methode erwartet werden. Aufgelistet entsprechend der
     *            Parameterreihenfolge.
     * @param valueMethodParameter Die Werte der Parameter welcher in der statischen Methode erwartet
     *            werden. Aufgelistet entsprechend der
     *            Parameterreihenfolge.
     * @param returnType Der Typ {@link Class} welcher von der statischen Methode zurückgegeben wird.
     * @param <ReturnType>
     * @param <ClassToTest>
     * @return Der Rückgabewert mit dem im Paremeter "returnType" definerten Datentypen.
     */
    public static <ReturnType, ClassToTest> ReturnType privateStaticMethodCall(final String privateMethodName,
            final Class<ClassToTest> classToTest,
            final Class<?>[] classMethodParameter,
            final Object[] valueMethodParameter,
            final Class<ReturnType> returnType) {
        Object result = null;
        try {
            Method method = classToTest.getDeclaredMethod(privateMethodName, classMethodParameter);
            method.setAccessible(true);
            result = method.invoke(null, valueMethodParameter);
        } catch (Exception exception) {
            Assertions.fail(exception);
        }
        return returnType.cast(result);
    }

    /**
     * Setzt einen Nutzer mit einem Username und einer Rolle, der für die Tests verwendet wird.
     *
     * @param username des Test-Nutzers
     * @param fachadmin Wenn {@code true}, erhält der Test-Nutzer die Rolle Fachadmin
     */
    public static void setSecurityContext(String username, boolean fachadmin) {
        final Jwt jwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of(UserInfoDataService.CLAIM_USERNAME, username)
        );

        final List<GrantedAuthority> authorities = new ArrayList<>();
        if (fachadmin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_FACHADMIN"));
        }

        final JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /**
     * Leert den Security Context. Diese Methode sollte nach jedem Testfall aufgerufen werden, um zu verhindern, dass sich die Tests gegenseitig beeinflussen.
     */
    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
