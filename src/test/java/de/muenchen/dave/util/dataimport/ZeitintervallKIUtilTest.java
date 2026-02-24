package de.muenchen.dave.util.dataimport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.muenchen.dave.domain.KIPredictionResult;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.util.DaveConstants;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ZeitintervallKIUtilTest {

    @Test
    void testGroupZeitintervalleByVerkehrsbeziehung() {
        // Arrange
        Zeitintervall zeitintervall1 = new Zeitintervall();
        zeitintervall1.setBewegungsbeziehungId(UUID.randomUUID());

        Zeitintervall zeitintervall2 = new Zeitintervall();
        zeitintervall2.setBewegungsbeziehungId(zeitintervall1.getBewegungsbeziehungId());

        Zeitintervall zeitintervall3 = new Zeitintervall();
        zeitintervall3.setBewegungsbeziehungId(UUID.randomUUID());

        // Act
        List<List<Zeitintervall>> result = ZeitintervallKIUtil
                .groupZeitintervalleByVerkehrsbeziehung(Arrays.asList(zeitintervall1, zeitintervall2, zeitintervall3));
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
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ZeitintervallKIUtil.createKIZeitintervalleFromKIPredictionResults(predictionResults, zeitintervalle));

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

        Verkehrsbeziehung verkehrsbeziehung1 = new Verkehrsbeziehung();
        verkehrsbeziehung1.setVon(1);
        verkehrsbeziehung1.setVon(2);
        Verkehrsbeziehung verkehrsbeziehung2 = new Verkehrsbeziehung();
        verkehrsbeziehung2.setVon(2);
        verkehrsbeziehung2.setVon(3);

        Zeitintervall zeitintervall1 = new Zeitintervall();
        zeitintervall1.setZaehlungId(UUID.randomUUID());
        zeitintervall1.setBewegungsbeziehungId(UUID.randomUUID());
        zeitintervall1.setVerkehrsbeziehung(verkehrsbeziehung1);
        Zeitintervall zeitintervall2 = new Zeitintervall();
        zeitintervall2.setZaehlungId(zeitintervall1.getZaehlungId());
        zeitintervall2.setBewegungsbeziehungId(UUID.randomUUID());
        zeitintervall2.setVerkehrsbeziehung(verkehrsbeziehung2);

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
        assertThat(kiZeitintervalle.get(0).getBewegungsbeziehungId(), equalTo(zeitintervall1.getBewegungsbeziehungId()));
        assertThat(kiZeitintervalle.get(1).getBewegungsbeziehungId(), equalTo(zeitintervall2.getBewegungsbeziehungId()));

        assertThat(kiZeitintervalle.get(0).getVerkehrsbeziehung(), equalTo(zeitintervall1.getVerkehrsbeziehung()));
        assertThat(kiZeitintervalle.get(1).getVerkehrsbeziehung(), equalTo(zeitintervall2.getVerkehrsbeziehung()));

        assertThat(kiZeitintervalle.get(0).getFahrradfahrer(), equalTo(6));
        assertThat(kiZeitintervalle.get(1).getFahrradfahrer(), equalTo(1));

    }

}
