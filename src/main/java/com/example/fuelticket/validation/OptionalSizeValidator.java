package com.example.fuelticket.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OptionalSizeValidator implements ConstraintValidator<OptionalSize, String> {
    private int min;
    private int max;

    @Override
    public void initialize(OptionalSize constraintAnnotation) {
        min = constraintAnnotation.min();
        max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Si la valeur est null ou vide, la validation passe (champ optionnel)
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        // Sinon, valider la taille
        int length = value.length();
        return length >= min && length <= max;
    }
}

