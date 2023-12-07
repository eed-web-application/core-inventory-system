package edu.stanford.slac.code_inventory_system.api.v1.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 *
 */
@Documented
@Constraint(validatedBy = NullOrRegexValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NullOrRegex {
    String regexp();
    String message() default "The field must be either null or match valid regex";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}