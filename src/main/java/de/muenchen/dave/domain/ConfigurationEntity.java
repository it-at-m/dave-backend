package de.muenchen.dave.domain;

import de.muenchen.dave.domain.enums.ConfigDataTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "configuration")
public class ConfigurationEntity extends BaseEntity {
    
    @Column(name = "keyname", unique = true, nullable = false, length = 255)
    private String keyname;

    @Column(name = "valuefield", nullable = false, length = 1024)
    private String valuefield;

    @Column(name = "category", nullable = false, length = 255)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "datatype", nullable = false)
    private ConfigDataTypes datatype;
}
