package de.muenchen.dave.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BearbeiteZaehlungValidator.class)
@Documented
public @interface BearbeiteZaehlungValid {
    String message() default "Die zu speichernde Zählung ist nicht valide.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
