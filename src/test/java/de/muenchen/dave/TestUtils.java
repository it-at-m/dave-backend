/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave;

import de.muenchen.dave.domain.Fahrbeziehung;
import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.util.DaveConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
     * @param startUhrzeit Die Startuhrzeit für den Zeitintervall.
     *            Die Endeuhrzeit entspricht der um 15 Minuten erhöhten Startuhrzeit.
     * @param value Der Wert wird bei alle Fahrzeugklassen und den hochgerechneten Fahrzeugkategorien
     *            gesetzt.
     * @param vonFahrbeziehung Die Nummer des Knotenarms
     * @param nachFahrbeziehung Die Nummer des Knotenarms
     * @param fahrbewegungKreisverkehr Information ob {@link FahrbewegungKreisverkehr#HINEIN},
     *            {@link FahrbewegungKreisverkehr#HERAUS} und {@link FahrbewegungKreisverkehr#VORBEI}.
     * @return
     */
    public static Zeitintervall createZeitintervall(final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final Integer value,
            final Integer vonFahrbeziehung,
            final Integer nachFahrbeziehung,
            final FahrbewegungKreisverkehr fahrbewegungKreisverkehr) {
        final Zeitintervall zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setFahrbeziehungId(UUID.randomUUID());
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
        zeitintervall.setFahrbeziehung(new Fahrbeziehung());
        zeitintervall.getFahrbeziehung().setVon(vonFahrbeziehung);
        zeitintervall.getFahrbeziehung().setNach(nachFahrbeziehung);
        zeitintervall.getFahrbeziehung().setFahrbewegungKreisverkehr(fahrbewegungKreisverkehr);
        return zeitintervall;
    }

    /**
     * @param privateMethodName Der Name der privaten statischen Methode.
     * @param classToTest Die Klasse in welcher diese statische Methode vorhanden ist.
     * @param classMethodParameter Die Typen {@link Class} welche als Parameter in der statischen
     *            Methode erwartet werden.
     *            Aufgelistet entsprechend der Parameterreihenfolge.
     * @param valueMethodParameter Die Werte der Parameter welcher in der statischen Methode erwartet
     *            werden.
     *            Aufgelistet entsprechend der Parameterreihenfolge.
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

}
