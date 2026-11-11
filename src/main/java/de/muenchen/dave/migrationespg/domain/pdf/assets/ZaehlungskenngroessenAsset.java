package de.muenchen.dave.migrationespg.domain.pdf.assets;

import de.muenchen.dave.domain.pdf.assets.BaseAsset;
import de.muenchen.dave.domain.pdf.helper.ZaehlungskenngroessenData;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class ZaehlungskenngroessenAsset extends BaseAsset {
    private String text;
    private UUID zaehlungId;
    private List<ZaehlungskenngroessenData> zaehldaten;
    private boolean printFuss;
    private boolean printRad;
    private boolean printKfz;
}
