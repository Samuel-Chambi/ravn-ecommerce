package com.ravn.ecommerce.application.usecases.order.command;

import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Limit;

public record GetUserOrdersCommand(Long userId, ScrollPosition position, Limit limit) {
}
