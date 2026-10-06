package de.muenchen.dave.domain.dtos.laden;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LadeZaehldatenZeitreiheDTO implements Serializable {
    @NotNull
    private List<String> datum = new ArrayList<>();
    @NotNull
    private List<BigDecimal> kfz = new ArrayList<>();
    @NotNull
    private List<BigDecimal> sv = new ArrayList<>();
    @NotNull
    private List<BigDecimal> gv = new ArrayList<>();
    @NotNull
    private List<Integer> rad = new ArrayList<>();
    @NotNull
    private List<Integer> fuss = new ArrayList<>();
    @NotNull
    private List<BigDecimal> svAnteilInProzent = new ArrayList<>();
    @NotNull
    private List<BigDecimal> gvAnteilInProzent = new ArrayList<>();
    @NotNull
    private List<BigDecimal> gesamt = new ArrayList<>();
}
