package de.muenchen.dave.domain.elasticsearch;

import com.github.javafaker.Faker;
import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import de.muenchen.dave.relational.zaehlstellen.ZaehlstelleEntity;
import de.muenchen.dave.relational.zaehlstellen.ZaehlungEntity;
import de.muenchen.dave.services.IndexServiceUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

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
    public static ZaehlstelleEntity getOne() {
        Map<String, String> x = (Map<String, String>) service.fetch("zaehlstelle.stellen");
        ZaehlstelleEntity z = new ZaehlstelleEntity();
        z.setId(UUID.fromString(Faker.instance().crypto().md5()));
        z.setNummer(Faker.instance().number().digits(10));
        z.setStadtbezirk(x.get(BEZIRK));
        z.setStadtbezirkNummer(Ints.tryParse(x.get(BEZIRKNUMMER)));
        z.setLatitude(Doubles.tryParse(x.get(LAT)));
        z.setLongitude(Doubles.tryParse(x.get(LNG)));
        // Zaehlungen
        List<ZaehlungEntity> zls = ZaehlungRandomFactory.getSome();
        z.setZaehlungen(zls);
        ZaehlungEntity zl1 = IndexServiceUtils.getLetzteAktiveZaehlung(zls);
        z.setLetzteZaehlungMonat(zl1.getMonat());
        z.setLetzteZaehlungMonatNummer(zl1.getDatum().getMonthValue());
        z.setGrundLetzteZaehlung(zl1.getZaehlsituation());
        z.setLetzteZaehlungJahr(Integer.parseInt(zl1.getJahr()));
        return z;
    }

    public static List<ZaehlstelleEntity> getSome(int some) {
        List<ZaehlstelleEntity> zs = new ArrayList<>();

        for (int i = 0; i < some; i++) {
            zs.add(getOne());
        }
        return zs;
    }

    public static List<ZaehlstelleEntity> getSomeRandom() {
        return getSome(Faker.instance().number().numberBetween(1, 10));
    }

}
