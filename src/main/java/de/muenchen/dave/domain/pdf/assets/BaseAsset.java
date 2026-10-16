package de.muenchen.dave.domain.pdf.assets;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.muenchen.dave.domain.enums.AssetType;
import de.muenchen.dave.domain.pdf.MustacheBean;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
            @JsonSubTypes.Type(value = DatatableAsset.class, name = "DATATABLE"),
            @JsonSubTypes.Type(value = MessstelleDatatableAsset.class, name = "DATATABLE_MESSSTELLE"),
            @JsonSubTypes.Type(value = ZaehlungskenngroessenAsset.class, name = "ZAEHLUNGSKENNGROESSEN")
    }
)
@Schema(
        name = "BaseAsset",
        discriminatorProperty = "type",
        subTypes = {
                ImageAsset.class,
                TextAsset.class,
                HeadingAsset.class,
                PagebreakAsset.class,
                NewlineAsset.class,
                DatatableAsset.class,
                MessstelleDatatableAsset.class,
                ZaehlungskenngroessenAsset.class
        },
        discriminatorMapping = {
                @DiscriminatorMapping(value = "IMAGE", schema = ImageAsset.class),
                @DiscriminatorMapping(value = "TEXT", schema = TextAsset.class),
                @DiscriminatorMapping(value = "HEADING1", schema = HeadingAsset.class),
                @DiscriminatorMapping(value = "HEADING2", schema = HeadingAsset.class),
                @DiscriminatorMapping(value = "HEADING3", schema = HeadingAsset.class),
                @DiscriminatorMapping(value = "HEADING4", schema = HeadingAsset.class),
                @DiscriminatorMapping(value = "HEADING5", schema = HeadingAsset.class),
                @DiscriminatorMapping(value = "PAGEBREAK", schema = PagebreakAsset.class),
                @DiscriminatorMapping(value = "NEWLINE", schema = NewlineAsset.class),
                @DiscriminatorMapping(value = "DATATABLE", schema = DatatableAsset.class),
                @DiscriminatorMapping(value = "DATATABLE_MESSSTELLE", schema = MessstelleDatatableAsset.class),
                @DiscriminatorMapping(value = "ZAEHLUNGSKENNGROESSEN", schema = ZaehlungskenngroessenAsset.class)
        }
)
@Data
public class BaseAsset implements MustacheBean {
    @NotNull
    private AssetType type;

    @NotNull
    private Long id;
}
