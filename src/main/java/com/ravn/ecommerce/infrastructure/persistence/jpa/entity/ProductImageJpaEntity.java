package com.ravn.ecommerce.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "product_id" , nullable = false)
    private Long productId;
    @Column(name = "image_url" , nullable = false , length = 500)
    private String imageUrl;
    @Column(name = "is_primary" , nullable = false)
    private Boolean isPrimary;
    @Column(name = "created_at" , nullable = false)
    private LocalDateTime createdAt;

    protected void onCreate() {
        if(createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if(isPrimary == null) {
            isPrimary = false;
        }
    }
}

