package edu.stanford.slac.code_inventory_system.api.v1.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 *
 */
public class NullOrRegexValidator implements ConstraintValidator<NullOrRegex, String> {
    private Pattern pattern = null;
    @Override
    public void initialize(NullOrRegex constraintAnnotation) {
        pattern = Pattern.compile(constraintAnnotation.regexp());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null is valid
        }
        return pattern.matcher(value).matches();
    }
}