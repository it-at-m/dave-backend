package de.muenchen.dave.domain.dtos.laden;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class LadeZaehlstelleWithUnreadMessageDTO implements Serializable {

    String id;

    String nummer;

    Double lat;

    Double lng;

    List<LadeZaehlungWithUnreadMessageDTO> zaehlungen;
}
