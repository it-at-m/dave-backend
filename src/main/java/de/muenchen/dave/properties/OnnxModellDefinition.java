package de.muenchen.dave.properties;

import de.muenchen.dave.domain.enums.Hochrechnungsziel;
import de.muenchen.dave.domain.enums.ModelleingabeSchema;
import de.muenchen.dave.domain.enums.Zaehldauer;
import lombok.Data;

@Data
public class OnnxModellDefinition {

    private String id;

    private String version;

    private Hochrechnungsziel ziel;

    private Zaehldauer zaehldauer;

    private String resourcePath;

    private String inputTensorName;

    private ModelleingabeSchema inputSchema;

}
