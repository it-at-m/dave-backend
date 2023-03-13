package de.muenchen.dave.util.dataimport;

import de.muenchen.dave.domain.Fahrbeziehung;
import de.muenchen.dave.domain.KIPredictionResult;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.util.DaveConstants;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZeitintervallKIUtilTest {

    @Test
    void testGroupZeitintervalleByFahrbeziehung() {
        // Arrange
        Zeitintervall zeitintervall1 = new Zeitintervall();
        zeitintervall1.setFahrbeziehungId(UUID.randomUUID());

        Zeitintervall zeitintervall2 = new Zeitintervall();
        zeitintervall2.setFahrbeziehungId(zeitintervall1.getFahrbeziehungId());

        Zeitintervall zeitintervall3 = new Zeitintervall();
        zeitintervall3.setFahrbeziehungId(UUID.randomUUID());

        // Act
        List<List<Zeitintervall>> result = ZeitintervallKIUtil.groupZeitintervalleByFahrbeziehung(Arrays.asList(zeitintervall1, zeitintervall2, zeitintervall3));
        result.sort(Comparator.comparingInt(List::size));

        // Assert
        assertThat(result.size(), equalTo(2));
        assertThat(result.get(0).size(), equalTo(1));
        assertThat(result.get(1).size(), equalTo(2));
    }

    @Test
    void testCreateKIZeitintervalleFromPredictionResultsLengthMismatch() {
        // Arrange
        List<KIPredictionResult> predictionResults = List.of(new KIPredictionResult(6));
        List<Zeitintervall> zeitintervalle = Arrays.asList(new Zeitintervall(), new Zeitintervall());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> ZeitintervallKIUtil.createKIZeitintervalleFromKIPredictionResults(predictionResults, zeitintervalle));

        String expectedMessage = ZeitintervallKIUtil.LIST_LENGTH_MISMATCH;
        String actualMessage = exception.getMessage();

        assertThat(actualMessage, equalTo(expectedMessage));
    }

    @Test
    void testCreateKIZeitintervalleFromPredictionResults() {
        // Arrange
        KIPredictionResult predictionResult1 = new KIPredictionResult(6);
        KIPredictionResult predictionResult2 = new KIPredictionResult(1);

        List<KIPredictionResult> predictionResults = Arrays.asList(predictionResult1, predictionResult2);

        Fahrbeziehung fahrbeziehung1 = new Fahrbeziehung();
        fahrbeziehung1.setVon(1);
        fahrbeziehung1.setVon(2);
        Fahrbeziehung fahrbeziehung2 = new Fahrbeziehung();
        fahrbeziehung2.setVon(2);
        fahrbeziehung2.setVon(3);

        Zeitintervall zeitintervall1 = new Zeitintervall();
        zeitintervall1.setZaehlungId(UUID.randomUUID());
        zeitintervall1.setFahrbeziehungId(UUID.randomUUID());
        zeitintervall1.setFahrbeziehung(fahrbeziehung1);
        Zeitintervall zeitintervall2 = new Zeitintervall();
        zeitintervall2.setZaehlungId(zeitintervall1.getZaehlungId());
        zeitintervall2.setFahrbeziehungId(UUID.randomUUID());
        zeitintervall2.setFahrbeziehung(fahrbeziehung2);

        List<Zeitintervall> zeitintervalle = Arrays.asList(zeitintervall1, zeitintervall2);

        // Act
        List<Zeitintervall> kiZeitintervalle = ZeitintervallKIUtil.createKIZeitintervalleFromKIPredictionResults(predictionResults, zeitintervalle);

        // Assert
        assertThat(kiZeitintervalle.size(), equalTo(2));

        // Allgemeine Eigenschaften
        kiZeitintervalle.forEach(kiZeitintervall -> {
            assertThat(kiZeitintervall.getZaehlungId(), equalTo(zeitintervall1.getZaehlungId()));
            assertThat(kiZeitintervall.getStartUhrzeit(), equalTo(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.MIDNIGHT)));
            assertThat(kiZeitintervall.getEndeUhrzeit(), equalTo(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.MAX)));
            assertThat(kiZeitintervall.getType(), equalTo(TypeZeitintervall.GESAMT_KI));
            assertThat(kiZeitintervall.getFussgaenger(), equalTo(0));
            assertThat(kiZeitintervall.getHochrechnung(), notNullValue());
            assertThat(kiZeitintervall.getHochrechnung().getFaktorGv(), nullValue());
            assertThat(kiZeitintervall.getHochrechnung().getFaktorSv(), nullValue());
            assertThat(kiZeitintervall.getHochrechnung().getFaktorKfz(), nullValue());
        });

        // Individuelle Eigenschaften
        assertThat(kiZeitintervalle.get(0).getFahrbeziehungId(), equalTo(zeitintervall1.getFahrbeziehungId()));
        assertThat(kiZeitintervalle.get(1).getFahrbeziehungId(), equalTo(zeitintervall2.getFahrbeziehungId()));

        assertThat(kiZeitintervalle.get(0).getFahrbeziehung(), equalTo(zeitintervall1.getFahrbeziehung()));
        assertThat(kiZeitintervalle.get(1).getFahrbeziehung(), equalTo(zeitintervall2.getFahrbeziehung()));

        assertThat(kiZeitintervalle.get(0).getFahrradfahrer(), equalTo(6));
        assertThat(kiZeitintervalle.get(1).getFahrradfahrer(), equalTo(1));;

    }

}
