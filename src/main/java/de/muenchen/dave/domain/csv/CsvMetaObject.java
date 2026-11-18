package de.muenchen.dave.domain.csv;

import de.muenchen.dave.relational.zaehlstellen.ZaehlstelleEntity;
import de.muenchen.dave.relational.zaehlstellen.ZaehlungEntity;
import lombok.Data;

@Data
public class CsvMetaObject {

    private ZaehlstelleEntity zaehlstelle;

    private ZaehlungEntity zaehlung;
}
