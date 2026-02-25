package com.ravn.ecommerce.application.usecases.address.command;

import com.ravn.ecommerce.application.dto.request.address.UpdateAddressRequest;

public record UpdateAddressCommand(Long userId, Long addressId, UpdateAddressRequest request) {
}
