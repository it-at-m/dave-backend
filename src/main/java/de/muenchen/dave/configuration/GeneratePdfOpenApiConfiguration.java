package de.muenchen.dave.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.Encoding;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beschreibt den Multipart-Request für die Report-PDF-Erzeugung explizit,
 * damit Generatoren das Asset-Array als JSON-Part und nicht als CSV-Formfeld
 * interpretieren.
 */
@Configuration
public class GeneratePdfOpenApiConfiguration {

    private static final String GENERATE_PDF_REPORT_PATH = "/generate-pdf/report";
    private static final String GENERATE_ZAEHLUNG_PDF_PATH = "/generate-pdf/zaehlung";
    private static final String GENERATE_MESSSTELLE_PDF_PATH = "/generate-pdf/messstelle";
    private static final String GENERATE_AUSWERTUNG_PDF_PATH = "/generate-pdf/auswertung";
    private static final String ZAEHLUNG_REQUEST_SCHEMA = "GenerateZaehlungPdfRequest";
    private static final String MESSSTELLE_REQUEST_SCHEMA = "GenerateMessstellePdfRequest";
    private static final String REPORT_REQUEST_SCHEMA = "GeneratePdfReportRequest";
    private static final String AUSWERTUNG_REQUEST_SCHEMA = "GeneratePdfAuswertungRequest";
    private static final String REPORT_ASSET_SCHEMA = "GeneratePdfReportAsset";
    private static final String MULTIPART_FORM_DATA = "multipart/form-data";
    private static final String APPLICATION_PDF = "application/pdf";

    @Bean
    OpenApiCustomizer generatePdfOpenApiCustomizer() {
        return openApi -> {
            final Components components = openApi.getComponents();
            components.addSchemas(ZAEHLUNG_REQUEST_SCHEMA, zaehlungRequestSchema());
            components.addSchemas(MESSSTELLE_REQUEST_SCHEMA, messstelleRequestSchema());
            components.addSchemas(REPORT_ASSET_SCHEMA, reportAssetSchema());
            components.addSchemas(REPORT_REQUEST_SCHEMA, reportRequestSchema());
            components.addSchemas(AUSWERTUNG_REQUEST_SCHEMA, auswertungRequestSchema());

            customizePdfOperation(openApi, GENERATE_ZAEHLUNG_PDF_PATH, ZAEHLUNG_REQUEST_SCHEMA,
                    List.of("options"), List.of("department", "chartAsBase64Png", "schematischeUebersichtAsBase64Png"));
            customizePdfOperation(openApi, GENERATE_MESSSTELLE_PDF_PATH, MESSSTELLE_REQUEST_SCHEMA,
                    List.of("options", "messswerte"),
                    List.of("department", "chartAsBase64Png", "schematischeUebersichtAsBase64Png"));
            customizePdfOperation(openApi, GENERATE_PDF_REPORT_PATH, REPORT_REQUEST_SCHEMA,
                    List.of("assets"), List.of("department"));
            customizePdfOperation(openApi, GENERATE_AUSWERTUNG_PDF_PATH, AUSWERTUNG_REQUEST_SCHEMA,
                    List.of("options", "auswertung"), List.of("department", "chartAsBase64Png"));
        };
    }

    private void customizePdfOperation(final OpenAPI openApi, final String path, final String requestSchema,
            final List<String> jsonParts, final List<String> textParts) {
        final var operation = openApi.getPaths().get(path).getPost();
        final MediaType multipartContent = new MediaType().schema(reference(requestSchema));
        jsonParts.forEach(part -> multipartContent.addEncoding(part, new Encoding().contentType("application/json")));
        textParts.forEach(part -> multipartContent.addEncoding(part, new Encoding().contentType("text/plain")));

        operation.setRequestBody(new RequestBody()
                .required(true)
                .content(new Content().addMediaType(MULTIPART_FORM_DATA, multipartContent)));
        operation.getResponses().addApiResponse("200", new ApiResponse()
                .description("PDF-Datei")
                .content(new Content().addMediaType(
                        APPLICATION_PDF,
                        new MediaType().schema(new Schema<>().type("string").format("binary")))));
    }

