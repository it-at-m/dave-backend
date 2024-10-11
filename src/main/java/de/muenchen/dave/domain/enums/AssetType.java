package de.muenchen.dave.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AssetType {
    TEXT("TEXT"), HEADING1("HEADING1"), HEADING2("HEADING2"), HEADING3("HEADING3"), HEADING4("HEADING4"), HEADING5("HEADING5"), IMAGE("IMAGE"), PAGEBREAK(
            "PAGEBREAK"), NEWLINE("NEWLINE"), LOGO(
            "LOGO"), DATATABLE("DATATABLE"), DATATABLE_MESSSTELLE("DATATABLE_MESSSTELLE"), ZAEHLUNGSKENNGROESSEN("ZAEHLUNGSKENNGROESSEN");

    private final String type;
}
