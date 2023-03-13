package de.muenchen.dave.domain.elasticsearch;

import com.github.javafaker.Faker;

public class HochrechnungsfaktorRandomFactory {

    public static Hochrechnungsfaktor getOne() {
        Hochrechnungsfaktor h = new Hochrechnungsfaktor();

        h.setGv(Faker.instance().number().randomDouble(3, 0, 3));
        h.setKfz(Faker.instance().number().randomDouble(3, 0, 3));
        h.setSv(Faker.instance().number().randomDouble(3, 0, 3));

        return h;
    }
}
