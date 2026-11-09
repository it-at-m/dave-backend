package de.muenchen.dave.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table(name = "stadtbezirk")
public class StadtbezirkEntity extends BaseEntity {

    @Column(name = "nummer", unique = true, nullable = false)
    private Integer nummer;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "stadt", unique = false, nullable = false)
    private String stadt;

}
