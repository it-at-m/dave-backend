package de.muenchen.dave.services.auswertung;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeAuswertungSpitzenstundeDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.domain.mapper.LadeZaehldatumMapper;
import de.muenchen.dave.domain.mapper.ZaehlungMapper;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.util.dataimport.ZeitintervallGleitendeSpitzenstundeUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

// Testklasse für AuswertungSpitzenstundeService
// Enthält Unit-Tests für unterschiedliche Pfade der Spitzenstundenauswertung.
@ExtendWith(MockitoExtension.class)
class AuswertungSpitzenstundeServiceTest {

    @Mock
    private ZeitintervallRepository zeitintervallRepository;

    @Mock
    private LadeZaehldatumMapper ladeZaehldatumMapper;

    @Mock
    private LadeZaehldatenService ladeZaehldatenService;

    @Mock
    private ZaehlungMapper zaehlungMapper;

    @InjectMocks
    private AuswertungSpitzenstundeService service;

    private Zaehlung zaehlungMock;

    @BeforeEach
    void setup() {
        zaehlungMock = Mockito.mock(Zaehlung.class);
    }

    @Test
    // Deutsch: Prüft erfolgreiche Extraktion der Spitzenstunden für eine Zählung ohne Kreisverkehr.
    void extractSpitzenstundenAllVerkehrsbeziehungen_nonKreisverkehr_success() throws Exception {
        final UUID zaehlungId = UUID.randomUUID();
        when(zaehlungMock.getId()).thenReturn(zaehlungId.toString());
        when(zaehlungMock.getZaehlart()).thenReturn(Zaehlart.N.name());
        when(zaehlungMock.getKreisverkehr()).thenReturn(Boolean.FALSE);

        try (MockedStatic<ZeitintervallGleitendeSpitzenstundeUtil> utilMock = Mockito.mockStatic(ZeitintervallGleitendeSpitzenstundeUtil.class)) {
            utilMock.when(() -> ZeitintervallGleitendeSpitzenstundeUtil.getRelevantTypeZeitintervallFromZeitauswahl("Spitzenstunde KFZ"))
                    .thenReturn(TypeZeitintervall.SPITZENSTUNDE_KFZ);

            final LinkedList<Zeitintervall> extractedSpitzenstunden = new LinkedList<>();
            final Zeitintervall overall = new Zeitintervall();
            overall.setStartUhrzeit(LocalDateTime.of(2022, 1, 1, 7, 0));
            overall.setEndeUhrzeit(LocalDateTime.of(2022, 1, 1, 8, 0));
            extractedSpitzenstunden.add(overall);
            when(ladeZaehldatenService.extractZeitintervalleSpitzenstunden(eq(zaehlungId), any(), any(), any(OptionsDTO.class)))
                    .thenReturn(extractedSpitzenstunden);

            final List<Zeitintervall> intervalle = new ArrayList<>();
            final Zeitintervall ti = new Zeitintervall();
            ti.setStartUhrzeit(LocalDateTime.of(2022, 1, 1, 7, 0));
            ti.setEndeUhrzeit(LocalDateTime.of(2022, 1, 1, 7, 15));
            intervalle.add(ti);
            when(zeitintervallRepository
                    .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonNotNullAndVerkehrsbeziehungNachNotNullAndTypeOrderBySortingIndexAsc(
                            eq(zaehlungId), eq(overall.getStartUhrzeit()), eq(overall.getEndeUhrzeit()), any(TypeZeitintervall.class)))
                    .thenReturn(intervalle);

            final List<Zeitintervall> gleitende = new ArrayList<>();
            final Zeitintervall g = new Zeitintervall();
            gleitende.add(g);
            utilMock.when(
                    () -> ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstundenByBewegungsbeziehung(eq(zaehlungId), any(), any(), any(), any()))
                    .thenReturn(gleitende);

            when(zaehlungMapper.mapVerkehrsbeziehungen(any())).thenReturn(new ArrayList<>());
            when(zaehlungMapper.mapLaengsverkehre(any())).thenReturn(new ArrayList<>());
            when(zaehlungMapper.mapQuerungsverkehre(any())).thenReturn(new ArrayList<>());

            final List<Zeitintervall> result = service.extractSpitzenstundenAllVerkehrsbeziehungen(
                    zaehlungMock,
                    Zeitblock.ZB_06_10,
                    "Spitzenstunde KFZ",
                    false);

            assertNotNull(result);
            assertEquals(1, result.size());

            utilMock.verify(() -> ZeitintervallGleitendeSpitzenstundeUtil.getRelevantTypeZeitintervallFromZeitauswahl("Spitzenstunde KFZ"), times(1));
            verify(ladeZaehldatenService, times(1)).extractZeitintervalleSpitzenstunden(eq(zaehlungId), any(), eq(Boolean.FALSE), any(OptionsDTO.class));
            verify(zeitintervallRepository, times(1))
                    .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonNotNullAndVerkehrsbeziehungNachNotNullAndTypeOrderBySortingIndexAsc(
                            eq(zaehlungId), eq(overall.getStartUhrzeit()), eq(overall.getEndeUhrzeit()), any(TypeZeitintervall.class));
            utilMock.verify(
                    () -> ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstundenByBewegungsbeziehung(eq(zaehlungId), any(), any(), any(), any()),
                    times(1));
        }
    }

