package edu.stanford.slac.code_inventory_system.api.v1.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.regex.Pattern;

/**
 *
 */
public class NullOrRegexValidator implements ConstraintValidator<NullOrRegex, Object> {
    private Pattern pattern = null;
    @Override
    public void initialize(NullOrRegex constraintAnnotation) {
        pattern = Pattern.compile(constraintAnnotation.regexp());
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null is valid
        }
        if (value instanceof List) {
            for (Object obj : (List) value) {
                if (!(obj instanceof String)) {
                    return true; // Non-string element in list
                }
                if (!pattern.matcher((String) obj).matches()) {
                    return false; // String does not match pattern
                }
            }
            return true; // All strings in list match pattern
        } else if (value instanceof String) {
            return pattern.matcher((String) value).matches(); // Single string matches pattern
        }
        return true; // Not a string or list of strings
    }
}