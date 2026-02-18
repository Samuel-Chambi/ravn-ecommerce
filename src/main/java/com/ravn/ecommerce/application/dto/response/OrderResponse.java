package com.ravn.ecommerce.application.dto.response;

import com.ravn.ecommerce.domain.model.order.Order;
import com.ravn.ecommerce.domain.model.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private Long id;
    private Long userId;
    private Long shippingAddressId;
    private OrderStatus status;
    private BigDecimal total;
    private List<OrderItemResponse> productOrderList;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;



    public static OrderResponse toDto(Order order){
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .shippingAddressId(order.getShippingAddressId())
                .status(order.getStatus())
                .total(order.calculateTotal())
                .productOrderList(order.getItems().stream()
                        .map(OrderItemResponse::toDto)
                        .collect(Collectors.toCollection(ArrayList::new)))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
