package de.muenchen.dave.migrationespg.domain.csv;

import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.ZaehlstelleEntity;
import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.ZaehlungEntity;
import lombok.Data;

@Data
public class CsvMetaObject {

    private ZaehlstelleEntity zaehlstelle;

    private ZaehlungEntity zaehlung;
}
