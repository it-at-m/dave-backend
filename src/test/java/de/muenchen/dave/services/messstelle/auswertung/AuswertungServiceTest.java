package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.messstelle.auswertung.Auswertung;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungMessstelle;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungMessstelleUndZeitraum;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.domain.model.messstelle.ValidateZeitraumAndTagesTypForMessstelleModel;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatDto;
import de.muenchen.dave.services.messstelle.Zeitraum;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
class AuswertungServiceTest {

    private final AuswertungService auswertungService = new AuswertungService(null, null, null, null, null, null);

    @Test
    void calculateZeitraeume() {
        final List<AuswertungsZeitraum> auswertungszeitraeume = new ArrayList<>();
        auswertungszeitraeume.add(AuswertungsZeitraum.QUARTAL_1);
        auswertungszeitraeume.add(AuswertungsZeitraum.QUARTAL_3);
        auswertungszeitraeume.add(AuswertungsZeitraum.FEBRUAR);
        final List<Integer> jahre = new ArrayList<>();
        jahre.add(2020);
        jahre.add(2021);

        final List<Zeitraum> result = auswertungService.createZeitraeume(auswertungszeitraeume, jahre);

        final List<Zeitraum> expected = new ArrayList<>();
        expected.add(new Zeitraum(YearMonth.of(2020, 1), YearMonth.of(2020, 3), AuswertungsZeitraum.QUARTAL_1));
        expected.add(new Zeitraum(YearMonth.of(2020, 7), YearMonth.of(2020, 9), AuswertungsZeitraum.QUARTAL_3));
        expected.add(new Zeitraum(YearMonth.of(2021, 1), YearMonth.of(2021, 3), AuswertungsZeitraum.QUARTAL_1));
        expected.add(new Zeitraum(YearMonth.of(2021, 7), YearMonth.of(2021, 9), AuswertungsZeitraum.QUARTAL_3));
        expected.add(new Zeitraum(YearMonth.of(2020, 2), YearMonth.of(2020, 2), AuswertungsZeitraum.FEBRUAR));
        expected.add(new Zeitraum(YearMonth.of(2021, 2), YearMonth.of(2021, 2), AuswertungsZeitraum.FEBRUAR));

        Assertions.assertThat(result)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFieldsMatchingRegexes(".*")
                .isEqualTo(expected);
    }

