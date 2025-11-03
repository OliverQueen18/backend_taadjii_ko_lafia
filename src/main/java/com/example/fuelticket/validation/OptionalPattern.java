package com.example.fuelticket.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = OptionalPatternValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionalPattern {
    String message() default "Format invalide";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String regexp();
}

