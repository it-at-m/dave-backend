package de.muenchen.dave.services.messstelle.auswertung;

import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessfaehigkeitDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.Auswertung;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungMessstelle;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungMessstelleUndZeitraum;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungIdDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungOptionsDTO;
import de.muenchen.dave.domain.enums.AuswertungsZeitraum;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.domain.mapper.detektor.AuswertungMapperImpl;
import de.muenchen.dave.domain.model.messstelle.ValidateZeitraumAndTagesTypForMessstelleModel;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatDto;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatResponseDto;
import de.muenchen.dave.services.messstelle.MessstelleService;
import de.muenchen.dave.services.messstelle.MesswerteService;
import de.muenchen.dave.services.messstelle.ValidierungService;
import de.muenchen.dave.services.messstelle.Zeitraum;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@Slf4j
@ExtendWith(MockitoExtension.class)
class AuswertungServiceTest {

    @Mock
    private MessstelleService messstelleService;

    @Mock
    private MesswerteService messwerteService;

    @Mock
    private SpreadsheetService spreadsheetService;

    @Mock
    private GanglinieGesamtauswertungService ganglinieGesamtauswertungService;

    @Mock
    private ValidierungService validierungService;

    private AuswertungService auswertungService;

    @BeforeEach
    void beforeEach() {
        auswertungService = new AuswertungService(
                messstelleService,
                messwerteService,
                new AuswertungMapperImpl(),
                spreadsheetService,
                ganglinieGesamtauswertungService,
                validierungService);
        Mockito.reset(messstelleService, messwerteService, spreadsheetService, ganglinieGesamtauswertungService, validierungService);
    }

    @Test
    void ladeAuswertungGroupedByMstId() {
        final var auswertungszeitraeume = new ArrayList<AuswertungsZeitraum>();
        auswertungszeitraeume.add(AuswertungsZeitraum.QUARTAL_1);
        auswertungszeitraeume.add(AuswertungsZeitraum.QUARTAL_3);

        final var jahre = new ArrayList<Integer>();
        jahre.add(2020);
        jahre.add(2021);

        final var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(true);
        fahrzeugOptions.setGueterverkehrsanteilProzent(true);
        fahrzeugOptions.setLastkraftwagen(true);
        fahrzeugOptions.setLastzuege(true);
        fahrzeugOptions.setBusse(true);
        fahrzeugOptions.setKraftraeder(true);
        fahrzeugOptions.setPersonenkraftwagen(true);
        fahrzeugOptions.setLieferwagen(true);
        fahrzeugOptions.setRadverkehr(true);

        final var messtellenAuswertungIds = new HashSet<MessstelleAuswertungIdDTO>();
        var messstelleAuswertungId = new MessstelleAuswertungIdDTO();
        messstelleAuswertungId.setMstId("1234");
        messstelleAuswertungId.setMqIds(Set.of("123401", "123402"));
        messtellenAuswertungIds.add(messstelleAuswertungId);
        messstelleAuswertungId = new MessstelleAuswertungIdDTO();
        messstelleAuswertungId.setMstId("4567");
        messstelleAuswertungId.setMqIds(Set.of("456701", "456702"));
        messtellenAuswertungIds.add(messstelleAuswertungId);

        final var auswertungOptions = new MessstelleAuswertungOptionsDTO();
        auswertungOptions.setZeitraum(auswertungszeitraeume);
        auswertungOptions.setJahre(jahre);
        auswertungOptions.setFahrzeuge(fahrzeugOptions);
        auswertungOptions.setTagesTyp(TagesTyp.MO_SO);
        auswertungOptions.setMessstelleAuswertungIds(messtellenAuswertungIds);

        //Mockito.when(messstelleService.getMessfaehigkeitenForZeitraumForMessstelle()).thenReturn()


        //final var result = auswertungService.ladeAuswertungGroupedByMstId(auswertungOptions);

        //final var expected =
    }

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
    void createValidateZeitraumAndTagesTyp() {
        final var zeitraum = new Zeitraum(YearMonth.of(2006, 1), YearMonth.of(2006, 12), AuswertungsZeitraum.JAHRE);
        final var mstId = "1234";
        final var mqIds = Set.of("123401", "123402");
        final var tagesTyp = TagesTyp.WERKTAG_MO_FR;

        final var messfaehigkeiten = List.of(new ReadMessfaehigkeitDTO(), new ReadMessfaehigkeitDTO());
        Mockito.when(messstelleService.getMessfaehigkeitenForZeitraumForMessstelle(
                mstId,
                zeitraum.getAuswertungsZeitraum().getZeitraumStart(),
                zeitraum.getAuswertungsZeitraum().getZeitraumEnd())).thenReturn(messfaehigkeiten);

        final var result = auswertungService.createValidateZeitraumAndTagesTyp(mstId, mqIds, zeitraum, tagesTyp);

        final var expected = new ValidateZeitraumAndTagesTypForMessstelleModel();
        expected.setTagesTyp(tagesTyp);
        expected.setMstId(mstId);
        expected.setMqIds(mqIds);
        expected.setZeitraum(zeitraum);
        expected.setMessfaehigkeiten(messfaehigkeiten);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);

