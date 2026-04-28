package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.dtos.laden.AbstractBelastungsplanDataDTO;
import de.muenchen.dave.domain.dtos.laden.BelastungsplanQjsDataDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import de.muenchen.dave.domain.enums.Zaehlart;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BelastungsplanDataQjsServiceTest {

    @Test
    public void testGetBelastungsplanData() {
        final Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeVerkehrsbeziehung = new HashMap<>();

        Verkehrsbeziehung verkehrsbeziehung = new Verkehrsbeziehung();
        verkehrsbeziehung.setVon(2);
        verkehrsbeziehung.setNach(4);
        verkehrsbeziehung.setStrassenseite(Himmelsrichtung.N);

        LadeZaehldatumDTO ladeZaehldatum = new LadeZaehldatumDTO();
        ladeZaehldatum.setFahrradfahrer(6);
        ladeZaehldatum.setFussgaenger(7);

        zaehldatenJeVerkehrsbeziehung.put(verkehrsbeziehung, new ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum(false, ladeZaehldatum));

        verkehrsbeziehung = new Verkehrsbeziehung();
        verkehrsbeziehung.setVon(4);
        verkehrsbeziehung.setNach(2);
        verkehrsbeziehung.setStrassenseite(Himmelsrichtung.N);

        ladeZaehldatum = new LadeZaehldatumDTO();
        ladeZaehldatum.setFahrradfahrer(60);
        ladeZaehldatum.setFussgaenger(70);

        zaehldatenJeVerkehrsbeziehung.put(verkehrsbeziehung, new ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum(false, ladeZaehldatum));

        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setKategorien(Arrays.asList(Fahrzeug.RAD, Fahrzeug.FUSS));
        zaehlung.setZaehlart(Zaehlart.QJS.toString());
        zaehlung.setKreisverkehr(false);

        final Map<Fahrzeug, AbstractBelastungsplanDataDTO> belastungsplanData = new BelastungsplanDataQjsService()
                .getBelastungsplanData(zaehldatenJeVerkehrsbeziehung, zaehlung);

        List<BelastungsplanQjsDataDTO.VerkehrsbeziehungValue> valuesFuss = ((BelastungsplanQjsDataDTO) belastungsplanData.get(Fahrzeug.FUSS))
                .getValuesVerkehrsbeziehungen();
        assertVerkehrsbeziehung(valuesFuss, 2, 4, Himmelsrichtung.N, 7);
        assertVerkehrsbeziehung(valuesFuss, 4, 2, Himmelsrichtung.N, 70);
        List<BelastungsplanQjsDataDTO.VerkehrsbeziehungValue> valuesRad = ((BelastungsplanQjsDataDTO) belastungsplanData.get(Fahrzeug.RAD))
                .getValuesVerkehrsbeziehungen();
        assertVerkehrsbeziehung(valuesRad, 2, 4, Himmelsrichtung.N, 6);
        assertVerkehrsbeziehung(valuesRad, 4, 2, Himmelsrichtung.N, 60);
    }

    private void assertVerkehrsbeziehung(List<BelastungsplanQjsDataDTO.VerkehrsbeziehungValue> values, int von, int nach, Himmelsrichtung strassenseite,
            int expectedValue) {
        assertThat(values
                .stream()
                .filter(vb -> vb.getVon() == von && vb.getNach() == nach && vb.getStrassenseite().equals(strassenseite))
                .map(BelastungsplanQjsDataDTO.VerkehrsbeziehungValue::getValue).findFirst().orElse(BigDecimal.ZERO),
                is(BigDecimal.valueOf(expectedValue)));
    }

}
