package com.ravn.ecommerce.application.usecases.address.command;

import com.ravn.ecommerce.application.dto.request.address.CreateAddressRequest;

public record CreateAddressCommand(Long userId, CreateAddressRequest request) {
}
