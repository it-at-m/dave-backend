package de.muenchen.dave.domain.dtos.laden;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.util.CalculationUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LadeZaehldatumDTO implements Serializable {

    /**
     * Der Typ leitet sich aus der Typsetzung in der Methode
     * {@link LadeZaehldatenService#mapToZaehldatum} ab.
     * Das Attribut nimmt den Wert "null" ein, sobald es sich um einen 15-Minütigen-, 30-Minütigen-
     * 60-Minütigen-Intervall handelt.
     *
     */
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
    private Integer pkwEinheiten;

    @JsonGetter
    @NotNull
    public BigDecimal getKfz() {
        return CalculationUtil.getKfz(this);
    }

    @JsonGetter
    @NotNull
    public BigDecimal getSchwerverkehr() {
        return CalculationUtil.getSchwerverkehr(this);
    }

    @JsonGetter
    @NotNull
    public BigDecimal getGueterverkehr() {
        return CalculationUtil.getGueterverkehr(this);
    }

    @JsonGetter
    @NotNull
    public BigDecimal getAnteilSchwerverkehrAnKfzProzent() {
        return CalculationUtil.calculateAnteilSchwerverkehrAnKfzProzent(this);
    }

    @JsonGetter
    @NotNull
    public BigDecimal getAnteilGueterverkehrAnKfzProzent() {
        return CalculationUtil.calculateAnteilGueterverkehrAnKfzProzent(this);
    }

}
