package de.muenchen.dave.domain.pdf.assets;

import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDto;
import de.muenchen.dave.domain.pdf.helper.DatentabellePdfZaehldaten;
import lombok.Data;

@Data
public class MessstelleDatatableAsset extends BaseAsset {

    private DatentabellePdfZaehldaten datentabelleZaehldaten;

    private String randomTableId;

    private String text;

    private MessstelleOptionsDto options;

    private String mstId;

}
