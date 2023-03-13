package de.muenchen.dave.domain.dtos;

import com.github.javafaker.Faker;

public class EmailAddressDTORandomFactory {

    public static EmailAddressDTO getOne() {
        EmailAddressDTO dto = new EmailAddressDTO();
        int x = Faker.instance().number().numberBetween(1, 2);
        dto.setParticipantId(x);
        dto.setEmailAddress("test@muenchen.de");
        return dto;
    }
}
