package de.muenchen.dave.domain.elasticsearch;

import com.github.javafaker.Faker;

public class FahrbeziehungKreuzungRandomFactory {

    public static Fahrbeziehung getOne() {
        Fahrbeziehung bean = new Fahrbeziehung();

        bean.setIsKreuzung(true);
        bean.setVon(Faker.instance().number().numberBetween(1, 8));
        bean.setNach(Faker.instance().number().numberBetween(1, 8));

        return bean;
    }
}
