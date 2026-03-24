package de.muenchen.dave.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.util.Objects;

public class EmptyListSerializer extends JsonSerializer<Object> {

    /**
     * Schreibt ein leeres JSON-Array "[]" wenn das Objekt im Parameter null ist.
     *
     * Beispielhafte Verwendung in {@link JsonSerialize#nullsUsing()}.
     *
     * @param value zum serialisieren.
     * @param gen Generator um JSON-Content zu erstellen.
     * @param serializers um ggf. Objekte zu serialisieren.
     * @throws IOException n/a
     */
    @Override
    public void serialize(
            final Object value,
            final JsonGenerator gen,
            final SerializerProvider serializers) throws IOException {
        if (Objects.isNull(value)) {
            gen.writeStartArray();
            gen.writeEndArray();
        }
    }
}
