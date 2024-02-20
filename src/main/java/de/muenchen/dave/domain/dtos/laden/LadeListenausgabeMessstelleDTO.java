/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.dtos.laden;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalTime;

@Data
public class LadeListenausgabeMessstelleDTO implements Serializable {

    private String type;

    private String startUhrzeit;

    private String endeUhrzeit;

    private Integer pkw;

    private Integer lkw;

    private Integer lfw;

    private Integer lastzuege;

    private Integer busse;

    private Integer kraftraeder;

    private Integer fahrradfahrer;

    private Integer fussgaenger;

    private Integer kfz;
    private Integer schwerverkehr;
    private Integer gueterverkehr;
    private Double anteilSchwerverkehrAnKfzProzent;
    private Double anteilGueterverkehrAnKfzProzent;
}
