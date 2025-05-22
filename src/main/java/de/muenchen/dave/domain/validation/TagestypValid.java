package de.muenchen.dave.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TagestypValidator.class)
@Documented
public @interface TagestypValid {
    String message() default "Bei einem Zeitraum muss der Wochentag angegeben sein.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
