package de.muenchen.dave.domain.elasticsearch;

import com.github.javafaker.Faker;
import de.muenchen.dave.relational.zaehlstellen.VerkehrsbeziehungEntity;

public class VerkehrsbeziehungKreuzungRandomFactory {

    public static VerkehrsbeziehungEntity getOne() {
        VerkehrsbeziehungEntity bean = new VerkehrsbeziehungEntity();

        bean.setIsKreuzung(true);
        bean.setVon(Faker.instance().number().numberBetween(1, 8));
        bean.setNach(Faker.instance().number().numberBetween(1, 8));

        return bean;
    }
}
