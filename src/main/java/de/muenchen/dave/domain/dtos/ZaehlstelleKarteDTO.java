package de.muenchen.dave.domain.dtos;

import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


@Data
public class ZaehlstelleKarteDTO implements Serializable {

    private String id;

    private String nummer;

    private String letzteZaehlungId;

    private Double longitude;

    private Double latitude;

    private TooltipDTO tooltip;

    private Set<ZaehlartenKarteDTO> zaehlartenKarte = new HashSet<>();

    private Boolean sichtbarDatenportal;

}