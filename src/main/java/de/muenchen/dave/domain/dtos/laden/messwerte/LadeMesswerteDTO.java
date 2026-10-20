package de.muenchen.dave.domain.dtos.laden.messwerte;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import java.io.Serializable;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LadeMesswerteDTO implements Serializable {

    private int sortingIndex;

    @NotNull
    private String type;

    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonFormat(pattern = "HH:mm")
    @NotNull
    private LocalTime startUhrzeit;

    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonFormat(pattern = "HH:mm")
    @NotNull
    private LocalTime endeUhrzeit;

    @NotNull
    private Integer pkw;
    @NotNull
    private Integer lkw;
    @NotNull
    private Integer lfw;
    @NotNull
    private Integer lastzuege;
    @NotNull
    private Integer busse;
    @NotNull
    private Integer kraftraeder;
    @NotNull
    private Integer fahrradfahrer;
    @NotNull
    private Integer fussgaenger;
    @NotNull
    private Integer kfz;
    @NotNull
    private Integer schwerverkehr;
    @NotNull
    private Integer gueterverkehr;
    @NotNull
    private Double anteilSchwerverkehrAnKfzProzent;
    @NotNull
    private Double anteilGueterverkehrAnKfzProzent;
}
