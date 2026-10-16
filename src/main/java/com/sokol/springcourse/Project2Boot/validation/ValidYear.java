package com.sokol.springcourse.Project2Boot.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Validates calendar years. */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = YearValidator.class)
public @interface ValidYear {
    /** Describes invalid input. */
    String message() default "Enter a four digit year no later than the current year";
    /** Selects validation groups. */
    Class<?>[] groups() default {};
    /** Supplies validation metadata. */
    Class<? extends Payload>[] payload() default {};
}
