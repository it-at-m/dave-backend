package de.muenchen.dave.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LadeZaehldatenOpenApiConfiguration {

    private static final String ABSTRACT_SCHEMA = "AbstractLadeBelastungsplanDTO";
    private static final String PROCESSED_BELASTUNGSPLAN_SCHEMA = "LadeProcessedZaehldatenBelastungsplan";

    private static final List<String> DATA_SCHEMAS = List.of(
            "BelastungsplanDataDTO",
            "BelastungsplanFjsDataDTO",
            "BelastungsplanQjsDataDTO",
            "BelastungsplanQuDataDTO"
    );

    @Bean
    public OpenApiCustomizer ladeZaehldatenOpenApiCustomizer() {
        return openAPI -> {
            final Components components = openAPI.getComponents();
            if (components == null || components.getSchemas() == null) {
                return;
            }

            final Schema abstractSchema = findAbstractBelastungsplanSchema(components);
            if (abstractSchema == null || abstractSchema.getProperties() == null) {
                return;
            }

            final List<Schema> dataSchemaReferences = DATA_SCHEMAS.stream()
                    .map(schemaName -> new Schema<>().$ref("#/components/schemas/" + schemaName))
                    .toList();

            final Map<String, Schema> properties = abstractSchema.getProperties();
            for (final String propertyName : List.of("value1", "value2", "value3")) {
                final Schema property = properties.get(propertyName);
                if (property != null) {
                    property.setType(null);
                    property.set$ref(null);
                    property.setOneOf(dataSchemaReferences);
                }
            }

            addBelastungsplanDiscriminator(components);
        };
    }

    private void addBelastungsplanDiscriminator(final Components components) {
        final Schema processedSchema = components.getSchemas().get("LadeProcessedZaehldatenDTO");
        if (processedSchema == null || processedSchema.getProperties() == null) {
            return;
        }

        final Schema belastungsplanProperty = (Schema) processedSchema.getProperties().get("zaehldatenBelastungsplan");
        if (belastungsplanProperty == null) {
            return;
        }

        final Schema belastungsplanSchema = new Schema<>()
                .oneOf(List.of(
                        schemaWrapper("LadeBelastungsplanDTO"),
                        schemaWrapper("LadeBelastungsplanFjsDTO"),
                        schemaWrapper("LadeBelastungsplanQjsDTO"),
                        schemaWrapper("LadeBelastungsplanQuDTO")
                ))
                .discriminator(new Discriminator()
                .propertyName("belastungsplanTyp")
                .mapping(Map.of(
                        "DEFAULT", "#/components/schemas/LadeBelastungsplanDTO",
                        "FJS", "#/components/schemas/LadeBelastungsplanFjsDTO",
                        "QJS", "#/components/schemas/LadeBelastungsplanQjsDTO",
                        "QU", "#/components/schemas/LadeBelastungsplanQuDTO"
                )));

        components.addSchemas(PROCESSED_BELASTUNGSPLAN_SCHEMA, belastungsplanSchema);
        belastungsplanProperty.setType(null);
        belastungsplanProperty.setOneOf(null);
        belastungsplanProperty.setDiscriminator(null);
        belastungsplanProperty.set$ref("#/components/schemas/" + PROCESSED_BELASTUNGSPLAN_SCHEMA);
    }

    private Schema schemaWrapper(final String schemaName) {
        return new Schema<>().allOf(List.of(new Schema<>().$ref("#/components/schemas/" + schemaName)));
    }

    private Schema findAbstractBelastungsplanSchema(final Components components) {
        final Schema namedSchema = components.getSchemas().get(ABSTRACT_SCHEMA);
        if (isBelastungsplanSchema(namedSchema)) {
            return namedSchema;
        }
        return components.getSchemas().values().stream()
                .filter(this::isBelastungsplanSchema)
                .findFirst()
                .orElse(null);
    }

    private boolean isBelastungsplanSchema(final Schema schema) {
        return schema != null
                && schema.getProperties() != null
                && schema.getProperties().containsKey("value1")
                && schema.getProperties().containsKey("value2")
                && schema.getProperties().containsKey("value3");
    }
}
