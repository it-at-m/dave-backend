/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.util;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitauswahl;
import de.muenchen.dave.domain.enums.Zeitblock;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ZaehldatenProcessingUtil {

    public static final DateTimeFormatter LOCAL_TIME = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Diese Methoder gibt {@link LadeZaehldatumDTO}#getStartUhrzeit() als String zurück.
     *
     * @param ladeZaehldatum zur Erstellung des Startuhrzeitstrings.
     * @return die Startuhrzeig als String.
     */
    public static String getStartUhrzeit(final LadeZaehldatumDTO ladeZaehldatum) {
        return ladeZaehldatum.getStartUhrzeit().format(LOCAL_TIME);
    }

    /**
     * Führt einen Cast von BigDecimal zu Integer mit den dazugehörigen Informationsverlust durch. Falls
     * der Parameterwert "null" entspricht wird auch eine
     * "null" zurückgegeben.
     *
     * @param value zum Cast auf Integer.
     * @return den Integerwert, andernfalls "null" falls Parameterwert "null".
     */
    public static Integer nullsafeCast(BigDecimal value) {
        return ObjectUtils.isNotEmpty(value) ? value.intValue() : null;
    }

    /**
     * Diese Methode fügt der List im ersten Parameter den Wert im zweiten Parameter an, falls dieser
     * noch nicht im ersten Parameter vorhanden ist.
     *
     * @param xAxisData zum Anfügen des zweiten Parameterwertes.
     * @param xAxisValue zum Anfügen an den ersten Parameter.
     * @return den um den zweiten Parameter erweiterten ersten Parameter.
     */
    public static List<String> checkAndAddToXAxisWhenNotAvailable(
            final List<String> xAxisData,
            final String xAxisValue) {
        if (!xAxisData.contains(xAxisValue)) {
            xAxisData.add(xAxisValue);
        }
        return xAxisData;
    }

    /**
     * @param valueToRound zum Runden auf den im zweiten Parameter genannten Rundungsschritt.
     * @param roundingStep auf dem Aufgerundet werden soll.
     * @return Diese Methode gibt den um den Rundungsschritt aufgerundeten Wert zurück. Ist der zu
     *         rundende Wert 0, so wird auf den Rundungsschritt
     *         aufgerundet.
     */
    public static Integer getValueRounded(final BigDecimal valueToRound, final int roundingStep) {
        final Integer roundedValue = valueToRound
                .divide(BigDecimal.valueOf(roundingStep))
                .setScale(0, RoundingMode.UP)
                .multiply(BigDecimal.valueOf(roundingStep))
                .intValue();
        return roundedValue == 0 ? roundingStep : roundedValue;
    }

    /**
     * @param valueToRound zum Runden auf den im zweiten Parameter genannten Rundungsschritt.
     * @param roundingStep auf dem Aufgerundet werden soll.
     * @return Diese Methode gibt den um den Rundungsschritt aufgerundeten Wert zurück. Ist der zu
     *         rundende Wert 0, so wird auf den Rundungsschritt
     *         aufgerundet.
     */
    public static Integer getValueRounded(final int valueToRound, final int roundingStep) {
        return getValueRounded(BigDecimal.valueOf(valueToRound), roundingStep);
    }

    public static Integer getZeroIfNull(final Integer value) {
        return ObjectUtils.isEmpty(value) ? 0 : value;
    }

    public static BigDecimal getZeroIfNull(final BigDecimal value) {
        return ObjectUtils.isEmpty(value) ? BigDecimal.ZERO : value;
    }

    /**
     * Die Methode ist erforderlich um im Backend ein Standard-OptionsDTO zur Datenextraktion zu
     * erstellen.
     *
     * @param zaehlung für das Standard-OptionsDTO.
     * @return das Standard-OptionsDTO zur Datenextraktion.
     */
    public static OptionsDTO createHardcodedOptions(final Zaehlung zaehlung) {
        final boolean getKfz = zaehlung.getKategorien().contains(Fahrzeug.KFZ);
        final boolean getRad = zaehlung.getKategorien().contains(Fahrzeug.RAD);
        final boolean getFuss = zaehlung.getKategorien().contains(Fahrzeug.FUSS);

        final var options = new OptionsDTO();
        options.setZaehldauer(Zaehldauer.valueOf(zaehlung.getZaehldauer()));
        options.setIntervall(null);
        options.setZeitblock(Zeitblock.ZB_00_24);
        options.setZeitauswahl(Zeitauswahl.TAGESWERT.getCapitalizedName());
        options.setKraftfahrzeugverkehr(getKfz);
        options.setSchwerverkehr(getKfz);
        options.setGueterverkehr(getKfz);
        options.setRadverkehr(getRad);
        options.setFussverkehr(getFuss);
        options.setBlocksumme(true);
        options.setTagessumme(true);
        options.setSpitzenstunde(true);
        options.setSpitzenstundeKfz(getKfz);
        options.setSpitzenstundeRad(getRad);
        options.setSpitzenstundeFuss(getFuss);
        return options;
    }

    public static String getMonatTextuell(final Integer monat) {
        return Month.of(monat).getDisplayName(TextStyle.FULL, Locale.GERMANY);
    }

    public static String getMonatTextuell(final LocalDate date) {
        return getMonatTextuell(date.getMonthValue());
    }

}
