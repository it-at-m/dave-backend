package de.muenchen.dave.domain.csv;

import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import lombok.Data;

@Data
public class CsvMetaObject {

    private Zaehlstelle zaehlstelle;

    private Zaehlung zaehlung;
}
