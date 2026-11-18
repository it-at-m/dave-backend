package de.muenchen.dave.services.pdfgenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.elasticsearch.PkwEinheit;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.AssetType;
import de.muenchen.dave.domain.enums.Bewegungsrichtung;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.mapper.LadeZaehldatumMapper;
import de.muenchen.dave.domain.mapper.OptionsMapper;
import de.muenchen.dave.domain.mapper.ZaehlungMapper;
import de.muenchen.dave.domain.mapper.ZeitintervallMapper;
import de.muenchen.dave.domain.pdf.assets.ZaehlungskenngroessenAsset;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.services.ladezaehldaten.SpitzenstundeCalculatorService;
import de.muenchen.dave.services.ladezaehldaten.ZaehldatenExtractorService;
import de.muenchen.dave.services.ladezaehldaten.ZeitintervallExtractorService;
import de.muenchen.dave.services.ladezaehldaten.ZeitintervallSummationService;
import de.muenchen.dave.services.processzaehldaten.ProcessZaehldatenService;
import de.muenchen.dave.util.DaveConstants;
import de.muenchen.dave.util.dataimport.ZeitintervallSortingIndexUtil;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;

class ReportZaehlungskenngroessenTest {

    @ParameterizedTest
    @EnumSource(value = Zaehlart.class, names = { "QJS", "FJS", "QU" })
    void reportAggregiertAlleBewegungsbeziehungen(final Zaehlart zaehlart) throws Exception {
        final var id = UUID.randomUUID();
        final var zaehlung = new Zaehlung();
        zaehlung.setId(id.toString());
        zaehlung.setZaehlart(zaehlart.name());
        zaehlung.setZaehldauer(Zaehldauer.DAUER_24_STUNDEN.name());
        zaehlung.setKreisverkehr(false);
        zaehlung.setPkwEinheit(new PkwEinheit());
        zaehlung.setKategorien(List.of(Fahrzeug.KFZ, Fahrzeug.RAD, Fahrzeug.FUSS));

        final var repository = mock(ZeitintervallRepository.class);
        switch (zaehlart) {
            case QJS -> {
                final var erste = new de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung();
                erste.setVon(1);
                erste.setNach(2);
                erste.setStrassenseite(Himmelsrichtung.N);
                final var zweite = new de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung();
                zweite.setVon(2);
                zweite.setNach(1);
                zweite.setStrassenseite(Himmelsrichtung.S);
                zaehlung.setVerkehrsbeziehungen(List.of(erste, zweite));
                when(repository.findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungNachAndTypeInAndVerkehrsbeziehungStrassenseiteOrderBySortingIndexAsc(
                        any(), any(), any(), anyInt(), anyInt(), any(), any()))
                        .thenAnswer(invocation -> intervalle(id, zaehlart, invocation.getArgument(3), invocation.getArgument(5)));
            }
            case FJS -> {
                final var erste = new de.muenchen.dave.domain.elasticsearch.Laengsverkehr();
                erste.setKnotenarm(1);
                erste.setRichtung(Bewegungsrichtung.EIN);
                erste.setStrassenseite(Himmelsrichtung.N);
                final var zweite = new de.muenchen.dave.domain.elasticsearch.Laengsverkehr();
                zweite.setKnotenarm(2);
                zweite.setRichtung(Bewegungsrichtung.EIN);
                zweite.setStrassenseite(Himmelsrichtung.N);
                zaehlung.setLaengsverkehr(List.of(erste, zweite));
                when(repository.findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndLaengsverkehrKnotenarmAndLaengsverkehrRichtungAndLaengsverkehrStrassenseiteAndTypeInOrderBySortingIndexAsc(
                        any(), any(), any(), anyInt(), any(), any(), any()))
                        .thenAnswer(invocation -> intervalle(id, zaehlart, invocation.getArgument(3), invocation.getArgument(6)));
            }
            case QU -> {
                final var erste = new de.muenchen.dave.domain.elasticsearch.Querungsverkehr();
                erste.setKnotenarm(1);
                erste.setRichtung(Himmelsrichtung.N);
                final var zweite = new de.muenchen.dave.domain.elasticsearch.Querungsverkehr();
                zweite.setKnotenarm(2);
                zweite.setRichtung(Himmelsrichtung.N);
                zaehlung.setQuerungsverkehr(List.of(erste, zweite));
                when(repository.findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndQuerungsverkehrKnotenarmAndQuerungsverkehrRichtungAndTypeInOrderBySortingIndexAsc(
                        any(), any(), any(), anyInt(), any(), any()))
                        .thenAnswer(invocation -> intervalle(id, zaehlart, invocation.getArgument(3), invocation.getArgument(5)));
            }
            default -> throw new IllegalArgumentException();
        }

        final var index = mock(ZaehlstelleIndexService.class);
        when(index.getZaehlung(id.toString())).thenReturn(zaehlung);
        final var extractor = new ZaehldatenExtractorService(new ZeitintervallExtractorService(repository),
                new ZeitintervallSummationService(), new SpitzenstundeCalculatorService(Mappers.getMapper(ZeitintervallMapper.class)),
                Mappers.getMapper(OptionsMapper.class));
        final var ladeService = new LadeZaehldatenService(index, extractor);
        final var processService = new ProcessZaehldatenService(ladeService, Mappers.getMapper(ZaehlungMapper.class), null, null, null, null);
        final var generateService = new GeneratePdfService(null, null, null);
        final var reportService = new ReportService(generateService, null, processService, index,
                Mappers.getMapper(LadeZaehldatumMapper.class), null, null);
        reportService.init();

        final var asset = new ZaehlungskenngroessenAsset();
        asset.setType(AssetType.ZAEHLUNGSKENNGROESSEN);
        asset.setZaehlungId(id.toString());
        final String html = reportService.generateReportBody(List.of(asset));

        assertTrue(html.contains("<th>KFZ</th>") && html.contains("<th>Rad</th>") && html.contains("<th>Fuß</th>"));
        assertTrue(html.contains("<td>Block</td>") && html.contains("<td>Gesamt</td>") && html.contains("<td>SpStdTag Fuß</td>"));
        final var block = asset.getZaehldaten().stream().filter(datum -> "Block".equals(datum.getType())).findFirst().orElseThrow();
        assertEquals("20", block.getFussgaenger());
        assertEquals("20", block.getFahrradfahrer());
        assertEquals("100", block.getKfz());
        final var spitzenstunde = asset.getZaehldaten().stream().filter(datum -> "SpStdTag Fuß".equals(datum.getType())).findFirst().orElseThrow();
        assertEquals("20", spitzenstunde.getFussgaenger());
        assertEquals("06:00", spitzenstunde.getStartUhrzeit());
        assertEquals("07:00", spitzenstunde.getEndeUhrzeit());
        assertTrue(html.contains("<td>20</td>"));
    }

