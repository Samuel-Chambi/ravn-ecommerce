package com.ravn.ecommerce.application.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for product prices.
 * Ensures price is within valid range (default: 0.01 to MAX).
 * 
 * Usage: @ValidPrice(min = 0.01, max = 10000.0) BigDecimal price
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PriceValidator.class)
@Documented
public @interface ValidPrice {
    String message() default "Price must be greater than 0";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    double min() default 0.01;

    double max() default Double.MAX_VALUE;
}
