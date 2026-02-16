package com.ravn.ecommerce.infrastructure.persistence.jpa.mapper;

import com.ravn.ecommerce.domain.model.product.Inventory;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.InventoryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper implements Mapper<InventoryJpaEntity, Inventory> {

    @Override
    public Inventory toDomain(InventoryJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Inventory(
                entity.getId(),
                entity.getProduct() != null ? entity.getProduct().getId() : null,
                entity.getQuantity(),
                entity.getUpdatedAt());
    }

    @Override
    public InventoryJpaEntity toJpaEntity(Inventory domain) {
        if (domain == null) {
            return null;
        }
        return InventoryJpaEntity.builder()
                .id(domain.getId())
                .quantity(domain.getQuantity())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
