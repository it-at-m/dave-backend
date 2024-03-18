package de.muenchen.dave.domain.pdf.assets;

import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.domain.pdf.helper.DatentabellePdfZaehldaten;
import lombok.Data;

@Data
public class MessstelleDatatableAsset extends BaseAsset {

    private DatentabellePdfZaehldaten datentabelleZaehldaten;

    private String randomTableId;

    private String text;

    private MessstelleOptionsDTO options;

    private String mstId;

}
