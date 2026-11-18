package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.relational.zaehlstellen.ZaehlungEntity;
import de.muenchen.dave.domain.enums.Zaehlart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BelastungsplanDataServiceFactory {

    private final BelastungsplanDataDefaultService belastungsplanDataDefaultService;
    private final BelastungsplanDataQjsService belastungsplanDataQjsService;
    private final BelastungsplanDataFjsService belastungsplanDataFjsService;
    private final BelastungsplanDataQuService belastungsplanDataQuService;

    public IBelastungsplanDataService getBelastungsplanDataService(ZaehlungEntity zaehlung) {
        if (Zaehlart.QJS.equals(zaehlung.getZaehlart())) {
            return belastungsplanDataQjsService;
        } else if (Zaehlart.FJS.equals(zaehlung.getZaehlart())) {
            return belastungsplanDataFjsService;
        } else if (Zaehlart.QU.equals(zaehlung.getZaehlart())) {
            return belastungsplanDataQuService;
        }
        return belastungsplanDataDefaultService;
    }
}
