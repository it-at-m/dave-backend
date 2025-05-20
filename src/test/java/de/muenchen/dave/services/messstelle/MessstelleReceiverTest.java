package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.elasticsearch.MessquerschnittRandomFactory;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.MessstelleStatus;
import de.muenchen.dave.domain.mapper.FahrzeugklassenMapperImpl;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
import de.muenchen.dave.domain.mapper.detektor.MessstelleReceiverMapper;
import de.muenchen.dave.domain.mapper.detektor.MessstelleReceiverMapperImpl;
import de.muenchen.dave.domain.model.MessstelleChangeMessage;
import de.muenchen.dave.geodateneai.gen.api.MessstelleApi;
import de.muenchen.dave.geodateneai.gen.model.MessquerschnittDto;
import de.muenchen.dave.geodateneai.gen.model.MessstelleDto;
import de.muenchen.dave.services.CustomSuggestIndexService;
import de.muenchen.dave.services.email.EmailSendService;
import de.muenchen.dave.services.lageplan.LageplanService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    @Mock
    private UnauffaelligeTageService unauffaelligeTageService;

    private MessstelleReceiverMapper messstelleReceiverMapper;

    private MessstelleReceiver messstelleReceiver;

    @BeforeEach
    public void beforeEach() throws IllegalAccessException {
        messstelleReceiverMapper = new MessstelleReceiverMapperImpl();
        FieldUtils.writeField(messstelleReceiverMapper, "fahrzeugklassenMapper", new FahrzeugklassenMapperImpl(), true);
        Mockito.reset(messstelleIndexService, customSuggestIndexService, lageplanService, emailSendService, messstelleApi);
        messstelleReceiver = new MessstelleReceiver(
                messstelleIndexService,
                customSuggestIndexService,
                stadtbezirkMapper,
                lageplanService,
                emailSendService,
                messstelleApi,
                messstelleReceiverMapper,
                unauffaelligeTageService);
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
    void updateMessquerschnitteOfMessstelleWithoutCreate() {
        final List<Messquerschnitt> messquerschnitte = MessquerschnittRandomFactory.getSomeMessquerschnitte();

        final List<MessquerschnittDto> messquerschnitteDto = new ArrayList<>();
        messquerschnitte.forEach(messquerschnitt -> {
            final MessquerschnittDto messquerschnittDto = MessquerschnittRandomFactory.getMessquerschnittDto();
            messquerschnittDto.setMqId(messquerschnitt.getMqId());
            messquerschnitteDto.add(messquerschnittDto);
        });

        final List<Messquerschnitt> expected = new MessstelleReceiverMapperImpl().createMessquerschnitte(messquerschnitteDto);
        expected.forEach(expectedMessquerschnitt -> messquerschnitte.forEach(messquerschnitt1 -> {
            if (expectedMessquerschnitt.getMqId().equalsIgnoreCase(messquerschnitt1.getMqId())) {
                expectedMessquerschnitt.setStandort(messquerschnitt1.getStandort());
                expectedMessquerschnitt.setPunkt(messquerschnitt1.getPunkt());
            }
        }));

        final List<Messquerschnitt> result = messstelleReceiver.updateMessquerschnitteOfMessstelle(messquerschnitte, messquerschnitteDto);
        Assertions.assertThat(result)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFieldsMatchingRegexes(".*")
                .ignoringFields("id")
                .isEqualTo(expected);
        result.forEach(messquerschnitt -> Assertions.assertThat(messquerschnitt.getId())
                .isNotNull());
    }

    @Test
    void updateMessquerschnitteOfMessstelleWithCreate() {
        final List<Messquerschnitt> messquerschnitte = MessquerschnittRandomFactory.getSomeMessquerschnitte();

        final List<MessquerschnittDto> messquerschnitteDto = new ArrayList<>();
        messquerschnitte.forEach(messquerschnitt -> {
            final MessquerschnittDto messquerschnittDto = MessquerschnittRandomFactory.getMessquerschnittDto();
            messquerschnittDto.setMqId(messquerschnitt.getMqId());
            messquerschnitteDto.add(messquerschnittDto);
        });
        final MessquerschnittDto addNewMessquerschnitt = MessquerschnittRandomFactory.getMessquerschnittDto();
        messquerschnitteDto.add(addNewMessquerschnitt);

        final List<Messquerschnitt> expected = new MessstelleReceiverMapperImpl().createMessquerschnitte(messquerschnitteDto);
        expected.forEach(expectedMessquerschnitt -> messquerschnitte.forEach(messquerschnitt1 -> {
            if (expectedMessquerschnitt.getMqId().equalsIgnoreCase(messquerschnitt1.getMqId())) {
                expectedMessquerschnitt.setStandort(messquerschnitt1.getStandort());
                expectedMessquerschnitt.setPunkt(messquerschnitt1.getPunkt());
            }
        }));

        final List<Messquerschnitt> result = messstelleReceiver.updateMessquerschnitteOfMessstelle(messquerschnitte, messquerschnitteDto);
        Assertions.assertThat(result)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFieldsMatchingRegexes(".*")
                .ignoringFields("id")
                .isEqualTo(expected);
        result.forEach(messquerschnitt -> Assertions.assertThat(messquerschnitt.getId())
                .isNotNull());
    }

    @Test
    void updateMessquerschnitteOfMessstelleOnlyCreate() {
        final List<Messquerschnitt> messquerschnitte = new ArrayList<>();

        final List<MessquerschnittDto> messquerschnitteDto = MessquerschnittRandomFactory.getSomeMessquerschnittDtos();

        final List<Messquerschnitt> expected = new MessstelleReceiverMapperImpl().createMessquerschnitte(messquerschnitteDto);

        final List<Messquerschnitt> result = messstelleReceiver.updateMessquerschnitteOfMessstelle(messquerschnitte, messquerschnitteDto);
        Assertions.assertThat(result)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFieldsMatchingRegexes(".*")
                .ignoringFields("id")
                .isEqualTo(expected);
        result.forEach(messquerschnitt -> Assertions.assertThat(messquerschnitt.getId())
                .isNotNull());
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
}
