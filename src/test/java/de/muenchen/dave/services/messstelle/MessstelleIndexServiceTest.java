package de.muenchen.dave.services.messstelle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MessstelleIndexServiceTest {

    @Mock
    private MessstelleIndex messstelleIndex;

    private MessstelleIndexService service;

    @BeforeEach
    public void setUp() {
        Mockito.reset(messstelleIndex);
        // Standardverhalten: die gespeicherte Messstelle wird 1:1 zurückgegeben
        when(messstelleIndex.save(any(Messstelle.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service = new MessstelleIndexService(messstelleIndex);
    }

    @Test
    void speichertMessstelle_beiNullerMessquerschnitte_wirdLeereListeGespeichert() {
        // Arrange: Messstelle mit null als Messquerschnitte
        final var messstelle = new Messstelle();
        messstelle.setMessquerschnitte(null);

        // Act
        final var result = service.saveMessstelleWithDetectors(messstelle);

        // Assert: die Liste ist leer und save wurde aufgerufen
        assertThat(result.getMessquerschnitte().isEmpty(), is(true));
        verify(messstelleIndex, times(1)).save(any(Messstelle.class));
    }

    @Test
    void filtertMessquerschnitteOhneDetektoren_undSpeichertNurMitDetektoren() {
        // Arrange: Messstelle mit drei Messquerschnitten (null, 0, >0)
        final var mqNull = new Messquerschnitt();
        mqNull.setMqId("mq-null");
        mqNull.setAnzahlDetektoren(null);

        final var mqZero = new Messquerschnitt();
        mqZero.setMqId("mq-zero");
        mqZero.setAnzahlDetektoren(0);

        final var mqTwo = new Messquerschnitt();
        mqTwo.setMqId("mq-two");
        mqTwo.setAnzahlDetektoren(2);

        final List<Messquerschnitt> list = new ArrayList<>();
        list.add(mqNull);
        list.add(mqZero);
        list.add(mqTwo);

        final var messstelle = new Messstelle();
        messstelle.setMessquerschnitte(list);

        // Act
        final var result = service.saveMessstelleWithDetectors(messstelle);

        // Assert: nur der Messquerschnitt mit 2 Detektoren bleibt
        assertThat(result.getMessquerschnitte().size(), is(1));
        assertThat(result.getMessquerschnitte().getFirst().getMqId(), is(equalTo("mq-two")));
        verify(messstelleIndex, times(1)).save(any(Messstelle.class));
    }

    @Test
    void behaltetAlleMessquerschnitte_wennAlleDetektorenMehrAlsNull() {
        // Arrange: Messstelle mit zwei Messquerschnitten (1 und 3 Detektoren)
        final var mqOne = new Messquerschnitt();
        mqOne.setMqId("mq-one");
        mqOne.setAnzahlDetektoren(1);

        final var mqThree = new Messquerschnitt();
        mqThree.setMqId("mq-three");
        mqThree.setAnzahlDetektoren(3);

        final List<Messquerschnitt> list = new ArrayList<>();
        list.add(mqOne);
        list.add(mqThree);

        final var messstelle = new Messstelle();
        messstelle.setMessquerschnitte(list);

        // Act
        final var result = service.saveMessstelleWithDetectors(messstelle);

        // Assert: beide Messquerschnitte bleiben erhalten
        assertThat(result.getMessquerschnitte().size(), is(2));
        verify(messstelleIndex, times(1)).save(any(Messstelle.class));
    }

    @Test
    void entferntAlleMessquerschnitte_wennKeineDetektorenVorhandenSind() {
        // Arrange: Messstelle mit zwei Messquerschnitten (null und 0)
        final var mqNull = new Messquerschnitt();
        mqNull.setMqId("mq-null");
        mqNull.setAnzahlDetektoren(null);

        final var mqZero = new Messquerschnitt();
        mqZero.setMqId("mq-zero");
        mqZero.setAnzahlDetektoren(0);

        final List<Messquerschnitt> list = new ArrayList<>();
        list.add(mqNull);
        list.add(mqZero);

        final var messstelle = new Messstelle();
        messstelle.setMessquerschnitte(list);

        // Act
        final var result = service.saveMessstelleWithDetectors(messstelle);

        // Assert: Liste ist leer
        assertThat(result.getMessquerschnitte().isEmpty(), is(true));
        verify(messstelleIndex, times(1)).save(any(Messstelle.class));
    }
}
