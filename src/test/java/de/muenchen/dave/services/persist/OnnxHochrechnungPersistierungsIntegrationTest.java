package de.muenchen.dave.services.persist;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.verify;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.ModelInputSchema;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.properties.OnnxModelDefinition;
import de.muenchen.dave.properties.OnnxModelProperties;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import de.muenchen.dave.services.hochrechnung.HochrechnungsService;
import de.muenchen.dave.services.hochrechnung.OnnxModelRegistry;
import de.muenchen.dave.services.hochrechnung.ReineFahrzeugwerteEncoder;
import de.muenchen.dave.util.DaveConstants;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class OnnxHochrechnungPersistierungsIntegrationTest {

    /**
     * Prüft, dass das 2x4h-Modell eine KI-Tagessumme erzeugt und in die Gesamthochrechnung übernimmt.
     */
    @Test
    void test_With2x4HoursModel_PersistsOnnxPredictionInKiAndGesamt() {
        assertOnnxPredictionIsPersisted(Zaehldauer.DAUER_2_X_4_STUNDEN, "models/RAD_2x4h_test.onnx");
    }

    /**
     * Prüft, dass das 13h-Modell eine KI-Tagessumme erzeugt und in die Gesamthochrechnung übernimmt.
     */
    @Test
    void test_With13HoursModel_PersistsOnnxPredictionInKiAndGesamt() {
        assertOnnxPredictionIsPersisted(Zaehldauer.DAUER_13_STUNDEN, "models/RAD_13h_test.onnx");
    }

    /**
     * Prüft, dass das 16h-Modell eine KI-Tagessumme erzeugt und in die Gesamthochrechnung übernimmt.
     */
    @Test
    void test_With16HoursModel_PersistsOnnxPredictionInKiAndGesamt() {
        assertOnnxPredictionIsPersisted(Zaehldauer.DAUER_16_STUNDEN, "models/RAD_16h_test.onnx");
    }

    private void assertOnnxPredictionIsPersisted(final Zaehldauer zaehldauer, final String resourcePath) {
        final ZeitintervallRepository zeitintervallRepository = Mockito.mock(ZeitintervallRepository.class);
        final OnnxModelRegistry modelRegistry = createModelRegistry(zaehldauer, resourcePath);
        try {
            final ZeitintervallPersistierungsService service = new ZeitintervallPersistierungsService(
                    zeitintervallRepository,
                    new HochrechnungsService(modelRegistry));

            service.aufbereitenUndPersistieren(zaehldauer, createZeitintervalle(zaehldauer), true);

            final List<Zeitintervall> persistedZeitintervalle = getPersistedZeitintervalle(zeitintervallRepository);
            final Zeitintervall kiZeitintervall = getZeitintervall(persistedZeitintervalle, TypeZeitintervall.GESAMT_KI);
            final Zeitintervall gesamtZeitintervall = getZeitintervall(persistedZeitintervalle, TypeZeitintervall.GESAMT);
            assertThat(kiZeitintervall.getFahrradfahrer(), notNullValue());
            assertThat(gesamtZeitintervall.getHochrechnung().getHochrechnungRad(), equalTo(kiZeitintervall.getFahrradfahrer()));
        } finally {
            modelRegistry.closeModels();
        }
    }

    private OnnxModelRegistry createModelRegistry(final Zaehldauer zaehldauer, final String resourcePath) {
        final OnnxModelDefinition definition = new OnnxModelDefinition();
        definition.setId("rad-" + zaehldauer.name());
        definition.setFahrzeug(Fahrzeug.RAD);
        definition.setZaehldauer(zaehldauer);
        definition.setResourcePath(resourcePath);
        definition.setInputTensorName("int64_input");
        definition.setInputSchema(ModelInputSchema.REINE_FAHRZEUGWERTE);
        final OnnxModelProperties properties = new OnnxModelProperties();
        properties.setModelle(List.of(definition));
        return new OnnxModelRegistry(properties, List.of(new ReineFahrzeugwerteEncoder()));
    }

    private List<Zeitintervall> getPersistedZeitintervalle(final ZeitintervallRepository zeitintervallRepository) {
        final ArgumentCaptor<List<Zeitintervall>> captor = ArgumentCaptor.forClass(List.class);
        verify(zeitintervallRepository).saveAllAndFlush(captor.capture());
        return captor.getValue();
    }

    private Zeitintervall getZeitintervall(final List<Zeitintervall> zeitintervalle, final TypeZeitintervall type) {
        return zeitintervalle.stream().filter(zeitintervall -> type.equals(zeitintervall.getType())).findFirst().orElseThrow();
    }

    private List<Zeitintervall> createZeitintervalle(final Zaehldauer zaehldauer) {
        final UUID zaehlungId = UUID.randomUUID();
        final UUID bewegungsbeziehungId = UUID.randomUUID();
        final Verkehrsbeziehung verkehrsbeziehung = new Verkehrsbeziehung();
        verkehrsbeziehung.setVon(1);
        verkehrsbeziehung.setNach(2);
        final List<Zeitintervall> zeitintervalle = new ArrayList<>();
        final LocalDateTime start = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0));
        for (int index = 0; index < zaehldauer.getAnzahlZeitintervalle(); index++) {
            final LocalDateTime startUhrzeit = getStartUhrzeit(zaehldauer, start, index);
            zeitintervalle.add(Zeitintervall.builder()
                    .zaehlungId(zaehlungId)
                    .bewegungsbeziehungId(bewegungsbeziehungId)
                    .verkehrsbeziehung(verkehrsbeziehung)
                    .startUhrzeit(startUhrzeit)
                    .endeUhrzeit(startUhrzeit.plusMinutes(15))
                    .fahrradfahrer(index)
                    .hochrechnung(new Hochrechnung())
                    .build());
        }
        return zeitintervalle;
    }

    private LocalDateTime getStartUhrzeit(final Zaehldauer zaehldauer, final LocalDateTime start, final int index) {
        if (Zaehldauer.DAUER_2_X_4_STUNDEN.equals(zaehldauer) && index >= 16) {
            return start.plusHours(9).plusMinutes((index - 16) * 15L);
        }
        return start.plusMinutes(index * 15L);
    }

}
