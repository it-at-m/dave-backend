package de.muenchen.dave.domain.dtos.laden.messwerte;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BelastungsplanMessquerschnitteDTO implements Serializable {

    @NotNull
    private List<LadeBelastungsplanMessquerschnittDataDTO> ladeBelastungsplanMessquerschnittDataDTOList;
    @NotNull
    private String strassenname;
    @NotNull
    private String mstId;
    @NotNull
    private Integer stadtbezirkNummer;
    @NotNull
    private Integer totalKfz;
    @NotNull
    private Integer totalSv;
    @NotNull
    private Integer totalGv;
    @NotNull
    private Integer totalRad;
    @NotNull
    private BigDecimal totalPercentSv;
    @NotNull
    private BigDecimal totalPercentGv;
    @NotNull
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startUhrzeitSpitzenstunde;
    @NotNull
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endeUhrzeitSpitzenstunde;
}
