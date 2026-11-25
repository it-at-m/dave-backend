package de.muenchen.dave.domain.dtos;

import com.github.javafaker.Faker;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteKnotenarmDto;

import java.util.ArrayList;
import java.util.List;

public class BearbeiteKnotenarmDTORandomFactory {

    public static BearbeiteKnotenarmDto getOne() {
        BearbeiteKnotenarmDto dto = new BearbeiteKnotenarmDto();

        dto.setNummer(Faker.instance().number().numberBetween(1, 8));
        dto.setStrassenname("Teststrasse_" + dto.getNummer());

        return dto;
    }

    public static List<BearbeiteKnotenarmDto> getSome() {
        List<BearbeiteKnotenarmDto> l = new ArrayList<>();

        int x = Faker.instance().number().numberBetween(1, 8);
        for (int i = 0; i < x; i++) {
            l.add(getOne());
        }
        return l;
    }
}
