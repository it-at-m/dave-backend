package de.muenchen.dave.domain.elasticsearch;

import com.github.javafaker.Faker;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

public class MessquerschnittRandomFactory {

    private static final Faker fakerInstance = Faker.instance();

    public static Messquerschnitt getOne() {
        final Messquerschnitt messquerschnitt = new Messquerschnitt();
        messquerschnitt.setId(UUID.randomUUID().toString());
        messquerschnitt.setNummer(fakerInstance.number().digits(10));
        messquerschnitt.setDatum(
                LocalDate.of(fakerInstance.number().numberBetween(2000, 2020), fakerInstance.number().numberBetween(1, 12), fakerInstance.number()
                        .numberBetween(1, 28)));
        messquerschnitt.setPunkt(new GeoPoint(fakerInstance.number().randomDouble(5, 0, 5), fakerInstance.number().randomDouble(5, 0, 5)));
        messquerschnitt.setStrassenname(fakerInstance.pokemon().location());
        messquerschnitt.setLage(fakerInstance.pokemon().location());
        messquerschnitt.setFahrrichtung(fakerInstance.pokemon().name());
        messquerschnitt.setAnzahlFahrspuren(fakerInstance.number().numberBetween(1, 4));
        messquerschnitt.setFahrzeugKlassen(fakerInstance.pokemon().name());
        messquerschnitt.setDetektierteVerkehrsarten("KFZ");
        messquerschnitt.setHersteller(fakerInstance.pokemon().name());
        messquerschnitt.setAnzahlDetektoren(fakerInstance.number().numberBetween(1, 3));
        return messquerschnitt;
    }

    public static List<Messquerschnitt> getSome() {
        return List.of(getOne(), getOne());
    }
}
