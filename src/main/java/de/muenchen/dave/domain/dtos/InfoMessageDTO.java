package de.muenchen.dave.domain.dtos;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class InfoMessageDTO {

    private UUID id;
    private String content;
    private LocalDate gueltigVon;
    private LocalDate gueltigBis;
    private boolean gueltig;
    private boolean aktiv;

}
