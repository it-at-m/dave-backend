package de.muenchen.dave.domain.pdf.helper;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ZeitreiheTableRow {

    private String datum;

    private BigDecimal kfz;

    private BigDecimal sv;

    private BigDecimal gv;

    private Integer rad;

    private Integer fuss;

    private BigDecimal svAnteilInProzent;

    private BigDecimal gvAnteilInProzent;

    private BigDecimal gesamt;

}
