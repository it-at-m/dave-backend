package de.muenchen.dave.domain.pdf.assets;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.pdf.helper.DatentabellePdfZaehldaten;
import java.util.UUID;
import lombok.Data;

@Data
public class DatatableAsset extends BaseAsset {

    private DatentabellePdfZaehldaten datentabelleZaehldaten;

    private String randomTableId;

    private String text;

    private OptionsDTO options;

    private UUID zaehlungId;

}
