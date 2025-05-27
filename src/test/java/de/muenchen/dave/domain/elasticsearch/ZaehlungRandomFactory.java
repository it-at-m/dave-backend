package de.muenchen.dave.domain.elasticsearch;

import com.github.javafaker.Faker;
import com.google.common.collect.Lists;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Status;
import de.muenchen.dave.services.IndexServiceUtils;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

public class ZaehlungRandomFactory {

    public static Zaehlung getOne() {
        final Zaehlung z = new Zaehlung();

        final Faker faker = Faker.instance(new Locale.Builder().setLanguage("test").build());

        // create random date
        final Date date = Faker.instance().date().between(new GregorianCalendar(1990, 0, 1).getTime(), new Date());
        final LocalDate d = LocalDate.of(2020, 12, 12);

        z.setId(UUID.randomUUID().toString());
        z.setDatum(d);
        z.setJahr(String.valueOf(d.getYear()));
        z.setMonat(d.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.GERMANY));
        z.setTagesTyp("Wochenende");
        z.setJahreszeit(IndexServiceUtils.jahreszeitenDetector(d));
        z.setStatus(Status.ACTIVE.name());
        z.setZaehlart("Q");
        z.setPunkt(new GeoPoint(new Random().nextDouble(), new Random().nextDouble()));

        z.setProjektNummer(Faker.instance().number().digits(10));
        z.setProjektName(Faker.instance().funnyName().name());
        z.setSonderzaehlung(true);
        z.setKategorien(generateKategorien());
        z.setZaehlsituation(faker.resolve("zaehlung.grund"));
        z.setZaehlsituationErweitert("Die erweiterte Zählsituation");
        z.setZaehlIntervall(15);
        z.setWetter(faker.resolve("zaehlung.wetter"));
        z.setZaehldauer(faker.resolve("zaehlung.zeit"));
        z.setSchulZeiten(faker.resolve("zaehlung.schule"));
        z.setSuchwoerter(Arrays.asList("foo", "bar", "foobar"));
        z.setDienstleisterkennung("dienstleisterkennung");

        z.setKnotenarme(Arrays.asList(KnotenarmRandomFactory.getOne(), KnotenarmRandomFactory.getOne()));
        z.setFahrbeziehungen(Arrays.asList(FahrbeziehungKreuzungRandomFactory.getOne(), FahrbeziehungKreuzungRandomFactory.getOne()));
        return z;
    }

    public static List<Zaehlung> getSome() {
        final List<Zaehlung> zs = new ArrayList<>();
        final int x = Faker.instance().number().numberBetween(1, 10);

        for (int i = 0; i < x; i++) {
            zs.add(getOne());
        }
        return zs;
    }

    /**
     * Creates a random list of "kategoorien" (PKW, LKW,...)
     *
     * @return
     */
    private static List<Fahrzeug> generateKategorien() {
        final int x = Faker.instance().number().numberBetween(1, 10);
        final Set<Fahrzeug> k = new HashSet<>();

        for (int i = 0; i < x; i++) {
            k.add(Fahrzeug.valueOf(Faker.instance(new Locale.Builder().setLanguage("test").build()).resolve("zaehlung.kategorie")));
        }

        return Lists.newArrayList(k);
    }

}
