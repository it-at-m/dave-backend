package de.muenchen.dave.domain.elasticsearch;

import com.github.javafaker.Faker;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

public class MessstelleRandomFactory {
    private static final Faker fakerInstance = Faker.instance();

    public static Messstelle getOne() {
        Messstelle messstelle = new Messstelle();
        messstelle.setId(UUID.randomUUID().toString());
        messstelle.setNummer(fakerInstance.number().digits(10));
        messstelle.setName(fakerInstance.pokemon().name());
        messstelle.setStatus(fakerInstance.starTrek().specie());
        messstelle.setRealisierungsdatum(
                LocalDate.of(fakerInstance.number().numberBetween(2000, 2020), fakerInstance.number().numberBetween(1, 12), fakerInstance.number()
                        .numberBetween(1, 28)));
        messstelle.setAbbaudatum(
                LocalDate.of(fakerInstance.number().numberBetween(2000, 2020), fakerInstance.number().numberBetween(1, 12), fakerInstance.number()
                        .numberBetween(1, 28)));
        messstelle.setStadtbezirkNummer(fakerInstance.number().numberBetween(1, 26));
        messstelle.setBemerkung(fakerInstance.pokemon().name());
        messstelle.setDatumLetztePlausibleMeldung(
                LocalDate.of(fakerInstance.number().numberBetween(2000, 2020), fakerInstance.number().numberBetween(1, 12), fakerInstance.number()
                        .numberBetween(1, 28)));
        messstelle.setPunkt(new GeoPoint(fakerInstance.number().randomDouble(5, 0, 5), fakerInstance.number().randomDouble(5, 0, 5)));
        messstelle.setSichtbarDatenportal(fakerInstance.bool().bool());
        messstelle.setGeprueft(fakerInstance.bool().bool());
        messstelle.setKommentar(fakerInstance.pokemon().name());
        messstelle.setStandort(fakerInstance.pokemon().location());
        final List<String> suchwoerter = new ArrayList<>();
        suchwoerter.add(fakerInstance.company().buzzword());
        suchwoerter.add(fakerInstance.company().buzzword());
        suchwoerter.add(fakerInstance.company().buzzword());
        messstelle.setSuchwoerter(suchwoerter);
        final List<String> customSuchwoerter = new ArrayList<>();
        customSuchwoerter.add(fakerInstance.company().buzzword());
        customSuchwoerter.add(fakerInstance.company().buzzword());
        customSuchwoerter.add(fakerInstance.company().buzzword());
        messstelle.setCustomSuchwoerter(customSuchwoerter);
        messstelle.setMessquerschnitte(MessquerschnittRandomFactory.getSome());
        return messstelle;
    }

    public static List<Messstelle> getSome(int some) {
        List<Messstelle> zs = new ArrayList<>();

        for (int i = 0; i < some; i++) {
            zs.add(getOne());
        }
        return zs;
    }

    public static List<Messstelle> getSomeRandom() {
        return getSome(Faker.instance().number().numberBetween(1, 10));
    }

}
