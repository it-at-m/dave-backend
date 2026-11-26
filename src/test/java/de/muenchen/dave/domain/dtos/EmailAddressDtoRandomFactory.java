package de.muenchen.dave.domain.dtos;

import com.github.javafaker.Faker;

public class EmailAddressDtoRandomFactory {

    public static EmailAddressDto getOne() {
        EmailAddressDto dto = new EmailAddressDto();
        int x = Faker.instance().number().numberBetween(1, 2);
        dto.setParticipantId(x);
        dto.setEmailAddress("test@muenchen.de");
        return dto;
    }
}
