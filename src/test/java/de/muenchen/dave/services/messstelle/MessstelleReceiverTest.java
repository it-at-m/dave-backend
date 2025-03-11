package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.elasticsearch.MessquerschnittRandomFactory;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.enums.MessstelleStatus;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
import de.muenchen.dave.domain.mapper.detektor.MessstelleReceiverMapperImpl;
import de.muenchen.dave.domain.model.MessstelleChangeMessage;
import de.muenchen.dave.geodateneai.gen.api.MessstelleApi;
import de.muenchen.dave.geodateneai.gen.model.MessquerschnittDto;
import de.muenchen.dave.services.CustomSuggestIndexService;
import de.muenchen.dave.services.email.EmailSendService;
import de.muenchen.dave.services.lageplan.LageplanService;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MessstelleReceiverTest {

    @Mock
    private MessstelleIndexService messstelleIndexService;

    @Mock
    private CustomSuggestIndexService customSuggestIndexService;

    @Mock
    private LageplanService lageplanService;

    @Mock
    private EmailSendService emailSendService;

    @Mock
    private MessstelleApi messstelleApi;

    private MessstelleReceiver messstelleReceiver;

    @BeforeEach
    public void beforeEach() {
        Mockito.reset(messstelleIndexService, customSuggestIndexService, lageplanService, emailSendService, messstelleApi);
        messstelleReceiver = new MessstelleReceiver(
                messstelleIndexService,
                customSuggestIndexService,
                new StadtbezirkMapper(),
                lageplanService,
                emailSendService,
                messstelleApi,
                new MessstelleReceiverMapperImpl());
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
