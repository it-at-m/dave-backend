package de.muenchen.dave.domain.dtos;

import java.io.Serializable;
import java.util.Set;

import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = { "zaehlarten" })
public class ZaehlartenKarteDTO implements Serializable {

    @NotNull
    private Double longitude;
    @NotNull
    private Double latitude;
    @NotNull
    private Set<String> zaehlarten;

}
