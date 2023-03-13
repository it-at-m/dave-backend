package de.muenchen.dave.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;

/**
 * Diese Klasse stellt die für das Ausführen von Vorhersagen notwendige Repräsentation eines Zeitintervalls dar.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KIZeitintervall {

    @Min(value = 0, message = "RAD must not be negative")
    int rad;

    // 1: Winter, 2: Frühling, 3: Sommer, 4: Herbst d.h. Quartale mit Offset 1
    @Range(min = 1, max = 4, message = "jahresZeit can only be between 1 and 4")
    int jahresZeit;

    @Min(value = 0, message = "jahreSeit89 must not be negative")
    int jahreSeit89;

    @Range(min = 0, max = 1, message = "montag can only be 0 or 1")
    int montag = 0;

    @Range(min = 0, max = 1, message = "dienstag can only be 0 or 1")
    int dienstag = 0;

    @Range(min = 0, max = 1, message = "mittwoch can only be 0 or 1")
    int mittwoch = 0;

    @Range(min = 0, max = 1, message = "donnerstag can only be 0 or 1")
    int donnerstag = 0;

    @Range(min = 0, max = 1, message = "freitag can only be 0 or 1")
    int freitag = 0;

    @Range(min = 0, max = 1, message = "samstag can only be 0 or 1")
    int samstag = 0;

    @Range(min = 0, max = 1, message = "sonntag can only be 0 or 1")
    int sonntag = 0;

    /**
     * Diese Methode wandelt ein KIZeitintervall-Objekt in ein Array mit primitivem long-Typ um, damit die ONNX-Runtime (int64) dieses zur Vorhersage verwenden kann.
     * @return Das KIZeitintervall als long[]-Repräsentation.
     */
    public long[] toArray() {
        return new long[] {
                rad,
                jahresZeit,
                jahreSeit89,
                montag,
                dienstag,
                mittwoch,
                donnerstag,
                freitag,
                samstag,
                sonntag
        };
    }

}
