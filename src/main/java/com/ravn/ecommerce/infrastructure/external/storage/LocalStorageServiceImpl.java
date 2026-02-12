package com.ravn.ecommerce.infrastructure.external.storage;

import com.ravn.ecommerce.application.config.AppConfig;
import com.ravn.ecommerce.application.services.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class LocalStorageServiceImpl implements ImageStorageService {

    private final AppConfig appConfig;

    private static final int MAX_WIDTH = 1200;
    private static final int MAX_HEIGHT = 1200;

    @Override
    public String store(MultipartFile file, Long productId) throws IOException {
        // Create directory if not exists
        Path uploadPath = getUploadPath();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Created upload directory: {}", uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = String.format("product_%d_%s%s",
                productId,
                UUID.randomUUID().toString(),
                extension);

        Path filePath = uploadPath.resolve(filename);

        // Process and save image
        byte[] processedImageBytes = processImage(file.getBytes(), extension);
        Files.write(filePath, processedImageBytes);

        log.info("Stored image: {}", filename);

        // Return URL using configured base URL
        return appConfig.getStorage().getLocal().getBaseUrl() + filename;
    }

    @Override
    public void delete(String imageUrl) throws IOException {
        // Extract filename from URL
        String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
        Path filePath = getUploadPath().resolve(filename);

        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("Deleted image: {}", filename);
        } else {
            log.warn("Image file not found: {}", filename);
        }
    }

    private byte[] processImage(byte[] imageBytes, String extension) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

        if (originalImage == null) {
            throw new IOException("Failed to read image");
        }

        // Resize if necessary
        BufferedImage processedImage = resizeImage(originalImage);

        // Convert to bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String formatName = extension.substring(1); // Remove the dot
        ImageIO.write(processedImage, formatName, baos);

        return baos.toByteArray();
    }

    private BufferedImage resizeImage(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Check if resize is needed
        if (originalWidth <= MAX_WIDTH && originalHeight <= MAX_HEIGHT) {
            return originalImage;
        }

        // Calculate new dimensions maintaining aspect ratio
        double widthRatio = (double) MAX_WIDTH / originalWidth;
        double heightRatio = (double) MAX_HEIGHT / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        // Create resized image
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        log.debug("Resized image from {}x{} to {}x{}", originalWidth, originalHeight, newWidth, newHeight);

        return resizedImage;
    }

    private Path getUploadPath() {
        String uploadDir = appConfig.getStorage().getLocal().getUploadDir();
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf('.'));
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