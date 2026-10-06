package de.muenchen.dave.configuration;

import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiLocaleCustomizer;
import org.springframework.stereotype.Component;

@Component
class OpenApiConfiguration implements OpenApiLocaleCustomizer {

    @Override
    public void customise(final io.swagger.v3.oas.models.OpenAPI openApi, final java.util.Locale locale) {
        Schema<?> pageMetadata = openApi.getComponents()
                .getSchemas()
                .get("PageMetadata");

        if (pageMetadata != null) {
            pageMetadata.setType("object");
        }
    }
}
