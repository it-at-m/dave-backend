package de.muenchen.dave.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
/**
 * Ergänzt die automatisch erzeugte OpenAPI-Spezifikation für das Laden
 * verarbeiteter Zähldaten um die konkrete Response und die polymorphe
 * Belastungsplan-Struktur.
 *
 * <p>
 * Die Struktur wird im Java-Modell durch Generics und Jackson-Subtypen
 * abgebildet. Diese Informationen werden von Springdoc jedoch nicht so in die
 * OpenAPI-Spezifikation übernommen, dass der TypeScript-Generator die vier
 * Belastungsplan-Varianten samt ihren konkreten {@code value}-Datentypen und
 * dem Discriminator {@code belastungsplanTyp} erkennen kann. Diese
 * Konfiguration beschreibt die Schemas und die {@code oneOf}-Beziehungen daher
 * explizit.
 * </p>
 *
 * <p>
 * Ohne diese Ergänzung würde der generierte TypeScript-Client die Response
 * von {@code /lade-zaehldaten-processed} nur unvollständig oder als nicht
 * näher typisiertes Objekt abbilden. Insbesondere gingen die konkreten
 * Belastungsplan-Varianten und ihre typsichere Unterscheidung verloren.
 * </p>
 */
public class LadeZaehldatenOpenApiConfiguration {

    private static final String PROCESSED_ZAEHLDATEN_PATH = "/lade-zaehldaten-processed";

    @Bean
    OpenApiCustomizer ladeZaehldatenOpenApiCustomizer() {
        return openApi -> {
            final Components components = openApi.getComponents();
            components.addSchemas("LadeBelastungsplanDTO", belastungsplanSchema("DEFAULT", "BelastungsplanDataDTO"));
            components.addSchemas("LadeBelastungsplanFjsDTO", belastungsplanSchema("FJS", "BelastungsplanFjsDataDTO"));
            components.addSchemas("LadeBelastungsplanQjsDTO", belastungsplanSchema("QJS", "BelastungsplanQjsDataDTO"));
            components.addSchemas("LadeBelastungsplanQuDTO", belastungsplanSchema("QU", "BelastungsplanQuDataDTO"));
            components.addSchemas("LadeProcessedZaehldatenBelastungsplanDTO", belastungsplanUnion());
            components.addSchemas("LadeProcessedZaehldatenDTO", processedZaehldatenSchema());

            final ApiResponses responses = openApi.getPaths().get(PROCESSED_ZAEHLDATEN_PATH).getPost().getResponses();
            responses.addApiResponse("200", new ApiResponse()
                    .description("OK")
                    .content(new io.swagger.v3.oas.models.media.Content().addMediaType(
                            "application/json",
                            new io.swagger.v3.oas.models.media.MediaType().schema(reference("LadeProcessedZaehldatenDTO")))));
        };
    }

    private Schema<?> processedZaehldatenSchema() {
        return new ObjectSchema()
                .addProperty("zaehldatenTable", reference("LadeZaehldatenTableDTO"))
                .addProperty("zaehldatenStepline", reference("LadeZaehldatenSteplineDTO"))
                .addProperty("zaehldatenHeatmap", reference("LadeZaehldatenHeatmapDTO"))
                .addProperty("zaehldatenBelastungsplan", reference("LadeProcessedZaehldatenBelastungsplanDTO"))
                .addProperty("zaehldatenZeitreihe", reference("LadeZaehldatenZeitreiheDTO"))
                .required(List.of("zaehldatenTable", "zaehldatenStepline", "zaehldatenHeatmap", "zaehldatenBelastungsplan", "zaehldatenZeitreihe"));
    }

    private Schema<?> belastungsplanUnion() {
        return new Schema<>()
                .addOneOfItem(reference("LadeBelastungsplanDTO"))
                .addOneOfItem(reference("LadeBelastungsplanFjsDTO"))
                .addOneOfItem(reference("LadeBelastungsplanQjsDTO"))
                .addOneOfItem(reference("LadeBelastungsplanQuDTO"))
                .discriminator(new Discriminator()
                        .propertyName("belastungsplanTyp")
                        .mapping(Map.of(
                                "DEFAULT", "#/components/schemas/LadeBelastungsplanDTO",
                                "FJS", "#/components/schemas/LadeBelastungsplanFjsDTO",
                                "QJS", "#/components/schemas/LadeBelastungsplanQjsDTO",
                                "QU", "#/components/schemas/LadeBelastungsplanQuDTO")));
    }

    private Schema<?> belastungsplanSchema(final String belastungsplanTyp, final String dataSchema) {
        return new ObjectSchema()
                .addProperty("belastungsplanTyp", new StringSchema()._enum(List.of(belastungsplanTyp)))
                .addProperty("streets", new ArraySchema().items(new StringSchema()))
                .addProperty("kreisverkehr", new Schema<>().type("boolean"))
                .addProperty("value1", reference(dataSchema))
                .addProperty("value2", reference(dataSchema))
                .addProperty("value3", reference(dataSchema))
                .required(List.of("belastungsplanTyp", "streets", "kreisverkehr", "value1", "value2", "value3"));
    }

    private Schema<?> reference(final String schemaName) {
        return new Schema<>().$ref("#/components/schemas/" + schemaName);
    }
}
