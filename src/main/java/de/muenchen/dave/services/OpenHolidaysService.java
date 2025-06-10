package de.muenchen.dave.services;

import de.muenchen.dave.configuration.LogExecutionTime;
import de.muenchen.dave.domain.Kalendertag;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.openholidays.gen.api.HolidaysApi;
import de.muenchen.dave.openholidays.gen.model.HolidayResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile({ "!konexternal && !prodexternal && !unittest" })
public class OpenHolidaysService {

    private final HolidaysApi holidaysApi;
    private final KalendertagService kalendertagService;

    @LogExecutionTime
    public void createKalendertageForNextYear() {
        log.info("#createKalendertageForNextYear");
        final int year = LocalDate.now().getYear() + 1;
        final LocalDate startDateIncluded = LocalDate.of(year, 1, 1);
        final LocalDate endDateIncluded = LocalDate.of(year, 12, 31);
        createKalendertage(startDateIncluded, endDateIncluded);

        // Feiertage laden
        final List<HolidayResponse> publicHolidays = loadPublicHolidaysByDate(startDateIncluded, endDateIncluded);
        // Funktioniert noch nicht richtig
        processingPublicHolidays(publicHolidays);

        // Schulferien laden
        final List<HolidayResponse> schoolHolidays = loadSchoolHolidaysByDate(startDateIncluded, endDateIncluded);
        // Funktioniert noch nicht richtig
        processingSchoolHolidays(schoolHolidays);
    }

    private void blub(final LocalDate startDateIncluded, final LocalDate endDateIncluded) {
        if (ObjectUtils.isNotEmpty(startDateIncluded) && ObjectUtils.isNotEmpty(endDateIncluded)) {
            final Stream<Kalendertag> kalendertage = startDateIncluded.datesUntil(endDateIncluded.plusDays(1))
                    .parallel()
                    .map(localDate -> {
                        final Kalendertag kalendertag = new Kalendertag();
                        kalendertag.setNextStartDateToLoadUnauffaelligeTage(null);
                        kalendertag.setDatum(localDate);
                        kalendertag.setTagestyp(getTagestypOfLocalDate(localDate));
                        return kalendertag;
                    });
            final List<HolidayResponse> publicHolidays = loadPublicHolidaysByDate(startDateIncluded, endDateIncluded);
            final List<Kalendertag> publicHolidaysKalendertage = publicHolidays.parallelStream().map(holiday -> {
                final Kalendertag kalendertag = new Kalendertag();
                kalendertag.setNextStartDateToLoadUnauffaelligeTage(null);
                kalendertag.setTagestyp(TagesTyp.SONNTAG_FEIERTAG);
                kalendertag.setDatum(holiday.getStartDate());
                return kalendertag;
            }).toList();

        }
    }

    @LogExecutionTime
    protected void createKalendertage(final LocalDate startDateIncluded, final LocalDate endDateIncluded) {
        log.info("#createKalendertage");
        if (ObjectUtils.isNotEmpty(startDateIncluded) && ObjectUtils.isNotEmpty(endDateIncluded)) {
            final List<Kalendertag> kalendertage = startDateIncluded.datesUntil(endDateIncluded.plusDays(1))
                    .parallel()
                    .map(localDate -> {
                        final Kalendertag kalendertag = new Kalendertag();
                        kalendertag.setNextStartDateToLoadUnauffaelligeTage(null);
                        kalendertag.setDatum(localDate);
                        kalendertag.setTagestyp(getTagestypOfLocalDate(localDate));
                        return kalendertag;
                    }).toList();
            kalendertagService.saveAllAndFlush(kalendertage);
        }
    }

