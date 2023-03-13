package de.muenchen.dave.domain.elasticsearch;

import com.github.javafaker.Faker;

public class KnotenarmRandomFactory {

    public static Knotenarm getOne() {
        Knotenarm k = new Knotenarm();

        k.setNummer(Faker.instance().number().numberBetween(1, 8));
        k.setStrassenname("Teststrasse_" + k.getNummer());

        return k;
    }
}
