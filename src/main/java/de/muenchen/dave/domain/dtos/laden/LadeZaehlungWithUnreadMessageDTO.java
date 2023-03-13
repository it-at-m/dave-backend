package de.muenchen.dave.domain.dtos.laden;

import de.muenchen.dave.domain.elasticsearch.Fahrbeziehung;
import de.muenchen.dave.domain.elasticsearch.Knotenarm;
import de.muenchen.dave.domain.elasticsearch.PkwEinheit;
import de.muenchen.dave.domain.enums.Fahrzeug;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Data
public class LadeZaehlungWithUnreadMessageDTO implements Serializable {

    Date datum;

    String projektName;

    Boolean unreadMessagesMobilitaetsreferat;

    Boolean unreadMessagesDienstleister;
}