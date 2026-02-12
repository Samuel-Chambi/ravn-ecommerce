package com.ravn.ecommerce.application.validation;

import com.ravn.ecommerce.domain.model.order.OrderStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validator implementation for @ValidOrderStatus annotation.
 * Validates order status transitions against allowed statuses.
 */
public class OrderStatusValidator implements ConstraintValidator<ValidOrderStatus, String> {
    private Set<String> allowedStatuses;

    @Override
    public void initialize(ValidOrderStatus constraintAnotation) {
        // Get allowed statuses from annotation, or use all if none specified
        this.allowedStatuses = Arrays.stream(constraintAnotation.allowedStatuses())
                .map(Enum::name)
                .collect(Collectors.toSet());
        if (this.allowedStatuses.isEmpty()) {
            this.allowedStatuses = Arrays.stream(OrderStatus.values())
                    .map(Enum::name)
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null)
            return true;
        if (!allowedStatuses.contains(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("Status must be one of: %s", String.join(", ", allowedStatuses)))
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
