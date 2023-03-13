package de.muenchen.dave.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
// Definition of getter, setter, ...
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class EmailAddress extends BaseEntity {

    @Column(name = "participant_id")
    private Integer participantId;

    @Column(name = "email_address")
    private String emailAddress;

}
