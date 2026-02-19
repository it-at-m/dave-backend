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
@Table(name = "city_district")
public class CityDistrictEntity extends BaseEntity {

    @Column(name = "name", unique = true, nullable = false, length = 255)
    private String name;

    @Column(name = "city", unique = false, nullable = false, length = 40)
    private String city;

    @Column(name = "number", unique = true, nullable = false)
    private Integer number;

}
