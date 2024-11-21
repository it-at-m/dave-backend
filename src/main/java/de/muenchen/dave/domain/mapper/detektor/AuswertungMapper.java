package de.muenchen.dave.domain.mapper.detektor;

import de.muenchen.dave.domain.dtos.messstelle.auswertung.Auswertung;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.AuswertungMessstelleUndZeitraum;
import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatDto;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatResponseDto;
import de.muenchen.dave.services.messstelle.Zeitraum;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuswertungMapper {

    @Mapping(target = "zeitraum", expression = "java( zeitraum )")
    @Mapping(target = "mstId", expression = "java( mstId )")
    AuswertungMessstelleUndZeitraum tagesaggregatDto2AuswertungProMessstelleUndZeitraum(
            final TagesaggregatResponseDto dto,
            @Context final Zeitraum zeitraum,
            @Context final String mstId);

    default IntervalDto tagesaggregat2Interval(final Auswertung auswertung) {
        final var interval = tagesaggregat2Interval(auswertung.getDaten());
        final var zeitraum = auswertung.getZeitraum();
        final var datumUhrzeitVon = LocalDateTime
                .from(zeitraum.getStart())
                .with(TemporalAdjusters.firstDayOfMonth())
                .with(LocalTime.MIN);
        interval.setDatumUhrzeitVon(datumUhrzeitVon);
        final var datumUhrzeitBis = LocalDateTime
                .from(zeitraum.getEnd())
                .with(TemporalAdjusters.lastDayOfMonth())
                .with(LocalTime.MAX);
        interval.setDatumUhrzeitBis(datumUhrzeitBis);
        return interval;
    }

    IntervalDto tagesaggregat2Interval(final TagesaggregatDto tagesaggregat);
}
