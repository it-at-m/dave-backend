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

    // -------------------- Tests für updateMessquerschnitteOfMessstelle --------------------

    @Test
    void updateMessquerschnitteOfMessstelle_nullInputs_gibtLeereListeZurueck() throws IllegalAccessException {
        // Wenn sowohl vorhandene Messquerschnitte als auch DTOs null sind,
        // dann soll eine leere Liste zurückgegeben werden.
        final var result = messstelleReceiver.updateMessquerschnitteOfMessstelle(null, null);

        Assertions.assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void updateMessquerschnitteOfMessstelle_entferntExactMatchUndFuegtNeueHinzu() throws IllegalAccessException {
        // Vorhandene Messquerschnitte: mq1, mq2
        final var existing1 = new Messquerschnitt();
        existing1.setMqId("mq1");
        final var existing2 = new Messquerschnitt();
        existing2.setMqId("mq2");
        final var existing = new ArrayList<Messquerschnitt>(List.of(existing1, existing2));

        // DTOs: mq1 mit 0 Detektoren (soll entfernt werden), mq3 mit >0 Detektoren (soll neu angelegt werden)
        final var dtoRemove = new MessquerschnittDto();
        dtoRemove.setMqId("mq1");
        dtoRemove.setAnzahlDetektoren(0);

        final var dtoNew = new MessquerschnittDto();
        dtoNew.setMqId("mq3");
        dtoNew.setAnzahlDetektoren(2);

        final var dtos = List.of(dtoRemove, dtoNew);

        // Spy des Mappers, damit createMessquerschnitt kontrolliert zurückgegeben werden kann
        final var spyMapper = Mockito.spy(this.messstelleReceiverMapper);
        final var createdMq3 = new Messquerschnitt();
        createdMq3.setMqId("mq3");
        Mockito.doReturn(createdMq3).when(spyMapper).createMessquerschnitt(dtoNew);

        // Mapper in den Receiver injizieren
        FieldUtils.writeField(messstelleReceiver, "messstelleReceiverMapper", spyMapper, true);

        final var result = messstelleReceiver.updateMessquerschnitteOfMessstelle(existing, dtos);

        // Erwartet: mq1 entfernt, mq2 bleibt, mq3 neu hinzugefügt
        Assertions.assertThat(result).hasSize(2);
        Assertions.assertThat(result).extracting(Messquerschnitt::getMqId).containsExactlyInAnyOrder("mq2", "mq3");

        // createMessquerschnitt sollte genau einmal für dtoNew aufgerufen worden sein
        Mockito.verify(spyMapper, Mockito.times(1)).createMessquerschnitt(dtoNew);
        // updateMessquerschnitt sollte nicht aufgerufen worden sein
        Mockito.verify(spyMapper, Mockito.times(0)).updateMessquerschnitt(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void updateMessquerschnitteOfMessstelle_updateIgnoreCase_fallsVorhanden() throws IllegalAccessException {
        // Vorhandenes Messquerschnitt mit MQID "MQ1" (Großbuchstaben)
        final var existing1 = new Messquerschnitt();
        existing1.setMqId("MQ1");
        final var existing = new ArrayList<Messquerschnitt>(List.of(existing1));

        // DTO mit gleicher mqId in gleicher Schreibweise "MQ1" und >0 Detektoren -> sollte updaten (equalsIgnoreCase)
        final var dto = new MessquerschnittDto();
        dto.setMqId("MQ1");
        dto.setAnzahlDetektoren(1);

        final var spyMapper = Mockito.spy(this.messstelleReceiverMapper);
        FieldUtils.writeField(messstelleReceiver, "messstelleReceiverMapper", spyMapper, true);

        final var result = messstelleReceiver.updateMessquerschnitteOfMessstelle(existing, List.of(dto));

        // Es sollte weiterhin genau ein Eintrag vorhanden sein
        Assertions.assertThat(result).hasSize(1);
        // MQID bleibt erhalten (das Objekt wurde ggf. aktualisiert)
        Assertions.assertThat(result.get(0).getMqId()).isEqualTo("MQ1");

        // updateMessquerschnitt sollte exakt einmal aufgerufen worden sein
        Mockito.verify(spyMapper, Mockito.times(1)).updateMessquerschnitt(existing1, dto, stadtbezirkMapper);
        // createMessquerschnitt sollte nicht aufgerufen worden sein
        Mockito.verify(spyMapper, Mockito.times(0)).createMessquerschnitt(Mockito.any());
    }

    @Test
    void updateMessquerschnitteOfMessstelle_combinedCase_sensitiveRemoval_and_caseInsensitiveUpdate() throws IllegalAccessException {
        // Vorhandene: A, B, C
        final var a = new Messquerschnitt();
        a.setMqId("A");
        final var b = new Messquerschnitt();
        b.setMqId("B");
        final var c = new Messquerschnitt();
        c.setMqId("C");
        final var existing = new ArrayList<Messquerschnitt>(List.of(a, b, c));

        // DTOs:
        // - "a" mit 0 Detektoren -> wegen case-sensitive Entfernen wird "A" NICHT entfernt
        // - "B" mit 0 Detektoren -> exakt match, B soll entfernt werden
        // - "c" mit >0 Detektoren -> update (equalsIgnoreCase)
        // - "D" mit >0 Detektoren -> create
        final var dtoAZero = new MessquerschnittDto();
        dtoAZero.setMqId("a");
        dtoAZero.setAnzahlDetektoren(0);
        final var dtoBZero = new MessquerschnittDto();
        dtoBZero.setMqId("B");
        dtoBZero.setAnzahlDetektoren(0);
        final var dtoCUpdate = new MessquerschnittDto();
        dtoCUpdate.setMqId("C");
        dtoCUpdate.setAnzahlDetektoren(2);
        final var dtoDCreate = new MessquerschnittDto();
        dtoDCreate.setMqId("D");
        dtoDCreate.setAnzahlDetektoren(1);

        final var dtos = List.of(dtoAZero, dtoBZero, dtoCUpdate, dtoDCreate);

        final var spyMapper = Mockito.spy(this.messstelleReceiverMapper);
        // create should return a Messquerschnitt with mqId D
        final var createdD = new Messquerschnitt();
        createdD.setMqId("D");
        Mockito.doReturn(createdD).when(spyMapper).createMessquerschnitt(dtoDCreate);
        FieldUtils.writeField(messstelleReceiver, "messstelleReceiverMapper", spyMapper, true);

        final var result = messstelleReceiver.updateMessquerschnitteOfMessstelle(existing, dtos);

        // Erwartet: A (nicht entfernt, da case-sensitive Removal), C (updated), D (neu)
        Assertions.assertThat(result).hasSize(3);
        Assertions.assertThat(result).extracting(Messquerschnitt::getMqId).containsExactlyInAnyOrder("A", "C", "D");

        // Verifizieren, dass B entfernt wurde
        Assertions.assertThat(result).doesNotContain(b);

        // update für C (equalsIgnoreCase) wurde aufgerufen
        Mockito.verify(spyMapper, Mockito.times(1)).updateMessquerschnitt(c, dtoCUpdate, stadtbezirkMapper);
        // create für D aufgerufen
        Mockito.verify(spyMapper, Mockito.times(1)).createMessquerschnitt(dtoDCreate);
        // create für andere Dtos nicht aufgerufen
        Mockito.verify(spyMapper, Mockito.times(0)).createMessquerschnitt(dtoCUpdate);
    }
}
