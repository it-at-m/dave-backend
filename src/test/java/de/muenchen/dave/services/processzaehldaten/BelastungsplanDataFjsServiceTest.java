package de.muenchen.dave.services.processzaehldaten;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.muenchen.dave.domain.Laengsverkehr;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractBelastungsplanDataDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractLadeBelastungsplanDTO;
import de.muenchen.dave.domain.dtos.laden.BelastungsplanFjsDataDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.Knotenarm;
import de.muenchen.dave.domain.elasticsearch.PkwEinheit;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Bewegungsrichtung;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import de.muenchen.dave.domain.enums.Zaehlart;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        final Map<Laengsverkehr, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeLaengsverkehr = new HashMap<>();

        var laengsverkehr = new Laengsverkehr();
        laengsverkehr.setKnotenarm(2);
        laengsverkehr.setRichtung(Bewegungsrichtung.EIN);
        laengsverkehr.setStrassenseite(Himmelsrichtung.N);

        LadeZaehldatumDTO ladeZaehldatum = new LadeZaehldatumDTO();
        ladeZaehldatum.setFahrradfahrer(6);
        ladeZaehldatum.setFussgaenger(7);

        zaehldatenJeLaengsverkehr.put(laengsverkehr, new ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum(false, ladeZaehldatum));

        laengsverkehr = new Laengsverkehr();
        laengsverkehr.setKnotenarm(4);
        laengsverkehr.setRichtung(Bewegungsrichtung.EIN);
        laengsverkehr.setStrassenseite(Himmelsrichtung.N);

        ladeZaehldatum = new LadeZaehldatumDTO();
        ladeZaehldatum.setFahrradfahrer(60);
        ladeZaehldatum.setFussgaenger(70);

        zaehldatenJeLaengsverkehr.put(laengsverkehr, new ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum(false, ladeZaehldatum));

        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setKategorien(Arrays.asList(Fahrzeug.RAD, Fahrzeug.FUSS));
        zaehlung.setZaehlart(Zaehlart.FJS.toString());
        zaehlung.setKreisverkehr(false);

        final Map<Fahrzeug, AbstractBelastungsplanDataDTO> belastungsplanData = new BelastungsplanDataFjsService()
                .buildBelastungsplanDataMap(zaehldatenJeLaengsverkehr, zaehlung);

        List<BelastungsplanFjsDataDTO.KnotenarmValue> valuesKnotenarmeFuss = ((BelastungsplanFjsDataDTO) belastungsplanData.get(Fahrzeug.FUSS))
                .getValuesKnotenarme();
        assertNotNull(valuesKnotenarmeFuss);
        assertEquals(2, valuesKnotenarmeFuss.size());

        assertNotNull(valuesKnotenarmeFuss.stream().filter(kn -> kn.getKnotenarm() == 2).findFirst().orElseThrow().getValuesStrassenseiten());
        assertEquals(1, valuesKnotenarmeFuss.stream().filter(kn -> kn.getKnotenarm() == 2).findFirst().orElseThrow().getValuesStrassenseiten().size());

        assertNotNull(valuesKnotenarmeFuss.stream().filter(kn -> kn.getKnotenarm() == 4).findFirst().orElseThrow().getValuesStrassenseiten());
        assertEquals(1, valuesKnotenarmeFuss.stream().filter(kn -> kn.getKnotenarm() == 4).findFirst().orElseThrow().getValuesStrassenseiten().size());

        assertLaengsverkehr(valuesKnotenarmeFuss, 2, Himmelsrichtung.N, Bewegungsrichtung.EIN, 7);
        assertLaengsverkehr(valuesKnotenarmeFuss, 4, Himmelsrichtung.N, Bewegungsrichtung.EIN, 70);

        List<BelastungsplanFjsDataDTO.KnotenarmValue> valuesKnotenarmeRad = ((BelastungsplanFjsDataDTO) belastungsplanData.get(Fahrzeug.RAD))
                .getValuesKnotenarme();
        assertNotNull(valuesKnotenarmeRad);
        assertEquals(2, valuesKnotenarmeRad.size());

        assertNotNull(valuesKnotenarmeRad.stream().filter(kn -> kn.getKnotenarm() == 2).findFirst().orElseThrow().getValuesStrassenseiten());
        assertEquals(1, valuesKnotenarmeRad.stream().filter(kn -> kn.getKnotenarm() == 2).findFirst().orElseThrow().getValuesStrassenseiten().size());

        assertNotNull(valuesKnotenarmeRad.stream().filter(kn -> kn.getKnotenarm() == 4).findFirst().orElseThrow().getValuesStrassenseiten());
        assertEquals(1, valuesKnotenarmeRad.stream().filter(kn -> kn.getKnotenarm() == 4).findFirst().orElseThrow().getValuesStrassenseiten().size());

        assertLaengsverkehr(valuesKnotenarmeRad, 2, Himmelsrichtung.N, Bewegungsrichtung.EIN, 6);
        assertLaengsverkehr(valuesKnotenarmeRad, 4, Himmelsrichtung.N, Bewegungsrichtung.EIN, 60);
    }

    @Test
    public void testBuildLadeBelastungsplanDTO() {
        final OptionsDTO options = new OptionsDTO();
        options.setFussverkehr(true);
        options.setRadverkehr(false);
        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setKategorien(List.of(Fahrzeug.FUSS));
        zaehlung.setPkwEinheit(new PkwEinheit());
        var kn1 = new Knotenarm();
        kn1.setNummer(1);
        kn1.setStrassenname("Eins");
        var kn3 = new Knotenarm();
        kn3.setNummer(3);
        kn3.setStrassenname("Drei");
        zaehlung.setKnotenarme(List.of(kn1, kn3));
        List<Zeitintervall> zeitintervalle = new ArrayList<>();
        var lv = new Laengsverkehr();
        lv.setKnotenarm(1);
        lv.setRichtung(Bewegungsrichtung.EIN);
        lv.setStrassenseite(Himmelsrichtung.W);
        Zeitintervall zi1 = new Zeitintervall();
        zi1.setFussgaenger(99);
        zi1.setLaengsverkehr(lv);
        zi1.setStartUhrzeit(LocalDateTime.now());
        zi1.setEndeUhrzeit(LocalDateTime.now());
        zeitintervalle.add(zi1);

        AbstractLadeBelastungsplanDTO<?> data = new BelastungsplanDataFjsService().buildLadeBelastungsplanDTO(options, zaehlung, zeitintervalle);
        assertNotNull(data);
        assertEquals("Eins", data.getStreets()[0]);
        assertNull(data.getStreets()[1]);
        assertEquals("Drei", data.getStreets()[2]);
        assertNotNull(data.getValue1());
        BelastungsplanFjsDataDTO value1 = (BelastungsplanFjsDataDTO) data.getValue1();
        assertEquals(BigDecimal.valueOf(99), value1.getValuesKnotenarme().getFirst().getSumKnotenarm());
        assertEquals(BigDecimal.valueOf(99), value1.getValuesKnotenarme().getFirst().getValuesStrassenseiten().getFirst().getSumStrassenseite());
        assertEquals(BigDecimal.valueOf(99),
                value1.getValuesKnotenarme().getFirst().getValuesStrassenseiten().getFirst().getValuesLaengsverkehre().getFirst().getValue());
    }

    private void assertLaengsverkehr(List<BelastungsplanFjsDataDTO.KnotenarmValue> values, int knotenarm, Himmelsrichtung strassenseite,
            Bewegungsrichtung richtung,
            int expectedValue) {
        BelastungsplanFjsDataDTO.KnotenarmValue knotenarmValue = values.stream().filter(kn -> kn.getKnotenarm() == knotenarm).findFirst().orElseThrow();
        BelastungsplanFjsDataDTO.StrassenseiteValue strassenseiteValue = knotenarmValue.getValuesStrassenseiten().stream()
                .filter(seite -> seite.getStrassenseite() == strassenseite).findFirst().orElseThrow();
        BelastungsplanFjsDataDTO.LaengsverkehrValue laengsverkehrValue = strassenseiteValue.getValuesLaengsverkehre().stream()
                .filter(lv -> lv.getRichtung() == richtung).findFirst().orElseThrow();
        assertThat(laengsverkehrValue.getValue(), is(BigDecimal.valueOf(expectedValue)));
    }

}
