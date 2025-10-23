package de.muenchen.dave.domain.pdf.assets;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LogoAsset extends BaseAsset {
    private String logoIcon;

    private String logoSubtitle;

}
