package de.muenchen.dave.domain.elasticsearch;

import com.github.javafaker.Faker;
import de.muenchen.dave.relational.zaehlstellen.KnotenarmEntity;

public class KnotenarmRandomFactory {

    public static KnotenarmEntity getOne() {
        KnotenarmEntity k = new KnotenarmEntity();

        k.setNummer(Faker.instance().number().numberBetween(1, 8));
        k.setStrassenname("Teststrasse_" + k.getNummer());

        return k;
    }
}
