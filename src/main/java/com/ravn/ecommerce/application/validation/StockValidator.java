package com.ravn.ecommerce.application.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StockValidator implements ConstraintValidator<ValidStock , Integer> {
    private int min;
    private int max;
    @Override
    public void initialize(ValidStock constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }
    @Override
    public boolean isValid(Integer value , ConstraintValidatorContext context) {
        if(value == null) return true;
        if(value < min || value > max) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("Stock must be between %d and %d" , min , max)
            ).addConstraintViolation();
            return false;
        }
        return true;
    }
}
