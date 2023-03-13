package de.muenchen.dave.domain.pdf.helper;

import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class ZaehlungskenngroessenData implements Comparable<ZaehlungskenngroessenData> {
    private String type;
    private String startUhrzeit;
    private String endeUhrzeit;
    private String kfz;
    private String schwerverkehr;
    private String gueterverkehr;
    private String fahrradfahrer;
    private String fussgaenger;

    /**
     * Listen mit Objekten der Klasse ZaehlungskenngroessenData sollen nach type in der Reihenfolge des unten erstellten order-Arrays sortiert werden.
     *
     * @param o Zu vergleichendes Objekt
     * @return Vergleichsergebnis
     */
    public int compareTo(final ZaehlungskenngroessenData o) {
        final List<String> order = Arrays.asList(
                LadeZaehldatenService.SPITZENSTUNDE_BLOCK_KFZ,
                LadeZaehldatenService.SPITZENSTUNDE_TAG_KFZ,
                LadeZaehldatenService.SPITZENSTUNDE_BLOCK_RAD,
                LadeZaehldatenService.SPITZENSTUNDE_TAG_RAD,
                LadeZaehldatenService.SPITZENSTUNDE_BLOCK_FUSS,
                LadeZaehldatenService.SPITZENSTUNDE_TAG_FUSS,
                LadeZaehldatenService.BLOCK,
                LadeZaehldatenService.TAGESWERT,
                LadeZaehldatenService.GESAMT
        );

        return order.indexOf(this.type) - order.indexOf(o.type);
    }
}
