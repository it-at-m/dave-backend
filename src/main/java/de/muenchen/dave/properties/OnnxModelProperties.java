package de.muenchen.dave.properties;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Bindet die Liste der zur Laufzeit verfuegbaren ONNX-Modelle aus der Anwendungskonfiguration.
 */
@Component
@ConfigurationProperties(prefix = "dave.onnx")
@Getter
@Setter
public class OnnxModelProperties {

    private List<OnnxModelDefinition> modelle = new ArrayList<>();

}
