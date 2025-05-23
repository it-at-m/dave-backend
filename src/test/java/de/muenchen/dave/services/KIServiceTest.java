package de.muenchen.dave.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import de.muenchen.dave.domain.KIPredictionResult;
import de.muenchen.dave.domain.KIZeitintervall;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.mapper.KIZeitintervallMapper;
import de.muenchen.dave.exceptions.PredictionFailedException;
import de.muenchen.dave.util.dataimport.ZeitintervallSortingIndexUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class KIServiceTest {

    private static KIService kiService;

    private static KIZeitintervallMapper mapper;

    @BeforeAll
    public static void initKIService() throws PredictionFailedException {
        mapper = Mockito.mock(KIZeitintervallMapper.class);
        kiService = new KIService("model/Rad_Modell_DAVE.onnx", mapper);
    }

    @Test
    void testPredictHochrechnungTageswerteForZeitIntervalleOfZaehlungNoFahrbeziehungen() {
        // Arrange
        KIZeitintervall kiIntervall = KIZeitintervall.builder()
                .rad(6)
                .jahresZeit(3)
                .jahreSeit89(31)
                .montag(0)
                .dienstag(0)
                .mittwoch(1)
                .donnerstag(0)
                .freitag(0)
                .samstag(0)
                .sonntag(0)
                .build();
        Mockito.when(mapper.zeitintervallToKIZeitintervall(any())).thenReturn(kiIntervall);

        // Act & Assert
        List<List<Zeitintervall>> input = new ArrayList<>();
        Exception exception = assertThrows(PredictionFailedException.class, () -> kiService.predictHochrechnungTageswerteForZeitIntervalleOfZaehlung(input));

        String expectedMessage = PredictionFailedException.NO_FAHRBEZIEHUNGEN;
        String actualMessage = exception.getMessage();

        assertThat(actualMessage, equalTo(expectedMessage));
    }

    @Test
    void testPredictHochrechnungTageswerteForZeitIntervalleOfZaehlungWrongInputDimension() {
        // Arrange
        KIZeitintervall kiIntervall = KIZeitintervall.builder()
                .rad(6)
                .jahresZeit(3)
                .jahreSeit89(31)
                .montag(0)
                .dienstag(0)
                .mittwoch(1)
                .donnerstag(0)
                .freitag(0)
                .samstag(0)
                .sonntag(0)
                .build();
        Mockito.when(mapper.zeitintervallToKIZeitintervall(any())).thenReturn(kiIntervall);

        // Act & Assert
        List<List<Zeitintervall>> input = Collections.nCopies(1, Collections.nCopies(10,
                Zeitintervall.builder().sortingIndex(ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_06_10).build()));
        Exception exception = assertThrows(PredictionFailedException.class, () -> kiService.predictHochrechnungTageswerteForZeitIntervalleOfZaehlung(input));

        String expectedMessage = PredictionFailedException.ONNX_INVALID_INPUT_DIMENSION;
        String actualMessage = exception.getMessage();

        assertThat(actualMessage, equalTo(expectedMessage));
    }

    @Test
    void testPredictHochrechnungTageswerteForZeitIntervalleOfZaehlungExcessInputs() throws PredictionFailedException {
        // Arrange
        KIZeitintervall kiIntervall = KIZeitintervall.builder()
                .rad(6)
                .jahresZeit(3)
                .jahreSeit89(31)
                .montag(0)
                .dienstag(0)
                .mittwoch(1)
                .donnerstag(0)
                .freitag(0)
                .samstag(0)
                .sonntag(0)
                .build();
        Mockito.when(mapper.zeitintervallToKIZeitintervall(any())).thenReturn(kiIntervall);

        // Act
        List<Zeitintervall> fahrbeziehung = new ArrayList<>();
        fahrbeziehung.addAll(Collections.nCopies(32,
                Zeitintervall.builder().sortingIndex(ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_06_10).build()));
        fahrbeziehung.addAll(Collections.nCopies(10,
                Zeitintervall.builder().sortingIndex(ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_00_06).build()));
        List<List<Zeitintervall>> input = Collections.nCopies(5, fahrbeziehung);
        KIPredictionResult[] result = kiService.predictHochrechnungTageswerteForZeitIntervalleOfZaehlung(input);

        // Assert
        assertThat(result.length, equalTo(5));
        KIPredictionResult firstResult = result[0];
        assertThat(firstResult.getRadTagessumme(), not(0));
    }

    @Test
    void testPredictHochrechnungTageswerteForZeitIntervalle() throws PredictionFailedException {
        // Arrange
        KIZeitintervall kiIntervall = KIZeitintervall.builder()
                .rad(6)
                .jahresZeit(3)
                .jahreSeit89(31)
                .montag(0)
                .dienstag(0)
                .mittwoch(1)
                .donnerstag(0)
                .freitag(0)
                .samstag(0)
                .sonntag(0)
                .build();
        Mockito.when(mapper.zeitintervallToKIZeitintervall(any())).thenReturn(kiIntervall);

        // Act
        List<List<Zeitintervall>> input = Collections.nCopies(5, Collections.nCopies(32,
                Zeitintervall.builder().sortingIndex(ZeitintervallSortingIndexUtil.SORTING_INDEX_ZB_06_10).build()));
        KIPredictionResult[] result = kiService.predictHochrechnungTageswerteForZeitIntervalleOfZaehlung(input);

        // Assert
        assertThat(result.length, equalTo(5));
        KIPredictionResult firstResult = result[0];
        assertThat(firstResult.getRadTagessumme(), not(0));
    }

}
