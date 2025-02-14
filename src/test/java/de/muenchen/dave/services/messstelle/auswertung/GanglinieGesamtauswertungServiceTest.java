package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBaseDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryBigDecimalDTO;
import de.muenchen.dave.domain.dtos.laden.StepLineSeriesEntryIntegerDTO;
import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.Auswertung;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungMessstelle;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatDto;
import de.muenchen.dave.services.messstelle.Zeitraum;
import de.muenchen.dave.util.messstelle.GanglinieUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class GanglinieGesamtauswertungServiceTest {

    private GanglinieGesamtauswertungService ganglinieGesamtauswertungService = new GanglinieGesamtauswertungService();

    @Test
    void createGanglinieForSingleMessstelle() {
        var auswertungMessstelle = new AuswertungMessstelle();
        auswertungMessstelle.setMstId("1");

        var auswertung = new Auswertung();
        var zeitraum = new Zeitraum(YearMonth.of(2024, 1), YearMonth.of(2024, 3), AuswertungsZeitraum.QUARTAL_1);
        var tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(100));
        tagesaggregat.setProzentGueterverkehr(BigDecimal.valueOf(20.5));
        auswertung.setZeitraum(zeitraum);
        auswertung.setDaten(tagesaggregat);
        auswertungMessstelle.getAuswertungenProZeitraum().add(auswertung);

        auswertung = new Auswertung();
        zeitraum = new Zeitraum(YearMonth.of(2024, 4), YearMonth.of(2024, 6), AuswertungsZeitraum.QUARTAL_2);
        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(101));
        tagesaggregat.setProzentGueterverkehr(BigDecimal.valueOf(21.5));
        auswertung.setZeitraum(zeitraum);
        auswertung.setDaten(tagesaggregat);
        auswertungMessstelle.getAuswertungenProZeitraum().add(auswertung);

        auswertung = new Auswertung();
        zeitraum = new Zeitraum(YearMonth.of(2024, 7), YearMonth.of(2024, 9), AuswertungsZeitraum.QUARTAL_3);
        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(102));
        tagesaggregat.setProzentGueterverkehr(BigDecimal.valueOf(22.5));
        auswertung.setZeitraum(zeitraum);
        auswertung.setDaten(tagesaggregat);
        auswertungMessstelle.getAuswertungenProZeitraum().add(auswertung);

        auswertung = new Auswertung();
        zeitraum = new Zeitraum(YearMonth.of(2024, 10), YearMonth.of(2024, 12), AuswertungsZeitraum.QUARTAL_4);
        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(103));
        tagesaggregat.setProzentGueterverkehr(BigDecimal.valueOf(23.5));
        auswertung.setZeitraum(zeitraum);
        auswertung.setDaten(tagesaggregat);
        auswertungMessstelle.getAuswertungenProZeitraum().add(auswertung);

        auswertung = new Auswertung();
        zeitraum = new Zeitraum(YearMonth.of(2025, 1), YearMonth.of(2025, 3), AuswertungsZeitraum.QUARTAL_1);
        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(104));
        tagesaggregat.setProzentGueterverkehr(BigDecimal.valueOf(24.5));
        auswertung.setZeitraum(zeitraum);
        auswertung.setDaten(tagesaggregat);
        auswertungMessstelle.getAuswertungenProZeitraum().add(auswertung);

        final var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setPersonenkraftwagen(true);
        fahrzeugOptions.setLieferwagen(true);
        fahrzeugOptions.setLastkraftwagen(true);
        fahrzeugOptions.setLastzuege(true);
        fahrzeugOptions.setBusse(true);
        fahrzeugOptions.setKraftraeder(true);
        fahrzeugOptions.setRadverkehr(true);
        fahrzeugOptions.setFussverkehr(true);
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(true);
        fahrzeugOptions.setGueterverkehrsanteilProzent(true);

        final var result = ganglinieGesamtauswertungService.createGanglinieForSingleMessstelle(auswertungMessstelle, fahrzeugOptions);

        final var expected = new LadeZaehldatenSteplineDTO();
        expected.setLegend(List.of("Pkw", "Lkw", "Lz", "Lfw", "Busse", "Krad", "Rad", "Kfz", "SV", "SV %", "GV", "GV %"));
        expected.setRangeMax(120);
        expected.setRangeMaxPercent(26);
        expected.setXAxisDataFirstChart(List.of("Q1.2024", "Q2.2024", "Q3.2024", "Q4.2024", "Q1.2025"));
        final var seriesEntriesFirstChart = new ArrayList<StepLineSeriesEntryBaseDTO>();
        var stepLineSeriesEntryInteger = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryInteger.setYAxisData(Arrays.asList(null, null, null, null, null));
        seriesEntriesFirstChart.add(stepLineSeriesEntryInteger);
        stepLineSeriesEntryInteger = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryInteger.setYAxisData(Arrays.asList(null, null, null, null, null));
        seriesEntriesFirstChart.add(stepLineSeriesEntryInteger);
        stepLineSeriesEntryInteger = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryInteger.setYAxisData(Arrays.asList(null, null, null, null, null));
        seriesEntriesFirstChart.add(stepLineSeriesEntryInteger);
        stepLineSeriesEntryInteger = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryInteger.setYAxisData(Arrays.asList(null, null, null, null, null));
        seriesEntriesFirstChart.add(stepLineSeriesEntryInteger);
        stepLineSeriesEntryInteger = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryInteger.setYAxisData(Arrays.asList(null, null, null, null, null));
        seriesEntriesFirstChart.add(stepLineSeriesEntryInteger);
        stepLineSeriesEntryInteger = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryInteger.setYAxisData(Arrays.asList(null, null, null, null, null));
        seriesEntriesFirstChart.add(stepLineSeriesEntryInteger);
        stepLineSeriesEntryInteger = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryInteger.setYAxisData(Arrays.asList(null, null, null, null, null));
        seriesEntriesFirstChart.add(stepLineSeriesEntryInteger);
        stepLineSeriesEntryInteger = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryInteger.setYAxisData(Arrays.asList(100, 101, 102, 103, 104));
        seriesEntriesFirstChart.add(stepLineSeriesEntryInteger);
        stepLineSeriesEntryInteger = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryInteger.setYAxisData(Arrays.asList(null, null, null, null, null));
        seriesEntriesFirstChart.add(stepLineSeriesEntryInteger);
        var stepLineSeriesEntryBigDecimal = new StepLineSeriesEntryBigDecimalDTO();
        stepLineSeriesEntryBigDecimal.setYAxisData(Arrays.asList(null, null, null, null, null));
        seriesEntriesFirstChart.add(stepLineSeriesEntryBigDecimal);
        stepLineSeriesEntryInteger = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryInteger.setYAxisData(Arrays.asList(null, null, null, null, null));
        seriesEntriesFirstChart.add(stepLineSeriesEntryInteger);
        stepLineSeriesEntryBigDecimal = new StepLineSeriesEntryBigDecimalDTO();
        stepLineSeriesEntryBigDecimal.setYAxisData(Arrays.asList(BigDecimal.valueOf(20.5), BigDecimal.valueOf(21.5), BigDecimal.valueOf(22.5),
                BigDecimal.valueOf(23.5), BigDecimal.valueOf(24.5)));
        seriesEntriesFirstChart.add(stepLineSeriesEntryBigDecimal);
        expected.setSeriesEntriesFirstChart(seriesEntriesFirstChart);

        Assertions.assertThat(result).isNotNull().isEqualTo(expected);
    }

    @Test
    void createGanglinieForMultipleMessstellen() {
        final var auswertungMessstellen = new ArrayList<AuswertungMessstelle>();
        var auswertungMessstelle = new AuswertungMessstelle();
        auswertungMessstelle.setMstId("1");

        var auswertung = new Auswertung();
        var zeitraum = new Zeitraum(YearMonth.of(2024, 1), YearMonth.of(2024, 3), AuswertungsZeitraum.QUARTAL_1);
        var tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(100));
        tagesaggregat.setAnzahlBus(BigDecimal.valueOf(50));
        auswertung.setZeitraum(zeitraum);
        auswertung.setDaten(tagesaggregat);
        auswertungMessstelle.getAuswertungenProZeitraum().add(auswertung);

        auswertung = new Auswertung();
        zeitraum = new Zeitraum(YearMonth.of(2024, 4), YearMonth.of(2024, 6), AuswertungsZeitraum.QUARTAL_2);
        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(101));
        tagesaggregat.setAnzahlBus(BigDecimal.valueOf(51));
        auswertung.setZeitraum(zeitraum);
        auswertung.setDaten(tagesaggregat);
        auswertungMessstelle.getAuswertungenProZeitraum().add(auswertung);

        auswertung = new Auswertung();
        zeitraum = new Zeitraum(YearMonth.of(2024, 7), YearMonth.of(2024, 9), AuswertungsZeitraum.QUARTAL_3);
        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(102));
        tagesaggregat.setAnzahlBus(BigDecimal.valueOf(52));
        auswertung.setZeitraum(zeitraum);
        auswertung.setDaten(tagesaggregat);
        auswertungMessstelle.getAuswertungenProZeitraum().add(auswertung);

        auswertung = new Auswertung();
        zeitraum = new Zeitraum(YearMonth.of(2024, 10), YearMonth.of(2024, 12), AuswertungsZeitraum.QUARTAL_4);
        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(103));
        tagesaggregat.setAnzahlBus(BigDecimal.valueOf(53));
        auswertung.setZeitraum(zeitraum);
        auswertung.setDaten(tagesaggregat);
        auswertungMessstelle.getAuswertungenProZeitraum().add(auswertung);

        auswertung = new Auswertung();
        zeitraum = new Zeitraum(YearMonth.of(2025, 1), YearMonth.of(2025, 3), AuswertungsZeitraum.QUARTAL_1);
        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(104));
        tagesaggregat.setAnzahlBus(BigDecimal.valueOf(54));
        auswertung.setZeitraum(zeitraum);
        auswertung.setDaten(tagesaggregat);
        auswertungMessstelle.getAuswertungenProZeitraum().add(auswertung);

        auswertungMessstellen.add(auswertungMessstelle);

        auswertungMessstelle = new AuswertungMessstelle();
        auswertungMessstelle.setMstId("2");

        auswertung = new Auswertung();
        zeitraum = new Zeitraum(YearMonth.of(2024, 1), YearMonth.of(2024, 3), AuswertungsZeitraum.QUARTAL_1);
        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(200));
        auswertung.setZeitraum(zeitraum);
        auswertung.setDaten(tagesaggregat);
        auswertungMessstelle.getAuswertungenProZeitraum().add(auswertung);

        auswertung = new Auswertung();
        zeitraum = new Zeitraum(YearMonth.of(2024, 4), YearMonth.of(2024, 6), AuswertungsZeitraum.QUARTAL_2);
        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(201));
        auswertung.setZeitraum(zeitraum);
        auswertung.setDaten(tagesaggregat);
        auswertungMessstelle.getAuswertungenProZeitraum().add(auswertung);

        auswertung = new Auswertung();
        zeitraum = new Zeitraum(YearMonth.of(2024, 7), YearMonth.of(2024, 9), AuswertungsZeitraum.QUARTAL_3);
        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(202));
        auswertung.setZeitraum(zeitraum);
        auswertung.setDaten(tagesaggregat);
        auswertungMessstelle.getAuswertungenProZeitraum().add(auswertung);

        auswertung = new Auswertung();
        zeitraum = new Zeitraum(YearMonth.of(2024, 10), YearMonth.of(2024, 12), AuswertungsZeitraum.QUARTAL_4);
        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(null);
        auswertung.setZeitraum(zeitraum);
        auswertung.setDaten(tagesaggregat);
        auswertungMessstelle.getAuswertungenProZeitraum().add(auswertung);

        auswertung = new Auswertung();
        zeitraum = new Zeitraum(YearMonth.of(2025, 1), YearMonth.of(2025, 3), AuswertungsZeitraum.QUARTAL_1);
        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(204));
        auswertung.setZeitraum(zeitraum);
        auswertung.setDaten(tagesaggregat);
        auswertungMessstelle.getAuswertungenProZeitraum().add(auswertung);

        auswertungMessstellen.add(auswertungMessstelle);

        final var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(true);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setRadverkehr(false);
        fahrzeugOptions.setFussverkehr(false);
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);

        final var result = ganglinieGesamtauswertungService.createGanglinieForMultipleMessstellen(auswertungMessstellen, fahrzeugOptions);

        final var expected = new LadeZaehldatenSteplineDTO();
        expected.setLegend(List.of("1", "2"));
        expected.setRangeMax(220);
        expected.setRangeMaxPercent(0);
        expected.setXAxisDataFirstChart(List.of("Q1.2024", "Q2.2024", "Q3.2024", "Q4.2024", "Q1.2025"));
        final var seriesEntriesFirstChart = new ArrayList<StepLineSeriesEntryBaseDTO>();
        var stepLineSeriesEntryInteger = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryInteger.setYAxisData(List.of(50, 51, 52, 53, 54));
        seriesEntriesFirstChart.add(stepLineSeriesEntryInteger);
        stepLineSeriesEntryInteger = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryInteger.setYAxisData(List.of(100, 101, 102, 103, 104));
        seriesEntriesFirstChart.add(stepLineSeriesEntryInteger);
        stepLineSeriesEntryInteger = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryInteger.setYAxisData(Arrays.asList(null, null, null, null, null));
        seriesEntriesFirstChart.add(stepLineSeriesEntryInteger);
        stepLineSeriesEntryInteger = new StepLineSeriesEntryIntegerDTO();
        stepLineSeriesEntryInteger.setYAxisData(Arrays.asList(200, 201, 202, null, 204));
        seriesEntriesFirstChart.add(stepLineSeriesEntryInteger);
        expected.setSeriesEntriesFirstChart(seriesEntriesFirstChart);

        Assertions.assertThat(result).isNotNull().isEqualTo(expected);

    }

    @Test
    void getZeitraumForXaxis() {
        var zeitraum = new Zeitraum(YearMonth.of(2024, 11), null, AuswertungsZeitraum.SEPTEMBER);
        var result = ganglinieGesamtauswertungService.getZeitraumForXaxis(zeitraum);
        Assertions.assertThat(result).isNotNull().isEqualTo("09.2024");

        zeitraum = new Zeitraum(YearMonth.of(2024, 11), null, AuswertungsZeitraum.HALBJAHR_2);
        result = ganglinieGesamtauswertungService.getZeitraumForXaxis(zeitraum);
        Assertions.assertThat(result).isNotNull().isEqualTo("H2.2024");

        zeitraum = new Zeitraum(YearMonth.of(2024, 11), null, AuswertungsZeitraum.QUARTAL_3);
        result = ganglinieGesamtauswertungService.getZeitraumForXaxis(zeitraum);
        Assertions.assertThat(result).isNotNull().isEqualTo("Q3.2024");

        zeitraum = new Zeitraum(YearMonth.of(2024, 11), null, AuswertungsZeitraum.JAHRE);
        result = ganglinieGesamtauswertungService.getZeitraumForXaxis(zeitraum);
        Assertions.assertThat(result).isNotNull().isEqualTo("2024");
    }

    @Test
    void setMstIdToNameOfSeriesEntries() {
        final var seriesEntries = new GanglinieUtil.SeriesEntries();
        final var mstId = "42";

        ganglinieGesamtauswertungService.setMstIdToNameOfSeriesEntries(mstId, seriesEntries);

        Assertions.assertThat(seriesEntries.getSeriesEntryPkw().getName()).isNotNull().isEqualTo("42");
        Assertions.assertThat(seriesEntries.getSeriesEntryLkw().getName()).isNotNull().isEqualTo("42");
        Assertions.assertThat(seriesEntries.getSeriesEntryLfw().getName()).isNotNull().isEqualTo("42");
        Assertions.assertThat(seriesEntries.getSeriesEntryLz().getName()).isNotNull().isEqualTo("42");
        Assertions.assertThat(seriesEntries.getSeriesEntryBus().getName()).isNotNull().isEqualTo("42");
        Assertions.assertThat(seriesEntries.getSeriesEntryKrad().getName()).isNotNull().isEqualTo("42");
        Assertions.assertThat(seriesEntries.getSeriesEntryRad().getName()).isNotNull().isEqualTo("42");
        Assertions.assertThat(seriesEntries.getSeriesEntryKfz().getName()).isNotNull().isEqualTo("42");
        Assertions.assertThat(seriesEntries.getSeriesEntrySv().getName()).isNotNull().isEqualTo("42");
        Assertions.assertThat(seriesEntries.getSeriesEntrySvProzent().getName()).isNotNull().isEqualTo("42");
        Assertions.assertThat(seriesEntries.getSeriesEntryGv().getName()).isNotNull().isEqualTo("42");
        Assertions.assertThat(seriesEntries.getSeriesEntryGvProzent().getName()).isNotNull().isEqualTo("42");
    }

}
