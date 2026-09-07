package de.muenchen.dave.services.hochrechnung;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import de.muenchen.dave.domain.enums.Hochrechnungskategorie;
import de.muenchen.dave.domain.enums.ModelInputSchema;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.properties.OnnxModelDefinition;
import de.muenchen.dave.properties.OnnxModelProperties;
import java.util.List;
import org.junit.jupiter.api.Test;

class OnnxModelRegistryTest {

    @Test
    void test_WithMissingModelArtifact_DoesNotRegisterModel() {
        final OnnxModelDefinition definition = new OnnxModelDefinition();
        definition.setId("nicht-vorhanden");
        definition.setHochrechnungskategorie(Hochrechnungskategorie.RAD);
        definition.setZaehldauer(Zaehldauer.DAUER_13_STUNDEN);
        definition.setResourcePath("model/nicht-vorhanden.onnx");
        definition.setInputTensorName("int64_input");
        definition.setInputSchema(ModelInputSchema.REINE_RADWERTE_V1);
        final OnnxModelProperties properties = new OnnxModelProperties();
        properties.setModelle(List.of(definition));

        final OnnxModelRegistry modelRegistry = new OnnxModelRegistry(properties, List.of(new ReineRadwerteV1Encoder()));

        assertThat(modelRegistry.findModel(Zaehldauer.DAUER_13_STUNDEN, Hochrechnungskategorie.RAD).stream().toList(), empty());
    }

}
