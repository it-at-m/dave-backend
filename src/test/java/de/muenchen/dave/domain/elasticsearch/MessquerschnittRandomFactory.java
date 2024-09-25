package de.muenchen.dave.domain.elasticsearch;

import com.github.javafaker.Faker;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.geodateneai.gen.model.MessquerschnittDto;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

public class MessquerschnittRandomFactory {

    private static final Faker fakerInstance = Faker.instance();

    public static Messquerschnitt getMessquerschnitt() {
        final Messquerschnitt messquerschnitt = new Messquerschnitt();
        messquerschnitt.setId(UUID.randomUUID().toString());
        messquerschnitt.setMqId(fakerInstance.number().digits(10));
        messquerschnitt.setPunkt(new GeoPoint(fakerInstance.number().randomDouble(5, 0, 5), fakerInstance.number().randomDouble(5, 0, 5)));
        messquerschnitt.setStrassenname(fakerInstance.pokemon().location());
        messquerschnitt.setLageMessquerschnitt(fakerInstance.pokemon().location());
        messquerschnitt.setFahrtrichtung(fakerInstance.pokemon().name());
        messquerschnitt.setAnzahlFahrspuren(fakerInstance.number().numberBetween(1, 4));
        messquerschnitt.setAnzahlDetektoren(fakerInstance.number().numberBetween(1, 3));
        messquerschnitt.setStandort(fakerInstance.pokemon().location());
        return messquerschnitt;
    }

    public static List<Messquerschnitt> getSomeMessquerschnitte() {
        final List<Messquerschnitt> messquerschnitt = new ArrayList<>();
        messquerschnitt.add(getMessquerschnitt());
        messquerschnitt.add(getMessquerschnitt());
        return messquerschnitt;
    }

    public static MessquerschnittDto getMessquerschnittDto() {
        final MessquerschnittDto dto = new MessquerschnittDto();
        dto.setMqId(UUID.randomUUID().toString());
        dto.setMstId(UUID.randomUUID().toString());
        dto.setStrassenname(fakerInstance.pokemon().location());
        dto.setLageMessquerschnitt(fakerInstance.pokemon().location());
        dto.setFahrtrichtung(fakerInstance.pokemon().location());
        dto.setAnzahlFahrspuren(fakerInstance.number().numberBetween(1, 4));
        dto.setAnzahlDetektoren(fakerInstance.number().numberBetween(1, 3));
        dto.setLongitude(fakerInstance.number().randomDouble(5, 0, 5));
        dto.setLatitude(fakerInstance.number().randomDouble(5, 0, 5));
        return dto;
    }

    public static List<MessquerschnittDto> getSomeMessquerschnittDtos() {
        return List.of(getMessquerschnittDto(), getMessquerschnittDto());
    }
}
