package de.muenchen.dave.domain.enums;

import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import de.muenchen.dave.geodateneai.gen.model.MesswertRequestDto;
import de.muenchen.dave.geodateneai.gen.model.TagesaggregatRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public enum TagesTyp {

    UNSPECIFIED("unspecified", MesswertRequestDto.TagesTypEnum.DTV, TagesaggregatRequestDto.TagesTypEnum.DTV, null),

    WERKTAG_DI_MI_DO("DTVw3 (Di,Mi,Do - außerhalb Ferien)", MesswertRequestDto.TagesTypEnum.DTV_W3, TagesaggregatRequestDto.TagesTypEnum.DTV_W3,
            IntervalDto.TagesTypEnum.DTV_W3),

    WERKTAG_MO_FR("DTVw5 (Mo-Fr - außerhalb Ferien)", MesswertRequestDto.TagesTypEnum.DTV_W5, TagesaggregatRequestDto.TagesTypEnum.DTV_W5,
            IntervalDto.TagesTypEnum.DTV_W5),

    SAMSTAG("Samstag in/außerhalb Ferien", MesswertRequestDto.TagesTypEnum.SAMSTAG, TagesaggregatRequestDto.TagesTypEnum.SAMSTAG,
            IntervalDto.TagesTypEnum.SAMSTAG),

    SONNTAG_FEIERTAG("Sonntag/Feiertag in/außerhalb Ferien", MesswertRequestDto.TagesTypEnum.SONNTAG_FEIERTAG,
            TagesaggregatRequestDto.TagesTypEnum.SONNTAG_FEIERTAG, IntervalDto.TagesTypEnum.SONNTAG_FEIERTAG),

    WERKTAG_FERIEN("Mo-Fr Ferien", MesswertRequestDto.TagesTypEnum.WERKTAG_FERIEN, TagesaggregatRequestDto.TagesTypEnum.WERKTAG_FERIEN,
            IntervalDto.TagesTypEnum.WERKTAG_FERIEN),

    MO_SO("DTV (Mo - So)", MesswertRequestDto.TagesTypEnum.DTV, TagesaggregatRequestDto.TagesTypEnum.DTV, IntervalDto.TagesTypEnum.DTV);

    private static final Map<IntervalDto.TagesTypEnum, TagesTyp> tagesTypByIntervallTyp = Stream
            .of(TagesTyp.values())
            .filter(tagesTyp -> ObjectUtils.isNotEmpty(tagesTyp.getIntervallTyp()))
            .collect(Collectors.toMap(TagesTyp::getIntervallTyp, Function.identity()));

    /**
     * Die Beschreibung zum TagesTyp.
     */
    @Getter
    private final String beschreibung;

    @Getter
    private final MesswertRequestDto.TagesTypEnum messwertTyp;

    @Getter
    private final TagesaggregatRequestDto.TagesTypEnum tagesaggregatTyp;

    @Getter
    private final IntervalDto.TagesTypEnum intervallTyp;

    public static TagesTyp getByIntervallTyp(final IntervalDto.TagesTypEnum messwertTyp) {
        return tagesTypByIntervallTyp.get(messwertTyp);
    }
}
