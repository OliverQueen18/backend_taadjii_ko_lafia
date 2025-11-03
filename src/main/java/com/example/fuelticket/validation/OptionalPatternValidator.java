package com.example.fuelticket.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class OptionalPatternValidator implements ConstraintValidator<OptionalPattern, String> {
    private Pattern pattern;

    @Override
    public void initialize(OptionalPattern constraintAnnotation) {
        pattern = Pattern.compile(constraintAnnotation.regexp());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Si la valeur est null ou vide, la validation passe (champ optionnel)
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        // Sinon, valider le pattern
        return pattern.matcher(value).matches();
    }
}

