package com.sokol.springcourse.Project2Boot.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Year;

/** Checks submitted years. */
public class YearValidator implements ConstraintValidator<ValidYear, String> {
    /** Checks calendar bounds. */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank()
                || value.matches("[0-9]{4}") && Integer.parseInt(value) > 0
                && Integer.parseInt(value) <= Year.now(context.getClockProvider().getClock()).getValue();
    }
}
