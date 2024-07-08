package de.muenchen.dave.domain;

import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
// Definition of getter, setter, ...
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Dienstleister extends BaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "kennung")
    private String kennung;

    @ElementCollection
    @Column(name = "email_addresses")
    private List<String> emailAddresses;

    @Column(name = "active")
    private Boolean active;

}
