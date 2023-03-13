package de.muenchen.dave.domain.relationaldb;

import com.github.javafaker.Faker;
import de.muenchen.dave.domain.EmailAddress;

import java.util.UUID;

public class EmailAddressRandomFactory {

    public static EmailAddress getOne() {
        EmailAddress emailAddress = new EmailAddress();
        emailAddress.setId(UUID.randomUUID());
        int x = Faker.instance().number().numberBetween(1, 2);
        emailAddress.setParticipantId(x);
        emailAddress.setEmailAddress("test@muenchen.de");
        return emailAddress;
    }

}
