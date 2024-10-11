package de.muenchen.dave.domain.pdf.assets;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.muenchen.dave.domain.enums.AssetType;
import de.muenchen.dave.domain.pdf.MustacheBean;
import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = ImageAsset.class, name = "IMAGE"),
                @JsonSubTypes.Type(value = TextAsset.class, name = "TEXT"),
                @JsonSubTypes.Type(value = HeadingAsset.class, name = "HEADING1"),
                @JsonSubTypes.Type(value = HeadingAsset.class, name = "HEADING2"),
                @JsonSubTypes.Type(value = HeadingAsset.class, name = "HEADING3"),
                @JsonSubTypes.Type(value = HeadingAsset.class, name = "HEADING4"),
                @JsonSubTypes.Type(value = HeadingAsset.class, name = "HEADING5"),
                @JsonSubTypes.Type(value = PagebreakAsset.class, name = "PAGEBREAK"),
                @JsonSubTypes.Type(value = NewlineAsset.class, name = "NEWLINE"),
                @JsonSubTypes.Type(value = LogoAsset.class, name = "LOGO"),
                @JsonSubTypes.Type(value = DatatableAsset.class, name = "DATATABLE"),
                @JsonSubTypes.Type(value = MessstelleDatatableAsset.class, name = "DATATABLE_MESSSTELLE"),
                @JsonSubTypes.Type(value = ZaehlungskenngroessenAsset.class, name = "ZAEHLUNGSKENNGROESSEN") }
)
@Data
public class BaseAsset implements MustacheBean {
    private AssetType type;
}
