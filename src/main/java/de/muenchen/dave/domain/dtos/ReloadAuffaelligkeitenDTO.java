package de.muenchen.dave.domain.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReloadAuffaelligkeitenDTO {

    private LocalDate dateToReload;

}
