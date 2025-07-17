package de.muenchen.dave.domain.pdf.helper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DatatableTitle {

    private String text;

    private String cssClass;

    private String cssClassOngoing;
}
