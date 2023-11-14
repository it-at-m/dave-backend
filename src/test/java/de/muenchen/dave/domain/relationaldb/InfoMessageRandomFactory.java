package de.muenchen.dave.domain.relationaldb;

import de.muenchen.dave.domain.InfoMessage;
import java.time.LocalDate;

public class InfoMessageRandomFactory {

    public static InfoMessage getOneAktiv() {
        final InfoMessage message = new InfoMessage();

        message.setContent("test");
        message.setGueltigBis(LocalDate.now());
        message.setGueltigVon(LocalDate.now());
        message.setAktiv(true);

        return message;
    }

    public static InfoMessage getOneInaktiv() {
        final InfoMessage message = getOneAktiv();
        message.setAktiv(false);
        return message;
    }

}
