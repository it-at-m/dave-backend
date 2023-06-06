package de.muenchen.dave.domain.pdf.assets;

import lombok.Data;

@Data
public class ImageAsset extends BaseAsset {
    private String caption;
    private String image;
    private Integer width;
}
