package de.muenchen.dave.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table(name = "stadtbezirk_viertel")
public class StadtbezirkViertelEntity extends BaseEntity {

    @Column(name = "nummer", unique = true, nullable = false, length = 20)
    private String nummer;

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "stadtbezirk_id")
    private StadtbezirkEntity stadtbezirk;

}
