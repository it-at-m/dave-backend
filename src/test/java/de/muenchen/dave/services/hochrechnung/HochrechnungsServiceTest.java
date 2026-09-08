package de.muenchen.dave.services.hochrechnung;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import de.muenchen.dave.domain.KIPredictionResult;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.exceptions.PredictionFailedException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HochrechnungsServiceTest {

    @Test
    void test_WithAvailableRadModel_ReturnsModelPrediction() throws PredictionFailedException {
        final OnnxModelRegistry modelRegistry = Mockito.mock(OnnxModelRegistry.class);
        final Hochrechnungsmodell model = Mockito.mock(Hochrechnungsmodell.class);
        final List<List<Zeitintervall>> zeitintervalle = List.of(List.of(new Zeitintervall()));
        when(modelRegistry.findModel(Zaehldauer.DAUER_13_STUNDEN, Fahrzeug.RAD)).thenReturn(Optional.of(model));
        when(model.calculate(zeitintervalle)).thenReturn(List.of(new KIPredictionResult(42)));

        final List<KIPredictionResult> result = new HochrechnungsService(modelRegistry)
                .calculateRadhochrechnung(Zaehldauer.DAUER_13_STUNDEN, zeitintervalle);

        assertThat(result, equalTo(List.of(new KIPredictionResult(42))));
    }

    @Test
    void test_WithUnavailableRadModel_ReturnsEmptyPrediction() throws PredictionFailedException {
        final OnnxModelRegistry modelRegistry = Mockito.mock(OnnxModelRegistry.class);
        when(modelRegistry.findModel(Zaehldauer.DAUER_16_STUNDEN, Fahrzeug.RAD)).thenReturn(Optional.empty());

        final List<KIPredictionResult> result = new HochrechnungsService(modelRegistry)
                .calculateRadhochrechnung(Zaehldauer.DAUER_16_STUNDEN, List.of(List.of(new Zeitintervall())));

        assertThat(result, empty());
    }

}
