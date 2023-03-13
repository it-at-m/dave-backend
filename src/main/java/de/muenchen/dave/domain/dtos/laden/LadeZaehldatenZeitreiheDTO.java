package de.muenchen.dave.domain.dtos.laden;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class LadeZaehldatenZeitreiheDTO implements Serializable {
    private List<String> datum = new ArrayList<>();
    private List<BigDecimal> kfz = new ArrayList<>();
    private List<BigDecimal> sv = new ArrayList<>();
    private List<BigDecimal> gv = new ArrayList<>();
    private List<Integer> rad = new ArrayList<>();
    private List<Integer> fuss = new ArrayList<>();
    private List<BigDecimal> svAnteilInProzent = new ArrayList<>();
    private List<BigDecimal> gvAnteilInProzent = new ArrayList<>();
    private List<BigDecimal> gesamt = new ArrayList<>();
}