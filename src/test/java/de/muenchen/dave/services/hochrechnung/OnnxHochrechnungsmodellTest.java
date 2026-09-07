package de.muenchen.dave.services.hochrechnung;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import de.muenchen.dave.domain.KIPredictionResult;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.Hochrechnungsziel;
import de.muenchen.dave.domain.enums.ModelleingabeSchema;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.mapper.KIZeitintervallMapper;
import de.muenchen.dave.exceptions.PredictionFailedException;
import de.muenchen.dave.properties.OnnxModellDefinition;
import de.muenchen.dave.util.DaveConstants;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OnnxHochrechnungsmodellTest {

    @Test
    void test_With2x4HoursModel_ReturnsOnePredictionPerMovementRelation() throws PredictionFailedException {
        final KIZeitintervallMapper mapper = Mockito.mock(KIZeitintervallMapper.class);
        final de.muenchen.dave.domain.KIZeitintervall kiZeitintervall = de.muenchen.dave.domain.KIZeitintervall.builder()
                .rad(6)
                .jahresZeit(3)
                .jahreSeit89(31)
                .mittwoch(1)
                .build();
        Mockito.when(mapper.zeitintervallToKIZeitintervall(Mockito.any())).thenReturn(kiZeitintervall);
        final OnnxHochrechnungsmodell modell = new OnnxHochrechnungsmodell(createDefinition(), new KontextRadV1Encoder(mapper));
        final List<List<Zeitintervall>> zeitintervalle = List.of(createZeitintervalle());

        final List<KIPredictionResult> ergebnis = modell.berechne(zeitintervalle);

        assertThat(ergebnis.size(), equalTo(1));
        assertThat(ergebnis.getFirst().getRadTagessumme(), equalTo(356));
        modell.schliessen();
    }

    private OnnxModellDefinition createDefinition() {
        final OnnxModellDefinition definition = new OnnxModellDefinition();
        definition.setId("rad-2x4h-v1");
        definition.setZiel(Hochrechnungsziel.RAD);
        definition.setZaehldauer(Zaehldauer.DAUER_2_X_4_STUNDEN);
        definition.setResourcePath("model/Rad_Modell_DAVE.onnx");
        definition.setInputTensorName("int64_input");
        definition.setInputSchema(ModelleingabeSchema.KONTEXT_RAD_V1);
        return definition;
    }

    private List<Zeitintervall> createZeitintervalle() {
        final List<Zeitintervall> zeitintervalle = new ArrayList<>();
        final LocalDateTime start = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0));
        for (int index = 0; index < 32; index++) {
            final LocalDateTime startUhrzeit = index < 16 ? start.plusMinutes(index * 15L) : start.plusHours(9).plusMinutes((index - 16) * 15L);
            zeitintervalle.add(Zeitintervall.builder()
                    .startUhrzeit(startUhrzeit)
                    .endeUhrzeit(startUhrzeit.plusMinutes(15))
                    .sortingIndex(index)
                    .fahrradfahrer(6)
                    .build());
        }
        return zeitintervalle;
    }

}
