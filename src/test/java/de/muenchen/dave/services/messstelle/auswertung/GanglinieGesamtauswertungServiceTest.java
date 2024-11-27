package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenSteplineDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.Auswertung;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungMessstelle;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatDto;
import de.muenchen.dave.services.messstelle.Zeitraum;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;

class GanglinieGesamtauswertungServiceTest {

    private GanglinieGesamtauswertungService ganglinieGesamtauswertungService = new GanglinieGesamtauswertungService();

    @Test
    void createGanglinieForMultipleMessstellen() {
        final var auswertungMessstellen = new ArrayList<AuswertungMessstelle>();
        var auswertungMessstelle = new AuswertungMessstelle();
        auswertungMessstelle.setMstId("1");

        var auswertung = new Auswertung();
        var zeitraum = new Zeitraum(YearMonth.of(2024, 1), YearMonth.of(2024, 3), AuswertungsZeitraum.QUARTAL_1);
        var tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(100));
        auswertung.setZeitraum(zeitraum);
        auswertung.setDaten(tagesaggregat);
        auswertungMessstelle.getAuswertungenProZeitraum().add(auswertung);

        auswertung = new Auswertung();
        zeitraum = new Zeitraum(YearMonth.of(2024, 4), YearMonth.of(2024, 6), AuswertungsZeitraum.QUARTAL_2);
        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(101));
        auswertung.setZeitraum(zeitraum);
        auswertung.setDaten(tagesaggregat);
        auswertungMessstelle.getAuswertungenProZeitraum().add(auswertung);

        auswertung = new Auswertung();
        zeitraum = new Zeitraum(YearMonth.of(2024, 7), YearMonth.of(2024, 9), AuswertungsZeitraum.QUARTAL_3);
        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(102));
        auswertung.setZeitraum(zeitraum);
        auswertung.setDaten(tagesaggregat);
        auswertungMessstelle.getAuswertungenProZeitraum().add(auswertung);

        auswertung = new Auswertung();
        zeitraum = new Zeitraum(YearMonth.of(2024, 10), YearMonth.of(2024, 12), AuswertungsZeitraum.QUARTAL_4);
        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(103));
        auswertung.setZeitraum(zeitraum);
        auswertung.setDaten(tagesaggregat);
        auswertungMessstelle.getAuswertungenProZeitraum().add(auswertung);

        auswertung = new Auswertung();
        zeitraum = new Zeitraum(YearMonth.of(2025, 1), YearMonth.of(2025, 3), AuswertungsZeitraum.QUARTAL_1);
        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(104));
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
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(203));
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

        final var result = ganglinieGesamtauswertungService.createGanglinieForMultipleMessstellen(auswertungMessstellen);

        final var expected = new LadeZaehldatenSteplineDTO();

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

}
