package com.ravn.ecommerce.application.usecases.productImage.commands;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record UploadProductImageCommand(
        Long productId,
        List<MultipartFile> files,
        Boolean markFirstAsPrimary
) {
}
