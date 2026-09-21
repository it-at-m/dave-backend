package de.muenchen.dave.services.hochrechnung;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.muenchen.dave.domain.KIPredictionResult;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.ModelInputSchema;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.exceptions.PredictionFailedException;
import de.muenchen.dave.properties.OnnxModelDefinition;
import de.muenchen.dave.util.DaveConstants;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Integrationstest mit echtem Modell für reine Fahrzeugwerte.
 */
class OnnxHochrechnungsmodellReineFahrzeugwerteTest {

    /**
     * Prüft, dass das 2x4h-Modell für jede Bewegungsbeziehung eine Vorhersage erzeugt.
     */
    @Test
    void test_With2x4hModel_ReturnsOnePredictionPerBewegungsbeziehung() throws PredictionFailedException {
        final OnnxHochrechnungsmodell model = new OnnxHochrechnungsmodell(create2x4hDefinition(), new ReineFahrzeugwerteEncoder());
        final List<List<Zeitintervall>> zeitintervalle = List.of(createZeitintervalle());

        final List<KIPredictionResult> result = model.calculate(zeitintervalle);

        assertThat(result.size(), equalTo(1));
        model.closeSession();
    }

    /**
     * Prüft, dass ein Modell mit inkompatibler Eingabedimension einen Vorhersagefehler auslöst.
     */
    @Test
    void test_WithWrongModel_ReturnsPredictionFailedException() throws PredictionFailedException {
        final OnnxHochrechnungsmodell model = new OnnxHochrechnungsmodell(createWrongDefinition(), new ReineFahrzeugwerteEncoder());
        final List<List<Zeitintervall>> zeitintervalle = List.of(createZeitintervalle());

        final PredictionFailedException exception = assertThrows(PredictionFailedException.class, () -> model.calculate(zeitintervalle));

        assertThat(exception.getModelId(), equalTo("rad-2x4h-wrong-test"));
        assertThat(exception.getDetails(), equalTo(PredictionFailedException.ONNX_RUN_MODEL_ERROR));
        assertThat(exception.getMessage(), equalTo("Modell 'rad-2x4h-wrong-test': " + PredictionFailedException.ONNX_RUN_MODEL_ERROR));

        model.closeSession();
    }

    /**
     * Prueft, dass Encoderfehler mit der ID des berechneten Modells angereichert werden.
     */
    @Test
    void test_WithMissingCountingValue_AddsModelIdToPredictionFailedException() throws PredictionFailedException {
        final OnnxHochrechnungsmodell model = new OnnxHochrechnungsmodell(create2x4hDefinition(), new ReineFahrzeugwerteEncoder());
        final List<Zeitintervall> zeitintervalle = createZeitintervalle();
        zeitintervalle.getFirst().setFahrradfahrer(null);

        final PredictionFailedException exception = assertThrows(
                PredictionFailedException.class, () -> model.calculate(List.of(zeitintervalle)));

        assertThat(exception.getModelId(), equalTo("rad-2x4h-test"));
        assertThat(exception.getDetails(), equalTo(PredictionFailedException.ONNX_MISSING_INPUT_VALUE));
        assertThat(exception.getMessage(), equalTo("Modell 'rad-2x4h-test': " + PredictionFailedException.ONNX_MISSING_INPUT_VALUE));

        model.closeSession();
    }

    private OnnxModelDefinition create2x4hDefinition() {
        final OnnxModelDefinition definition = new OnnxModelDefinition();
        definition.setId("rad-2x4h-test");
        definition.setFahrzeug(Fahrzeug.RAD);
        definition.setZaehldauer(Zaehldauer.DAUER_2_X_4_STUNDEN);
        definition.setResourcePath("models/RAD_2x4h_test.onnx");
        definition.setInputTensorName("int64_input");
        definition.setInputSchema(ModelInputSchema.REINE_FAHRZEUGWERTE);
        return definition;
    }

    private OnnxModelDefinition createWrongDefinition() {
        final OnnxModelDefinition definition = new OnnxModelDefinition();
        definition.setId("rad-2x4h-wrong-test");
        definition.setFahrzeug(Fahrzeug.RAD);
        definition.setZaehldauer(Zaehldauer.DAUER_2_X_4_STUNDEN);
        definition.setResourcePath("models/RAD_13h_test.onnx");
        definition.setInputTensorName("int64_input");
        definition.setInputSchema(ModelInputSchema.REINE_FAHRZEUGWERTE);
        return definition;
    }

    private List<Zeitintervall> createZeitintervalle() {
        final List<Zeitintervall> zeitintervalle = new ArrayList<>();
        final LocalDateTime start = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0));
        for (int index = 0; index < 32; index++) {
            final LocalDateTime startUhrzeit = index < 16 ? start.plusMinutes(index * 15L) : start.plusHours(9).plusMinutes((index - 16) * 15L);
            zeitintervalle.add(Zeitintervall.builder()
                    .startUhrzeit(startUhrzeit)
                    .endeUhrzeit(startUhrzeit.plusMinutes(15))
                    .sortingIndex(index)
                    .fahrradfahrer(6)
                    .build());
        }
        return zeitintervalle;
    }

}
