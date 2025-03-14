package de.muenchen.dave.domain.validation;

import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.util.messstelle.MesswerteBaseUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TagestypValidator implements ConstraintValidator<TagestypValid, MessstelleOptionsDTO> {

    @Override
    public boolean isValid(final MessstelleOptionsDTO options, final ConstraintValidatorContext constraintValidatorContext) {
        boolean valid = true;

        if (MesswerteBaseUtil.isDateRange(options.getZeitraum())) {
            valid = ObjectUtils.isNotEmpty(options.getTagesTyp()) && !TagesTyp.UNSPECIFIED
                    .equals(options.getTagesTyp());
        }

        return valid;
    }
}
