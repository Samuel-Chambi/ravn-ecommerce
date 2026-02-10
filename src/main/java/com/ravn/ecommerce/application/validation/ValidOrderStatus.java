package com.ravn.ecommerce.application.validation;

import com.ravn.ecommerce.domain.model.order.OrderStatus;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD , ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OrderStatusValidator.class)
@Documented
public @interface ValidOrderStatus {
    String message() default "Invalid order status transition";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    OrderStatus[] allowedStatuses() default {};
}
