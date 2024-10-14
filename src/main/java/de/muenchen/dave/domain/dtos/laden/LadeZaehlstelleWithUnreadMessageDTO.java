package de.muenchen.dave.domain.dtos.laden;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LadeZaehlstelleWithUnreadMessageDTO implements Serializable {

    String id;

    String nummer;

    Double lat;

    Double lng;

    List<LadeZaehlungWithUnreadMessageDTO> zaehlungen;
}
