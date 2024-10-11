/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain;

import de.muenchen.dave.domain.enums.Zaehldauer;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Die Hochrechnung und die hochgerechneten Werte zur Ermittlung des Tageswert einer Zaehlung vom Typ {@link Zaehldauer#DAUER_2_X_4_STUNDEN}.
 */
@Embeddable
@Data
public class Hochrechnung {

    @Column(name = "faktorkfz")
    private BigDecimal faktorKfz;

    @Column(name = "faktorsv")
    private BigDecimal faktorSv;

    @Column(name = "faktorgv")
    private BigDecimal faktorGv;

    @Column(name = "hochrechnungkfz")
    private BigDecimal hochrechnungKfz;

    @Column(name = "hochrechnungsv")
    private BigDecimal hochrechnungSv;

    @Column(name = "hochrechnunggv")
    private BigDecimal hochrechnungGv;

    @Column(name = "hochrechnungrad")
    private Integer hochrechnungRad;

}