    @Test
    // Deutsch: Prüft, dass bei Kreisverkehr die spezielle Repository-Methode mit HINEIN aufgerufen wird.
    void extractSpitzenstundenAllVerkehrsbeziehungen_kreisverkehr_uses_HINEIN() throws Exception {
        final UUID zaehlungId = UUID.randomUUID();
        when(zaehlungMock.getId()).thenReturn(zaehlungId.toString());
        when(zaehlungMock.getZaehlart()).thenReturn(Zaehlart.N.name());
        when(zaehlungMock.getKreisverkehr()).thenReturn(Boolean.TRUE);

        try (MockedStatic<ZeitintervallGleitendeSpitzenstundeUtil> utilMock = Mockito.mockStatic(ZeitintervallGleitendeSpitzenstundeUtil.class)) {
            utilMock.when(() -> ZeitintervallGleitendeSpitzenstundeUtil.getRelevantTypeZeitintervallFromZeitauswahl("Spitzenstunde KFZ"))
                    .thenReturn(TypeZeitintervall.SPITZENSTUNDE_KFZ);

            final LinkedList<Zeitintervall> extractedSpitzenstunden = new LinkedList<>();
            final Zeitintervall overall = new Zeitintervall();
            overall.setStartUhrzeit(LocalDateTime.of(2022, 1, 1, 7, 0));
            overall.setEndeUhrzeit(LocalDateTime.of(2022, 1, 1, 8, 0));
            extractedSpitzenstunden.add(overall);
            when(ladeZaehldatenService.extractZeitintervalleSpitzenstunden(eq(zaehlungId), any(), any(), any(OptionsDTO.class)))
                    .thenReturn(extractedSpitzenstunden);

            when(zeitintervallRepository
                    .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonNotNullAndVerkehrsbeziehungFahrbewegungKreisverkehrAndTypeOrderBySortingIndexAsc(
                            eq(zaehlungId), any(), any(), eq(FahrbewegungKreisverkehr.HINEIN), any(TypeZeitintervall.class)))
                    .thenReturn(new ArrayList<>());

            when(zaehlungMapper.mapVerkehrsbeziehungen(any())).thenReturn(new ArrayList<>());
            when(zaehlungMapper.mapLaengsverkehre(any())).thenReturn(new ArrayList<>());
            when(zaehlungMapper.mapQuerungsverkehre(any())).thenReturn(new ArrayList<>());

            final List<Zeitintervall> result = service.extractSpitzenstundenAllVerkehrsbeziehungen(
                    zaehlungMock,
                    Zeitblock.ZB_06_10,
                    "Spitzenstunde KFZ",
                    true);

            assertNotNull(result);
            verify(zeitintervallRepository, times(1))
                    .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonNotNullAndVerkehrsbeziehungFahrbewegungKreisverkehrAndTypeOrderBySortingIndexAsc(
                            eq(zaehlungId), any(), any(), eq(FahrbewegungKreisverkehr.HINEIN), any(TypeZeitintervall.class));
        }
    }

