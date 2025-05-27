package de.muenchen.dave.services.auswertung;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.exceptions.IncorrectZeitauswahlException;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
class AuswertungSpitzenstundeServiceTest {

    private final AuswertungSpitzenstundeService auswertungSpitzenstundeService = new AuswertungSpitzenstundeService(null, null, null);

    @Test
    void getSortingIndex() {
        Integer result = auswertungSpitzenstundeService.getSortingIndex(
                Zeitblock.ZB_06_10,
                TypeZeitintervall.SPITZENSTUNDE_FUSS);
        assertThat(result, is(24000000));

        result = auswertungSpitzenstundeService.getSortingIndex(
                Zeitblock.ZB_06_10,
                TypeZeitintervall.SPITZENSTUNDE_RAD);
        assertThat(result, is(23000000));

        result = auswertungSpitzenstundeService.getSortingIndex(
                Zeitblock.ZB_06_10,
                TypeZeitintervall.SPITZENSTUNDE_KFZ);
        assertThat(result, is(22000000));

        result = auswertungSpitzenstundeService.getSortingIndex(
                Zeitblock.ZB_00_24,
                TypeZeitintervall.SPITZENSTUNDE_FUSS);
        assertThat(result, is(80000000));

        result = auswertungSpitzenstundeService.getSortingIndex(
                Zeitblock.ZB_00_24,
                TypeZeitintervall.SPITZENSTUNDE_RAD);
        assertThat(result, is(70000000));

        result = auswertungSpitzenstundeService.getSortingIndex(
                Zeitblock.ZB_00_24,
                TypeZeitintervall.SPITZENSTUNDE_KFZ);
        assertThat(result, is(60000000));

        result = auswertungSpitzenstundeService.getSortingIndex(
                Zeitblock.ZB_00_24,
                TypeZeitintervall.GESAMT);
        assertThat(result, is(-1));
    }

    @Test
    public void getRelevantTypeZeitintervallFromZeitauswahl() throws IncorrectZeitauswahlException {
        String zeitauswahl = LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_KFZ;
        TypeZeitintervall result = auswertungSpitzenstundeService.getRelevantTypeZeitintervallFromZeitauswahl(zeitauswahl);
        assertThat(result, is(TypeZeitintervall.SPITZENSTUNDE_KFZ));

        zeitauswahl = LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_RAD;
        result = auswertungSpitzenstundeService.getRelevantTypeZeitintervallFromZeitauswahl(zeitauswahl);
        assertThat(result, is(TypeZeitintervall.SPITZENSTUNDE_RAD));

        zeitauswahl = LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_FUSS;
        result = auswertungSpitzenstundeService.getRelevantTypeZeitintervallFromZeitauswahl(zeitauswahl);
        assertThat(result, is(TypeZeitintervall.SPITZENSTUNDE_FUSS));

        Assertions.assertThrows(IncorrectZeitauswahlException.class, () -> {
            auswertungSpitzenstundeService.getRelevantTypeZeitintervallFromZeitauswahl("Other");
        });
    }

}
