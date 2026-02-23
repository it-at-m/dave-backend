package de.muenchen.dave.services.ladezaehldaten;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.elasticsearch.PkwEinheit;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumTageswertDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenTableDTO;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import de.muenchen.dave.domain.enums.Zaehlart;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LadeZaehldatenServiceTest {

    @Mock
    private ZaehlstelleIndexService indexService;

    @Mock
    private ZaehldatenExtractorService zaehldatenExtractorService;

    private LadeZaehldatenService service;

    @BeforeEach
    void setUp() {
        service = new LadeZaehldatenService(indexService, zaehldatenExtractorService);
    }

    @Test
    void testLadeZaehldaten_normalPath_mapsToLadeZaehldatumAndUsesMocksOnce() throws Exception {
        final UUID zaehlungId = UUID.randomUUID();
        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setZaehlart(Zaehlart.N.name());
        final PkwEinheit pkwEinheit = new PkwEinheit();
        pkwEinheit.setPkw(BigDecimal.ONE);
        pkwEinheit.setLkw(BigDecimal.ONE);
        pkwEinheit.setLastzuege(BigDecimal.ONE);
        pkwEinheit.setBusse(BigDecimal.ONE);
        pkwEinheit.setKraftraeder(BigDecimal.ONE);
        pkwEinheit.setFahrradfahrer(BigDecimal.ONE);
        zaehlung.setPkwEinheit(pkwEinheit);
        zaehlung.setKreisverkehr(false);

        when(indexService.getZaehlung(eq(zaehlungId.toString()))).thenReturn(zaehlung);

        final OptionsDTO options = new OptionsDTO();
        options.setZeitauswahl("None");
        options.setZeitblock(Zeitblock.ZB_06_10);
        options.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL);
        options.setZaehldauer(Zaehldauer.DAUER_24_STUNDEN);

        final Zeitintervall zi = new Zeitintervall();
        zi.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zi.setStartUhrzeit(LocalDateTime.of(2022,1,1,6,0));
        zi.setEndeUhrzeit(LocalDateTime.of(2022,1,1,6,15));
        zi.setPkw(10);
        zi.setLkw(2);
        zi.setLastzuege(1);
        zi.setBusse(1);
        zi.setKraftraeder(1);
        zi.setFahrradfahrer(5);
        zi.setFussgaenger(0);
        zi.setSortingIndex(1);

        when(zaehldatenExtractorService.extractZeitintervalle(eq(zaehlungId), eq(Zaehlart.N), eq(options.getZeitblock().getStart()), eq(options.getZeitblock().getEnd()), eq(false), eq(options), anySet()))
                .thenReturn(List.of(zi));

        final LadeZaehldatenTableDTO result = service.ladeZaehldaten(zaehlungId, options);

        assertEquals(1, result.getZaehldaten().size());
        final LadeZaehldatumDTO dto = result.getZaehldaten().get(0);
        assertEquals(10, dto.getPkw().intValue());
        assertEquals(2, dto.getLkw().intValue());
        // pkwEinheiten = sum of counts * 1 (pkwEinheit values are 1)
        assertEquals(10 + 2 + 1 + 1 + 1 + 5, dto.getPkwEinheiten().intValue());

        verify(indexService, times(1)).getZaehlung(eq(zaehlungId.toString()));
        verify(zaehldatenExtractorService, times(1)).extractZeitintervalle(eq(zaehlungId), eq(Zaehlart.N), eq(options.getZeitblock().getStart()), eq(options.getZeitblock().getEnd()), eq(false), eq(options), anySet());
        verifyNoMoreInteractions(indexService, zaehldatenExtractorService);
    }

    @Test
    void testLadeZaehldaten_spitzenstundePath_usesSpitzenstundeExtractorAndMaps() throws Exception {
        final UUID zaehlungId = UUID.randomUUID();
        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setZaehlart(Zaehlart.N.name());
        zaehlung.setPkwEinheit(new PkwEinheit());
        zaehlung.setKreisverkehr(false);

        when(indexService.getZaehlung(eq(zaehlungId.toString()))).thenReturn(zaehlung);

        final OptionsDTO options = new OptionsDTO();
        options.setZeitauswahl(LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE);
        options.setZeitblock(Zeitblock.ZB_06_10);
        options.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL);
        options.setZaehldauer(Zaehldauer.DAUER_24_STUNDEN);
        // Spitzenstunde wählen
        options.setSpitzenstunde(true);

        final Zeitintervall zi = new Zeitintervall();
        zi.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zi.setStartUhrzeit(LocalDateTime.of(2022,1,1,7,0));
        zi.setEndeUhrzeit(LocalDateTime.of(2022,1,1,7,15));
        zi.setPkw(1);
        zi.setSortingIndex(10);

        when(zaehldatenExtractorService.extractZeitintervalleSpitzenstunde(eq(zaehlungId), eq(Zaehlart.N), eq(options.getZeitblock().getStart()), eq(options.getZeitblock().getEnd()), eq(false), eq(options), anySet()))
                .thenReturn(List.of(zi));

        // Wenn für die Spitzenstunde die eigentlichen Intervalle angefragt werden, auch ein Ergebnis liefern
        when(zaehldatenExtractorService.extractZeitintervalle(eq(zaehlungId), eq(Zaehlart.N), eq(zi.getStartUhrzeit()), eq(zi.getEndeUhrzeit()), eq(false), eq(options), anySet()))
                .thenReturn(List.of(zi));

        final LadeZaehldatenTableDTO result = service.ladeZaehldaten(zaehlungId, options);

        assertEquals(2, result.getZaehldaten().size());

        verify(indexService, times(1)).getZaehlung(eq(zaehlungId.toString()));
        verify(zaehldatenExtractorService, times(1)).extractZeitintervalleSpitzenstunde(eq(zaehlungId), eq(Zaehlart.N), eq(options.getZeitblock().getStart()), eq(options.getZeitblock().getEnd()), eq(false), eq(options), anySet());
        verifyNoMoreInteractions(indexService, zaehldatenExtractorService);
    }

    @Test
    void testLadeZaehldaten_tageswertMapping_producesTageswertDtoAndVerifiesMocks() throws Exception {
        final UUID zaehlungId = UUID.randomUUID();
        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setZaehlart(Zaehlart.N.name());
        final PkwEinheit pkwEinheit = new PkwEinheit();
        pkwEinheit.setPkw(BigDecimal.ONE);
        zaehlung.setPkwEinheit(pkwEinheit);
        zaehlung.setKreisverkehr(false);

        when(indexService.getZaehlung(eq(zaehlungId.toString()))).thenReturn(zaehlung);

        final OptionsDTO options = new OptionsDTO();
        options.setZeitauswahl("None");
        options.setZeitblock(Zeitblock.ZB_00_24);
        options.setIntervall(ZaehldatenIntervall.STUNDE_KOMPLETT);
        // Tageswert mapping requires zaehldauer != DAUER_24_STUNDEN
        options.setZaehldauer(Zaehldauer.DAUER_2_X_4_STUNDEN);

        final Zeitintervall zi = new Zeitintervall();
        zi.setType(TypeZeitintervall.GESAMT);
        final Hochrechnung hoch = new Hochrechnung();
        hoch.setHochrechnungKfz(new BigDecimal("123.4"));
        hoch.setHochrechnungGv(new BigDecimal("10"));
        hoch.setHochrechnungSv(new BigDecimal("20"));
        hoch.setHochrechnungRad(42);
        zi.setHochrechnung(hoch);
        zi.setStartUhrzeit(LocalDateTime.of(2022,1,1,0,0));
        zi.setEndeUhrzeit(LocalDateTime.of(2022,1,1,23,59));

        when(zaehldatenExtractorService.extractZeitintervalle(eq(zaehlungId), eq(Zaehlart.N), eq(options.getZeitblock().getStart()), eq(options.getZeitblock().getEnd()), eq(false), eq(options), anySet()))
                .thenReturn(List.of(zi));

        final LadeZaehldatenTableDTO result = service.ladeZaehldaten(zaehlungId, options);

        assertEquals(1, result.getZaehldaten().size());
        final LadeZaehldatumDTO dto = result.getZaehldaten().get(0);
        assertTrue(dto instanceof LadeZaehldatumTageswertDTO);
        final LadeZaehldatumTageswertDTO tageswert = (LadeZaehldatumTageswertDTO) dto;
        assertEquals(new BigDecimal("123"), tageswert.getKfz());
        assertEquals(new BigDecimal("10"), tageswert.getGueterverkehr());
        assertEquals(new BigDecimal("20"), tageswert.getSchwerverkehr());
        assertEquals(42, tageswert.getFahrradfahrer().intValue());

        verify(indexService, times(1)).getZaehlung(eq(zaehlungId.toString()));
        verify(zaehldatenExtractorService, times(1)).extractZeitintervalle(eq(zaehlungId), eq(Zaehlart.N), eq(options.getZeitblock().getStart()), eq(options.getZeitblock().getEnd()), eq(false), eq(options), anySet());
        verifyNoMoreInteractions(indexService, zaehldatenExtractorService);
    }

    @Test
    void testExtractZeitintervalleForSpitzenstunde_appendsSpitzenstundeWhenOptionTrueAndVerifiesMocks() throws Exception {
        final UUID zaehlungId = UUID.randomUUID();
        final OptionsDTO options = new OptionsDTO();
        options.setZeitblock(Zeitblock.ZB_06_10);
        options.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL);
        options.setSpitzenstunde(true);

        final Zeitintervall sp1 = new Zeitintervall();
        sp1.setType(TypeZeitintervall.STUNDE_VIERTEL);
        sp1.setStartUhrzeit(LocalDateTime.of(2022,1,1,6,0));
        sp1.setEndeUhrzeit(LocalDateTime.of(2022,1,1,6,15));

        final Zeitintervall spitzenStunde = new Zeitintervall();
        spitzenStunde.setType(TypeZeitintervall.STUNDE_VIERTEL);
        spitzenStunde.setStartUhrzeit(LocalDateTime.of(2022,1,1,7,0));
        spitzenStunde.setEndeUhrzeit(LocalDateTime.of(2022,1,1,8,0));

        final java.util.LinkedList<Zeitintervall> spitzenList = new java.util.LinkedList<>();
        spitzenList.add(sp1);
        spitzenList.add(spitzenStunde);

        when(zaehldatenExtractorService.extractZeitintervalleSpitzenstunde(eq(zaehlungId), eq(Zaehlart.N), eq(options.getZeitblock().getStart()), eq(options.getZeitblock().getEnd()), eq(false), eq(options), anySet()))
                .thenReturn(spitzenList);

        final Zeitintervall quarter = new Zeitintervall();
        quarter.setType(TypeZeitintervall.STUNDE_VIERTEL);
        quarter.setStartUhrzeit(spitzenStunde.getStartUhrzeit());
        quarter.setEndeUhrzeit(spitzenStunde.getStartUhrzeit().plusMinutes(15));

        when(zaehldatenExtractorService.extractZeitintervalle(eq(zaehlungId), eq(Zaehlart.N), eq(spitzenStunde.getStartUhrzeit()), eq(spitzenStunde.getEndeUhrzeit()), eq(false), eq(options), anySet()))
                .thenReturn(List.of(quarter));

        final List<Zeitintervall> result = service.extractZeitintervalleForSpitzenstunde(zaehlungId, Zaehlart.N, false, options);

        assertEquals(2, result.size());
        assertTrue(result.contains(quarter));
        assertTrue(result.contains(spitzenStunde));

        verify(zaehldatenExtractorService, times(1)).extractZeitintervalleSpitzenstunde(eq(zaehlungId), eq(Zaehlart.N), eq(options.getZeitblock().getStart()), eq(options.getZeitblock().getEnd()), eq(false), eq(options), anySet());
        verify(zaehldatenExtractorService, times(1)).extractZeitintervalle(eq(zaehlungId), eq(Zaehlart.N), eq(spitzenStunde.getStartUhrzeit()), eq(spitzenStunde.getEndeUhrzeit()), eq(false), eq(options), anySet());
        verifyNoMoreInteractions(indexService, zaehldatenExtractorService);
    }

}