    protected TagesTyp getTagestypOfLocalDate(final LocalDate localDate) {
        final DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        TagesTyp tagestypOfLocalDate = TagesTyp.UNSPECIFIED;
        if (dayOfWeek == DayOfWeek.MONDAY) {
            tagestypOfLocalDate = TagesTyp.WERKTAG_MO_FR;
        } else if (dayOfWeek == DayOfWeek.TUESDAY) {
            tagestypOfLocalDate = TagesTyp.WERKTAG_DI_MI_DO;
        } else if (dayOfWeek == DayOfWeek.WEDNESDAY) {
            tagestypOfLocalDate = TagesTyp.WERKTAG_DI_MI_DO;
        } else if (dayOfWeek == DayOfWeek.THURSDAY) {
            tagestypOfLocalDate = TagesTyp.WERKTAG_DI_MI_DO;
        } else if (dayOfWeek == DayOfWeek.FRIDAY) {
            tagestypOfLocalDate = TagesTyp.WERKTAG_MO_FR;
        } else if (dayOfWeek == DayOfWeek.SATURDAY) {
            tagestypOfLocalDate = TagesTyp.SAMSTAG;
        } else if (dayOfWeek == DayOfWeek.SUNDAY) {
            tagestypOfLocalDate = TagesTyp.SONNTAG_FEIERTAG;
        }
        return tagestypOfLocalDate;
    }

    @LogExecutionTime
    protected List<HolidayResponse> loadPublicHolidaysByDate(final LocalDate validFrom, final LocalDate validTo) {
        return Objects.requireNonNull(holidaysApi.publicHolidaysGetWithHttpInfo("DE", validFrom, validTo, "DE", "DE-BY").block()).getBody();
    }

    @LogExecutionTime
    protected List<HolidayResponse> loadSchoolHolidaysByDate(final LocalDate validFrom, final LocalDate validTo) {
        return Objects.requireNonNull(holidaysApi.schoolHolidaysGetWithHttpInfo("DE", validFrom, validTo, "DE", "DE-BY").block()).getBody();
    }

    @LogExecutionTime
    protected void processingPublicHolidays(final List<HolidayResponse> holidays) {
        log.debug("#processingPublicHolidays");
        final List<Kalendertag> kalendertage = holidays.parallelStream().map(holiday -> {
            final Optional<Kalendertag> optionalKalendertag = kalendertagService.findByDatum(holiday.getStartDate());
            if (optionalKalendertag.isPresent()) {
                final Kalendertag kalendertag = optionalKalendertag.get();
                kalendertag.setTagestyp(TagesTyp.SONNTAG_FEIERTAG);
                return kalendertag;
            } else {
                return null;
            }
        }).filter(Objects::nonNull).toList();
        kalendertagService.saveAllAndFlush(kalendertage);

        //        holidays.parallelStream().forEach(holiday -> {
        //            final Optional<Kalendertag> optionalKalendertag = kalendertagService.findByDatum(holiday.getStartDate());
        //            if (optionalKalendertag.isPresent()) {
        //                final Kalendertag kalendertag = optionalKalendertag.get();
        //                kalendertag.setTagestyp(TagesTyp.SONNTAG_FEIERTAG);
        //                kalendertagService.save(kalendertag);
        //            }});
    }

    @LogExecutionTime
    protected void processingSchoolHolidays(final List<HolidayResponse> holidays) {
        log.debug("#processingSchoolHolidays");
        final List<Kalendertag> kalendertage = holidays
                .parallelStream()
                .flatMap(holiday -> holiday.getStartDate().datesUntil(holiday.getEndDate().plusDays(1))
                        .parallel()
                        .map(day -> {
                            final Optional<Kalendertag> optionalKalendertag = kalendertagService.findByDatum(day);
                            if (optionalKalendertag.isPresent()) {
                                final Kalendertag kalendertag = optionalKalendertag.get();
                                if (kalendertag.getTagestyp().equals(TagesTyp.WERKTAG_MO_FR) ||
                                        kalendertag.getTagestyp().equals(TagesTyp.WERKTAG_DI_MI_DO)) {
                                    kalendertag.setTagestyp(TagesTyp.WERKTAG_FERIEN);
                                }
                                return kalendertag;
                            } else {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull))
                .toList();
        kalendertagService.saveAllAndFlush(kalendertage);
    }
}
