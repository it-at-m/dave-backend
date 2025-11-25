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
import lombok.Data;

@Data
public class BelastungsplanMessquerschnitteDTO implements Serializable {

    private List<LadeBelastungsplanMessquerschnittDataDTO> ladeBelastungsplanMessquerschnittDataDTOList;

    private String strassenname;

    private String mstId;

    private Integer stadtbezirkNummer;

    private Integer totalKfz;

    private Integer totalSv;

    private Integer totalGv;

    private Integer totalRad;

    private BigDecimal totalPercentSv;

    private BigDecimal totalPercentGv;

    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startUhrzeitSpitzenstunde;

    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endeUhrzeitSpitzenstunde;
}
