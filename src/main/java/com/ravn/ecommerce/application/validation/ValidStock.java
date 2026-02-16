package com.ravn.ecommerce.application.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for product stock quantities.
 * Ensures stock is within valid range (default: 0 to MAX).
 * 
 * Usage: @ValidStock(min = 0, max = 1000) Integer stock
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StockValidator.class)
@Documented
public @interface ValidStock {

    String message() default "Stock must be zero or positive";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int min() default 0;

    int max() default Integer.MAX_VALUE;
}