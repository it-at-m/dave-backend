package de.muenchen.dave.properties;

import de.muenchen.dave.domain.enums.Hochrechnungskategorie;
import de.muenchen.dave.domain.enums.ModelInputSchema;
import de.muenchen.dave.domain.enums.Zaehldauer;
import lombok.Data;

/**
 * Technische Beschreibung eines ONNX-Modells aus {@code dave.onnx.modelle}.
 *
 * <p>
 * Die Zaehlungsdauer bestimmt Zeitfenster und Tensorlaenge. Diese Angaben werden deshalb nicht
 * erneut pro Modell konfiguriert.
 * </p>
 */
@Data
public class OnnxModelDefinition {

    private String id;

    private Hochrechnungskategorie hochrechnungskategorie;

    private Zaehldauer zaehldauer;

    private String resourcePath;

    private String inputTensorName;

    private ModelInputSchema inputSchema;

}
