package de.muenchen.dave.domain.pdf.assets;

import de.muenchen.dave.domain.pdf.helper.ZaehlungskenngroessenData;
import java.util.List;
import lombok.Data;

@Data
public class ZaehlungskenngroessenAsset extends BaseAsset {
    private String zaehlungId;
    private List<ZaehlungskenngroessenData> zaehldaten;
    private boolean printFuss;
    private boolean printRad;
    private boolean printKfz;
}
