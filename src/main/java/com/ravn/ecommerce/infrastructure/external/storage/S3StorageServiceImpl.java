package com.ravn.ecommerce.infrastructure.external.storage;

import com.ravn.ecommerce.application.config.AppConfig;
import com.ravn.ecommerce.application.services.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;

/**
 * S3 Storage Service implementation
 * Supports LocalStack (development)
 */
@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
@RequiredArgsConstructor
@Slf4j
public class S3StorageServiceImpl implements ImageStorageService {

    private final S3Client s3Client;
    private final AppConfig appConfig;

    private static final int MAX_WIDTH = 1200;
    private static final int MAX_HEIGHT = 1200;
    private static final String FOLDER_PREFIX = "products/";

    @Override
    public String store(MultipartFile file, Long productId) throws IOException {
        String bucket = appConfig.getStorage().getS3().getBucket();

        // Generate unique key
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String key = FOLDER_PREFIX + String.format("product_%d_%s%s",
                productId,
                UUID.randomUUID().toString(),
                extension);

        // Process image (resize if needed)
        byte[] processedImageBytes = processImage(file.getBytes(), extension);

        // Upload to S3
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(processedImageBytes));

            log.info("Stored image in S3: bucket={}, key={}", bucket, key);

            // Return URL
            return buildS3Url(bucket, key);

        } catch (S3Exception e) {
            log.error("Failed to upload image to S3: {}", e.getMessage(), e);
            throw new IOException("Failed to upload image to S3: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        try {
            String bucket = appConfig.getStorage().getS3().getBucket();
            String key = extractKeyFromUrl(fileUrl);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Deleted image from S3: bucket={}, key={}", bucket, key);

        } catch (S3Exception e) {
            log.error("Failed to delete image from S3: {}", e.getMessage(), e);
        }
    }

    /**
     * Process image: resize if needed and maintain aspect ratio
     */
    private byte[] processImage(byte[] imageBytes, String extension) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

        if (originalImage == null) {
            throw new IOException("Failed to read image");
        }

        // Check if resizing is needed
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        if (width <= MAX_WIDTH && height <= MAX_HEIGHT) {
            return imageBytes; // No resizing needed
        }

        // Calculate new dimensions maintaining aspect ratio
        double aspectRatio = (double) width / height;
        int newWidth = width;
        int newHeight = height;

        if (width > MAX_WIDTH) {
            newWidth = MAX_WIDTH;
            newHeight = (int) (MAX_WIDTH / aspectRatio);
        }

        if (newHeight > MAX_HEIGHT) {
            newHeight = MAX_HEIGHT;
            newWidth = (int) (MAX_HEIGHT * aspectRatio);
        }

        // Resize image
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        graphics.dispose();

        // Convert to bytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String formatName = extension.substring(1); // Remove dot
        ImageIO.write(resizedImage, formatName, outputStream);

        return outputStream.toByteArray();
    }

    /**
     * Build S3 URL for the uploaded file
     */
    private String buildS3Url(String bucket, String key) {
        var s3Config = appConfig.getStorage().getS3();

        if (s3Config.getEndpoint() != null && !s3Config.getEndpoint().isEmpty()) {
            return String.format("%s/%s/%s", s3Config.getEndpoint(), bucket, key);
        }

        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucket,
                s3Config.getRegion(),
                key);
    }

    /**
     * Extract S3 key from URL
     */
    private String extractKeyFromUrl(String fileUrl) {
        // LocalStack: http://localhost:4566/bucket/products/file.jpg

        if (fileUrl.contains("localhost:4566")) {
            // LocalStack format
            String[] parts = fileUrl.split("/");
            // Skip protocol, host, port, and bucket name
            return String.join("/", Arrays.copyOfRange(parts, 4, parts.length));
        } else {
            // AWS-S3 format
            URI uri = URI.create(fileUrl);
            return uri.getPath().substring(1); // Remove leading slash
        }
    }

    // Get file extension
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    @Override
    public boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // Validate file size (max 5MB)
        long MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > MAX_SIZE_BYTES) {
            log.warn("Image file too large: {} bytes (max: {} bytes)", file.getSize(), MAX_SIZE_BYTES);
            return false;
        }

        // Validate MIME type
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        return contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/webp");
    }
}