    @Test
    void mapAuswertungMapToListOfAuswertungProMessstelle() {

        final ConcurrentMap<String, List<AuswertungMessstelleUndZeitraum>> request = new ConcurrentHashMap<>();

        final List<AuswertungMessstelleUndZeitraum> messstelle1 = new ArrayList<>();
        final AuswertungMessstelleUndZeitraum m1Z1 = new AuswertungMessstelleUndZeitraum();
        final String mstId1 = "123";
        m1Z1.setMstId(mstId1);
        m1Z1.setZeitraum(new Zeitraum(YearMonth.of(2020, 1), YearMonth.of(2020, 3), AuswertungsZeitraum.QUARTAL_1));
        final AuswertungMessstelleUndZeitraum m1Z2 = new AuswertungMessstelleUndZeitraum();
        m1Z2.setMstId(mstId1);
        m1Z2.setZeitraum(new Zeitraum(YearMonth.of(2020, 7), YearMonth.of(2020, 9), AuswertungsZeitraum.QUARTAL_3));
        final AuswertungMessstelleUndZeitraum m1Z3 = new AuswertungMessstelleUndZeitraum();
        m1Z3.setMstId(mstId1);
        m1Z3.setZeitraum(new Zeitraum(YearMonth.of(2021, 1), YearMonth.of(2021, 3), AuswertungsZeitraum.QUARTAL_1));
        final AuswertungMessstelleUndZeitraum m1Z4 = new AuswertungMessstelleUndZeitraum();
        m1Z4.setMstId(mstId1);
        m1Z4.setZeitraum(new Zeitraum(YearMonth.of(2021, 7), YearMonth.of(2021, 9), AuswertungsZeitraum.QUARTAL_3));
        messstelle1.add(m1Z1);
        messstelle1.add(m1Z2);
        messstelle1.add(m1Z3);
        messstelle1.add(m1Z4);

        final List<AuswertungMessstelleUndZeitraum> messstelle2 = new ArrayList<>();
        final AuswertungMessstelleUndZeitraum m2Z1 = new AuswertungMessstelleUndZeitraum();
        String mstId2 = "456";
        m2Z1.setMstId(mstId2);
        m2Z1.setZeitraum(new Zeitraum(YearMonth.of(2020, 1), YearMonth.of(2020, 3), AuswertungsZeitraum.QUARTAL_1));
        final AuswertungMessstelleUndZeitraum m2Z2 = new AuswertungMessstelleUndZeitraum();
        m2Z2.setMstId(mstId2);
        m2Z2.setZeitraum(new Zeitraum(YearMonth.of(2020, 7), YearMonth.of(2020, 9), AuswertungsZeitraum.QUARTAL_3));
        final AuswertungMessstelleUndZeitraum m2Z3 = new AuswertungMessstelleUndZeitraum();
        m2Z3.setMstId(mstId2);
        m2Z3.setZeitraum(new Zeitraum(YearMonth.of(2021, 1), YearMonth.of(2021, 3), AuswertungsZeitraum.QUARTAL_1));
        final AuswertungMessstelleUndZeitraum m2Z4 = new AuswertungMessstelleUndZeitraum();
        m2Z4.setMstId(mstId2);
        m2Z4.setZeitraum(new Zeitraum(YearMonth.of(2021, 7), YearMonth.of(2021, 9), AuswertungsZeitraum.QUARTAL_3));
        messstelle2.add(m2Z1);
        messstelle2.add(m2Z2);
        messstelle2.add(m2Z3);
        messstelle2.add(m2Z4);

        request.put(mstId1, messstelle1);
        request.put(mstId2, messstelle2);

        final List<AuswertungMessstelle> result = auswertungService.mapAuswertungMapToListOfAuswertungProMessstelle(request);

        final List<AuswertungMessstelle> expected = new ArrayList<>();
        final AuswertungMessstelle auswertungMessstelle1 = new AuswertungMessstelle();
        auswertungMessstelle1.setMstId(mstId1);
        final Auswertung auswertungM1Z1 = new Auswertung();
        auswertungM1Z1.setZeitraum(m1Z1.getZeitraum());
        auswertungM1Z1.setObjectId(mstId1);
        final Auswertung auswertungM1Z2 = new Auswertung();
        auswertungM1Z2.setZeitraum(m1Z2.getZeitraum());
        auswertungM1Z2.setObjectId(mstId1);
        final Auswertung auswertungM1Z3 = new Auswertung();
        auswertungM1Z3.setZeitraum(m1Z3.getZeitraum());
        auswertungM1Z3.setObjectId(mstId1);
        final Auswertung auswertungM1Z4 = new Auswertung();
        auswertungM1Z4.setZeitraum(m1Z4.getZeitraum());
        auswertungM1Z4.setObjectId(mstId1);
        auswertungMessstelle1.setAuswertungenProZeitraum(new ArrayList<>());
        auswertungMessstelle1.getAuswertungenProZeitraum().add(auswertungM1Z1);
        auswertungMessstelle1.getAuswertungenProZeitraum().add(auswertungM1Z2);
        auswertungMessstelle1.getAuswertungenProZeitraum().add(auswertungM1Z3);
        auswertungMessstelle1.getAuswertungenProZeitraum().add(auswertungM1Z4);

        final AuswertungMessstelle auswertungMessstelle2 = new AuswertungMessstelle();
        auswertungMessstelle2.setMstId(mstId2);
        final Auswertung auswertungM2Z1 = new Auswertung();
        auswertungM2Z1.setZeitraum(m2Z1.getZeitraum());
        auswertungM2Z1.setObjectId(mstId2);
        final Auswertung auswertungM2Z2 = new Auswertung();
        auswertungM2Z2.setZeitraum(m2Z2.getZeitraum());
        auswertungM2Z2.setObjectId(mstId2);
        final Auswertung auswertungM2Z3 = new Auswertung();
        auswertungM2Z3.setZeitraum(m2Z3.getZeitraum());
        auswertungM2Z3.setObjectId(mstId2);
        final Auswertung auswertungM2Z4 = new Auswertung();
        auswertungM2Z4.setZeitraum(m2Z4.getZeitraum());
        auswertungM2Z4.setObjectId(mstId2);
        auswertungMessstelle2.setAuswertungenProZeitraum(new ArrayList<>());
        auswertungMessstelle2.getAuswertungenProZeitraum().add(auswertungM2Z1);
        auswertungMessstelle2.getAuswertungenProZeitraum().add(auswertungM2Z2);
        auswertungMessstelle2.getAuswertungenProZeitraum().add(auswertungM2Z3);
        auswertungMessstelle2.getAuswertungenProZeitraum().add(auswertungM2Z4);

        expected.add(auswertungMessstelle1);
        expected.add(auswertungMessstelle2);

        Assertions.assertThat(result)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFieldsMatchingRegexes(".*")
                .isEqualTo(expected);
    }

