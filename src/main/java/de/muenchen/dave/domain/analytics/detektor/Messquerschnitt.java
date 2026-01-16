package de.muenchen.dave.domain.analytics.detektor;

import de.muenchen.dave.domain.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Entity
// Definition of getter, setter, ...
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Messquerschnitt extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "messstelle", referencedColumnName = "id")
    private Messstelle messstelle;

    @Column(name = "mq_id")
    private String mqId;

    @Embedded
    @AttributeOverrides(
        {
                @AttributeOverride(name = "lat", column = @Column(name = "latitude")),
                @AttributeOverride(name = "lon", column = @Column(name = "longitude")),
        }
    )
    private GeoPoint punkt;

    @Column(name = "strassenname")
    private String strassenname;

    @Column(name = "lage_messquerschnitt")
    private String lageMessquerschnitt;

    @Column(name = "fahrtrichtung")
    private String fahrtrichtung;

    @Column(name = "anzahl_fahrspuren")
    private Integer anzahlFahrspuren;

    @Column(name = "anzahl_detektoren")
    private Integer anzahlDetektoren;

    @Column(name = "standort")
    private String standort;
}
