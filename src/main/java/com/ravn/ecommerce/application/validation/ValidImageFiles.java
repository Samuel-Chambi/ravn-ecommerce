package com.ravn.ecommerce.application.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for image file uploads.
 * Validates MIME type, extension, and size using ImageStorageService.
 * Usage: @ValidImageFiles List<MultipartFile> images
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ImageFilesValidator.class)
@Documented
public @interface ValidImageFiles {
    String message() default "Invalid image file(s)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
