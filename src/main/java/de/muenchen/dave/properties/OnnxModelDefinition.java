package de.muenchen.dave.properties;

import de.muenchen.dave.domain.enums.Hochrechnungskategorie;
import de.muenchen.dave.domain.enums.ModelInputSchema;
import de.muenchen.dave.domain.enums.Zaehldauer;
import lombok.Data;

@Data
public class OnnxModelDefinition {

    private String id;

    private String version;

    private Hochrechnungskategorie hochrechnungskategorie;

    private Zaehldauer zaehldauer;

    private String resourcePath;

    private String inputTensorName;

    private ModelInputSchema inputSchema;

}
