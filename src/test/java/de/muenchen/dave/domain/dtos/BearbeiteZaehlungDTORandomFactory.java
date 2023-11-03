package de.muenchen.dave.domain.dtos;

import com.github.javafaker.Faker;
import com.google.common.collect.Lists;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.enums.Fahrzeug;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

public class BearbeiteZaehlungDTORandomFactory {

    public static BearbeiteZaehlungDTO getOne() {

        BearbeiteZaehlungDTO dto = new BearbeiteZaehlungDTO();

        Faker faker = Faker.instance(new Locale("test"));

        LocalDate d = LocalDate.of(2020, 12, 12);

        dto.setDatum(d);
        dto.setZaehlart("Q");
        dto.setLat(new Random().nextDouble());
        dto.setLng(new Random().nextDouble());
        dto.setPunkt(new GeoPoint(dto.getLat(), dto.getLng()));
        dto.setTagesTyp(faker.resolve("zaehlung.tag"));
        dto.setProjektNummer(Faker.instance().number().digits(10));
        dto.setProjektName(Faker.instance().funnyName().name());
        dto.setSonderzaehlung(true);
        dto.setKategorien(Lists.newArrayList(Fahrzeug.KFZ, Fahrzeug.LKW, Fahrzeug.RAD));
        dto.setZaehlsituation(faker.resolve("zaehlung.grund"));
        dto.setZaehlsituationErweitert("Erweiterte Zählsituation");
        dto.setZaehlIntervall(15);
        dto.setWetter(faker.resolve("zaehlung.wetter"));
        dto.setZaehldauer(faker.resolve("zaehlung.zeit"));
        dto.setSchulZeiten(faker.resolve("zaehlung.schule"));
        dto.setCustomSuchwoerter(Arrays.asList("Test", "Foo", "Bar"));
        return dto;
    }
}
