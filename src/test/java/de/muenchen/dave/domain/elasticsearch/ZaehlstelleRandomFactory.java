package de.muenchen.dave.domain.elasticsearch;

import com.github.javafaker.Faker;
import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import de.muenchen.dave.services.IndexServiceUtils;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ZaehlstelleRandomFactory {
    private final static FakeValuesService service = new FakeValuesService(new Locale.Builder().setLanguage("test").build(), new RandomService());
    private final static String BEZIRKNUMMER = "bezirknummer";
    private final static String BEZIRK = "bezirk";
    private final static String LAT = "lat";
    private final static String LNG = "lng";
    private final static String NAME = "name";
    private final static String STRASSEN = "strassen";
    private final static String GEO = "geo";

    /**
     * Creates a random "zaehlstelle". You can find the data under /test/resources/test.yml.
     *
     * @return
     */
    public static Zaehlstelle getOne() {
        Map<String, String> x = (Map<String, String>) service.fetch("zaehlstelle.stellen");
        Zaehlstelle z = new Zaehlstelle();
        z.setId(Faker.instance().crypto().md5());
        z.setNummer(Faker.instance().number().digits(10));
        z.setStadtbezirk(x.get(BEZIRK));
        z.setStadtbezirkNummer(Ints.tryParse(x.get(BEZIRKNUMMER)));
        z.setPunkt(new GeoPoint(Doubles.tryParse(x.get(LAT)), Doubles.tryParse(x.get(LNG))));
        // Zaehlungen
        List<Zaehlung> zls = ZaehlungRandomFactory.getSome();
        z.setZaehlungen(zls);
        Zaehlung zl1 = IndexServiceUtils.getLetzteZaehlung(zls);
        z.setLetzteZaehlungMonat(zl1.getMonat());
        z.setLetzteZaehlungMonatNummer(zl1.getDatum().getMonthValue());
        z.setGrundLetzteZaehlung(zl1.getZaehlsituation());
        z.setLetzteZaehlungJahr(Integer.parseInt(zl1.getJahr()));
        return z;
    }

    public static List<Zaehlstelle> getSome(int some) {
        List<Zaehlstelle> zs = new ArrayList<>();

        for (int i = 0; i < some; i++) {
            zs.add(getOne());
        }
        return zs;
    }

    public static List<Zaehlstelle> getSomeRandom() {
        return getSome(Faker.instance().number().numberBetween(1, 10));
    }

}