    @Test
    void mapAuswertungMapToListOfAuswertungProMessstelle2() {

        final ConcurrentMap<String, List<AuswertungMessstelleUndZeitraum>> request = new ConcurrentHashMap<>();

        final List<AuswertungMessstelleUndZeitraum> messstelle1 = new ArrayList<>();
        final AuswertungMessstelleUndZeitraum m1Z1 = new AuswertungMessstelleUndZeitraum();
        final String mstId1 = "123";
        m1Z1.setMstId(mstId1);
        m1Z1.setZeitraum(new Zeitraum(YearMonth.of(2020, 1), YearMonth.of(2020, 3), AuswertungsZeitraum.QUARTAL_1));
        TagesaggregatDto dto1 = new TagesaggregatDto();
        dto1.setMqId(12301);
        TagesaggregatDto dto2 = new TagesaggregatDto();
        dto2.setMqId(12302);

        m1Z1.setMeanOfAggregatesForEachMqId(new ArrayList<>());
        m1Z1.getMeanOfAggregatesForEachMqId().add(dto1);
        m1Z1.getMeanOfAggregatesForEachMqId().add(dto2);
        messstelle1.add(m1Z1);

        final AuswertungMessstelleUndZeitraum m1Z2 = new AuswertungMessstelleUndZeitraum();
        m1Z2.setMstId(mstId1);
        m1Z2.setZeitraum(new Zeitraum(YearMonth.of(2021, 1), YearMonth.of(2021, 3), AuswertungsZeitraum.QUARTAL_1));
        m1Z2.setMeanOfAggregatesForEachMqId(new ArrayList<>());
        m1Z2.getMeanOfAggregatesForEachMqId().add(dto1);
        m1Z2.getMeanOfAggregatesForEachMqId().add(dto2);
        messstelle1.add(m1Z2);

        request.put(mstId1, messstelle1);

        final List<AuswertungMessstelle> result = auswertungService.mapAuswertungMapToListOfAuswertungProMessstelle(request);

        final List<AuswertungMessstelle> expected = new ArrayList<>();
        final AuswertungMessstelle auswertungMessstelle1 = new AuswertungMessstelle();
        auswertungMessstelle1.setMstId(mstId1);
        final Auswertung auswertungM1Z1 = new Auswertung();
        auswertungM1Z1.setZeitraum(m1Z1.getZeitraum());
        auswertungM1Z1.setObjectId(mstId1);
        final Auswertung auswertungM1Z2 = new Auswertung();
        auswertungM1Z2.setZeitraum(m1Z2.getZeitraum());
        auswertungM1Z2.setObjectId(mstId1);
        auswertungMessstelle1.setAuswertungenProZeitraum(List.of(auswertungM1Z1, auswertungM1Z2));

        final Map<String, List<Auswertung>> auswertungenProMq = new HashMap<>();
        Auswertung auswertung = new Auswertung();
        auswertung.setDaten(dto1);
        auswertung.setZeitraum(m1Z1.getZeitraum());
        auswertung.setObjectId(String.valueOf(dto1.getMqId()));
        Auswertung auswertung1 = new Auswertung();
        auswertung1.setDaten(dto1);
        auswertung1.setZeitraum(m1Z2.getZeitraum());
        auswertung1.setObjectId(String.valueOf(dto1.getMqId()));
        auswertungenProMq.put(auswertung.getObjectId(), List.of(auswertung, auswertung1));
        auswertung = new Auswertung();
        auswertung.setDaten(dto2);
        auswertung.setZeitraum(m1Z1.getZeitraum());
        auswertung.setObjectId(String.valueOf(dto2.getMqId()));
        auswertung1 = new Auswertung();
        auswertung1.setDaten(dto2);
        auswertung1.setZeitraum(m1Z2.getZeitraum());
        auswertung1.setObjectId(String.valueOf(dto2.getMqId()));
        auswertungenProMq.put(auswertung.getObjectId(), List.of(auswertung, auswertung1));
        auswertungMessstelle1.setAuswertungenProMq(auswertungenProMq);

        expected.add(auswertungMessstelle1);

        Assertions.assertThat(result)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFieldsMatchingRegexes(".*")
                .isEqualTo(expected);
    }

    @Test
    void createValidateZeitraumAndTagesTypModel() {
        final Zeitraum zeitraum = new Zeitraum(YearMonth.of(2006, 1), YearMonth.of(2006, 12), AuswertungsZeitraum.JAHRE);
        final String mstId = "1234";
        final TagesTyp tagesTyp = TagesTyp.WERKTAG_MO_FR;
        final var result = auswertungService.createValidateZeitraumAndTagesTypModel(mstId, zeitraum, tagesTyp);

        final var expected = new ValidateZeitraumAndTagesTypForMessstelleModel();
        expected.setTagesTyp(tagesTyp);
        expected.setMstId(mstId);
        final var expectedZeitraum = new ArrayList<LocalDate>();
        expectedZeitraum.add(LocalDate.of(zeitraum.getStart().getYear(), zeitraum.getStart().getMonthValue(), 1));
        expectedZeitraum.add(LocalDate.of(zeitraum.getEnd().getYear(), zeitraum.getEnd().getMonthValue(),
                zeitraum.getEnd().atEndOfMonth().getDayOfMonth()));
        expected.setZeitraum(expectedZeitraum);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

}
