package com.ravn.ecommerce.application.validation;

import com.ravn.ecommerce.application.services.ImageStorageService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Validator implementation for @ValidImageFiles annotation.
 * Delegates validation to ImageStorageService.isValidImage() for each file.
 */
@RequiredArgsConstructor
public class ImageFilesValidator implements ConstraintValidator<ValidImageFiles, List<MultipartFile>> {
    private final ImageStorageService imageStorageService;

    @Override
    public boolean isValid(List<MultipartFile> files, ConstraintValidatorContext context) {
        if (files == null || files.isEmpty())
            return false;

        // Validate each file using ImageStorageService
        for (MultipartFile file : files) {
            if (!imageStorageService.isValidImage(file)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "Invalid image file: " + file.getOriginalFilename()).addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
