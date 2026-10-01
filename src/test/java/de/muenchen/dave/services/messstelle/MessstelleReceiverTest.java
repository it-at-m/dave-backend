package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.MessstelleStatus;
import de.muenchen.dave.domain.mapper.FahrzeugklassenMapperImpl;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
import de.muenchen.dave.domain.mapper.VerkehrsartMapperImpl;
import de.muenchen.dave.domain.mapper.detektor.MessstelleReceiverMapper;
import de.muenchen.dave.domain.mapper.detektor.MessstelleReceiverMapperImpl;
import de.muenchen.dave.domain.model.MessstelleChangeMessage;
import de.muenchen.dave.geodateneai.gen.api.MessstelleApi;
import de.muenchen.dave.geodateneai.gen.model.MessquerschnittDto;
import de.muenchen.dave.geodateneai.gen.model.MessstelleDto;
import de.muenchen.dave.services.CustomSuggestIndexService;
import de.muenchen.dave.services.email.EmailSendService;
import de.muenchen.dave.services.lageplan.LageplanService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@Slf4j
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MessstelleReceiverTest {

    @Mock
    private MessstelleIndexService messstelleIndexService;

    @Mock
    private CustomSuggestIndexService customSuggestIndexService;

    private final StadtbezirkMapper stadtbezirkMapper = new StadtbezirkMapper();

    @Mock
    private LageplanService lageplanService;

    @Mock
    private EmailSendService emailSendService;

    @Mock
    private MessstelleApi messstelleApi;

    private MessstelleReceiverMapper messstelleReceiverMapper;

    private MessstelleReceiver messstelleReceiver;

    @BeforeEach
    public void beforeEach() throws IllegalAccessException {
        messstelleReceiverMapper = new MessstelleReceiverMapperImpl();
        FieldUtils.writeField(messstelleReceiverMapper, "fahrzeugklassenMapper", new FahrzeugklassenMapperImpl(), true);
        FieldUtils.writeField(messstelleReceiverMapper, "verkehrsartMapper", new VerkehrsartMapperImpl(), true);
        Mockito.reset(messstelleIndexService, customSuggestIndexService, lageplanService, emailSendService, messstelleApi);
        messstelleReceiver = new MessstelleReceiver(
                messstelleIndexService,
                customSuggestIndexService,
                stadtbezirkMapper,
                lageplanService,
                emailSendService,
                messstelleApi,
                messstelleReceiverMapper);
    }

    @Test
    void processingMessstellen() {
        final var messstelleDto1 = new MessstelleDto();
        messstelleDto1.setMstId("1");
        final var messstelleDto2 = new MessstelleDto();
        messstelleDto2.setMstId("2");
        final var messstelleDto3 = new MessstelleDto();
        messstelleDto3.setMstId("3");
        final var messstelleDto4 = new MessstelleDto();
        messstelleDto4.setMstId("4");
        final var messstellenToProcess = List.of(messstelleDto1, messstelleDto2, messstelleDto3, messstelleDto4);

        final var messstelleReceiverSpy = Mockito.spy(this.messstelleReceiver);

        final var messstelle1 = new Messstelle();
        messstelle1.setMstId("1");
        Mockito.when(messstelleIndexService.findByMstId("1")).thenReturn(Optional.of(messstelle1));

        Mockito.when(messstelleIndexService.findByMstId("2")).thenReturn(Optional.empty());

        final var messstelle3 = new Messstelle();
        messstelle1.setMstId("3");
        Mockito.when(messstelleIndexService.findByMstId("3")).thenReturn(Optional.of(messstelle3));

        Mockito.when(messstelleIndexService.findByMstId("4")).thenReturn(Optional.empty());

        Mockito.doNothing().when(messstelleReceiverSpy).createMessstelle(Mockito.any());

        Mockito.doNothing().when(messstelleReceiverSpy).updateMessstelle(Mockito.any(), Mockito.any());

        messstelleReceiverSpy.processingMessstellen(messstellenToProcess);

        Mockito.verify(messstelleReceiverSpy, Mockito.times(1)).updateMessstelle(messstelle1, messstelleDto1);

        Mockito.verify(messstelleReceiverSpy, Mockito.times(1)).createMessstelle(messstelleDto2);

        Mockito.verify(messstelleReceiverSpy, Mockito.times(1)).updateMessstelle(messstelle3, messstelleDto3);

        Mockito.verify(messstelleReceiverSpy, Mockito.times(1)).createMessstelle(messstelleDto4);
    }

    @Test
    void createMessstelle() {
        final var messstelleDto1 = new MessstelleDto();
        messstelleDto1.setMstId("1");

        final var messstelle1 = new Messstelle();
        messstelle1.setMstId("1");
        messstelle1.setStatus(MessstelleStatus.IN_BESTAND);

        final var messstelleReceiverMapperSpy = Mockito.spy(this.messstelleReceiverMapper);

        Mockito.when(messstelleReceiverMapperSpy.createMessstelle(messstelleDto1, stadtbezirkMapper)).thenReturn(messstelle1);

        final var messstelle1Saved = new Messstelle();
        messstelle1Saved.setId("1234");
        messstelle1Saved.setMstId("1");
        messstelle1Saved.setStatus(MessstelleStatus.IN_BESTAND);
        Mockito.when(messstelleIndexService.saveMessstelle(Mockito.any(Messstelle.class))).thenReturn(messstelle1Saved);

        final var messstelleReceiverSpy = Mockito.spy(this.messstelleReceiver);

        messstelleReceiverSpy.createMessstelle(messstelleDto1);

        Mockito.verify(messstelleReceiverMapperSpy, Mockito.times(1)).createMessstelle(messstelleDto1, stadtbezirkMapper);

        Mockito.verify(messstelleIndexService, Mockito.times(1)).saveMessstelle(Mockito.any(Messstelle.class));

        Mockito.verify(messstelleReceiverSpy, Mockito.times(1)).sendMailForUpdatedOrChangedMessstelle(
                messstelle1Saved.getId(),
                messstelle1Saved.getMstId(),
                null,
                messstelle1Saved.getStatus());
    }

    @Test
    void updateMessstelleOldStatusAndNewStatusUnequal() {
        final var messstelleDto = new MessstelleDto();
        messstelleDto.setMstId("1");
        messstelleDto.setStatus(MessstelleDto.StatusEnum.IN_BESTAND);
        messstelleDto.setMessquerschnitte(List.of());

        final var existingMessstelle = new Messstelle();
        existingMessstelle.setId("1234");
        existingMessstelle.setMstId("1");
        existingMessstelle.setStatus(MessstelleStatus.IN_PLANUNG);

        Mockito.when(lageplanService.lageplanVorhanden("1")).thenReturn(true);

        final var updatedMessstelle = new Messstelle();
        updatedMessstelle.setId("1234");
        updatedMessstelle.setMstId("1");
        updatedMessstelle.setStatus(MessstelleStatus.IN_BESTAND);
        updatedMessstelle.setMessquerschnitte(List.of());
        updatedMessstelle.setMessfaehigkeiten(List.of());
        updatedMessstelle.setSuchwoerter(List.of("1"));
        updatedMessstelle.setLageplanVorhanden(true);

        final var savedMessstelle = new Messstelle();
        savedMessstelle.setId("1234");
        savedMessstelle.setMstId("1");
        savedMessstelle.setStatus(MessstelleStatus.IN_BESTAND);
        savedMessstelle.setMessquerschnitte(List.of());
        savedMessstelle.setMessfaehigkeiten(List.of());
        savedMessstelle.setSuchwoerter(List.of("1"));
        savedMessstelle.setLageplanVorhanden(true);
        Mockito.when(messstelleIndexService.saveMessstelle(updatedMessstelle)).thenReturn(savedMessstelle);

        final var messstelleReceiverSpy = Mockito.spy(this.messstelleReceiver);

        Mockito.doNothing().when(messstelleReceiverSpy).sendMailForUpdatedOrChangedMessstelle(
                savedMessstelle.getId(),
                savedMessstelle.getMstId(),
                MessstelleStatus.IN_PLANUNG,
                MessstelleStatus.IN_BESTAND);

        messstelleReceiverSpy.updateMessstelle(existingMessstelle, messstelleDto);

        Mockito.verify(lageplanService, Mockito.times(1)).lageplanVorhanden("1");

        Mockito.verify(customSuggestIndexService, Mockito.times(1)).updateSuggestionsForMessstelle(updatedMessstelle);

        Mockito.verify(messstelleIndexService, Mockito.times(1)).saveMessstelle(updatedMessstelle);

        Mockito.verify(messstelleReceiverSpy, Mockito.times(1)).sendMailForUpdatedOrChangedMessstelle(
                savedMessstelle.getId(),
                savedMessstelle.getMstId(),
                MessstelleStatus.IN_PLANUNG,
                MessstelleStatus.IN_BESTAND);
    }

    @Test
    void updateMessstelleOldStatusAndNewStatusEqual() {
        final var messstelleDto = new MessstelleDto();
        messstelleDto.setMstId("1");
        messstelleDto.setStatus(MessstelleDto.StatusEnum.IN_BESTAND);
        messstelleDto.setMessquerschnitte(List.of());

        final var existingMessstelle = new Messstelle();
        existingMessstelle.setId("1234");
        existingMessstelle.setMstId("1");
        existingMessstelle.setStatus(MessstelleStatus.IN_BESTAND);

        Mockito.when(lageplanService.lageplanVorhanden("1")).thenReturn(true);

        final var updatedMessstelle = new Messstelle();
        updatedMessstelle.setId("1234");
        updatedMessstelle.setMstId("1");
        updatedMessstelle.setStatus(MessstelleStatus.IN_BESTAND);
        updatedMessstelle.setMessquerschnitte(List.of());
        updatedMessstelle.setMessfaehigkeiten(List.of());
        updatedMessstelle.setSuchwoerter(List.of("1"));
        updatedMessstelle.setLageplanVorhanden(true);

        final var savedMessstelle = new Messstelle();
        savedMessstelle.setId("1234");
        savedMessstelle.setMstId("1");
        savedMessstelle.setStatus(MessstelleStatus.IN_BESTAND);
        savedMessstelle.setMessquerschnitte(List.of());
        savedMessstelle.setMessfaehigkeiten(List.of());
        savedMessstelle.setSuchwoerter(List.of("1"));
        savedMessstelle.setLageplanVorhanden(true);
        Mockito.when(messstelleIndexService.saveMessstelle(updatedMessstelle)).thenReturn(savedMessstelle);

        final var messstelleReceiverSpy = Mockito.spy(this.messstelleReceiver);

        Mockito.doNothing().when(messstelleReceiverSpy).sendMailForUpdatedOrChangedMessstelle(
                savedMessstelle.getId(),
                savedMessstelle.getMstId(),
                MessstelleStatus.IN_PLANUNG,
                MessstelleStatus.IN_BESTAND);

        messstelleReceiverSpy.updateMessstelle(existingMessstelle, messstelleDto);

        Mockito.verify(lageplanService, Mockito.times(1)).lageplanVorhanden("1");

        Mockito.verify(customSuggestIndexService, Mockito.times(1)).updateSuggestionsForMessstelle(updatedMessstelle);

        Mockito.verify(messstelleIndexService, Mockito.times(1)).saveMessstelle(updatedMessstelle);

        Mockito.verify(messstelleReceiverSpy, Mockito.times(0)).sendMailForUpdatedOrChangedMessstelle(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any());
    }

    @Test
    void sendMailForUpdatedOrChangedMessstelle() {
        messstelleReceiver.sendMailForUpdatedOrChangedMessstelle(
                "id",
                "mstId",
                MessstelleStatus.IN_BESTAND,
                MessstelleStatus.ABGEBAUT);

        final var messstelleChangeMessage = new MessstelleChangeMessage();
        messstelleChangeMessage.setTechnicalIdMst("id");
        messstelleChangeMessage.setMstId("mstId");
        messstelleChangeMessage.setStatusAlt(MessstelleStatus.IN_BESTAND);
        messstelleChangeMessage.setStatusNeu(MessstelleStatus.ABGEBAUT);

        Mockito.verify(emailSendService, Mockito.times(1)).sendMailForMessstelleChangeMessage(messstelleChangeMessage);
    }

    // Beide Parameter null -> Ergebnisliste leer
    @Test
    void updateMessquerschnitte_bothNull_returnsEmptyList() {
        final var result = messstelleReceiver.updateMessquerschnitteOfMessstelle(null, null);
        Assertions.assertThat(result).isNotNull().isEmpty();
    }

    // DTO-Liste null -> vorhandene Messquerschnitte bleiben unverändert
    @Test
    void updateMessquerschnitte_dtoNull_returnsExistingList() {
        final var existing = new ArrayList<Messquerschnitt>();
        final var mq = new Messquerschnitt();
        mq.setMqId("mq1");
        existing.add(mq);

        final var result = messstelleReceiver.updateMessquerschnitteOfMessstelle(existing, null);

        Assertions.assertThat(result).hasSize(1).containsExactly(mq);
    }

    // Wenn DTO einen Messquerschnitt ohne Detektoren meldet, wird der vorhandene entfernt (case-insensitive)
    @Test
    void updateMessquerschnitte_dtoWithoutDetectors_removesExisting() {
        final var existing = new ArrayList<Messquerschnitt>();
        final var mq = new Messquerschnitt();
        mq.setMqId("MQ1");
        existing.add(mq);

        final var dto = new MessquerschnittDto();
        dto.setMqId("mq1");
        dto.setAnzahlDetektoren(0);

        final var result = messstelleReceiver.updateMessquerschnitteOfMessstelle(existing, List.of(dto));

        Assertions.assertThat(result).isEmpty();
    }

    // Wenn DTO einen Messquerschnitt mit Detektoren meldet, wird der vorhandene aktualisiert (updateMessquerschnitt aufgerufen)
    @Test
    void updateMessquerschnitte_dtoWithDetectors_updatesExisting() throws IllegalAccessException {
        final var existing = new ArrayList<Messquerschnitt>();
        final var mq = new Messquerschnitt();
        mq.setMqId("MQID");
        existing.add(mq);

        final var dto = new MessquerschnittDto();
        dto.setMqId("mqid"); // case-insensitive match
        dto.setAnzahlDetektoren(2);

        // Spy des Mappers injizieren, um die Interaktion zu prüfen
        final var mapperSpy = Mockito.spy(this.messstelleReceiverMapper);
        FieldUtils.writeField(messstelleReceiver, "messstelleReceiverMapper", mapperSpy, true);

        final var result = messstelleReceiver.updateMessquerschnitteOfMessstelle(existing, List.of(dto));

        // Der vorhandene Messquerschnitt bleibt erhalten und wird aktualisiert
        Assertions.assertThat(result).hasSize(1).containsExactly(mq);
        Mockito.verify(mapperSpy, Mockito.times(1)).updateMessquerschnitt(mq, dto, stadtbezirkMapper);
    }

    // Wenn DTO einen neuen Messquerschnitt mit Detektoren meldet, wird ein neuer Eintrag erstellt (createMessquerschnitt aufgerufen)
    @Test
    void updateMessquerschnitte_dtoWithDetectors_createsNew() throws IllegalAccessException {
        final var existing = new ArrayList<Messquerschnitt>();

        final var dto = new MessquerschnittDto();
        dto.setMqId("NEWMQ");
        dto.setAnzahlDetektoren(1);

        // Spy des Mappers injizieren und create stubben
        final var mapperSpy = Mockito.spy(this.messstelleReceiverMapper);
        final var created = new Messquerschnitt();
        created.setMqId("NEWMQ_CREATED");
        Mockito.doReturn(created).when(mapperSpy).createMessquerschnitt(dto);
        FieldUtils.writeField(messstelleReceiver, "messstelleReceiverMapper", mapperSpy, true);

        final var result = messstelleReceiver.updateMessquerschnitteOfMessstelle(existing, List.of(dto));

        Assertions.assertThat(result).hasSize(1).containsExactly(created);
        Mockito.verify(mapperSpy, Mockito.times(1)).createMessquerschnitt(dto);
    }

    // Case-Insensitive: vorhandener MQ 'AbC' wird entfernt, wenn DTO 'abc' ohne Detektoren meldet
    @Test
    void updateMessquerschnitte_caseInsensitive_removal() {
        final var existing = new ArrayList<Messquerschnitt>();
        final var mq = new Messquerschnitt();
        mq.setMqId("AbC");
        existing.add(mq);

        final var dto = new MessquerschnittDto();
        dto.setMqId("abc");
        dto.setAnzahlDetektoren(0);

        final var result = messstelleReceiver.updateMessquerschnitteOfMessstelle(existing, List.of(dto));

        Assertions.assertThat(result).isEmpty();
    }

    // Gemischter DTO-Fall: Entfernen, Aktualisieren und Erstellen in einem Aufruf
    @Test
    void updateMessquerschnitte_mixed_removeUpdateCreate() throws IllegalAccessException {
        final var existing = new ArrayList<Messquerschnitt>();
        final var mqA = new Messquerschnitt();
        mqA.setMqId("A");
        final var mqB = new Messquerschnitt();
        mqB.setMqId("B");
        final var mqC = new Messquerschnitt();
        mqC.setMqId("C");
        existing.add(mqA);
        existing.add(mqB);
        existing.add(mqC);

        final var dtoRemove = new MessquerschnittDto();
        dtoRemove.setMqId("a");
        dtoRemove.setAnzahlDetektoren(0);
        final var dtoUpdate = new MessquerschnittDto();
        dtoUpdate.setMqId("b");
        dtoUpdate.setAnzahlDetektoren(2);
        final var dtoCreate = new MessquerschnittDto();
        dtoCreate.setMqId("D");
        dtoCreate.setAnzahlDetektoren(1);

        final var mapperSpy = Mockito.spy(this.messstelleReceiverMapper);
        final var created = new Messquerschnitt();
        created.setMqId("D");
        Mockito.doReturn(created).when(mapperSpy).createMessquerschnitt(Mockito.eq(dtoCreate));
        FieldUtils.writeField(messstelleReceiver, "messstelleReceiverMapper", mapperSpy, true);

        final var result = messstelleReceiver.updateMessquerschnitteOfMessstelle(existing, List.of(dtoRemove, dtoUpdate, dtoCreate));

        // A wurde entfernt, B aktualisiert, C bleibt erhalten, D neu erstellt
        Assertions.assertThat(result).hasSize(3).containsExactlyInAnyOrder(mqB, mqC, created);
        Mockito.verify(mapperSpy, Mockito.times(1)).updateMessquerschnitt(mqB, dtoUpdate, stadtbezirkMapper);
        Mockito.verify(mapperSpy, Mockito.times(1)).createMessquerschnitt(dtoCreate);
    }

    // DTO mit null AnzahlDetektoren -> wird wie 0 behandelt (Entfernen)
    @Test
    void updateMessquerschnitte_dtoWithNullAnzahlDetektoren_treatedAsZero() {
        final var existing = new ArrayList<Messquerschnitt>();
        final var mq = new Messquerschnitt();
        mq.setMqId("mq1");
        existing.add(mq);

        final var dto = new MessquerschnittDto();
        dto.setMqId("mq1"); // AnzahlDetektoren bleibt null

        final var result = messstelleReceiver.updateMessquerschnitteOfMessstelle(existing, List.of(dto));

        // null wird mittels ObjectUtils.getIfNull(...,0) zu 0 -> Entfernen
        Assertions.assertThat(result).isEmpty();
    }

    // Vorhandener Messquerschnitt hat null mqId -> aktuell wird eine NullPointerException erwartet
    @Test
    void updateMessquerschnitte_existingWithNullMqId_throwsNPE() {
        final var existing = new ArrayList<Messquerschnitt>();
        final var mq = new Messquerschnitt();
        mq.setMqId(null);
        existing.add(mq);

        // Keine DTOs (oder leere Liste) -> bei Filter wird auf mqId::equalsIgnoreCase zugegriffen -> NPE
        Assertions.assertThatThrownBy(() -> messstelleReceiver.updateMessquerschnitteOfMessstelle(existing, List.of()))
                .isInstanceOf(NullPointerException.class);
    }

    // Doppelte DTO-mqId: erstes DTO erzeugt, zweites DTO führt zum Update des erzeugten Eintrags
    @Test
    void updateMessquerschnitte_duplicateDtoIds_createThenUpdate() throws IllegalAccessException {
        final var existing = new ArrayList<Messquerschnitt>();

        final var dto1 = new MessquerschnittDto();
        dto1.setMqId("dup");
        dto1.setAnzahlDetektoren(1);
        final var dto2 = new MessquerschnittDto();
        dto2.setMqId("dup");
        dto2.setAnzahlDetektoren(1);

        final var mapperSpy = Mockito.spy(this.messstelleReceiverMapper);
        final var created = new Messquerschnitt();
        created.setMqId("dup");
        Mockito.doReturn(created).when(mapperSpy).createMessquerschnitt(Mockito.any());
        FieldUtils.writeField(messstelleReceiver, "messstelleReceiverMapper", mapperSpy, true);

        final var result = messstelleReceiver.updateMessquerschnitteOfMessstelle(existing, List.of(dto1, dto2));

        // Es sollte nur ein Eintrag existieren
        Assertions.assertThat(result).hasSize(1).containsExactly(created);
        // create einmal, update einmal (für das zweite DTO)
        Mockito.verify(mapperSpy, Mockito.times(1)).createMessquerschnitt(Mockito.any());
        Mockito.verify(mapperSpy, Mockito.times(1)).updateMessquerschnitt(Mockito.eq(created), Mockito.eq(dto2), Mockito.eq(stadtbezirkMapper));
    }
}
