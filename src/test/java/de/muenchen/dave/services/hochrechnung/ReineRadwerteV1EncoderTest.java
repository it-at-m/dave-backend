package de.muenchen.dave.services.hochrechnung;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.exceptions.PredictionFailedException;
import de.muenchen.dave.util.DaveConstants;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReineRadwerteV1EncoderTest {

    private final ReineRadwerteV1Encoder encoder = new ReineRadwerteV1Encoder();

    @Test
    void test_With13HoursAndUnsortedIntervals_Encodes52SortedRadValues() throws PredictionFailedException {
        final List<Zeitintervall> zeitintervalle = createZeitintervalle(52);
        Collections.reverse(zeitintervalle);

        final long[][] result = encoder.encode(Zaehldauer.DAUER_13_STUNDEN, List.of(zeitintervalle));

        assertThat(result.length, equalTo(1));
        assertThat(result[0].length, equalTo(52));
        for (int index = 0; index < result[0].length; index++) {
            assertThat(result[0][index], equalTo((long) index));
        }
    }

    @Test
    void test_With16Hours_Encodes64RadValues() throws PredictionFailedException {
        final long[][] result = encoder.encode(Zaehldauer.DAUER_16_STUNDEN, List.of(createZeitintervalle(64)));

        assertThat(result.length, equalTo(1));
        assertThat(result[0].length, equalTo(64));
        assertThat(result[0][63], equalTo(63L));
    }

    @Test
    void test_WithIncompleteIntervals_ThrowsPredictionFailedException() {
        final PredictionFailedException exception = assertThrows(
                PredictionFailedException.class,
                () -> encoder.encode(Zaehldauer.DAUER_13_STUNDEN, List.of(createZeitintervalle(51))));

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
