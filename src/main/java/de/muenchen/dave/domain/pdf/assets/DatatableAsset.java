package de.muenchen.dave.domain.pdf.assets;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.pdf.helper.DatentabellePdfZaehldaten;
import lombok.Data;

@Data
public class DatatableAsset extends BaseAsset {

    private DatentabellePdfZaehldaten datentabelleZaehldaten;

    private String randomTableId;

    private String text;

    private OptionsDTO options;

    private String zaehlungId;

}