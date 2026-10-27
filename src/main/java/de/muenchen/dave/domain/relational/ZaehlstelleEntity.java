package de.muenchen.dave.domain.relational;

import de.muenchen.dave.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "zaehlstelle")
public class ZaehlstelleEntity extends BaseEntity {

    private String nummer;
    private String stadtbezirk;

    @Column(name = "stadtbezirk_nummer")
    private Integer stadtbezirkNummer;

    private String kommentar;
    private Double latitude;
    private Double longitude;

    @Column(name = "sichtbar_datenportal")
    private Boolean sichtbarDatenportal;

    @OneToMany(mappedBy = "zaehlstelle")
    @Builder.Default
    private List<ZaehlungEntity> zaehlungen = new ArrayList<>();
}
