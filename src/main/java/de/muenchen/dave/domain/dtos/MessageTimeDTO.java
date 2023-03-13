package de.muenchen.dave.domain.dtos;

import lombok.Data;

@Data
public class MessageTimeDTO {
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    private int millisecond;
}
