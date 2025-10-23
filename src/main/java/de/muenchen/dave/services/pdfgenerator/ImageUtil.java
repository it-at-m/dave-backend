package de.muenchen.dave.services.pdfgenerator;

import org.apache.commons.codec.binary.Base64;

public class ImageUtil {
    private final static String IMAGE_DATASOURCE_FORMAT = "data:image/jpeg;base64,%s";

    public static String getImageDatasource(final byte[] content){
        return String.format(IMAGE_DATASOURCE_FORMAT, Base64.encodeBase64String(content));
    }
}
