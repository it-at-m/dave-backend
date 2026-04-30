package de.muenchen.dave.services.processzaehldaten;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractBelastungsplanDataDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractLadeBelastungsplanDTO;
import de.muenchen.dave.domain.dtos.laden.BelastungsplanFjsDataDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.Knotenarm;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import de.muenchen.dave.domain.enums.Zaehlart;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BelastungsplanDataFjsServiceTest {

    @Test
    public void testBuildBelastungsplanDataMap() {
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
        zaehlung.setZaehlart(Zaehlart.FJS.toString());
        zaehlung.setKreisverkehr(false);

        final Map<Fahrzeug, AbstractBelastungsplanDataDTO> belastungsplanData = new BelastungsplanDataFjsService()
                .buildBelastungsplanDataMap(zaehldatenJeVerkehrsbeziehung, zaehlung);

        List<BelastungsplanFjsDataDTO.LaengsverkehrValue> valuesFuss = ((BelastungsplanFjsDataDTO) belastungsplanData.get(Fahrzeug.FUSS))
                .getValuesLaengsverkehr();
        assertLaengsverkehr(valuesFuss, 2, Himmelsrichtung.N, 7);
        assertLaengsverkehr(valuesFuss, 4, Himmelsrichtung.N, 70);
        List<BelastungsplanFjsDataDTO.LaengsverkehrValue> valuesRad = ((BelastungsplanFjsDataDTO) belastungsplanData.get(Fahrzeug.RAD))
                .getValuesLaengsverkehr();
        assertLaengsverkehr(valuesRad, 2, Himmelsrichtung.N, 6);
        assertLaengsverkehr(valuesRad, 4, Himmelsrichtung.N, 60);
    }

    @Test
    public void testBuildLadeBelastungsplanDTO() {
        final OptionsDTO options = new OptionsDTO();
        options.setFussverkehr(true);
        options.setRadverkehr(false);
        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setKategorien(List.of(Fahrzeug.FUSS));
        var kn1 = new Knotenarm();
        kn1.setNummer(1);
        kn1.setStrassenname("Eins");
        var kn3 = new Knotenarm();
        kn3.setNummer(3);
        kn3.setStrassenname("Drei");
        zaehlung.setKnotenarme(List.of(kn1, kn3));
        final Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> ladeZaehldatumBelastungsplan = new HashMap<>();
        var vb = new Verkehrsbeziehung();
        vb.setVon(1);
        vb.setNach(3);
        vb.setStrassenseite(Himmelsrichtung.W);
        boolean isTageswert = true;
        LadeZaehldatumDTO ladeZaehldatum = new LadeZaehldatumDTO();
        ladeZaehldatum.setFussgaenger(99);
        ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum tupel = new ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum(isTageswert,
                ladeZaehldatum);
        ladeZaehldatumBelastungsplan.put(vb, tupel);
        AbstractLadeBelastungsplanDTO<?> data = new BelastungsplanDataFjsService().buildLadeBelastungsplanDTO(options, zaehlung, ladeZaehldatumBelastungsplan);
        assertNotNull(data);
        assertEquals("Eins", data.getStreets()[0]);
        assertNull(data.getStreets()[1]);
        assertEquals("Drei", data.getStreets()[2]);
        assertNotNull(data.getValue1());
        BelastungsplanFjsDataDTO value1 = (BelastungsplanFjsDataDTO) data.getValue1();
        assertEquals(BigDecimal.valueOf(99), value1.getSumAll());
        assertEquals(BigDecimal.valueOf(99), value1.getValuesStrassenseite().getFirst().getValue());
        assertEquals(BigDecimal.valueOf(99), value1.getValuesLaengsverkehr().getFirst().getValue());
    }

    private void assertLaengsverkehr(List<BelastungsplanFjsDataDTO.LaengsverkehrValue> values, int von, Himmelsrichtung strassenseite,
            int expectedValue) {
        assertThat(values
                .stream()
                .filter(vb -> vb.getVon() == von && vb.getStrassenseite().equals(strassenseite))
                .map(BelastungsplanFjsDataDTO.LaengsverkehrValue::getValue).findFirst().orElse(BigDecimal.ZERO),
                is(BigDecimal.valueOf(expectedValue)));
    }

}
