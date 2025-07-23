package de.muenchen.dave.services.messstelle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessfaehigkeitDTO;
import de.muenchen.dave.domain.dtos.messstelle.ValidateZeitraumAndTagestypForMessstelleDTO;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.domain.model.messstelle.ValidateZeitraumAndTagesTypForMessstelleModel;
import de.muenchen.dave.services.KalendertagService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ValidierungServiceTest {

    @Mock
    private UnauffaelligeTageService unauffaelligeTageService;

    @Mock
    private KalendertagService kalendertagService;

    private ValidierungService validierungService;

    @BeforeEach
    void beforeEach() {
        validierungService = new ValidierungService(unauffaelligeTageService, kalendertagService);
        Mockito.reset(unauffaelligeTageService, kalendertagService);
    }

    @Test
    void isZeitraumAndTagestypValidMoreThanTwoUnauffaelligeTageAndMoreThanFiftyPercentUnauffaelligeTage() {
        final var request = new ValidateZeitraumAndTagestypForMessstelleDTO();
        request.setMstId("1234");
        request.setZeitraum(List.of(LocalDate.of(2008, 1, 1), LocalDate.of(2008, 1, 31)));
        request.setTagesTyp(TagesTyp.WERKTAG_MO_FR);

        Mockito.when(kalendertagService.countAllKalendertageByDatumAndTagestypen(
                LocalDate.of(2008, 1, 1),
                LocalDate.of(2008, 1, 31),
                TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR)))
                .thenReturn(5L);

        Mockito.when(unauffaelligeTageService.countAllUnauffaelligetageByMstIdAndTimerangeAndTagestypen(
                request.getMstId(),
                LocalDate.of(2008, 1, 1),
                LocalDate.of(2008, 1, 31),
                TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR)))
                .thenReturn(3L);

        final var result = validierungService.isZeitraumAndTagestypValid(request);

        assertThat(result, is(true));
    }

    @Test
    void isZeitraumAndTagestypValidMoreThanTwoUnauffaelligeTageAndLessThenFiftyPercentUnauffaelligeTage() {
        final var request = new ValidateZeitraumAndTagestypForMessstelleDTO();
        request.setMstId("1234");
        request.setZeitraum(List.of(LocalDate.of(2008, 1, 1), LocalDate.of(2008, 1, 31)));
        request.setTagesTyp(TagesTyp.WERKTAG_MO_FR);

        Mockito.when(kalendertagService.countAllKalendertageByDatumAndTagestypen(
                LocalDate.of(2008, 1, 1),
                LocalDate.of(2008, 1, 31),
                TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR)))
                .thenReturn(10L);

        Mockito.when(unauffaelligeTageService.countAllUnauffaelligetageByMstIdAndTimerangeAndTagestypen(
                request.getMstId(),
                LocalDate.of(2008, 1, 1),
                LocalDate.of(2008, 1, 31),
                TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR)))
                .thenReturn(3L);

        final var result = validierungService.isZeitraumAndTagestypValid(request);

        assertThat(result, is(false));
    }

    @Test
    void isZeitraumAndTagestypValidLessThanTwoUnauffaelligeTageAndMoreThenFiftyPercentUnauffaelligeTage() {
        final var request = new ValidateZeitraumAndTagestypForMessstelleDTO();
        request.setMstId("1234");
        request.setZeitraum(List.of(LocalDate.of(2008, 1, 1), LocalDate.of(2008, 1, 31)));
        request.setTagesTyp(TagesTyp.WERKTAG_MO_FR);

        Mockito.when(kalendertagService.countAllKalendertageByDatumAndTagestypen(
                LocalDate.of(2008, 1, 1),
                LocalDate.of(2008, 1, 31),
                TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR)))
                .thenReturn(1L);

        Mockito.when(unauffaelligeTageService.countAllUnauffaelligetageByMstIdAndTimerangeAndTagestypen(
                request.getMstId(),
                LocalDate.of(2008, 1, 1),
                LocalDate.of(2008, 1, 31),
                TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR)))
                .thenReturn(1L);

        final var result = validierungService.isZeitraumAndTagestypValid(request);

        assertThat(result, is(false));
    }

    @Test
    void isZeitraumAndTagestypValidLessThanTwoUnauffaelligeTageAndLessThenFiftyPercentUnauffaelligeTage() {
        final var request = new ValidateZeitraumAndTagestypForMessstelleDTO();
        request.setMstId("1234");
        request.setZeitraum(List.of(LocalDate.of(2008, 1, 1), LocalDate.of(2008, 1, 31)));
        request.setTagesTyp(TagesTyp.WERKTAG_MO_FR);

        Mockito.when(kalendertagService.countAllKalendertageByDatumAndTagestypen(
                LocalDate.of(2008, 1, 1),
                LocalDate.of(2008, 1, 31),
                TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR)))
                .thenReturn(3L);

        Mockito.when(unauffaelligeTageService.countAllUnauffaelligetageByMstIdAndTimerangeAndTagestypen(
                request.getMstId(),
                LocalDate.of(2008, 1, 1),
                LocalDate.of(2008, 1, 31),
                TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR)))
                .thenReturn(1L);

        final var result = validierungService.isZeitraumAndTagestypValid(request);

        assertThat(result, is(false));
    }

    @Test
    void areZeitraeumeAndTagesTypForMessstelleValidWithMoreThanTwoUnauffaelligeTageAndMoreThanFiftyPercentUnauffaelligeTage() {
        final var mstId = "1234";
        final var zeitraeume = List.of(
                List.of(LocalDate.of(2008, 1, 1), LocalDate.of(2008, 1, 31)),
                List.of(LocalDate.of(2008, 2, 1), LocalDate.of(2008, 2, 28)));
        final var tagesTyp = TagesTyp.WERKTAG_MO_FR;

        Mockito.when(
                kalendertagService.countAllKalendertageByDatumAndTagestypen(
                        LocalDate.of(2008, 1, 1),
                        LocalDate.of(2008, 1, 31),
                        TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR)))
                .thenReturn(10L);

        Mockito.when(
                kalendertagService.countAllKalendertageByDatumAndTagestypen(
                        LocalDate.of(2008, 2, 1),
                        LocalDate.of(2008, 2, 28),
                        TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR)))
                .thenReturn(11L);

        Mockito.when(unauffaelligeTageService.countAllUnauffaelligetageByMstIdAndTimerangeAndTagestypen(
                mstId,
                LocalDate.of(2008, 1, 1),
                LocalDate.of(2008, 1, 31),
                TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR))).thenReturn(5L);

        Mockito.when(unauffaelligeTageService.countAllUnauffaelligetageByMstIdAndTimerangeAndTagestypen(
                mstId,
                LocalDate.of(2008, 2, 1),
                LocalDate.of(2008, 2, 28),
                TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR))).thenReturn(6L);

        final var result = validierungService.areZeitraeumeAndTagesTypForMessstelleValid(mstId, zeitraeume, tagesTyp);

        final var expected = new ValidierungService.ValidationResult();
        expected.setValid(true);
        expected.setNumberOfUnauffaelligeTage(11L);
        expected.setNumberOfRelevantKalendertage(21L);
        assertThat(result, is(expected));
    }

    @Test
    void areZeitraeumeAndTagesTypForMessstelleValidWithMoreThanTwoUnauffaelligeTageAndLessThenFiftyPercentUnauffaelligeTage() {
        final var mstId = "1234";
        final var zeitraeume = List.of(
                List.of(LocalDate.of(2008, 1, 1), LocalDate.of(2008, 1, 31)),
                List.of(LocalDate.of(2008, 2, 1), LocalDate.of(2008, 2, 28)));
        final var tagesTyp = TagesTyp.WERKTAG_MO_FR;

        Mockito.when(
                kalendertagService.countAllKalendertageByDatumAndTagestypen(
                        LocalDate.of(2008, 1, 1),
                        LocalDate.of(2008, 1, 31),
                        TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR)))
                .thenReturn(10L);

        Mockito.when(
                kalendertagService.countAllKalendertageByDatumAndTagestypen(
                        LocalDate.of(2008, 2, 1),
                        LocalDate.of(2008, 2, 28),
                        TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR)))
                .thenReturn(11L);

        Mockito.when(unauffaelligeTageService.countAllUnauffaelligetageByMstIdAndTimerangeAndTagestypen(
                mstId,
                LocalDate.of(2008, 1, 1),
                LocalDate.of(2008, 1, 31),
                TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR))).thenReturn(5L);

        Mockito.when(unauffaelligeTageService.countAllUnauffaelligetageByMstIdAndTimerangeAndTagestypen(
                mstId,
                LocalDate.of(2008, 2, 1),
                LocalDate.of(2008, 2, 28),
                TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR))).thenReturn(5L);

        final var result = validierungService.areZeitraeumeAndTagesTypForMessstelleValid(mstId, zeitraeume, tagesTyp);

        final var expected = new ValidierungService.ValidationResult();
        expected.setValid(false);
        expected.setNumberOfUnauffaelligeTage(10L);
        expected.setNumberOfRelevantKalendertage(21L);
        assertThat(result, is(expected));
    }

    @Test
    void areZeitraeumeAndTagesTypForMessstelleValidWithLessThanTwoUnauffaelligeTageAndMoreThenFiftyPercentUnauffaelligeTage() {
        final var mstId = "1234";
        final var zeitraeume = List.of(
                List.of(LocalDate.of(2008, 1, 1), LocalDate.of(2008, 1, 31)),
                List.of(LocalDate.of(2008, 2, 1), LocalDate.of(2008, 2, 28)));
        final var tagesTyp = TagesTyp.WERKTAG_MO_FR;

        Mockito.when(
                kalendertagService.countAllKalendertageByDatumAndTagestypen(
                        LocalDate.of(2008, 1, 1),
                        LocalDate.of(2008, 1, 31),
                        TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR)))
                .thenReturn(1L);

        Mockito.when(
                kalendertagService.countAllKalendertageByDatumAndTagestypen(
                        LocalDate.of(2008, 2, 1),
                        LocalDate.of(2008, 2, 28),
                        TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR)))
                .thenReturn(0L);

        Mockito.when(unauffaelligeTageService.countAllUnauffaelligetageByMstIdAndTimerangeAndTagestypen(
                mstId,
                LocalDate.of(2008, 1, 1),
                LocalDate.of(2008, 1, 31),
                TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR))).thenReturn(1L);

        Mockito.when(unauffaelligeTageService.countAllUnauffaelligetageByMstIdAndTimerangeAndTagestypen(
                mstId,
                LocalDate.of(2008, 2, 1),
                LocalDate.of(2008, 2, 28),
                TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR))).thenReturn(0L);

        final var result = validierungService.areZeitraeumeAndTagesTypForMessstelleValid(mstId, zeitraeume, tagesTyp);

        final var expected = new ValidierungService.ValidationResult();
        expected.setValid(false);
        expected.setNumberOfUnauffaelligeTage(1L);
        expected.setNumberOfRelevantKalendertage(1L);
        assertThat(result, is(expected));
    }

    @Test
    void areZeitraeumeAndTagesTypForMessstelleValidWithLessThanTwoUnauffaelligeTageAndLessThenFiftyPercentUnauffaelligeTage() {
        final var mstId = "1234";
        final var zeitraeume = List.of(
                List.of(LocalDate.of(2008, 1, 1), LocalDate.of(2008, 1, 31)),
                List.of(LocalDate.of(2008, 2, 1), LocalDate.of(2008, 2, 28)));
        final var tagesTyp = TagesTyp.WERKTAG_MO_FR;

        Mockito.when(
                kalendertagService.countAllKalendertageByDatumAndTagestypen(
                        LocalDate.of(2008, 1, 1),
                        LocalDate.of(2008, 1, 31),
                        TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR)))
                .thenReturn(1L);

        Mockito.when(
                kalendertagService.countAllKalendertageByDatumAndTagestypen(
                        LocalDate.of(2008, 2, 1),
                        LocalDate.of(2008, 2, 28),
                        TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR)))
                .thenReturn(2L);

        Mockito.when(unauffaelligeTageService.countAllUnauffaelligetageByMstIdAndTimerangeAndTagestypen(
                mstId,
                LocalDate.of(2008, 1, 1),
                LocalDate.of(2008, 1, 31),
                TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR))).thenReturn(1L);

        Mockito.when(unauffaelligeTageService.countAllUnauffaelligetageByMstIdAndTimerangeAndTagestypen(
                mstId,
                LocalDate.of(2008, 2, 1),
                LocalDate.of(2008, 2, 28),
                TagesTyp.getIncludedTagestypen(TagesTyp.WERKTAG_MO_FR))).thenReturn(0L);

        final var result = validierungService.areZeitraeumeAndTagesTypForMessstelleValid(mstId, zeitraeume, tagesTyp);

        final var expected = new ValidierungService.ValidationResult();
        expected.setValid(false);
        expected.setNumberOfUnauffaelligeTage(1L);
        expected.setNumberOfRelevantKalendertage(3L);
        assertThat(result, is(expected));
    }

    @Test
    void hasMinimuOfTwoUnauffaelligeTage() {
        long numberOfUnauffaelligeTage = 0;
        boolean result = validierungService.hasMinimuOfTwoUnauffaelligeTage(numberOfUnauffaelligeTage);
        assertThat(result, is(false));

        numberOfUnauffaelligeTage = 1;
        result = validierungService.hasMinimuOfTwoUnauffaelligeTage(numberOfUnauffaelligeTage);
        assertThat(result, is(false));

        numberOfUnauffaelligeTage = 2;
        result = validierungService.hasMinimuOfTwoUnauffaelligeTage(numberOfUnauffaelligeTage);
        assertThat(result, is(true));

        numberOfUnauffaelligeTage = 10;
        result = validierungService.hasMinimuOfTwoUnauffaelligeTage(numberOfUnauffaelligeTage);
        assertThat(result, is(true));
    }

    @Test
    void hasMinimuOfFiftyPercentUnauffaelligeTage() {
        long numberOfRelevantKalendertage = 10;
        long numberOfUnauffaelligeTage = 1;
        boolean result = validierungService.hasMinimuOfFiftyPercentUnauffaelligeTage(numberOfUnauffaelligeTage, numberOfRelevantKalendertage);
        assertThat(result, is(false));

        numberOfUnauffaelligeTage = 5;
        result = validierungService.hasMinimuOfFiftyPercentUnauffaelligeTage(numberOfUnauffaelligeTage, numberOfRelevantKalendertage);
        assertThat(result, is(true));

        numberOfUnauffaelligeTage = 10;
        result = validierungService.hasMinimuOfFiftyPercentUnauffaelligeTage(numberOfUnauffaelligeTage, numberOfRelevantKalendertage);
        assertThat(result, is(true));

        numberOfUnauffaelligeTage = 0;
        result = validierungService.hasMinimuOfFiftyPercentUnauffaelligeTage(numberOfUnauffaelligeTage, numberOfRelevantKalendertage);
        assertThat(result, is(false));

        numberOfRelevantKalendertage = 0;
        numberOfUnauffaelligeTage = 10;
        result = validierungService.hasMinimuOfFiftyPercentUnauffaelligeTage(numberOfUnauffaelligeTage, numberOfRelevantKalendertage);
        assertThat(result, is(false));
    }

    @Test
    void getRelevantMessfaehigkeitenAccordingFahrzeugklasseForAchtPlusEins() {
        final var messfaehigkeiten = new ArrayList<ReadMessfaehigkeitDTO>();
        var messfaehigkeit = new ReadMessfaehigkeitDTO();
        messfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.ACHT_PLUS_EINS);
        messfaehigkeiten.add(messfaehigkeit);

        messfaehigkeit = new ReadMessfaehigkeitDTO();
        messfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.ZWEI_PLUS_EINS);
        messfaehigkeiten.add(messfaehigkeit);

        messfaehigkeit = new ReadMessfaehigkeitDTO();
        messfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.SUMME_KFZ);
        messfaehigkeiten.add(messfaehigkeit);

        messfaehigkeit = new ReadMessfaehigkeitDTO();
        messfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.RAD);
        messfaehigkeiten.add(messfaehigkeit);

        final ValidateZeitraumAndTagesTypForMessstelleModel validationObject = new ValidateZeitraumAndTagesTypForMessstelleModel();
        validationObject.setMessfaehigkeiten(messfaehigkeiten);

        var result = validierungService.getRelevantMessfaehigkeitenAccordingFahrzeugklasse(validationObject, Fahrzeugklasse.ACHT_PLUS_EINS);
        var expected = new ArrayList<ReadMessfaehigkeitDTO>();
        var expectedMessfaehigkeit = new ReadMessfaehigkeitDTO();
        expectedMessfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.ACHT_PLUS_EINS);
        expected.add(expectedMessfaehigkeit);
        assertThat(result, is(expected));

    }

    @Test
    void getRelevantMessfaehigkeitenAccordingFahrzeugklasseForZweiPlusEins() {
        final var messfaehigkeiten = new ArrayList<ReadMessfaehigkeitDTO>();
        var messfaehigkeit = new ReadMessfaehigkeitDTO();
        messfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.ACHT_PLUS_EINS);
        messfaehigkeiten.add(messfaehigkeit);

        messfaehigkeit = new ReadMessfaehigkeitDTO();
        messfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.ZWEI_PLUS_EINS);
        messfaehigkeiten.add(messfaehigkeit);

        messfaehigkeit = new ReadMessfaehigkeitDTO();
        messfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.SUMME_KFZ);
        messfaehigkeiten.add(messfaehigkeit);

        messfaehigkeit = new ReadMessfaehigkeitDTO();
        messfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.RAD);
        messfaehigkeiten.add(messfaehigkeit);

        final ValidateZeitraumAndTagesTypForMessstelleModel validationObject = new ValidateZeitraumAndTagesTypForMessstelleModel();
        validationObject.setMessfaehigkeiten(messfaehigkeiten);

        var result = validierungService.getRelevantMessfaehigkeitenAccordingFahrzeugklasse(validationObject, Fahrzeugklasse.ZWEI_PLUS_EINS);
        var expected = new ArrayList<ReadMessfaehigkeitDTO>();
        var expectedMessfaehigkeit = new ReadMessfaehigkeitDTO();
        expectedMessfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.ACHT_PLUS_EINS);
        expected.add(expectedMessfaehigkeit);

        expectedMessfaehigkeit = new ReadMessfaehigkeitDTO();
        expectedMessfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.ZWEI_PLUS_EINS);
        expected.add(expectedMessfaehigkeit);
        assertThat(result, is(expected));
    }

    @Test
    void getRelevantMessfaehigkeitenAccordingFahrzeugklasseForSummeKfz() {
        final var messfaehigkeiten = new ArrayList<ReadMessfaehigkeitDTO>();
        var messfaehigkeit = new ReadMessfaehigkeitDTO();
        messfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.ACHT_PLUS_EINS);
        messfaehigkeiten.add(messfaehigkeit);

        messfaehigkeit = new ReadMessfaehigkeitDTO();
        messfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.ZWEI_PLUS_EINS);
        messfaehigkeiten.add(messfaehigkeit);

        messfaehigkeit = new ReadMessfaehigkeitDTO();
        messfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.SUMME_KFZ);
        messfaehigkeiten.add(messfaehigkeit);

        messfaehigkeit = new ReadMessfaehigkeitDTO();
        messfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.RAD);
        messfaehigkeiten.add(messfaehigkeit);

        final ValidateZeitraumAndTagesTypForMessstelleModel validationObject = new ValidateZeitraumAndTagesTypForMessstelleModel();
        validationObject.setMessfaehigkeiten(messfaehigkeiten);

        var result = validierungService.getRelevantMessfaehigkeitenAccordingFahrzeugklasse(validationObject, Fahrzeugklasse.SUMME_KFZ);
        var expected = new ArrayList<ReadMessfaehigkeitDTO>();
        var expectedMessfaehigkeit = new ReadMessfaehigkeitDTO();
        expectedMessfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.ACHT_PLUS_EINS);
        expected.add(expectedMessfaehigkeit);

        expectedMessfaehigkeit = new ReadMessfaehigkeitDTO();
        expectedMessfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.ZWEI_PLUS_EINS);
        expected.add(expectedMessfaehigkeit);

        expectedMessfaehigkeit = new ReadMessfaehigkeitDTO();
        expectedMessfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.SUMME_KFZ);
        expected.add(expectedMessfaehigkeit);
        assertThat(result, is(expected));
    }

    @Test
    void getRelevantMessfaehigkeitenAccordingFahrzeugklasseForRad() {
        final var messfaehigkeiten = new ArrayList<ReadMessfaehigkeitDTO>();
        var messfaehigkeit = new ReadMessfaehigkeitDTO();
        messfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.ACHT_PLUS_EINS);
        messfaehigkeiten.add(messfaehigkeit);

        messfaehigkeit = new ReadMessfaehigkeitDTO();
        messfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.ZWEI_PLUS_EINS);
        messfaehigkeiten.add(messfaehigkeit);

        messfaehigkeit = new ReadMessfaehigkeitDTO();
        messfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.SUMME_KFZ);
        messfaehigkeiten.add(messfaehigkeit);

        messfaehigkeit = new ReadMessfaehigkeitDTO();
        messfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.RAD);
        messfaehigkeiten.add(messfaehigkeit);

        final ValidateZeitraumAndTagesTypForMessstelleModel validationObject = new ValidateZeitraumAndTagesTypForMessstelleModel();
        validationObject.setMessfaehigkeiten(messfaehigkeiten);

        var result = validierungService.getRelevantMessfaehigkeitenAccordingFahrzeugklasse(validationObject, Fahrzeugklasse.RAD);
        var expected = new ArrayList<ReadMessfaehigkeitDTO>();
        var expectedMessfaehigkeit = new ReadMessfaehigkeitDTO();
        expectedMessfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.RAD);
        expected.add(expectedMessfaehigkeit);
        assertThat(result, is(expected));
    }

    @Test
    void getFahrzeugklasseAccordingChoosenFahrzeugoptions() {
        var fahrzeugOptions = new FahrzeugOptionsDTO();
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
        var result = validierungService.getFahrzeugklasseAccordingChoosenFahrzeugoptions(fahrzeugOptions);
        assertThat(result, is(Fahrzeugklasse.ACHT_PLUS_EINS));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.getFahrzeugklasseAccordingChoosenFahrzeugoptions(fahrzeugOptions);
        assertThat(result, is(Fahrzeugklasse.ZWEI_PLUS_EINS));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.getFahrzeugklasseAccordingChoosenFahrzeugoptions(fahrzeugOptions);
        assertThat(result, is(Fahrzeugklasse.SUMME_KFZ));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        result = validierungService.getFahrzeugklasseAccordingChoosenFahrzeugoptions(fahrzeugOptions);
        assertThat(result, is(Fahrzeugklasse.RAD));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        fahrzeugOptions.setRadverkehr(false);
        result = validierungService.getFahrzeugklasseAccordingChoosenFahrzeugoptions(fahrzeugOptions);
        assertThat(result, is(nullValue()));
    }

    @Test
    void areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen() {
        var fahrzeugOptions = new FahrzeugOptionsDTO();
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
        var result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));
    }

    @Test
    void areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen() {
        var fahrzeugOptions = new FahrzeugOptionsDTO();
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
        fahrzeugOptions.setRadverkehr(true);
        var result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(true);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(true);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(true);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(true);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(true);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(true);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(true);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(true);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(false));
    }

    @Test
    void areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen() {
        var fahrzeugOptions = new FahrzeugOptionsDTO();
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
        fahrzeugOptions.setRadverkehr(true);
        var result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
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
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        fahrzeugOptions.setLieferwagen(true);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));
    }

    @Test
    void areFahrzeugoptionsForFahrzeugklasseRadChoosen() {
        var fahrzeugOptions = new FahrzeugOptionsDTO();
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
        var result = validierungService.areFahrzeugoptionsForFahrzeugklasseRadChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        fahrzeugOptions.setRadverkehr(false);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseRadChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
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
        fahrzeugOptions.setRadverkehr(false);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseRadChoosen(fahrzeugOptions);
        assertThat(result, is(false));
    }

    @Test
    void isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompareAchtPlusEinsEqualsAchtPlusEins() {
        assertThat(validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(
                Fahrzeugklasse.ACHT_PLUS_EINS, Fahrzeugklasse.ACHT_PLUS_EINS), is(true));
    }

    @Test
    void isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompareZweiPlusEinsEqualsZweiPlusEins() {
        assertThat(validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(
                Fahrzeugklasse.ZWEI_PLUS_EINS, Fahrzeugklasse.ZWEI_PLUS_EINS), is(true));
    }

    @Test
    void isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompareZweiPlusEinsContainsAchtPlusEins() {
        assertThat(validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(
                Fahrzeugklasse.ZWEI_PLUS_EINS, Fahrzeugklasse.ACHT_PLUS_EINS), is(true));
    }

    @Test
    void isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompareAchtPlusEinsNotContainedInZweiPlusEins() {
        assertThat(validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(
                Fahrzeugklasse.ACHT_PLUS_EINS, Fahrzeugklasse.ZWEI_PLUS_EINS), is(false));
    }

    @Test
    void isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompareSummeKfzContainsAchtPlusEins() {
        assertThat(validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(
                Fahrzeugklasse.SUMME_KFZ, Fahrzeugklasse.ACHT_PLUS_EINS), is(true));
    }

    @Test
    void isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompareSummeKfzContainsZweiPlusEins() {
        assertThat(validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(
                Fahrzeugklasse.SUMME_KFZ, Fahrzeugklasse.ZWEI_PLUS_EINS), is(true));
    }

    @Test
    void isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompareSummeKfzContainsItself() {
        assertThat(validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(
                Fahrzeugklasse.SUMME_KFZ, Fahrzeugklasse.SUMME_KFZ), is(true));
    }

    @Test
    void isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompareRadEqualsRad() {
        assertThat(validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(
                Fahrzeugklasse.RAD, Fahrzeugklasse.RAD), is(true));
    }

    @Test
    void isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompareRadNotContainedInAchtPlusEins() {
        assertThat(validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(
                Fahrzeugklasse.RAD, Fahrzeugklasse.ACHT_PLUS_EINS), is(false));
    }

    @Test
    void isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompareRadNotContainedInZweiPlusEins() {
        assertThat(validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(
                Fahrzeugklasse.RAD, Fahrzeugklasse.ZWEI_PLUS_EINS), is(false));
    }

    @Test
    void isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompareRadNotContainedInSummeKfz() {
        assertThat(validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(
                Fahrzeugklasse.RAD, Fahrzeugklasse.SUMME_KFZ), is(false));
    }

    @Test
    void isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompareAchtPlusEinsNotContainedInRad() {
        assertThat(validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(
                Fahrzeugklasse.ACHT_PLUS_EINS, Fahrzeugklasse.RAD), is(false));
    }

    @Test
    void isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompareZweiPlusEinsNotContainedInRad() {
        assertThat(validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(
                Fahrzeugklasse.ZWEI_PLUS_EINS, Fahrzeugklasse.RAD), is(false));
    }

    @Test
    void isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompareSummeKfzNotContainedInRad() {
        assertThat(validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(
                Fahrzeugklasse.SUMME_KFZ, Fahrzeugklasse.RAD), is(false));
    }
}
