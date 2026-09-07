package de.muenchen.dave.services.hochrechnung;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import de.muenchen.dave.domain.KIPredictionResult;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.Hochrechnungsziel;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.exceptions.PredictionFailedException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HochrechnungsServiceTest {

    @Test
    void test_WithAvailableRadModel_ReturnsModelPrediction() throws PredictionFailedException {
        final OnnxModellRegistry modellRegistry = Mockito.mock(OnnxModellRegistry.class);
        final Hochrechnungsmodell modell = Mockito.mock(Hochrechnungsmodell.class);
        final List<List<Zeitintervall>> zeitintervalle = List.of(List.of(new Zeitintervall()));
        when(modellRegistry.findeModell(Zaehldauer.DAUER_13_STUNDEN, Hochrechnungsziel.RAD)).thenReturn(Optional.of(modell));
        when(modell.berechne(zeitintervalle)).thenReturn(List.of(new KIPredictionResult(42)));

        final List<KIPredictionResult> ergebnis = new HochrechnungsService(modellRegistry)
                .berechneRadhochrechnung(Zaehldauer.DAUER_13_STUNDEN, zeitintervalle);

        assertThat(ergebnis, equalTo(List.of(new KIPredictionResult(42))));
    }

    @Test
    void test_WithUnavailableRadModel_ReturnsEmptyPrediction() throws PredictionFailedException {
        final OnnxModellRegistry modellRegistry = Mockito.mock(OnnxModellRegistry.class);
        when(modellRegistry.findeModell(Zaehldauer.DAUER_16_STUNDEN, Hochrechnungsziel.RAD)).thenReturn(Optional.empty());

        final List<KIPredictionResult> ergebnis = new HochrechnungsService(modellRegistry)
                .berechneRadhochrechnung(Zaehldauer.DAUER_16_STUNDEN, List.of(List.of(new Zeitintervall())));

        assertThat(ergebnis, empty());
    }

}
