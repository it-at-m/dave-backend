package de.muenchen.dave.domain.dtos.laden;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import de.muenchen.dave.util.CalculationUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;
import lombok.Data;

@Data
public class LadeZaehldatumDTO implements Serializable {

    public LadeZaehldatumDTO() {
        // Default-Konstruktor für die Deserialisierung
    }

    public LadeZaehldatumDTO(Integer pkw, Integer lkw, Integer lastzuege, Integer busse, Integer kraftraeder, Integer fahrradfahrer,
            Integer fussgaenger, LocalTime startUhrzeit, LocalTime endeUhrzeit) {
        this.pkw = pkw;
        this.lkw = lkw;
        this.lastzuege = lastzuege;
        this.busse = busse;
        this.kraftraeder = kraftraeder;
        this.fahrradfahrer = fahrradfahrer;
        this.fussgaenger = fussgaenger;
        this.startUhrzeit = startUhrzeit;
        this.endeUhrzeit = endeUhrzeit;
    }

    private String type;

    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startUhrzeit;

    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endeUhrzeit;

    private Integer pkw;

    private Integer lkw;

    private Integer lastzuege;

    private Integer busse;

    private Integer kraftraeder;

    private Integer fahrradfahrer;

    private Integer fussgaenger;

    private Integer pkwEinheiten;

    @JsonGetter
    public BigDecimal getKfz() {
        return CalculationUtil.getKfz(this);
    }

    @JsonGetter
    public BigDecimal getSchwerverkehr() {
        return CalculationUtil.getSchwerverkehr(this);
    }

    @JsonGetter
    public BigDecimal getGueterverkehr() {
        return CalculationUtil.getGueterverkehr(this);
    }

    @JsonGetter
    public BigDecimal getAnteilSchwerverkehrAnKfzProzent() {
        return CalculationUtil.calculateAnteilSchwerverkehrAnKfzProzent(this);
    }

    @JsonGetter
    public BigDecimal getAnteilGueterverkehrAnKfzProzent() {
        return CalculationUtil.calculateAnteilGueterverkehrAnKfzProzent(this);
    }

}
