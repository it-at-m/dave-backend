package de.muenchen.dave.domain.converter;
// Source - https://stackoverflow.com/a

// Posted by Jonck van der Kogel, modified by community. See post 'Timeline' for change history
// Retrieved 2026-01-13, License - CC BY-SA 4.0

import static java.util.Collections.*;

import de.muenchen.dave.domain.enums.Fahrzeug;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.List;

@Converter
public class FahrzeugListConverter implements AttributeConverter<List<Fahrzeug>, String> {
    private static final String SPLIT_CHAR = ";";

    @Override
    public String convertToDatabaseColumn(List<Fahrzeug> fahrzeugList) {
        String result = "";
        if (fahrzeugList == null) {
            return result;
        }
        List<String> fahrzeugNames = fahrzeugList.stream().map(Fahrzeug::getName).toList();
        return fahrzeugList != null ? String.join(SPLIT_CHAR, fahrzeugNames) : "";
    }

    @Override
    public List<Fahrzeug> convertToEntityAttribute(String string) {
        List<String> result = string != null && !string.isEmpty() ? Arrays.asList(string.split(SPLIT_CHAR)) : emptyList();

        List<Fahrzeug> fahrzeugList = result.stream()
                .map(name -> Arrays.stream(Fahrzeug.values())
                        .filter(fahrzeug -> fahrzeug.getName().equals(name))
                        .findFirst()
                        .orElse(null))
                .filter(fahrzeug -> fahrzeug != null)
                .toList();
        return fahrzeugList;
    }
}
