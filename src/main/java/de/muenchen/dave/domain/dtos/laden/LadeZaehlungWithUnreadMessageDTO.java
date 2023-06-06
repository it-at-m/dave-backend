package de.muenchen.dave.domain.dtos.laden;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class LadeZaehlungWithUnreadMessageDTO implements Serializable {

    Date datum;

    String projektName;

    Boolean unreadMessagesMobilitaetsreferat;

    Boolean unreadMessagesDienstleister;
}
