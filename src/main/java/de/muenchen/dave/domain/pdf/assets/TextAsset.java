package de.muenchen.dave.domain.pdf.assets;

import lombok.Data;

@Data
public class TextAsset extends BaseAsset {
    private String text;
    private String size;
}
