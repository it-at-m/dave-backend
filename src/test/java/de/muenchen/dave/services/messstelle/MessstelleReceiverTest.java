package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.elasticsearch.MessquerschnittRandomFactory;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.mapper.detektor.MessstelleReceiverMapperImpl;
import de.muenchen.dave.geodateneai.gen.model.MessquerschnittDto;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class MessstelleReceiverTest {

    private final MessstelleReceiver messstelleReceiver = new MessstelleReceiver(
            null,
            null,
            null,
            new MessstelleReceiverMapperImpl());

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
}