    private Schema<?> zaehlungRequestSchema() {
        return new ObjectSchema()
                .addProperty("department", new StringSchema().minLength(1))
                .addProperty("options", reference("OptionsDTO"))
                .addProperty("chartAsBase64Png", new StringSchema())
                .addProperty("schematischeUebersichtAsBase64Png", new StringSchema())
                .required(List.of("department", "options"));
    }

    private Schema<?> messstelleRequestSchema() {
        return new ObjectSchema()
                .addProperty("department", new StringSchema().minLength(1))
                .addProperty("options", reference("MessstelleOptionsDTO"))
                .addProperty("messswerte", reference("LadeProcessedMesswerteDTO"))
                .addProperty("chartAsBase64Png", new StringSchema())
                .addProperty("schematischeUebersichtAsBase64Png", new StringSchema())
                .required(List.of("department", "options", "messswerte"));
    }

    private Schema<?> reportRequestSchema() {
        return new ObjectSchema()
                .addProperty("assets", new ArraySchema().items(reference(REPORT_ASSET_SCHEMA)))
                .addProperty("department", new StringSchema().minLength(1))
                .required(List.of("assets", "department"));
    }

    private Schema<?> reportAssetSchema() {
        return new Schema<>()
                .addOneOfItem(reference("DatatableAsset"))
                .addOneOfItem(reference("Heading1Asset"))
                .addOneOfItem(reference("Heading2Asset"))
                .addOneOfItem(reference("Heading3Asset"))
                .addOneOfItem(reference("Heading4Asset"))
                .addOneOfItem(reference("Heading5Asset"))
                .addOneOfItem(reference("ImageAsset"))
                .addOneOfItem(reference("MessstelleDatatableAsset"))
                .addOneOfItem(reference("NewlineAsset"))
                .addOneOfItem(reference("PagebreakAsset"))
                .addOneOfItem(reference("TextAsset"))
                .addOneOfItem(reference("ZaehlungskenngroessenAsset"))
                .discriminator(new Discriminator()
                        .propertyName("type")
                        .mapping(Map.ofEntries(
                                Map.entry("DATATABLE", "#/components/schemas/DatatableAsset"),
                                Map.entry("HEADING1", "#/components/schemas/Heading1Asset"),
                                Map.entry("HEADING2", "#/components/schemas/Heading2Asset"),
                                Map.entry("HEADING3", "#/components/schemas/Heading3Asset"),
                                Map.entry("HEADING4", "#/components/schemas/Heading4Asset"),
                                Map.entry("HEADING5", "#/components/schemas/Heading5Asset"),
                                Map.entry("IMAGE", "#/components/schemas/ImageAsset"),
                                Map.entry("DATATABLE_MESSSTELLE", "#/components/schemas/MessstelleDatatableAsset"),
                                Map.entry("NEWLINE", "#/components/schemas/NewlineAsset"),
                                Map.entry("PAGEBREAK", "#/components/schemas/PagebreakAsset"),
                                Map.entry("TEXT", "#/components/schemas/TextAsset"),
                                Map.entry("ZAEHLUNGSKENNGROESSEN", "#/components/schemas/ZaehlungskenngroessenAsset"))));
    }

    private Schema<?> auswertungRequestSchema() {
        return new ObjectSchema()
                .addProperty("department", new StringSchema().minLength(1))
                .addProperty("options", reference("MessstelleAuswertungOptionsDTO"))
                .addProperty("auswertung", reference("LadeZaehldatenSteplineDTO"))
                .addProperty("chartAsBase64Png", new StringSchema())
                .required(List.of("department", "options", "auswertung"));
    }

    private Schema<?> reference(final String schemaName) {
        return new Schema<>().$ref("#/components/schemas/" + schemaName);
    }
}
