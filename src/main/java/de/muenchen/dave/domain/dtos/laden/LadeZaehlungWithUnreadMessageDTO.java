package de.muenchen.dave.domain.dtos.laden;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class LadeZaehlungWithUnreadMessageDTO implements Serializable {

    Date datum;

    String projektName;

    Boolean unreadMessagesMobilitaetsreferat;

    Boolean unreadMessagesDienstleister;
}
