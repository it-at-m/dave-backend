package de.muenchen.dave.domain.dtos.laden.messwerte;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class LadeMesswerteListenausgabeDTO implements Serializable {

    List<LadeMesswerteDTO> zaehldaten = new ArrayList<>();

}
