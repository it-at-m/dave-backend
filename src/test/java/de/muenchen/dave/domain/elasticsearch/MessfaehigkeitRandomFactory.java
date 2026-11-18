package de.muenchen.dave.domain.elasticsearch;

import com.github.javafaker.Faker;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.geodateneai.gen.model.MessfaehigkeitDto;
import de.muenchen.dave.relational.messtellen.MessfaehigkeitEntity;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MessfaehigkeitRandomFactory {

    private static final Faker fakerInstance = Faker.instance();

    public static MessfaehigkeitEntity getMessfaehigkeit() {
        final MessfaehigkeitEntity messfaehigkeit = new MessfaehigkeitEntity();
        messfaehigkeit.setFahrzeugklasse(Fahrzeugklasse.values()[ThreadLocalRandom.current().nextInt(Fahrzeugklasse.values().length)]);
        messfaehigkeit.setIntervall(ZaehldatenIntervall.values()[ThreadLocalRandom.current().nextInt(ZaehldatenIntervall.values().length)]);
        messfaehigkeit
                .setGueltigAb(LocalDate.of(fakerInstance.number().numberBetween(2000, 2020), fakerInstance.number().numberBetween(1, 12), fakerInstance.number()
                        .numberBetween(1, 28)));
        messfaehigkeit.setGueltigBis(
                LocalDate.of(fakerInstance.number().numberBetween(2000, 2020), fakerInstance.number().numberBetween(1, 12), fakerInstance.number()
                        .numberBetween(1, 28)));
        return messfaehigkeit;
    }

    public static List<MessfaehigkeitEntity> getSomeMessfaehigkeiten() {
        final List<MessfaehigkeitEntity> messfaehigkeiten = new ArrayList<>();
        messfaehigkeiten.add(getMessfaehigkeit());
        messfaehigkeiten.add(getMessfaehigkeit());
        return messfaehigkeiten;
    }

    public static MessfaehigkeitDto getMessfaehigkeitDto() {
        final MessfaehigkeitDto dto = new MessfaehigkeitDto();
        dto.setFahrzeugklasse(
                MessfaehigkeitDto.FahrzeugklasseEnum.values()[ThreadLocalRandom.current().nextInt(MessfaehigkeitDto.FahrzeugklasseEnum.values().length)]);
        dto.setIntervall(MessfaehigkeitDto.IntervallEnum.values()[ThreadLocalRandom.current().nextInt(MessfaehigkeitDto.IntervallEnum.values().length)]);
        dto.setGueltigAb(LocalDate.of(fakerInstance.number().numberBetween(2000, 2020), fakerInstance.number().numberBetween(1, 12), fakerInstance.number()
                .numberBetween(1, 28)));
        dto.setGueltigBis(LocalDate.of(fakerInstance.number().numberBetween(2000, 2020), fakerInstance.number().numberBetween(1, 12), fakerInstance.number()
                .numberBetween(1, 28)));
        return dto;
    }

    public static List<MessfaehigkeitDto> getSomeMessfaehigkeitenDtos() {
        return List.of(getMessfaehigkeitDto(), getMessfaehigkeitDto());
    }
}
