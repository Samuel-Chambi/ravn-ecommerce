package com.ravn.ecommerce.application.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Image storage service interface with multiple implementations.
 * - LocalStorageServiceImpl: for localStorage
 * - S3StorageServiceImpl: for LocalStack
 */
public interface ImageStorageService {

    /**
     * Stores an image and returns its public URL.
     */
    String store(MultipartFile file, Long productId) throws IOException;

    /**
     * Deletes an image by its URL.
     */
    void delete(String imageUrl) throws IOException;

    /**
     * Validates if a file is a valid image (MIME type, extension, size).
     */
    boolean isValidImage(MultipartFile file);
}