        Mockito.verify(messstelleService, Mockito.times(1)).getMessfaehigkeitenForZeitraumForMessstelle(
                mstId,
                zeitraum.getAuswertungsZeitraum().getZeitraumStart(),
                zeitraum.getAuswertungsZeitraum().getZeitraumEnd());
    }

    @Test
    void createEmptyTagesaggregatResponse() {
        final var result = auswertungService.createEmptyTagesaggregatResponse(Set.of("1", "2"));

        final var expected = new TagesaggregatResponseDto();
        final var emptyTagesaggregate = new ArrayList<TagesaggregatDto>();
        Set.of("1", "2").forEach(mqId -> {
            final TagesaggregatDto tagesaggregatDto = new TagesaggregatDto();
            tagesaggregatDto.setMqId(Integer.valueOf(mqId));
            emptyTagesaggregate.add(tagesaggregatDto);
        });
        expected.setMeanOfAggregatesForEachMqId(emptyTagesaggregate);
        expected.setSumOverAllAggregatesOfAllMqId(new TagesaggregatDto());

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void getZeitraeumeOfGivenMessfaehigkeiten() {
        final var messfaehigkeiten = new ArrayList<ReadMessfaehigkeitDTO>();
        var messfaehigkeit = new ReadMessfaehigkeitDTO();
        messfaehigkeit.setGueltigAb("2008-02-01");
        messfaehigkeit.setGueltigBis("2009-05-15");
        messfaehigkeiten.add(messfaehigkeit);
        messfaehigkeit = new ReadMessfaehigkeitDTO();
        messfaehigkeit.setGueltigAb("2009-04-10");
        messfaehigkeit.setGueltigBis("2009-07-31");
        messfaehigkeiten.add(messfaehigkeit);
        messfaehigkeit = new ReadMessfaehigkeitDTO();
        messfaehigkeit.setGueltigAb("2010-01-01");
        messfaehigkeit.setGueltigBis("2011-12-31");
        messfaehigkeiten.add(messfaehigkeit);

        final var result = auswertungService.getZeitraeumeOfGivenMessfaehigkeiten(messfaehigkeiten);

        final var expected = List.of(
                List.of(LocalDate.of(2008, 2, 1), LocalDate.of(2009, 5, 15)),
                List.of(LocalDate.of(2009, 4, 10), LocalDate.of(2009, 7, 31)),
                List.of(LocalDate.of(2010, 1, 1), LocalDate.of(2011, 12, 31)));

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void getAdaptedFahrzeugOptionsAccordingFahrzeugklasseAndGivenFahrzeugOptionsForFahrzeugklasseAchtPlusEins() {
        final var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(true);
        fahrzeugOptions.setGueterverkehrsanteilProzent(true);
        fahrzeugOptions.setLastkraftwagen(true);
        fahrzeugOptions.setLastzuege(true);
        fahrzeugOptions.setBusse(true);
        fahrzeugOptions.setKraftraeder(true);
        fahrzeugOptions.setPersonenkraftwagen(true);
        fahrzeugOptions.setLieferwagen(true);
        fahrzeugOptions.setRadverkehr(true);

        final var result = auswertungService.getAdaptedFahrzeugOptionsAccordingFahrzeugklasseAndGivenFahrzeugOptions(
                Fahrzeugklasse.ACHT_PLUS_EINS,
                fahrzeugOptions);

        final var expected = new FahrzeugOptionsDTO();
        expected.setKraftfahrzeugverkehr(true);
        expected.setSchwerverkehr(true);
        expected.setSchwerverkehrsanteilProzent(true);
        expected.setGueterverkehr(true);
        expected.setGueterverkehrsanteilProzent(true);
        expected.setLastkraftwagen(true);
        expected.setLastzuege(true);
        expected.setBusse(true);
        expected.setKraftraeder(true);
        expected.setPersonenkraftwagen(true);
        expected.setLieferwagen(true);
        expected.setRadverkehr(true);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void getAdaptedFahrzeugOptionsAccordingFahrzeugklasseAndGivenFahrzeugOptionsForFahrzeugklasseZweiPlusEins() {
        final var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(true);
        fahrzeugOptions.setGueterverkehrsanteilProzent(true);
        fahrzeugOptions.setLastkraftwagen(true);
        fahrzeugOptions.setLastzuege(true);
        fahrzeugOptions.setBusse(true);
        fahrzeugOptions.setKraftraeder(true);
        fahrzeugOptions.setPersonenkraftwagen(true);
        fahrzeugOptions.setLieferwagen(true);
        fahrzeugOptions.setRadverkehr(true);

        final var result = auswertungService.getAdaptedFahrzeugOptionsAccordingFahrzeugklasseAndGivenFahrzeugOptions(
                Fahrzeugklasse.ZWEI_PLUS_EINS,
                fahrzeugOptions);

        final var expected = new FahrzeugOptionsDTO();
        expected.setKraftfahrzeugverkehr(true);
        expected.setSchwerverkehr(true);
        expected.setSchwerverkehrsanteilProzent(true);
        expected.setGueterverkehr(false);
        expected.setGueterverkehrsanteilProzent(false);
        expected.setLastkraftwagen(false);
        expected.setLastzuege(false);
        expected.setBusse(false);
        expected.setKraftraeder(false);
        expected.setPersonenkraftwagen(false);
        expected.setLieferwagen(false);
        expected.setRadverkehr(true);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void getAdaptedFahrzeugOptionsAccordingFahrzeugklasseAndGivenFahrzeugOptionsForFahrzeugklasseSummeKfz() {
        final var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(true);
        fahrzeugOptions.setGueterverkehrsanteilProzent(true);
        fahrzeugOptions.setLastkraftwagen(true);
        fahrzeugOptions.setLastzuege(true);
        fahrzeugOptions.setBusse(true);
        fahrzeugOptions.setKraftraeder(true);
        fahrzeugOptions.setPersonenkraftwagen(true);
        fahrzeugOptions.setLieferwagen(true);
        fahrzeugOptions.setRadverkehr(true);

        final var result = auswertungService.getAdaptedFahrzeugOptionsAccordingFahrzeugklasseAndGivenFahrzeugOptions(
                Fahrzeugklasse.SUMME_KFZ,
                fahrzeugOptions);

        final var expected = new FahrzeugOptionsDTO();
        expected.setKraftfahrzeugverkehr(true);
        expected.setSchwerverkehr(false);
        expected.setSchwerverkehrsanteilProzent(false);
        expected.setGueterverkehr(false);
        expected.setGueterverkehrsanteilProzent(false);
        expected.setLastkraftwagen(false);
        expected.setLastzuege(false);
        expected.setBusse(false);
        expected.setKraftraeder(false);
        expected.setPersonenkraftwagen(false);
        expected.setLieferwagen(false);
        expected.setRadverkehr(true);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void getAdaptedFahrzeugOptionsAccordingFahrzeugklasseAndGivenFahrzeugOptionsForFahrzeugklasseRad() {
        final var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(true);
        fahrzeugOptions.setGueterverkehrsanteilProzent(true);
        fahrzeugOptions.setLastkraftwagen(true);
        fahrzeugOptions.setLastzuege(true);
        fahrzeugOptions.setBusse(true);
        fahrzeugOptions.setKraftraeder(true);
        fahrzeugOptions.setPersonenkraftwagen(true);
        fahrzeugOptions.setLieferwagen(true);
        fahrzeugOptions.setRadverkehr(true);

        final var result = auswertungService.getAdaptedFahrzeugOptionsAccordingFahrzeugklasseAndGivenFahrzeugOptions(
                Fahrzeugklasse.RAD,
                fahrzeugOptions);

        final var expected = new FahrzeugOptionsDTO();
        expected.setKraftfahrzeugverkehr(false);
        expected.setSchwerverkehr(false);
        expected.setSchwerverkehrsanteilProzent(false);
        expected.setGueterverkehr(false);
        expected.setGueterverkehrsanteilProzent(false);
        expected.setLastkraftwagen(false);
        expected.setLastzuege(false);
        expected.setBusse(false);
        expected.setKraftraeder(false);
        expected.setPersonenkraftwagen(false);
        expected.setLieferwagen(false);
        expected.setRadverkehr(true);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void nullingAttributesOfTagesaggregateInTagesaggregatResponseAccordingChosenFahrzeugoptions() {
        final var tagesaggregatResponse = new TagesaggregatResponseDto();

        var tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(1));
        tagesaggregat.setSummeSchwerverkehr(BigDecimal.valueOf(2));
        tagesaggregat.setProzentSchwerverkehr(BigDecimal.valueOf(3));
        tagesaggregat.setSummeGueterverkehr(BigDecimal.valueOf(4));
        tagesaggregat.setProzentGueterverkehr(BigDecimal.valueOf(5));
        tagesaggregat.setAnzahlLkw(BigDecimal.valueOf(6));
        tagesaggregat.setSummeLastzug(BigDecimal.valueOf(7));
        tagesaggregat.setAnzahlBus(BigDecimal.valueOf(8));
        tagesaggregat.setAnzahlKrad(BigDecimal.valueOf(9));
        tagesaggregat.setSummeAllePkw(BigDecimal.valueOf(10));
        tagesaggregat.setAnzahlLfw(BigDecimal.valueOf(11));
        tagesaggregat.setAnzahlRad(BigDecimal.valueOf(12));
        tagesaggregatResponse.setSumOverAllAggregatesOfAllMqId(tagesaggregat);

        var tagesaggregate = new ArrayList<TagesaggregatDto>();
        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(13));
        tagesaggregat.setSummeSchwerverkehr(BigDecimal.valueOf(14));
        tagesaggregat.setProzentSchwerverkehr(BigDecimal.valueOf(15));
        tagesaggregat.setSummeGueterverkehr(BigDecimal.valueOf(16));
        tagesaggregat.setProzentGueterverkehr(BigDecimal.valueOf(17));
        tagesaggregat.setAnzahlLkw(BigDecimal.valueOf(18));
        tagesaggregat.setSummeLastzug(BigDecimal.valueOf(19));
        tagesaggregat.setAnzahlBus(BigDecimal.valueOf(20));
        tagesaggregat.setAnzahlKrad(BigDecimal.valueOf(21));
        tagesaggregat.setSummeAllePkw(BigDecimal.valueOf(22));
        tagesaggregat.setAnzahlLfw(BigDecimal.valueOf(23));
        tagesaggregat.setAnzahlRad(BigDecimal.valueOf(24));
        tagesaggregate.add(tagesaggregat);

        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(25));
        tagesaggregat.setSummeSchwerverkehr(BigDecimal.valueOf(26));
        tagesaggregat.setProzentSchwerverkehr(BigDecimal.valueOf(27));
        tagesaggregat.setSummeGueterverkehr(BigDecimal.valueOf(28));
        tagesaggregat.setProzentGueterverkehr(BigDecimal.valueOf(29));
        tagesaggregat.setAnzahlLkw(BigDecimal.valueOf(30));
        tagesaggregat.setSummeLastzug(BigDecimal.valueOf(31));
        tagesaggregat.setAnzahlBus(BigDecimal.valueOf(32));
        tagesaggregat.setAnzahlKrad(BigDecimal.valueOf(33));
        tagesaggregat.setSummeAllePkw(BigDecimal.valueOf(34));
        tagesaggregat.setAnzahlLfw(BigDecimal.valueOf(35));
        tagesaggregat.setAnzahlRad(BigDecimal.valueOf(36));
        tagesaggregate.add(tagesaggregat);

        tagesaggregatResponse.setMeanOfAggregatesForEachMqId(tagesaggregate);

        final var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(true);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(true);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(false);

        final var result = auswertungService.nullingAttributesOfTagesaggregateInTagesaggregatResponseAccordingChosenFahrzeugoptions(tagesaggregatResponse,
                fahrzeugOptions);

        final var expected = new TagesaggregatResponseDto();

        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(null);
        tagesaggregat.setSummeSchwerverkehr(BigDecimal.valueOf(2));
        tagesaggregat.setProzentSchwerverkehr(null);
        tagesaggregat.setSummeGueterverkehr(null);
        tagesaggregat.setProzentGueterverkehr(null);
        tagesaggregat.setAnzahlLkw(BigDecimal.valueOf(6));
        tagesaggregat.setSummeLastzug(null);
        tagesaggregat.setAnzahlBus(null);
        tagesaggregat.setAnzahlKrad(null);
        tagesaggregat.setSummeAllePkw(BigDecimal.valueOf(10));
        tagesaggregat.setAnzahlLfw(null);
        tagesaggregat.setAnzahlRad(null);
        expected.setSumOverAllAggregatesOfAllMqId(tagesaggregat);

        tagesaggregate = new ArrayList<>();
        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(null);
        tagesaggregat.setSummeSchwerverkehr(BigDecimal.valueOf(14));
        tagesaggregat.setProzentSchwerverkehr(null);
        tagesaggregat.setSummeGueterverkehr(null);
        tagesaggregat.setProzentGueterverkehr(null);
        tagesaggregat.setAnzahlLkw(BigDecimal.valueOf(18));
        tagesaggregat.setSummeLastzug(null);
        tagesaggregat.setAnzahlBus(null);
        tagesaggregat.setAnzahlKrad(null);
        tagesaggregat.setSummeAllePkw(BigDecimal.valueOf(22));
        tagesaggregat.setAnzahlLfw(null);
        tagesaggregat.setAnzahlRad(null);
        tagesaggregate.add(tagesaggregat);

        tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(null);
        tagesaggregat.setSummeSchwerverkehr(BigDecimal.valueOf(26));
        tagesaggregat.setProzentSchwerverkehr(null);
        tagesaggregat.setSummeGueterverkehr(null);
        tagesaggregat.setProzentGueterverkehr(null);
        tagesaggregat.setAnzahlLkw(BigDecimal.valueOf(30));
        tagesaggregat.setSummeLastzug(null);
        tagesaggregat.setAnzahlBus(null);
        tagesaggregat.setAnzahlKrad(null);
        tagesaggregat.setSummeAllePkw(BigDecimal.valueOf(34));
        tagesaggregat.setAnzahlLfw(null);
        tagesaggregat.setAnzahlRad(null);
        tagesaggregate.add(tagesaggregat);

        expected.setMeanOfAggregatesForEachMqId(tagesaggregate);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptionsForKraftfahrzeugverkehr() {
        final var tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(1));
        tagesaggregat.setSummeSchwerverkehr(BigDecimal.valueOf(2));
        tagesaggregat.setProzentSchwerverkehr(BigDecimal.valueOf(3));
        tagesaggregat.setSummeGueterverkehr(BigDecimal.valueOf(4));
        tagesaggregat.setProzentGueterverkehr(BigDecimal.valueOf(5));
        tagesaggregat.setAnzahlLkw(BigDecimal.valueOf(6));
        tagesaggregat.setSummeLastzug(BigDecimal.valueOf(7));
        tagesaggregat.setAnzahlBus(BigDecimal.valueOf(8));
        tagesaggregat.setAnzahlKrad(BigDecimal.valueOf(9));
        tagesaggregat.setSummeAllePkw(BigDecimal.valueOf(10));
        tagesaggregat.setAnzahlLfw(BigDecimal.valueOf(11));
        tagesaggregat.setAnzahlRad(BigDecimal.valueOf(12));
        final var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(false);

        final var result = auswertungService.nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptions(tagesaggregat, fahrzeugOptions);

        final var expected = new TagesaggregatDto();
        expected.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(1));
        expected.setSummeSchwerverkehr(null);
        expected.setProzentSchwerverkehr(null);
        expected.setSummeGueterverkehr(null);
        expected.setProzentGueterverkehr(null);
        expected.setAnzahlLkw(null);
        expected.setSummeLastzug(null);
        expected.setAnzahlBus(null);
        expected.setAnzahlKrad(null);
        expected.setSummeAllePkw(null);
        expected.setAnzahlLfw(null);
        expected.setAnzahlRad(null);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptionsForSchwerverkehr() {
        final var tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(1));
        tagesaggregat.setSummeSchwerverkehr(BigDecimal.valueOf(2));
        tagesaggregat.setProzentSchwerverkehr(BigDecimal.valueOf(3));
        tagesaggregat.setSummeGueterverkehr(BigDecimal.valueOf(4));
        tagesaggregat.setProzentGueterverkehr(BigDecimal.valueOf(5));
        tagesaggregat.setAnzahlLkw(BigDecimal.valueOf(6));
        tagesaggregat.setSummeLastzug(BigDecimal.valueOf(7));
        tagesaggregat.setAnzahlBus(BigDecimal.valueOf(8));
        tagesaggregat.setAnzahlKrad(BigDecimal.valueOf(9));
        tagesaggregat.setSummeAllePkw(BigDecimal.valueOf(10));
        tagesaggregat.setAnzahlLfw(BigDecimal.valueOf(11));
        tagesaggregat.setAnzahlRad(BigDecimal.valueOf(12));
        final var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(false);

        final var result = auswertungService.nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptions(tagesaggregat, fahrzeugOptions);

        final var expected = new TagesaggregatDto();
        expected.setSummeKraftfahrzeugverkehr(null);
        expected.setSummeSchwerverkehr(BigDecimal.valueOf(2));
        expected.setProzentSchwerverkehr(null);
        expected.setSummeGueterverkehr(null);
        expected.setProzentGueterverkehr(null);
        expected.setAnzahlLkw(null);
        expected.setSummeLastzug(null);
        expected.setAnzahlBus(null);
        expected.setAnzahlKrad(null);
        expected.setSummeAllePkw(null);
        expected.setAnzahlLfw(null);
        expected.setAnzahlRad(null);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptionsForSchwerverkehrsanteilProzent() {
        final var tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(1));
        tagesaggregat.setSummeSchwerverkehr(BigDecimal.valueOf(2));
        tagesaggregat.setProzentSchwerverkehr(BigDecimal.valueOf(3));
        tagesaggregat.setSummeGueterverkehr(BigDecimal.valueOf(4));
        tagesaggregat.setProzentGueterverkehr(BigDecimal.valueOf(5));
        tagesaggregat.setAnzahlLkw(BigDecimal.valueOf(6));
        tagesaggregat.setSummeLastzug(BigDecimal.valueOf(7));
        tagesaggregat.setAnzahlBus(BigDecimal.valueOf(8));
        tagesaggregat.setAnzahlKrad(BigDecimal.valueOf(9));
        tagesaggregat.setSummeAllePkw(BigDecimal.valueOf(10));
        tagesaggregat.setAnzahlLfw(BigDecimal.valueOf(11));
        tagesaggregat.setAnzahlRad(BigDecimal.valueOf(12));
        final var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(false);

        final var result = auswertungService.nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptions(tagesaggregat, fahrzeugOptions);

        final var expected = new TagesaggregatDto();
        expected.setSummeKraftfahrzeugverkehr(null);
        expected.setSummeSchwerverkehr(null);
        expected.setProzentSchwerverkehr(BigDecimal.valueOf(3));
        expected.setSummeGueterverkehr(null);
        expected.setProzentGueterverkehr(null);
        expected.setAnzahlLkw(null);
        expected.setSummeLastzug(null);
        expected.setAnzahlBus(null);
        expected.setAnzahlKrad(null);
        expected.setSummeAllePkw(null);
        expected.setAnzahlLfw(null);
        expected.setAnzahlRad(null);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptionsForGueterverkehr() {
        final var tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(1));
        tagesaggregat.setSummeSchwerverkehr(BigDecimal.valueOf(2));
        tagesaggregat.setProzentSchwerverkehr(BigDecimal.valueOf(3));
        tagesaggregat.setSummeGueterverkehr(BigDecimal.valueOf(4));
        tagesaggregat.setProzentGueterverkehr(BigDecimal.valueOf(5));
        tagesaggregat.setAnzahlLkw(BigDecimal.valueOf(6));
        tagesaggregat.setSummeLastzug(BigDecimal.valueOf(7));
        tagesaggregat.setAnzahlBus(BigDecimal.valueOf(8));
        tagesaggregat.setAnzahlKrad(BigDecimal.valueOf(9));
        tagesaggregat.setSummeAllePkw(BigDecimal.valueOf(10));
        tagesaggregat.setAnzahlLfw(BigDecimal.valueOf(11));
        tagesaggregat.setAnzahlRad(BigDecimal.valueOf(12));
        final var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(true);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(false);

        final var result = auswertungService.nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptions(tagesaggregat, fahrzeugOptions);

        final var expected = new TagesaggregatDto();
        expected.setSummeKraftfahrzeugverkehr(null);
        expected.setSummeSchwerverkehr(null);
        expected.setProzentSchwerverkehr(null);
        expected.setSummeGueterverkehr(BigDecimal.valueOf(4));
        expected.setProzentGueterverkehr(null);
        expected.setAnzahlLkw(null);
        expected.setSummeLastzug(null);
        expected.setAnzahlBus(null);
        expected.setAnzahlKrad(null);
        expected.setSummeAllePkw(null);
        expected.setAnzahlLfw(null);
        expected.setAnzahlRad(null);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptionsForGueterverkehrsanteilProzent() {
        final var tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(1));
        tagesaggregat.setSummeSchwerverkehr(BigDecimal.valueOf(2));
        tagesaggregat.setProzentSchwerverkehr(BigDecimal.valueOf(3));
        tagesaggregat.setSummeGueterverkehr(BigDecimal.valueOf(4));
        tagesaggregat.setProzentGueterverkehr(BigDecimal.valueOf(5));
        tagesaggregat.setAnzahlLkw(BigDecimal.valueOf(6));
        tagesaggregat.setSummeLastzug(BigDecimal.valueOf(7));
        tagesaggregat.setAnzahlBus(BigDecimal.valueOf(8));
        tagesaggregat.setAnzahlKrad(BigDecimal.valueOf(9));
        tagesaggregat.setSummeAllePkw(BigDecimal.valueOf(10));
        tagesaggregat.setAnzahlLfw(BigDecimal.valueOf(11));
        tagesaggregat.setAnzahlRad(BigDecimal.valueOf(12));
        final var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(true);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(false);

        final var result = auswertungService.nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptions(tagesaggregat, fahrzeugOptions);

        final var expected = new TagesaggregatDto();
        expected.setSummeKraftfahrzeugverkehr(null);
        expected.setSummeSchwerverkehr(null);
        expected.setProzentSchwerverkehr(null);
        expected.setSummeGueterverkehr(null);
        expected.setProzentGueterverkehr(BigDecimal.valueOf(5));
        expected.setAnzahlLkw(null);
        expected.setSummeLastzug(null);
        expected.setAnzahlBus(null);
        expected.setAnzahlKrad(null);
        expected.setSummeAllePkw(null);
        expected.setAnzahlLfw(null);
        expected.setAnzahlRad(null);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptionsForLastkraftwagen() {
        final var tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(1));
        tagesaggregat.setSummeSchwerverkehr(BigDecimal.valueOf(2));
        tagesaggregat.setProzentSchwerverkehr(BigDecimal.valueOf(3));
        tagesaggregat.setSummeGueterverkehr(BigDecimal.valueOf(4));
        tagesaggregat.setProzentGueterverkehr(BigDecimal.valueOf(5));
        tagesaggregat.setAnzahlLkw(BigDecimal.valueOf(6));
        tagesaggregat.setSummeLastzug(BigDecimal.valueOf(7));
        tagesaggregat.setAnzahlBus(BigDecimal.valueOf(8));
        tagesaggregat.setAnzahlKrad(BigDecimal.valueOf(9));
        tagesaggregat.setSummeAllePkw(BigDecimal.valueOf(10));
        tagesaggregat.setAnzahlLfw(BigDecimal.valueOf(11));
        tagesaggregat.setAnzahlRad(BigDecimal.valueOf(12));
        final var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(true);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(false);

        final var result = auswertungService.nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptions(tagesaggregat, fahrzeugOptions);

        final var expected = new TagesaggregatDto();
        expected.setSummeKraftfahrzeugverkehr(null);
        expected.setSummeSchwerverkehr(null);
        expected.setProzentSchwerverkehr(null);
        expected.setSummeGueterverkehr(null);
        expected.setProzentGueterverkehr(null);
        expected.setAnzahlLkw(BigDecimal.valueOf(6));
        expected.setSummeLastzug(null);
        expected.setAnzahlBus(null);
        expected.setAnzahlKrad(null);
        expected.setSummeAllePkw(null);
        expected.setAnzahlLfw(null);
        expected.setAnzahlRad(null);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptionsForLastzuege() {
        final var tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(1));
        tagesaggregat.setSummeSchwerverkehr(BigDecimal.valueOf(2));
        tagesaggregat.setProzentSchwerverkehr(BigDecimal.valueOf(3));
        tagesaggregat.setSummeGueterverkehr(BigDecimal.valueOf(4));
        tagesaggregat.setProzentGueterverkehr(BigDecimal.valueOf(5));
        tagesaggregat.setAnzahlLkw(BigDecimal.valueOf(6));
        tagesaggregat.setSummeLastzug(BigDecimal.valueOf(7));
        tagesaggregat.setAnzahlBus(BigDecimal.valueOf(8));
        tagesaggregat.setAnzahlKrad(BigDecimal.valueOf(9));
        tagesaggregat.setSummeAllePkw(BigDecimal.valueOf(10));
        tagesaggregat.setAnzahlLfw(BigDecimal.valueOf(11));
        tagesaggregat.setAnzahlRad(BigDecimal.valueOf(12));
        final var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(true);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(false);

        final var result = auswertungService.nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptions(tagesaggregat, fahrzeugOptions);

        final var expected = new TagesaggregatDto();
        expected.setSummeKraftfahrzeugverkehr(null);
        expected.setSummeSchwerverkehr(null);
        expected.setProzentSchwerverkehr(null);
        expected.setSummeGueterverkehr(null);
        expected.setProzentGueterverkehr(null);
        expected.setAnzahlLkw(null);
        expected.setSummeLastzug(BigDecimal.valueOf(7));
        expected.setAnzahlBus(null);
        expected.setAnzahlKrad(null);
        expected.setSummeAllePkw(null);
        expected.setAnzahlLfw(null);
        expected.setAnzahlRad(null);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptionsForBusse() {
        final var tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(1));
        tagesaggregat.setSummeSchwerverkehr(BigDecimal.valueOf(2));
        tagesaggregat.setProzentSchwerverkehr(BigDecimal.valueOf(3));
        tagesaggregat.setSummeGueterverkehr(BigDecimal.valueOf(4));
        tagesaggregat.setProzentGueterverkehr(BigDecimal.valueOf(5));
        tagesaggregat.setAnzahlLkw(BigDecimal.valueOf(6));
        tagesaggregat.setSummeLastzug(BigDecimal.valueOf(7));
        tagesaggregat.setAnzahlBus(BigDecimal.valueOf(8));
        tagesaggregat.setAnzahlKrad(BigDecimal.valueOf(9));
        tagesaggregat.setSummeAllePkw(BigDecimal.valueOf(10));
        tagesaggregat.setAnzahlLfw(BigDecimal.valueOf(11));
        tagesaggregat.setAnzahlRad(BigDecimal.valueOf(12));
        final var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(true);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(false);

        final var result = auswertungService.nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptions(tagesaggregat, fahrzeugOptions);

        final var expected = new TagesaggregatDto();
        expected.setSummeKraftfahrzeugverkehr(null);
        expected.setSummeSchwerverkehr(null);
        expected.setProzentSchwerverkehr(null);
        expected.setSummeGueterverkehr(null);
        expected.setProzentGueterverkehr(null);
        expected.setAnzahlLkw(null);
        expected.setSummeLastzug(null);
        expected.setAnzahlBus(BigDecimal.valueOf(8));
        expected.setAnzahlKrad(null);
        expected.setSummeAllePkw(null);
        expected.setAnzahlLfw(null);
        expected.setAnzahlRad(null);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptionsForKraftraeder() {
        final var tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(1));
        tagesaggregat.setSummeSchwerverkehr(BigDecimal.valueOf(2));
        tagesaggregat.setProzentSchwerverkehr(BigDecimal.valueOf(3));
        tagesaggregat.setSummeGueterverkehr(BigDecimal.valueOf(4));
        tagesaggregat.setProzentGueterverkehr(BigDecimal.valueOf(5));
        tagesaggregat.setAnzahlLkw(BigDecimal.valueOf(6));
        tagesaggregat.setSummeLastzug(BigDecimal.valueOf(7));
        tagesaggregat.setAnzahlBus(BigDecimal.valueOf(8));
        tagesaggregat.setAnzahlKrad(BigDecimal.valueOf(9));
        tagesaggregat.setSummeAllePkw(BigDecimal.valueOf(10));
        tagesaggregat.setAnzahlLfw(BigDecimal.valueOf(11));
        tagesaggregat.setAnzahlRad(BigDecimal.valueOf(12));
        final var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(true);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(false);

        final var result = auswertungService.nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptions(tagesaggregat, fahrzeugOptions);

        final var expected = new TagesaggregatDto();
        expected.setSummeKraftfahrzeugverkehr(null);
        expected.setSummeSchwerverkehr(null);
        expected.setProzentSchwerverkehr(null);
        expected.setSummeGueterverkehr(null);
        expected.setProzentGueterverkehr(null);
        expected.setAnzahlLkw(null);
        expected.setSummeLastzug(null);
        expected.setAnzahlBus(null);
        expected.setAnzahlKrad(BigDecimal.valueOf(9));
        expected.setSummeAllePkw(null);
        expected.setAnzahlLfw(null);
        expected.setAnzahlRad(null);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptionsForPersonenkraftwagen() {
        final var tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(1));
        tagesaggregat.setSummeSchwerverkehr(BigDecimal.valueOf(2));
        tagesaggregat.setProzentSchwerverkehr(BigDecimal.valueOf(3));
        tagesaggregat.setSummeGueterverkehr(BigDecimal.valueOf(4));
        tagesaggregat.setProzentGueterverkehr(BigDecimal.valueOf(5));
        tagesaggregat.setAnzahlLkw(BigDecimal.valueOf(6));
        tagesaggregat.setSummeLastzug(BigDecimal.valueOf(7));
        tagesaggregat.setAnzahlBus(BigDecimal.valueOf(8));
        tagesaggregat.setAnzahlKrad(BigDecimal.valueOf(9));
        tagesaggregat.setSummeAllePkw(BigDecimal.valueOf(10));
        tagesaggregat.setAnzahlLfw(BigDecimal.valueOf(11));
        tagesaggregat.setAnzahlRad(BigDecimal.valueOf(12));
        final var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(true);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(false);

        final var result = auswertungService.nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptions(tagesaggregat, fahrzeugOptions);

        final var expected = new TagesaggregatDto();
        expected.setSummeKraftfahrzeugverkehr(null);
        expected.setSummeSchwerverkehr(null);
        expected.setProzentSchwerverkehr(null);
        expected.setSummeGueterverkehr(null);
        expected.setProzentGueterverkehr(null);
        expected.setAnzahlLkw(null);
        expected.setSummeLastzug(null);
        expected.setAnzahlBus(null);
        expected.setAnzahlKrad(null);
        expected.setSummeAllePkw(BigDecimal.valueOf(10));
        expected.setAnzahlLfw(null);
        expected.setAnzahlRad(null);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptionsForLieferwagen() {
        final var tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(1));
        tagesaggregat.setSummeSchwerverkehr(BigDecimal.valueOf(2));
        tagesaggregat.setProzentSchwerverkehr(BigDecimal.valueOf(3));
        tagesaggregat.setSummeGueterverkehr(BigDecimal.valueOf(4));
        tagesaggregat.setProzentGueterverkehr(BigDecimal.valueOf(5));
        tagesaggregat.setAnzahlLkw(BigDecimal.valueOf(6));
        tagesaggregat.setSummeLastzug(BigDecimal.valueOf(7));
        tagesaggregat.setAnzahlBus(BigDecimal.valueOf(8));
        tagesaggregat.setAnzahlKrad(BigDecimal.valueOf(9));
        tagesaggregat.setSummeAllePkw(BigDecimal.valueOf(10));
        tagesaggregat.setAnzahlLfw(BigDecimal.valueOf(11));
        tagesaggregat.setAnzahlRad(BigDecimal.valueOf(12));
        final var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(true);
        fahrzeugOptions.setRadverkehr(false);

        final var result = auswertungService.nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptions(tagesaggregat, fahrzeugOptions);

        final var expected = new TagesaggregatDto();
        expected.setSummeKraftfahrzeugverkehr(null);
        expected.setSummeSchwerverkehr(null);
        expected.setProzentSchwerverkehr(null);
        expected.setSummeGueterverkehr(null);
        expected.setProzentGueterverkehr(null);
        expected.setAnzahlLkw(null);
        expected.setSummeLastzug(null);
        expected.setAnzahlBus(null);
        expected.setAnzahlKrad(null);
        expected.setSummeAllePkw(null);
        expected.setAnzahlLfw(BigDecimal.valueOf(11));
        expected.setAnzahlRad(null);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptionsForRadverkehr() {
        final var tagesaggregat = new TagesaggregatDto();
        tagesaggregat.setSummeKraftfahrzeugverkehr(BigDecimal.valueOf(1));
        tagesaggregat.setSummeSchwerverkehr(BigDecimal.valueOf(2));
        tagesaggregat.setProzentSchwerverkehr(BigDecimal.valueOf(3));
        tagesaggregat.setSummeGueterverkehr(BigDecimal.valueOf(4));
        tagesaggregat.setProzentGueterverkehr(BigDecimal.valueOf(5));
        tagesaggregat.setAnzahlLkw(BigDecimal.valueOf(6));
        tagesaggregat.setSummeLastzug(BigDecimal.valueOf(7));
        tagesaggregat.setAnzahlBus(BigDecimal.valueOf(8));
        tagesaggregat.setAnzahlKrad(BigDecimal.valueOf(9));
        tagesaggregat.setSummeAllePkw(BigDecimal.valueOf(10));
        tagesaggregat.setAnzahlLfw(BigDecimal.valueOf(11));
        tagesaggregat.setAnzahlRad(BigDecimal.valueOf(12));
        final var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);

        final var result = auswertungService.nullingAttributesOfTagesaggregatAccordingChosenFahrzeugoptions(tagesaggregat, fahrzeugOptions);

        final var expected = new TagesaggregatDto();
        expected.setSummeKraftfahrzeugverkehr(null);
        expected.setSummeSchwerverkehr(null);
        expected.setProzentSchwerverkehr(null);
        expected.setSummeGueterverkehr(null);
        expected.setProzentGueterverkehr(null);
        expected.setAnzahlLkw(null);
        expected.setSummeLastzug(null);
        expected.setAnzahlBus(null);
        expected.setAnzahlKrad(null);
        expected.setSummeAllePkw(null);
        expected.setAnzahlLfw(null);
        expected.setAnzahlRad(BigDecimal.valueOf(12));

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(expected);
    }

}
