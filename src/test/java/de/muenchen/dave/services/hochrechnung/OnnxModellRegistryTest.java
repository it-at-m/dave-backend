package de.muenchen.dave.services.hochrechnung;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import de.muenchen.dave.domain.enums.Hochrechnungsziel;
import de.muenchen.dave.domain.enums.ModelleingabeSchema;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.properties.OnnxModellDefinition;
import de.muenchen.dave.properties.OnnxModellProperties;
import java.util.List;
import org.junit.jupiter.api.Test;

class OnnxModellRegistryTest {

    @Test
    void test_WithMissingModelArtifact_DoesNotRegisterModel() {
        final OnnxModellDefinition definition = new OnnxModellDefinition();
        definition.setId("nicht-vorhanden");
        definition.setZiel(Hochrechnungsziel.RAD);
        definition.setZaehldauer(Zaehldauer.DAUER_13_STUNDEN);
        definition.setResourcePath("model/nicht-vorhanden.onnx");
        definition.setInputTensorName("int64_input");
        definition.setInputSchema(ModelleingabeSchema.REINE_RADWERTE_V1);
        final OnnxModellProperties properties = new OnnxModellProperties();
        properties.setModelle(List.of(definition));

        final OnnxModellRegistry modellRegistry = new OnnxModellRegistry(properties, List.of(new ReineRadwerteV1Encoder()));

        assertThat(modellRegistry.findeModell(Zaehldauer.DAUER_13_STUNDEN, Hochrechnungsziel.RAD).stream().toList(), empty());
    }

}
