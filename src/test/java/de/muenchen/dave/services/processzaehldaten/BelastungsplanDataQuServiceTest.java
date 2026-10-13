package de.muenchen.dave.services.processzaehldaten;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import de.muenchen.dave.domain.Querungsverkehr;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractBelastungsplanDataDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractLadeBelastungsplanDTO;
import de.muenchen.dave.domain.dtos.laden.BelastungsplanQuDataDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.Knotenarm;
import de.muenchen.dave.domain.elasticsearch.PkwEinheit;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import de.muenchen.dave.domain.enums.Rounding;
import de.muenchen.dave.domain.enums.Zaehlart;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BelastungsplanDataQuServiceTest {

    @Test
    public void testBuildBelastungsplanDataMap() {
        final Map<Querungsverkehr, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeQuerungsverkehr = new HashMap<>();

        var querungsverkehr = new Querungsverkehr();
        querungsverkehr.setKnotenarm(2);
        querungsverkehr.setRichtung(Himmelsrichtung.O);

        LadeZaehldatumDTO ladeZaehldatum = new LadeZaehldatumDTO();
        ladeZaehldatum.setFahrradfahrer(6);
        ladeZaehldatum.setFussgaenger(7);

        zaehldatenJeQuerungsverkehr.put(querungsverkehr, new ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum(false, ladeZaehldatum));

        querungsverkehr = new Querungsverkehr();
        querungsverkehr.setKnotenarm(4);
        querungsverkehr.setRichtung(Himmelsrichtung.O);

        ladeZaehldatum = new LadeZaehldatumDTO();
        ladeZaehldatum.setFahrradfahrer(60);
        ladeZaehldatum.setFussgaenger(70);

        zaehldatenJeQuerungsverkehr.put(querungsverkehr, new ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum(false, ladeZaehldatum));

        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setKategorien(Arrays.asList(Fahrzeug.RAD, Fahrzeug.FUSS));
        zaehlung.setZaehlart(Zaehlart.FJS.toString());
        zaehlung.setKreisverkehr(false);

        final Map<Fahrzeug, AbstractBelastungsplanDataDTO> belastungsplanData = new BelastungsplanDataQuService()
                .buildBelastungsplanDataMap(zaehldatenJeQuerungsverkehr, zaehlung);

        List<BelastungsplanQuDataDTO.KnotenarmValue> valuesKnotenarmeFuss = ((BelastungsplanQuDataDTO) belastungsplanData.get(Fahrzeug.FUSS))
                .getValuesKnotenarme();
        assertNotNull(valuesKnotenarmeFuss);
        assertEquals(2, valuesKnotenarmeFuss.size());

        assertQuerungsverkehr(valuesKnotenarmeFuss, 2, Himmelsrichtung.O, 7);
        assertQuerungsverkehr(valuesKnotenarmeFuss, 4, Himmelsrichtung.O, 70);

        List<BelastungsplanQuDataDTO.KnotenarmValue> valuesKnotenarmeRad = ((BelastungsplanQuDataDTO) belastungsplanData.get(Fahrzeug.RAD))
                .getValuesKnotenarme();
        assertNotNull(valuesKnotenarmeRad);
        assertEquals(2, valuesKnotenarmeRad.size());

        assertQuerungsverkehr(valuesKnotenarmeRad, 2, Himmelsrichtung.O, 6);
        assertQuerungsverkehr(valuesKnotenarmeRad, 4, Himmelsrichtung.O, 60);
    }

    @Test
    public void testBuildLadeBelastungsplanDTO() {
        final OptionsDTO options = new OptionsDTO();
        options.setFussverkehr(true);
        options.setRadverkehr(false);
        options.setRounding(Rounding.NONE);
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
        var qv = new Querungsverkehr();
        qv.setKnotenarm(1);
        qv.setRichtung(Himmelsrichtung.W);
        Zeitintervall zi1 = new Zeitintervall();
        zi1.setFussgaenger(99);
        zi1.setQuerungsverkehr(qv);
        zi1.setStartUhrzeit(LocalDateTime.now());
        zi1.setEndeUhrzeit(LocalDateTime.now());
        zeitintervalle.add(zi1);

        AbstractLadeBelastungsplanDTO<?> data = new BelastungsplanDataQuService().buildLadeBelastungsplanDTO(options, zaehlung, zeitintervalle);
        assertNotNull(data);
        assertEquals("Eins", data.getStreets()[0]);
        assertNull(data.getStreets()[1]);
        assertEquals("Drei", data.getStreets()[2]);
        assertNotNull(data.getValue1());
        BelastungsplanQuDataDTO value1 = (BelastungsplanQuDataDTO) data.getValue1();
        assertEquals(BigDecimal.valueOf(99), value1.getValuesKnotenarme().getFirst().getSumKnotenarm());
        assertEquals(BigDecimal.valueOf(99), value1.getValuesKnotenarme().getFirst().getValuesQuerungsverkehre().getFirst().getValue());
    }

    private void assertQuerungsverkehr(List<BelastungsplanQuDataDTO.KnotenarmValue> values, int knotenarm, Himmelsrichtung richtung, int expectedValue) {
        BelastungsplanQuDataDTO.KnotenarmValue knotenarmValue = values.stream().filter(kn -> kn.getKnotenarm() == knotenarm).findFirst().orElseThrow();
        BelastungsplanQuDataDTO.QuerungsverkehrValue querungsverkehrValue = knotenarmValue.getValuesQuerungsverkehre().stream()
                .filter(qv -> qv.getRichtung() == richtung).findFirst().orElseThrow();
        assertThat(querungsverkehrValue.getValue(), is(BigDecimal.valueOf(expectedValue)));
    }

}
