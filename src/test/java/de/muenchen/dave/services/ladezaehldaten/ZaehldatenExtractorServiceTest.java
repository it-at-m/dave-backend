package de.muenchen.dave.services.ladezaehldaten;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.external.ExternalLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.external.ExternalQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.external.ExternalVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.enums.Bewegungsrichtung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.enums.Zeitblock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import de.muenchen.dave.domain.mapper.OptionsMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ZaehldatenExtractorServiceTest {

    @Mock
    private ZeitintervallExtractorService zeitintervallExtractorService;

    @Mock
    private ZeitintervallSummationService zeitintervallSummationService;

    @Mock
    private SpitzenstundeCalculatorService spitzenstundeCalculatorService;

    @Mock
    private OptionsMapper optionsMapper;

    @InjectMocks
    private ZaehldatenExtractorService service;

    private UUID zaehlungId;

    @BeforeEach
    void setUp() {
        zaehlungId = UUID.randomUUID();
    }

    private Zeitintervall createZeitintervall(TypeZeitintervall type, int sortingIndex) {
        return Zeitintervall.builder()
                .type(type)
                .sortingIndex(sortingIndex)
                .startUhrzeit(LocalDateTime.of(2020, 1, 1, 6, 0))
                .endeUhrzeit(LocalDateTime.of(2020, 1, 1, 6, 15))
                .build();
    }

    private OptionsDTO createBaseOptions() {
        OptionsDTO options = new OptionsDTO();
        options.setZeitblock(Zeitblock.ZB_06_10);
        options.setZeitauswahl("TEST");
        options.setKraftfahrzeugverkehr(true);
        options.setSchwerverkehr(false);
        options.setGueterverkehr(false);
        options.setRadverkehr(true);
        options.setFussverkehr(true);
        options.setSchwerverkehrsanteilProzent(false);
        options.setGueterverkehrsanteilProzent(false);
        options.setPkwEinheiten(true);
        options.setPersonenkraftwagen(true);
        options.setLastkraftwagen(false);
        options.setLastzuege(false);
        options.setBusse(false);
        options.setKraftraeder(false);
        options.setStundensumme(false);
        options.setBlocksumme(false);
        options.setTagessumme(false);
        options.setSpitzenstunde(false);
        options.setSpitzenstundeKfz(false);
        options.setSpitzenstundeRad(false);
        options.setSpitzenstundeFuss(false);
        options.setMittelwert(false);
        options.setFahrzeugklassenStapeln(false);
        options.setBeschriftung(false);
        options.setDatentabelle(false);
        options.setWerteHundertRunden(false);
        options.setDifferenzdatenDarstellen(false);
        options.setZeitreiheGesamt(false);
        return options;
    }

    @Test
    void testExtractZeitintervalle_ohneSpitzenstunde_sortierungUndAnreicherung() {
        // Arrange: Extraktor liefert Map mit einer Bewegungsbeziehung und zwei Intervalle
        Map<Object, List<Zeitintervall>> byBewegung = new HashMap<>();
        var zi1 = createZeitintervall(TypeZeitintervall.STUNDE_KOMPLETT, 5);
        var zi2 = createZeitintervall(TypeZeitintervall.STUNDE_KOMPLETT, 1);
        byBewegung.put(new Object(), List.of(zi1, zi2));

        when(zeitintervallExtractorService.extractZeitintervalle(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any()))
                .thenReturn((Map) byBewegung);

        // Summation liefert eine aggregierte Liste (unsortiert)
        List<Zeitintervall> summed = new ArrayList<>();
        summed.add(zi1);
        summed.add(zi2);
        when(zeitintervallSummationService.sumZeitintervelleOverBewegungsbeziehung(Mockito.any())).thenReturn(summed);

        // Act
        OptionsDTO options = createBaseOptions();
        options.setVonKnotenarm(1);
        options.setNachKnotenarm(2);
        var result = service.extractZeitintervalle(zaehlungId, Zaehlart.N, LocalDateTime.now(), LocalDateTime.now(), true, options,
                Set.of(TypeZeitintervall.STUNDE_KOMPLETT));

        // Assert: Ergebnis ist sortiert nach sortingIndex
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getSortingIndex());
        assertEquals(5, result.get(1).getSortingIndex());

        // Assert: Verkehrsbeziehung wurde im else-Branch ergänzt und FahrbewegungKreisverkehr gesetzt (beide Knotenarme vorhanden -> VORBEI)
        assertNotNull(result.get(0).getVerkehrsbeziehung());
        assertEquals(FahrbewegungKreisverkehr.VORBEI, result.get(0).getVerkehrsbeziehung().getFahrbewegungKreisverkehr());

        // Verify: Mocks wurden korrekt aufgerufen bzw. nicht aufgerufen
        Mockito.verify(zeitintervallExtractorService, Mockito.times(1)).extractZeitintervalle(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(zeitintervallSummationService, Mockito.times(1)).sumZeitintervelleOverBewegungsbeziehung(Mockito.any());
        Mockito.verify(spitzenstundeCalculatorService, Mockito.times(0)).calculateSpitzenstundeForGivenZeitintervalle(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any());
    }

    @Test
    void testExtractZeitintervalle_mitSpitzenstunde_wirdHinzugefuegt() {
        // Arrange
        when(zeitintervallExtractorService.extractZeitintervalle(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any()))
                .thenReturn(new HashMap<>());

        var summed = new ArrayList<>(List.of(createZeitintervall(TypeZeitintervall.STUNDE_KOMPLETT, 10)));
        when(zeitintervallSummationService.sumZeitintervelleOverBewegungsbeziehung(Mockito.any())).thenReturn(summed);

        var spitzen = List.of(createZeitintervall(TypeZeitintervall.SPITZENSTUNDE_KFZ, 99));
        // Use a spy to avoid invoking optionsMapper.deepCopy and the full internal 15-minute extraction logic
        var spy = Mockito.spy(service);
        Mockito.doReturn(spitzen).when(spy).extractZeitintervalleSpitzenstundeFor15MinuteIntervals(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any());

        OptionsDTO options = createBaseOptions();
        options.setZeitblock(Zeitblock.ZB_06_10);
        // Setze von/nach damit die Anreicherung mit Verkehrsbeziehung im else-Branch sichtbar wird
        options.setVonKnotenarm(1);
        options.setNachKnotenarm(2);

        // Act: types enthalten eine Spitzenstunde -> Service soll spitzenstunde an Ergebnis anhängen
        var result = spy.extractZeitintervalle(zaehlungId, Zaehlart.N, LocalDateTime.now(), LocalDateTime.now(), false, options,
                Set.of(TypeZeitintervall.SPITZENSTUNDE_KFZ));

        // Assert: Ergebnis enthält sowohl die summierten Intervalle als auch die Spitzenstunde
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(z -> TypeZeitintervall.SPITZENSTUNDE_KFZ.equals(z.getType())));
        assertTrue(result.stream().anyMatch(z -> TypeZeitintervall.STUNDE_KOMPLETT.equals(z.getType())));

        // Die Intervalle sollten nach sortingIndex sortiert sein (10, 99)
        assertEquals(10, result.get(0).getSortingIndex());
        assertEquals(99, result.get(1).getSortingIndex());

        // Beide Intervalle wurden angereichert mit Verkehrsbeziehung aus options
        for (Zeitintervall zi : result) {
            assertNotNull(zi.getVerkehrsbeziehung());
            assertEquals(1, zi.getVerkehrsbeziehung().getVon());
            assertEquals(2, zi.getVerkehrsbeziehung().getNach());
        }

        // Verify: Extraktor und Summation je einmal; Spy-Methode für Spitzenstunden wurde aufgerufen, der Calculator nicht direkt
        Mockito.verify(zeitintervallExtractorService, Mockito.times(1)).extractZeitintervalle(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(zeitintervallSummationService, Mockito.times(1)).sumZeitintervelleOverBewegungsbeziehung(Mockito.any());
        Mockito.verify(spy, Mockito.times(1)).extractZeitintervalleSpitzenstundeFor15MinuteIntervals(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(spitzenstundeCalculatorService, Mockito.times(0)).calculateSpitzenstundeForGivenZeitintervalle(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any());
    }

    @Test
    void testExtractZeitintervalleSpitzenstundeFor15MinuteIntervals_filtertNurSpitzenstunden() {
        // Arrange
        var normal = createZeitintervall(TypeZeitintervall.STUNDE_KOMPLETT, 1);
        var spitzen = createZeitintervall(TypeZeitintervall.SPITZENSTUNDE_RAD, 2);

        // Mock OptionsMapper.deepCopy to return a copy (we can reuse base options)
        OptionsDTO options = createBaseOptions();
        when(optionsMapper.deepCopy(Mockito.any())).thenReturn(options);

        // The extractor returns some map (content is irrelevant for this test)
        when(zeitintervallExtractorService.extractZeitintervalle(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any())).thenReturn(new HashMap<>());

        // The summation returns both a normal and a spitzen interval
        when(zeitintervallSummationService.sumZeitintervelleOverBewegungsbeziehung(Mockito.any())).thenReturn(List.of(normal, spitzen));

        // The calculator should compute and return only the Spitzenstunde
        when(spitzenstundeCalculatorService.calculateSpitzenstundeForGivenZeitintervalle(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(List.of(spitzen));

        // Act: types enthalten sowohl eine Spitzenstunde als auch ein Intervall-Typ
        var result = service.extractZeitintervalleSpitzenstundeFor15MinuteIntervals(zaehlungId, Zaehlart.N, LocalDateTime.now(), LocalDateTime.now(), false,
                options,
                Set.of(TypeZeitintervall.SPITZENSTUNDE_RAD, TypeZeitintervall.STUNDE_KOMPLETT));

        // Assert: nur die Spitzenstunde bleibt übrig
        assertEquals(1, result.size());
        assertEquals(TypeZeitintervall.SPITZENSTUNDE_RAD, result.get(0).getType());

        // Verify interactions with dependencies
        Mockito.verify(optionsMapper, Mockito.times(1)).deepCopy(Mockito.any());
        Mockito.verify(zeitintervallExtractorService, Mockito.times(1)).extractZeitintervalle(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(zeitintervallSummationService, Mockito.times(1)).sumZeitintervelleOverBewegungsbeziehung(Mockito.any());
        Mockito.verify(spitzenstundeCalculatorService, Mockito.times(1)).calculateSpitzenstundeForGivenZeitintervalle(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void testIsZeitintervallOfTypeSpitzenstunde_trueAndFalse() {
        var spitzen = createZeitintervall(TypeZeitintervall.SPITZENSTUNDE_FUSS, 1);
        var normal = createZeitintervall(TypeZeitintervall.STUNDE_KOMPLETT, 1);

        assertTrue(service.isZeitintervallOfTypeSpitzenstunde(spitzen));
        assertTrue(!service.isZeitintervallOfTypeSpitzenstunde(normal));

        // Verify: Keine Interaktion mit externen Services
        Mockito.verifyNoInteractions(zeitintervallExtractorService, zeitintervallSummationService, spitzenstundeCalculatorService);
    }

    @Test
    void testEnrich_QU_singleChosenSetsQuerungsverkehr() {
        OptionsDTO options = createBaseOptions();
        var ext = new ExternalQuerungsverkehrDTO();
        ext.setKnotenarm(5);
        ext.setRichtung(Himmelsrichtung.N);
        LinkedList<ExternalQuerungsverkehrDTO> list = new LinkedList<>();
        list.add(ext);
        options.setChosenQuerungsverkehre(list);

        var zi = createZeitintervall(TypeZeitintervall.STUNDE_VIERTEL, 1);
        var res = service.enrichZeitintervalleByBewegungsbeziehung(zi, options, Zaehlart.QU, false);

        assertNotNull(res.getQuerungsverkehr());
        assertEquals(5, res.getQuerungsverkehr().getKnotenarm());
        assertEquals(Himmelsrichtung.N, res.getQuerungsverkehr().getRichtung());

        // Verify: Keine Interaktion mit externen Services
        Mockito.verifyNoInteractions(zeitintervallExtractorService, zeitintervallSummationService, spitzenstundeCalculatorService);
    }

    @Test
    void testEnrich_FJS_singleChosenSetsLaengsverkehr() {
        OptionsDTO options = createBaseOptions();
        var ext = new ExternalLaengsverkehrDTO();
        ext.setKnotenarm(7);
        ext.setRichtung(Bewegungsrichtung.EIN);
        ext.setStrassenseite(Himmelsrichtung.O);
        LinkedList<ExternalLaengsverkehrDTO> list = new LinkedList<>();
        list.add(ext);
        options.setChosenLaengsverkehre(list);

        var zi = createZeitintervall(TypeZeitintervall.STUNDE_VIERTEL, 1);
        var res = service.enrichZeitintervalleByBewegungsbeziehung(zi, options, Zaehlart.FJS, false);

        assertNotNull(res.getLaengsverkehr());
        assertEquals(7, res.getLaengsverkehr().getKnotenarm());
        assertEquals(Bewegungsrichtung.EIN, res.getLaengsverkehr().getRichtung());
        assertEquals(Himmelsrichtung.O, res.getLaengsverkehr().getStrassenseite());

        // Verify: Keine Interaktion mit externen Services
        Mockito.verifyNoInteractions(zeitintervallExtractorService, zeitintervallSummationService, spitzenstundeCalculatorService);
    }

    @Test
    void testEnrich_QJS_singleChosenSetsVerkehrsbeziehung() {
        OptionsDTO options = createBaseOptions();
        var ext = new ExternalVerkehrsbeziehungDTO();
        ext.setVon(11);
        ext.setNach(12);
        ext.setStrassenseite(Himmelsrichtung.N);
        LinkedList<ExternalVerkehrsbeziehungDTO> list = new LinkedList<>();
        list.add(ext);
        options.setChosenVerkehrsbeziehungen(list);

        var zi = createZeitintervall(TypeZeitintervall.STUNDE_VIERTEL, 1);
        var res = service.enrichZeitintervalleByBewegungsbeziehung(zi, options, Zaehlart.QJS, false);

        assertNotNull(res.getVerkehrsbeziehung());
        assertEquals(11, res.getVerkehrsbeziehung().getVon());
        assertEquals(12, res.getVerkehrsbeziehung().getNach());
        assertEquals(Himmelsrichtung.N, res.getVerkehrsbeziehung().getStrassenseite());

        // Verify: Keine Interaktion mit externen Services
        Mockito.verifyNoInteractions(zeitintervallExtractorService, zeitintervallSummationService, spitzenstundeCalculatorService);
    }

    @Test
    void testEnrich_elseBranch_kreisverkehrCases() {
        OptionsDTO options = createBaseOptions();

        // HINEIN: von gesetzt, nach null
        options.setVonKnotenarm(1);
        options.setNachKnotenarm(null);
        var zi1 = createZeitintervall(TypeZeitintervall.STUNDE_KOMPLETT, 1);
        var r1 = service.enrichZeitintervalleByBewegungsbeziehung(zi1, options, Zaehlart.N, true);
        assertEquals(FahrbewegungKreisverkehr.HINEIN, r1.getVerkehrsbeziehung().getFahrbewegungKreisverkehr());

        // HERAUS: von null, nach gesetzt
        options.setVonKnotenarm(null);
        options.setNachKnotenarm(2);
        var zi2 = createZeitintervall(TypeZeitintervall.STUNDE_KOMPLETT, 2);
        var r2 = service.enrichZeitintervalleByBewegungsbeziehung(zi2, options, Zaehlart.N, true);
        assertEquals(FahrbewegungKreisverkehr.HERAUS, r2.getVerkehrsbeziehung().getFahrbewegungKreisverkehr());

        // VORBEI: beide gesetzt
        options.setVonKnotenarm(3);
        options.setNachKnotenarm(4);
        var zi3 = createZeitintervall(TypeZeitintervall.STUNDE_KOMPLETT, 3);
        var r3 = service.enrichZeitintervalleByBewegungsbeziehung(zi3, options, Zaehlart.N, true);
        assertEquals(FahrbewegungKreisverkehr.VORBEI, r3.getVerkehrsbeziehung().getFahrbewegungKreisverkehr());

        // Verify: Keine Interaktion mit externen Services
        Mockito.verifyNoInteractions(zeitintervallExtractorService, zeitintervallSummationService, spitzenstundeCalculatorService);
    }

}