    @Test
    // Deutsch: Prüft, dass bei fehlender Spitzenstunde eine DataNotFoundException geworfen wird.
    void extractSpitzenstundenAllVerkehrsbeziehungen_noSpitzenstunde_throws() throws Exception {
        when(zaehlungMock.getId()).thenReturn(UUID.randomUUID().toString());
        when(zaehlungMock.getZaehlart()).thenReturn(Zaehlart.N.name());
        when(zaehlungMock.getKreisverkehr()).thenReturn(Boolean.FALSE);

        try (MockedStatic<ZeitintervallGleitendeSpitzenstundeUtil> utilMock = Mockito.mockStatic(ZeitintervallGleitendeSpitzenstundeUtil.class)) {
            utilMock.when(() -> ZeitintervallGleitendeSpitzenstundeUtil.getRelevantTypeZeitintervallFromZeitauswahl("Spitzenstunde KFZ"))
                    .thenReturn(TypeZeitintervall.SPITZENSTUNDE_KFZ);

            when(ladeZaehldatenService.extractZeitintervalleSpitzenstunden(any(), any(), any(), any(OptionsDTO.class)))
                    .thenReturn(new LinkedList<>());

            assertThrows(DataNotFoundException.class, () -> service.extractSpitzenstundenAllVerkehrsbeziehungen(
                    zaehlungMock,
                    Zeitblock.ZB_06_10,
                    "Spitzenstunde KFZ",
                    false));

            utilMock.verify(() -> ZeitintervallGleitendeSpitzenstundeUtil.getRelevantTypeZeitintervallFromZeitauswahl("Spitzenstunde KFZ"), times(1));
            verify(ladeZaehldatenService, times(1)).extractZeitintervalleSpitzenstunden(any(), any(), any(), any(OptionsDTO.class));
        }
    }

    @Test
    // Deutsch: Prüft das Mapping eines Zeitintervalls auf das DTO und das Setzen von von/nach.
    void mapToAuswertungSpitzenstundeDTO_maps_and_sets_von_nach() {
        final Zeitintervall zi = new Zeitintervall();
        final Verkehrsbeziehung vb = new Verkehrsbeziehung();
        vb.setVon(1);
        vb.setNach(2);
        zi.setVerkehrsbeziehung(vb);

        try (MockedStatic<LadeZaehldatenService> ladeMock = Mockito.mockStatic(LadeZaehldatenService.class)) {
            final LadeZaehldatumDTO lade = new LadeZaehldatumDTO();
            lade.setPkw(5);
            ladeMock.when(() -> LadeZaehldatenService.mapToZaehldatum(eq(zi), any(), any())).thenReturn(lade);

            final LadeAuswertungSpitzenstundeDTO dto = new LadeAuswertungSpitzenstundeDTO();
            when(ladeZaehldatumMapper.ladeZaehldatumDtoToLadeAuswertungSpitzenstundeDto(eq(lade))).thenReturn(dto);

            final LadeAuswertungSpitzenstundeDTO result = service.mapToAuswertungSpitzenstundeDTO(zi, null);

            assertNotNull(result);
            assertEquals(1, result.getVon());
            assertEquals(2, result.getNach());

            ladeMock.verify(() -> LadeZaehldatenService.mapToZaehldatum(eq(zi), any(), any()), times(1));
            verify(ladeZaehldatumMapper, times(1)).ladeZaehldatumDtoToLadeAuswertungSpitzenstundeDto(eq(lade));
        }
    }

    @Test
    void getSortingIndex() {
        Integer result = service.getSortingIndex(
                Zeitblock.ZB_06_10,
                TypeZeitintervall.SPITZENSTUNDE_FUSS);
        assertThat(result, is(24000000));

        result = service.getSortingIndex(
                Zeitblock.ZB_06_10,
                TypeZeitintervall.SPITZENSTUNDE_RAD);
        assertThat(result, is(23000000));

        result = service.getSortingIndex(
                Zeitblock.ZB_06_10,
                TypeZeitintervall.SPITZENSTUNDE_KFZ);
        assertThat(result, is(22000000));

        result = service.getSortingIndex(
                Zeitblock.ZB_00_24,
                TypeZeitintervall.SPITZENSTUNDE_FUSS);
        assertThat(result, is(80000000));

        result = service.getSortingIndex(
                Zeitblock.ZB_00_24,
                TypeZeitintervall.SPITZENSTUNDE_RAD);
        assertThat(result, is(70000000));

        result = service.getSortingIndex(
                Zeitblock.ZB_00_24,
                TypeZeitintervall.SPITZENSTUNDE_KFZ);
        assertThat(result, is(60000000));

        result = service.getSortingIndex(
                Zeitblock.ZB_00_24,
                TypeZeitintervall.GESAMT);
        assertThat(result, is(-1));
    }

}
