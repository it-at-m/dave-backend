package de.muenchen.dave.domain.dtos;

import com.github.javafaker.Faker;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDto;
import java.util.ArrayList;
import java.util.List;

public class BearbeiteVerkehrsbeziehungKreuzungDTORandomFactory {

    public static BearbeiteVerkehrsbeziehungDto getOne() {
        BearbeiteVerkehrsbeziehungDto dto = new BearbeiteVerkehrsbeziehungDto();

        dto.setIsKreuzung(true);
        dto.setNach(Faker.instance().number().numberBetween(1, 8));
        dto.setVon(Faker.instance().number().numberBetween(1, 8));

        return dto;
    }

    public static List<BearbeiteVerkehrsbeziehungDto> getSome() {
        List<BearbeiteVerkehrsbeziehungDto> l = new ArrayList<>();

        int x = Faker.instance().number().numberBetween(1, 8);
        for (int i = 0; i < x; i++) {
            l.add(getOne());
        }
        return l;
    }
}
