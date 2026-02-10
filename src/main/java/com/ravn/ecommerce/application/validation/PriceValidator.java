package com.ravn.ecommerce.application.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class PriceValidator implements ConstraintValidator<ValidPrice , Object> {
    private double min;
    private double max;

    @Override
    public void initialize(ValidPrice constraintAnnotation){
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context){
        if(value == null) return true;
        double price;
        if(value instanceof BigDecimal) {
            price = ((BigDecimal) value).doubleValue();
        }else if(value instanceof Double) {
            price = ((Double) value);
        }else if(value instanceof Float) {
            price = ((Float) value).doubleValue();
        }else if(value instanceof Number) {
            price = ((Number) value).doubleValue();
        }else {
            return false;
        }
        if(price < min || price > max) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("Price must be between %.2f and %.2f", min, max)
            ).addConstraintViolation();
            return false;
        }
        return true;
    }
}
