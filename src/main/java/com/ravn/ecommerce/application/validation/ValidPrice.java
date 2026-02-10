package com.ravn.ecommerce.application.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD , ElementType.PARAMETER})
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
