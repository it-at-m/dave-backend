/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.ZeitintervallDTO;
import de.muenchen.dave.util.DaveConstants;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Mapper(componentModel = "spring")
public interface ZeitintervallMapper {

    /**
     * Die vorgehaltene {@link ZeitintervallDTO}#getStartUhrzeit() und
     * {@link ZeitintervallDTO}#getEndeUhrzeit()
     * ist im Format {@link DaveConstants}#ZEITINTERVALL_TIME_FORMAT hinterlegt.
     * Diese Methode erweitert die Uhrzeit um das Datum {@link DaveConstants#DEFAULT_LOCALDATE}
     * damit in der eigentlichen Mappermethode ein Parsing von String nach
     * {@link LocalDateTime} durchgeführt werden kann.
     *
     * @param zeitintervalle als Dto.
     */
    @BeforeMapping
    default void setCorrectDateString(final ZeitintervallDTO zeitintervalle) {
        LocalDateTime time = LocalDateTime.of(
                DaveConstants.DEFAULT_LOCALDATE,
                LocalTime.parse(
                        zeitintervalle.getStartUhrzeit(),
                        DateTimeFormatter.ofPattern(DaveConstants.ZEITINTERVALL_TIME_FORMAT, Locale.GERMANY)));
        zeitintervalle.setStartUhrzeit(time.toString());
        time = LocalDateTime.of(
                DaveConstants.DEFAULT_LOCALDATE,
                LocalTime.parse(
                        zeitintervalle.getEndeUhrzeit(),
                        DateTimeFormatter.ofPattern(DaveConstants.ZEITINTERVALL_TIME_FORMAT, Locale.GERMANY)));
        zeitintervalle.setEndeUhrzeit(time.toString());
    }

    Zeitintervall zeitintervallDtoToZeitintervall(final ZeitintervallDTO zeitintervalle);

}
