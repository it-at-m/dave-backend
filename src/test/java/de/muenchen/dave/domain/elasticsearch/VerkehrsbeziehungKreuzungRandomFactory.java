package de.muenchen.dave.domain.elasticsearch;

import com.github.javafaker.Faker;

public class VerkehrsbeziehungKreuzungRandomFactory {

    public static Verkehrsbeziehung getOne() {
        Verkehrsbeziehung bean = new Verkehrsbeziehung();

        bean.setIsKreuzung(true);
        bean.setVon(Faker.instance().number().numberBetween(1, 8));
        bean.setNach(Faker.instance().number().numberBetween(1, 8));

        return bean;
    }
}
