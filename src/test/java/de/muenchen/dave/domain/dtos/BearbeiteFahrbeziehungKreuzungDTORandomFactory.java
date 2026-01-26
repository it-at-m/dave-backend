package de.muenchen.dave.domain.dtos;

import com.github.javafaker.Faker;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
import java.util.ArrayList;
import java.util.List;

public class BearbeiteFahrbeziehungKreuzungDTORandomFactory {

    public static BearbeiteVerkehrsbeziehungDTO getOne() {
        BearbeiteVerkehrsbeziehungDTO dto = new BearbeiteVerkehrsbeziehungDTO();

        dto.setIsKreuzung(true);
        dto.setNach(Faker.instance().number().numberBetween(1, 8));
        dto.setVon(Faker.instance().number().numberBetween(1, 8));

        return dto;
    }

    public static List<BearbeiteVerkehrsbeziehungDTO> getSome() {
        List<BearbeiteVerkehrsbeziehungDTO> l = new ArrayList<>();

        int x = Faker.instance().number().numberBetween(1, 8);
        for (int i = 0; i < x; i++) {
            l.add(getOne());
        }
        return l;
    }
}
