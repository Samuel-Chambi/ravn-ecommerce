package com.ravn.ecommerce.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "quantity", nullable = false)
    private int quantity;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    private ProductJpaEntity product;
}
