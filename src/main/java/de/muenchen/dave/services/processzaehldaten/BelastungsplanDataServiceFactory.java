package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.elasticsearch.Zaehlung;
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

    public IBelastungsplanDataService getBelastungsplanDataService(Zaehlung zaehlung) {
        if (Zaehlart.QJS.name().equals(zaehlung.getZaehlart())) {
            return belastungsplanDataQjsService;
        } else if (Zaehlart.FJS.name().equals(zaehlung.getZaehlart())) {
            return belastungsplanDataFjsService;
        } else if (Zaehlart.QU.name().equals(zaehlung.getZaehlart())) {
            return belastungsplanDataQuService;
        }
        return belastungsplanDataDefaultService;
    }
}
