package de.muenchen.dave.services.hochrechnung;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.exceptions.PredictionFailedException;
import de.muenchen.dave.util.DaveConstants;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReineFahrzeugwerteEncoderTest {

    private final ReineFahrzeugwerteEncoder encoder = new ReineFahrzeugwerteEncoder();

    /**
     * Prüft, dass der Encoder unsortierte 13h-Radintervalle zeitlich sortiert und vollständig kodiert.
     */
    @Test
    void test_With13HoursAndUnsortedIntervals_Encodes52SortedRadValues() throws PredictionFailedException {
        final List<Zeitintervall> zeitintervalle = createZeitintervalle(52);
        Collections.reverse(zeitintervalle);

        final long[][] result = encoder.encode(Zaehldauer.DAUER_13_STUNDEN, Fahrzeug.RAD, List.of(zeitintervalle));

        assertThat(result.length, equalTo(1));
        assertThat(result[0].length, equalTo(52));
        for (int index = 0; index < result[0].length; index++) {
            assertThat(result[0][index], equalTo((long) index));
        }
    }

    /**
     * Prüft, dass der Encoder alle 64 Radwerte einer 16h-Zählung kodiert.
     */
    @Test
    void test_With16Hours_Encodes64RadValues() throws PredictionFailedException {
        final long[][] result = encoder.encode(Zaehldauer.DAUER_16_STUNDEN, Fahrzeug.RAD, List.of(createZeitintervalle(64)));

        assertThat(result.length, equalTo(1));
        assertThat(result[0].length, equalTo(64));
        assertThat(result[0][63], equalTo(63L));
    }

    /**
     * Prüft, dass der Encoder für 2x4h nur die Zeitfenster von 06:00 bis 10:00 und 15:00 bis 19:00
     * verwendet.
     */
    @Test
    void test_With2x4Hours_EncodesValuesFromBothModelInputTimeBlocks() throws PredictionFailedException {
        final long[][] result = encoder.encode(Zaehldauer.DAUER_2_X_4_STUNDEN, Fahrzeug.RAD, List.of(createZeitintervalle(64)));

        assertThat(result.length, equalTo(1));
        assertThat(result[0].length, equalTo(32));
        for (int index = 0; index < 16; index++) {
            assertThat(result[0][index], equalTo((long) index));
            assertThat(result[0][index + 16], equalTo((long) index + 36));
        }
    }

    /**
     * Prüft, dass der Encoder die für das angeforderte Fahrzeug hinterlegten Werte verwendet.
     */
    @Test
    void test_WithPkwCategory_EncodesPkwValues() throws PredictionFailedException {
        final List<Zeitintervall> zeitintervalle = createZeitintervalle(52);
        for (int index = 0; index < zeitintervalle.size(); index++) {
            zeitintervalle.get(index).setPkw(index + 100);
        }

        final long[][] result = encoder.encode(Zaehldauer.DAUER_13_STUNDEN, Fahrzeug.PKW, List.of(zeitintervalle));

        assertThat(result[0][0], equalTo(100L));
        assertThat(result[0][51], equalTo(151L));
    }

    /**
     * Prüft, dass eine unvollständige Intervallreihe als ungültige Modelldimension abgelehnt wird.
     */
    @Test
    void test_WithIncompleteIntervals_ThrowsPredictionFailedException() {
        final PredictionFailedException exception = assertThrows(
                PredictionFailedException.class,
                () -> encoder.encode(Zaehldauer.DAUER_13_STUNDEN, Fahrzeug.RAD, List.of(createZeitintervalle(51))));

        assertThat(exception.getMessage(), equalTo(PredictionFailedException.ONNX_INVALID_INPUT_DIMENSION));
    }

    private List<Zeitintervall> createZeitintervalle(final int anzahl) {
        final List<Zeitintervall> zeitintervalle = new ArrayList<>();
        final LocalDateTime start = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0));
        for (int index = 0; index < anzahl; index++) {
            final LocalDateTime startUhrzeit = start.plusMinutes(index * 15L);
            zeitintervalle.add(Zeitintervall.builder()
                    .startUhrzeit(startUhrzeit)
                    .endeUhrzeit(startUhrzeit.plusMinutes(15))
                    .sortingIndex(index)
                    .fahrradfahrer(index)
                    .build());
        }
        return zeitintervalle;
    }

}