    private List<Zeitintervall> intervalle(final UUID id, final Zaehlart zaehlart, final int arm, final Set<TypeZeitintervall> types) {
        final var result = new ArrayList<Zeitintervall>();
        final var start = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0));
        for (int i = 0; i < 4; i++) {
            final var viertelstunde = zeitintervall(id, zaehlart, arm, TypeZeitintervall.STUNDE_VIERTEL,
                    start.plusMinutes(15L * i), start.plusMinutes(15L * (i + 1)), arm + 1);
            viertelstunde.setSortingIndex(ZeitintervallSortingIndexUtil.getSortingIndexWithinBlock(viertelstunde));
            result.add(viertelstunde);
        }
        final var block = zeitintervall(id, zaehlart, arm, TypeZeitintervall.BLOCK, start, start.plusHours(4), 4 * (arm + 1));
        block.setSortingIndex(ZeitintervallSortingIndexUtil.getSortingIndexWithinBlock(block));
        result.add(block);
        final var gesamt = zeitintervall(id, zaehlart, arm, TypeZeitintervall.GESAMT,
                start.withHour(0), start.withHour(23).withMinute(59), 4 * (arm + 1));
        gesamt.setSortingIndex(ZeitintervallSortingIndexUtil.SORTING_INDEX_GESAMT_DAY);
        result.add(gesamt);
        return result.stream().filter(interval -> types.contains(interval.getType())).toList();
    }

    private Zeitintervall zeitintervall(final UUID id, final Zaehlart zaehlart, final int arm, final TypeZeitintervall type,
            final LocalDateTime start, final LocalDateTime ende, final int value) {
        final var interval = Zeitintervall.builder().zaehlungId(id).type(type).startUhrzeit(start).endeUhrzeit(ende)
                .pkw(value).lkw(value).lastzuege(value).busse(value).kraftraeder(value).fahrradfahrer(value).fussgaenger(value).build();
        switch (zaehlart) {
            case QJS -> {
                final var beziehung = new de.muenchen.dave.domain.Verkehrsbeziehung();
                beziehung.setVon(arm);
                beziehung.setNach(arm == 1 ? 2 : 1);
                beziehung.setStrassenseite(arm == 1 ? Himmelsrichtung.N : Himmelsrichtung.S);
                interval.setVerkehrsbeziehung(beziehung);
            }
            case FJS -> {
                final var beziehung = new de.muenchen.dave.domain.Laengsverkehr();
                beziehung.setKnotenarm(arm);
                beziehung.setRichtung(Bewegungsrichtung.EIN);
                beziehung.setStrassenseite(Himmelsrichtung.N);
                interval.setLaengsverkehr(beziehung);
            }
            case QU -> {
                final var beziehung = new de.muenchen.dave.domain.Querungsverkehr();
                beziehung.setKnotenarm(arm);
                beziehung.setRichtung(Himmelsrichtung.N);
                interval.setQuerungsverkehr(beziehung);
            }
            default -> throw new IllegalArgumentException();
        }
        return interval;
    }
}
