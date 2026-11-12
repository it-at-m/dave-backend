package de.muenchen.dave.migrationespg.services.prozesszaehldaten;

import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.ZaehlungEntity;
import de.muenchen.dave.domain.enums.Zaehlart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BelastungsplanDataServiceFactoryMig {

    private final BelastungsplanDataDefaultServiceMig belastungsplanDataDefaultServiceMig;
    private final BelastungsplanDataQjsServiceMig belastungsplanDataQjsServiceMig;
    private final BelastungsplanDataFjsServiceMig belastungsplanDataFjsServiceMig;
    private final BelastungsplanDataQuServiceMig belastungsplanDataQuServiceMig;

    public IBelastungsplanDataService getBelastungsplanDataService(ZaehlungEntity zaehlung) {
        if (Zaehlart.QJS.equals(zaehlung.getZaehlart())) {
            return belastungsplanDataQjsServiceMig;
        } else if (Zaehlart.FJS.equals(zaehlung.getZaehlart())) {
            return belastungsplanDataFjsServiceMig;
        } else if (Zaehlart.QU.equals(zaehlung.getZaehlart())) {
            return belastungsplanDataQuServiceMig;
        }
        return belastungsplanDataDefaultServiceMig;
    }
}
