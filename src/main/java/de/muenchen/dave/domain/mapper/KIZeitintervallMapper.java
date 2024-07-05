package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.KIZeitintervall;
import de.muenchen.dave.domain.Zeitintervall;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

/**
 * Diese Klasse realisiert das Mapping zwischen Zeitintervall-Objekten und der für die
 * ONNX-Vorhersage.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class KIZeitintervallMapper {

    private static final String STARTTIMESTAMP_TO_STARTSTUNDE = "StartTimestampToStartStunde";
    private static final String STARTTIMESTAMP_TO_STARTMINUTE = "StartTimestampToStartMinute";
    private static final String STARTTIMESTAMP_TO_JAHRESEIT89 = "StartTimestampToJahreSeit89";
    private static final String STARTTIMESTAMP_TO_JAHRESZEIT = "StartTimestampToJahresZeit";

    @Mapping(target = "rad", source = "fahrradfahrer")
    @Mapping(target = "jahresZeit", source = "startUhrzeit", qualifiedByName = STARTTIMESTAMP_TO_JAHRESZEIT)
    @Mapping(target = "jahreSeit89", source = "startUhrzeit", qualifiedByName = STARTTIMESTAMP_TO_JAHRESEIT89)
    @Mapping(target = "montag", ignore = true)
    @Mapping(target = "dienstag", ignore = true)
    @Mapping(target = "mittwoch", ignore = true)
    @Mapping(target = "donnerstag", ignore = true)
    @Mapping(target = "freitag", ignore = true)
    @Mapping(target = "samstag", ignore = true)
    @Mapping(target = "sonntag", ignore = true)
    @BeanMapping(builder = @Builder(disableBuilder = true))
    public abstract KIZeitintervall zeitintervallToKIZeitintervall(Zeitintervall zeitintervall);

    @Named(STARTTIMESTAMP_TO_JAHRESZEIT)
    public int startTimestampToJahresZeit(LocalDateTime startTimestamp) {
        int jahresZeit = startTimestamp.get(IsoFields.QUARTER_OF_YEAR) + 1;
        if (jahresZeit == 5) return 1;
        return jahresZeit;
    }

    @Named(STARTTIMESTAMP_TO_JAHRESEIT89)
    public int startTimestampToJahreSeit89(LocalDateTime startTimestamp) {
        LocalDate year89 = LocalDate.of(1989, 1, 1);
        LocalDateTime timestamp89 = LocalDateTime.of(year89, LocalTime.MIDNIGHT);

        if (startTimestamp.isBefore(timestamp89)) return 0;

        return (int) ChronoUnit.YEARS.between(timestamp89, startTimestamp);
    }

    @Named(STARTTIMESTAMP_TO_STARTSTUNDE)
    public int startTimestampToStartStunde(LocalDateTime startTimestamp) {
        return startTimestamp.getHour();
    }

    @Named(STARTTIMESTAMP_TO_STARTMINUTE)
    public int startTimestampToStartMinute(LocalDateTime startTimestamp) {
        return startTimestamp.getMinute();
    }

    @AfterMapping
    protected void setDay(Zeitintervall zeitintervall, @MappingTarget KIZeitintervall kiZeitintervall) {
        final DayOfWeek day = zeitintervall.getStartUhrzeit().getDayOfWeek();
        switch (day) {
        case MONDAY:
            kiZeitintervall.setMontag(1);
            break;
        case TUESDAY:
            kiZeitintervall.setDienstag(1);
            break;
        case WEDNESDAY:
            kiZeitintervall.setMittwoch(1);
            break;
        case THURSDAY:
            kiZeitintervall.setDonnerstag(1);
            break;
        case FRIDAY:
            kiZeitintervall.setFreitag(1);
            break;
        case SATURDAY:
            kiZeitintervall.setSamstag(1);
            break;
        case SUNDAY:
            kiZeitintervall.setSonntag(1);
            break;
        }
    }

}
